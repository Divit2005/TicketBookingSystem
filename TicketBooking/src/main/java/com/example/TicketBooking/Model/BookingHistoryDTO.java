package com.example.TicketBooking.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingHistoryDTO {
    private Long bookingId;
    private Long movieId;
    private String movieTitle;
    private String movieGenre;
    private String movieLanguage;
    private String theatreName;
    private String theatreLocation;
    private ScreenType screenType;
    private LocalDateTime showTime;
    private List<String> seatNumbers;
    private Double totalAmount;
    private BookingStatus status;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime bookingTime;
    private Double userRemainingBalance;
}
