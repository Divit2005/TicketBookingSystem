package com.example.TicketBooking.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    private Long movieId;
    private List<Long> seatIds;         // Optional: IDs of the seats
    private List<String> seatNumbers;   // Optional: e.g. ["A1", "A2", "B5"]
    private String paymentMethod;       // Defaults to "WALLET"
}
