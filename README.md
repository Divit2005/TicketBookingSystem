# 🎬 CinePass – Concurrency-Safe Movie Ticket Booking System

A robust, full-stack movie ticket reservation platform built with **Spring Boot 4**, **Spring Security**, and **PostgreSQL**. The application simulates real-world multiplex booking with tiered seat layouts, instant wallet transactions, and concurrency safeguards to eliminate double-booking race conditions.

---

### ✨ Key Highlights

- **🔒 Concurrency-Safe Seat Reservations:** Implements **Pessimistic Row-Level Locking (`PESSIMISTIC_WRITE`)** within transactional boundaries to prevent multiple users from booking the same seat at the exact same millisecond.
- **🛡️ Secure Stateless Authentication:** Built with **Spring Security** and **JWT (JSON Web Tokens)** for stateless session management, role-based access, and protected checkout routes.
- **🎟️ Dynamic Tiered Seating & Pricing:** Interactive real-time seating map supporting tiered seat categories (**VIP**, **Premium**, **Regular**) with automatic pricing and convenience fee calculation.
- **💳 Simulated Digital Wallet & Payments:** Atomic wallet balance deduction, transaction logging (`TXN-ID`), and instant booking confirmation with detailed receipt history.
- **🎨 Modern Glassmorphism UI:** Built with responsive HTML5, modern CSS, and JavaScript with live seat selection states and booking history tracking.

---

### 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot, Spring Data JPA, Hibernate
- **Security:** Spring Security, JJWT (JSON Web Token)
- **Database:** PostgreSQL (ACID Transactions, Row Locks)
- **Frontend:** HTML5, CSS3, JavaScript (Fetch API, Responsive UI)
- **Build Tool:** Maven

---

### 🚀 Getting Started

#### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL

#### Setup & Run
1. **Clone the repository:**
   ```bash
   git clone https://github.com/<your-username>/TicketBooking.git
   cd TicketBooking
