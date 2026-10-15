package com.example.TicketBooking.Controller;

import com.example.TicketBooking.Model.BookingHistoryDTO;
import com.example.TicketBooking.Model.BookingRequest;
import com.example.TicketBooking.Model.Seat;
import com.example.TicketBooking.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * Book seats with concurrency lock (Pessimistic Locking) and fake wallet balance deduction.
     */
    @PostMapping("")
    public ResponseEntity<?> bookTickets(
            @RequestBody BookingRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User is not authenticated. Please log in."));
        }

        try {
            String userEmail = authentication.getName();
            BookingHistoryDTO booking = bookingService.bookTickets(userEmail, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (IllegalStateException e) {
            // 409 Conflict: Seat already booked by another concurrent user
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 400 Bad Request: Insufficient balance or invalid input
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Booking failed: " + e.getMessage()));
        }
    }

    /**
     * Get booking history for the currently logged-in user.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getBookingHistory(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User is not authenticated. Please log in."));
        }

        try {
            String userEmail = authentication.getName();
            List<BookingHistoryDTO> history = bookingService.getUserBookingHistory(userEmail);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get live seats for a movie (shows real booked/available status).
     */
    @GetMapping("/movies/{movieId}/seats")
    public ResponseEntity<List<Seat>> getSeatsForMovie(@PathVariable Long movieId) {
        List<Seat> seats = bookingService.getSeatsForMovie(movieId);
        return ResponseEntity.ok(seats);
    }
}
