package com.example.TicketBooking.Repo;

import com.example.TicketBooking.Model.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepo extends JpaRepository<Seat, Long> {

    List<Seat> findByMovieId(Long movieId);

    List<Seat> findByMovieIdOrderByRowCharAscSeatNumberAsc(Long movieId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    List<Seat> findAllByIdWithLock(@Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.movieId = :movieId AND CONCAT(s.rowChar, s.seatNumber) IN :seatCodes")
    List<Seat> findSeatsByMovieIdAndCodesWithLock(@Param("movieId") Long movieId, @Param("seatCodes") List<String> seatCodes);

    @Query("SELECT s FROM Seat s WHERE s.movieId = :movieId AND CONCAT(s.rowChar, s.seatNumber) IN :seatCodes")
    List<Seat> findSeatsByMovieIdAndCodes(@Param("movieId") Long movieId, @Param("seatCodes") List<String> seatCodes);
}