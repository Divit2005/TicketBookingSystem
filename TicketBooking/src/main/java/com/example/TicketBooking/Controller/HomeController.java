package com.example.TicketBooking.Controller;

import com.example.TicketBooking.Model.Movie;
import com.example.TicketBooking.Service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Home")
public class HomeController {
    @Autowired
    HomeService homeService;

    @GetMapping("")
    public List<Movie> getData(){
        return homeService.getData();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable long id){
        return homeService.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
