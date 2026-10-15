package com.example.TicketBooking.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Double amount;
    private String paymentMethod; // CARD, UPI, NET_BANKING, WALLET
    private String paymentStatus; // SUCCESS, FAILED
    private String transactionId;
    private LocalDateTime paymentTime;
}
