package com.example.TicketBooking.Service;


import com.example.TicketBooking.Model.Movie;
import com.example.TicketBooking.Repo.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HomeService {
    @Autowired
    MovieRepo movieRepo;

    public List<Movie> getData(){
        return movieRepo.findAll();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepo.findById(id);
    }
}
