package com.example.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Model.Ticket;
import com.example.Service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    TicketService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket bookTicket(@Valid @RequestBody Ticket ticket) {
        return service.bookTicket(ticket);
    }

    @GetMapping
    public List<Ticket> getAllTickets() {
        return service.getAllTickets();
    }

    @GetMapping("/{id}")
    public Ticket getTicketById(@PathVariable Long id) {
        return service.getTicketById(id);
    }

    @PutMapping("/{id}")
    public Ticket updateTicket(@PathVariable Long id,
                               @Valid @RequestBody Ticket ticket) {
        return service.updateTicket(id, ticket);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> cancelTicket(@PathVariable Long id) {
        return Map.of("message", service.cancelTicket(id));
    }

    @GetMapping("/movie")
    public List<Ticket> getByMovie(@RequestParam String name) {
        return service.getByMovie(name);
    }

    @GetMapping("/customer")
    public List<Ticket> searchByCustomer(@RequestParam String keyword) {
        return service.searchByCustomer(keyword);
    }

    @GetMapping("/price")
    public List<Ticket> getByPriceRange(@RequestParam BigDecimal min,
                                        @RequestParam BigDecimal max) {
        return service.getByPriceRange(min, max);
    }

    @GetMapping("/sorted")
    public List<Ticket> getSortedTickets(
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.getSortedTickets(sortBy, direction);
    }

    @GetMapping("/seats")
    public List<String> getBookedSeats(@RequestParam String movie) {
        return service.getBookedSeats(movie);
    }

    @GetMapping("/collection")
    public Map<String, Object> getTotalCollection(@RequestParam String movie) {
        return Map.of("movie", movie,
                      "totalCollection", service.getTotalCollection(movie));
    }

    @GetMapping("/revenue")
    public Map<String, BigDecimal> getMovieRevenue() {
        return service.getMovieRevenue();
    }

    @GetMapping("/costliest")
    public Ticket getCostliestTicket() {
        return service.getCostliestTicket();
    }

}