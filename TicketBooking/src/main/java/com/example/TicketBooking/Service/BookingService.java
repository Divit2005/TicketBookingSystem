package com.example.TicketBooking.Service;

import com.example.TicketBooking.Model.*;
import com.example.TicketBooking.Repo.BookingRepo;
import com.example.TicketBooking.Repo.MovieRepo;
import com.example.TicketBooking.Repo.PaymentRepo;
import com.example.TicketBooking.Repo.SeatRepo;
import com.example.TicketBooking.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private SeatRepo seatRepo;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PaymentRepo paymentRepo;

    /**
     * Concurrency-safe ticket booking with Pessimistic Locking.
     * Prevents race conditions where two or more users attempt to book the same seat simultaneously.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingHistoryDTO bookTickets(String userEmail, BookingRequest request) {
        // 1. Fetch User
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        // 2. Fetch Movie
        Movie movie = movieRepo.findById(request.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found with ID: " + request.getMovieId()));

        // Ensure seats exist for this movie in DB
        ensureSeatsExistForMovie(movie.getId());

        // 3. Acquire Pessimistic Row Lock on Requested Seats
        List<Seat> seatsToBook;
        if (request.getSeatIds() != null && !request.getSeatIds().isEmpty()) {
            seatsToBook = seatRepo.findAllByIdWithLock(request.getSeatIds());
        } else if (request.getSeatNumbers() != null && !request.getSeatNumbers().isEmpty()) {
            seatsToBook = seatRepo.findSeatsByMovieIdAndCodesWithLock(movie.getId(), request.getSeatNumbers());
        } else {
            throw new IllegalArgumentException("No seats specified for booking.");
        }

        int requestedCount = (request.getSeatIds() != null && !request.getSeatIds().isEmpty())
                ? request.getSeatIds().size()
                : request.getSeatNumbers().size();

        if (seatsToBook.size() != requestedCount) {
            throw new IllegalArgumentException("One or more selected seats do not exist for this movie.");
        }

        // 4. Concurrency check: verify if ANY seat is already booked
        List<Seat> alreadyBookedSeats = seatsToBook.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsBooked()))
                .toList();

        if (!alreadyBookedSeats.isEmpty()) {
            String bookedLabels = alreadyBookedSeats.stream()
                    .map(s -> s.getRowChar() + s.getSeatNumber())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("Seat(s) " + bookedLabels + " have already been booked by another user! Please select different seats.");
        }

        // 5. Calculate Total Amount
        double basePrice = movie.getPrice() != null ? movie.getPrice() : 250.0;
        double seatTotal = seatsToBook.stream().mapToDouble(seat -> {
            if (seat.getSeatType() == SeatType.VIP) return basePrice * 1.4;
            if (seat.getSeatType() == SeatType.PREMIUM) return basePrice * 1.2;
            return basePrice;
        }).sum();

        double convenienceFee = seatsToBook.size() * 25.0;
        double grandTotal = Math.round((seatTotal + convenienceFee) * 100.0) / 100.0;

        // 6. Check Fake Wallet Balance
        if (user.getBalance() == null || user.getBalance() < grandTotal) {
            double currentBal = user.getBalance() != null ? user.getBalance() : 0.0;
            throw new IllegalArgumentException("Insufficient wallet balance! Required: ₹" + grandTotal + ", Available: ₹" + currentBal);
        }

        // 7. Deduct User Wallet Balance
        user.setBalance(Math.round((user.getBalance() - grandTotal) * 100.0) / 100.0);
        userRepo.save(user);

        // 8. Mark Seats as Booked
        seatsToBook.forEach(seat -> seat.setIsBooked(true));
        seatRepo.saveAll(seatsToBook);

        // 9. Create & Save Booking
        List<String> seatLabels = seatsToBook.stream()
                .map(s -> s.getRowChar() + s.getSeatNumber())
                .sorted()
                .collect(Collectors.toList());

        Booking booking = Booking.builder()
                .userId(user.getId())
                .movieId(movie.getId())
                .bookingTime(LocalDateTime.now())
                .totalAmount(grandTotal)
                .status(BookingStatus.CONFIRMED)
                .seatNumbers(seatLabels)
                .build();

        Booking savedBooking = bookingRepo.save(booking);

        // 10. Record Payment
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String method = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank())
                ? request.getPaymentMethod()
                : "WALLET";

        Payment payment = Payment.builder()
                .bookingId(savedBooking.getId())
                .amount(grandTotal)
                .paymentMethod(method)
                .paymentStatus("SUCCESS")
                .transactionId(txnId)
                .paymentTime(LocalDateTime.now())
                .build();

        paymentRepo.save(payment);

        // 11. Build Response DTO
        return BookingHistoryDTO.builder()
                .bookingId(savedBooking.getId())
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .movieGenre(movie.getGenre())
                .movieLanguage(movie.getLanguage())
                .theatreName(movie.getTheatreName())
                .theatreLocation(movie.getTheatreLocation())
                .screenType(movie.getScreenType())
                .showTime(movie.getShowTime())
                .seatNumbers(savedBooking.getSeatNumbers())
                .totalAmount(savedBooking.getTotalAmount())
                .status(savedBooking.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .bookingTime(savedBooking.getBookingTime())
                .userRemainingBalance(user.getBalance())
                .build();
    }

    /**
     * Get booking history for the logged-in user.
     */
    @Transactional(readOnly = true)
    public List<BookingHistoryDTO> getUserBookingHistory(String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        List<Booking> bookings = bookingRepo.findByUserIdOrderByBookingTimeDesc(user.getId());

        return bookings.stream().map(booking -> {
            Movie movie = movieRepo.findById(booking.getMovieId()).orElse(null);
            Payment payment = paymentRepo.findByBookingId(booking.getId()).orElse(null);

            return BookingHistoryDTO.builder()
                    .bookingId(booking.getId())
                    .movieId(booking.getMovieId())
                    .movieTitle(movie != null ? movie.getTitle() : "Unknown Movie")
                    .movieGenre(movie != null ? movie.getGenre() : "")
                    .movieLanguage(movie != null ? movie.getLanguage() : "")
                    .theatreName(movie != null ? movie.getTheatreName() : "Multiplex")
                    .theatreLocation(movie != null ? movie.getTheatreLocation() : "")
                    .screenType(movie != null ? movie.getScreenType() : null)
                    .showTime(movie != null ? movie.getShowTime() : null)
                    .seatNumbers(booking.getSeatNumbers())
                    .totalAmount(booking.getTotalAmount())
                    .status(booking.getStatus())
                    .paymentMethod(payment != null ? payment.getPaymentMethod() : "WALLET")
                    .paymentStatus(payment != null ? payment.getPaymentStatus() : "SUCCESS")
                    .transactionId(payment != null ? payment.getTransactionId() : "N/A")
                    .bookingTime(booking.getBookingTime())
                    .userRemainingBalance(user.getBalance())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Get live seat layout and booking status for a specific movie.
     */
    @Transactional
    public List<Seat> getSeatsForMovie(Long movieId) {
        ensureSeatsExistForMovie(movieId);
        return seatRepo.findByMovieIdOrderByRowCharAscSeatNumberAsc(movieId);
    }

    /**
     * Seeds initial movie seats if not already present in the database.
     */
    @Transactional
    public void ensureSeatsExistForMovie(Long movieId) {
        List<Seat> existing = seatRepo.findByMovieId(movieId);
        if (existing.isEmpty()) {
            List<Seat> seedSeats = new ArrayList<>();
            // Row A: VIP (8 seats)
            for (int i = 1; i <= 8; i++) {
                seedSeats.add(Seat.builder()
                        .movieId(movieId)
                        .rowChar("A")
                        .seatNumber(i)
                        .seatType(SeatType.VIP)
                        .isBooked(false)
                        .build());
            }
            // Rows B, C, D: Premium (10 seats each)
            for (String row : List.of("B", "C", "D")) {
                for (int i = 1; i <= 10; i++) {
                    seedSeats.add(Seat.builder()
                            .movieId(movieId)
                            .rowChar(row)
                            .seatNumber(i)
                            .seatType(SeatType.PREMIUM)
                            .isBooked(false)
                            .build());
                }
            }
            // Rows E, F, G: Regular (10 seats each)
            for (String row : List.of("E", "F", "G")) {
                for (int i = 1; i <= 10; i++) {
                    seedSeats.add(Seat.builder()
                            .movieId(movieId)
                            .rowChar(row)
                            .seatNumber(i)
                            .seatType(SeatType.REGULAR)
                            .isBooked(false)
                            .build());
                }
            }
            seatRepo.saveAll(seedSeats);
        }
    }
}
