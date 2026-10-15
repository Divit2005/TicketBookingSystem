package com.example.TicketBooking.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // Automatically ensure the balance column exists in the users table in PostgreSQL
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS balance DOUBLE PRECISION DEFAULT 10000.0");
            jdbcTemplate.execute("UPDATE users SET balance = 10000.0 WHERE balance IS NULL");
            System.out.println(">>> Database Schema synchronized: 'balance' column verified on 'users' table.");
        } catch (Exception e) {
            System.err.println(">>> DatabaseInitializer notice: " + e.getMessage());
        }
    }
}
