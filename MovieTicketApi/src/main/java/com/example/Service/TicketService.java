package com.example.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.Exception.InvalidTicketException;
import com.example.Exception.SeatAlreadyBookedException;
import com.example.Exception.TicketNotFoundException;
import com.example.Model.Ticket;

@Service
public class TicketService {

    private final ArrayList<Ticket> tickets = new ArrayList<>();

    private long nextId = 1L;

    // returns a copy, so outside code cannot change the stored ticket
    private Ticket copyTicket(Ticket ticket) {
        return new Ticket(
                ticket.getTicketId(),
                ticket.getMovieName(),
                ticket.getCustomerName(),
                ticket.getSeatNumber(),
                ticket.getTicketPrice()
        );
    }

    private void checkNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidTicketException(message);
        }
    }

    private Ticket findTicket(Long id) {
        return tickets.stream()
                .filter(ticket -> ticket.getTicketId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new TicketNotFoundException(
                                "Ticket not found with ID: " + id
                        ));
    }

    // same movie + same seat cannot be booked twice
    // ignoreId is the ticket being updated (null when booking a new ticket)
    private void checkSeatFree(String movie, String seat, Long ignoreId) {

        boolean booked = tickets.stream()
                .filter(ticket -> ticket.getMovieName().equalsIgnoreCase(movie))
                .filter(ticket -> ticket.getSeatNumber().equalsIgnoreCase(seat))
                .anyMatch(ticket -> !ticket.getTicketId().equals(ignoreId));

        if (booked) {
            throw new SeatAlreadyBookedException(
                    "Seat " + seat + " is already booked for " + movie
            );
        }
    }

    // ---------- CRUD ----------

    public Ticket bookTicket(Ticket request) {

        String movie = request.getMovieName().trim();
        String seat = request.getSeatNumber().toUpperCase(Locale.ROOT);

        checkSeatFree(movie, seat, null);

        Ticket ticket = new Ticket(
                nextId++,
                movie,
                request.getCustomerName().trim(),
                seat,
                request.getTicketPrice()
        );

        tickets.add(ticket);

        return copyTicket(ticket);
    }

    public List<Ticket> getAllTickets() {

        return tickets.stream()
                .map(this::copyTicket)
                .toList();
    }

    public Ticket getTicketById(Long id) {
        return copyTicket(findTicket(id));
    }

    public Ticket updateTicket(Long id, Ticket request) {

        Ticket ticket = findTicket(id);

        String movie = request.getMovieName().trim();
        String seat = request.getSeatNumber().toUpperCase(Locale.ROOT);

        checkSeatFree(movie, seat, id);

        ticket.setMovieName(movie);
        ticket.setCustomerName(request.getCustomerName().trim());
        ticket.setSeatNumber(seat);
        ticket.setTicketPrice(request.getTicketPrice());

        return copyTicket(ticket);
    }

    // cancel ticket
    public String cancelTicket(Long id) {

        Ticket ticket = findTicket(id);

        tickets.remove(ticket);

        return "Ticket cancelled with ID: " + id;
    }

    // ---------- filter ----------

    // filter 1: by movie name
    public List<Ticket> getByMovie(String movieName) {

        checkNotBlank(movieName, "Movie name must not be blank");

        return tickets.stream()
                .filter(ticket ->
                        ticket.getMovieName()
                                .equalsIgnoreCase(movieName.trim()))
                .map(this::copyTicket)
                .toList();
    }

    // filter 2: partial customer name match
    public List<Ticket> searchByCustomer(String keyword) {

        checkNotBlank(keyword, "Search keyword must not be blank");

        String searchText = keyword.trim().toLowerCase(Locale.ROOT);

        return tickets.stream()
                .filter(ticket ->
                        ticket.getCustomerName()
                                .toLowerCase(Locale.ROOT)
                                .contains(searchText))
                .map(this::copyTicket)
                .toList();
    }

    // filter 3: price range, using compareTo
    public List<Ticket> getByPriceRange(BigDecimal min, BigDecimal max) {

        if (min == null || max == null
                || min.signum() < 0
                || min.compareTo(max) > 0) {
            throw new InvalidTicketException(
                    "Enter a valid price range (min must not be more than max)"
            );
        }

        return tickets.stream()
                .filter(ticket -> ticket.getTicketPrice().compareTo(min) >= 0)
                .filter(ticket -> ticket.getTicketPrice().compareTo(max) <= 0)
                .sorted((a, b) -> a.getTicketPrice().compareTo(b.getTicketPrice()))
                .map(this::copyTicket)
                .toList();
    }

    // ---------- sorting ----------

    public List<Ticket> getSortedTickets(String sortBy, String direction) {

        checkNotBlank(sortBy, "Sort field must not be blank");

        Comparator<Ticket> comparator =
                switch (sortBy.trim().toLowerCase(Locale.ROOT)) {
                    case "id" -> Comparator.comparing(Ticket::getTicketId);
                    case "movie" -> Comparator.comparing(
                            Ticket::getMovieName,
                            String.CASE_INSENSITIVE_ORDER);
                    case "customer" -> Comparator.comparing(
                            Ticket::getCustomerName,
                            String.CASE_INSENSITIVE_ORDER);
                    case "seat" -> Comparator.comparing(Ticket::getSeatNumber);
                    case "price" -> (a, b) ->
                            a.getTicketPrice().compareTo(b.getTicketPrice());
                    default -> throw new InvalidTicketException(
                            "Sort field must be id, movie, customer, seat or price");
                };

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        } else if (!"asc".equalsIgnoreCase(direction)) {
            throw new InvalidTicketException("Direction must be asc or desc");
        }

        return tickets.stream()
                .sorted(comparator)
                .map(this::copyTicket)
                .toList();
    }

    // ---------- map and other Java APIs ----------

    // map + sorted: booked seats of one movie
    public List<String> getBookedSeats(String movieName) {

        checkNotBlank(movieName, "Movie name must not be blank");

        return tickets.stream()
                .filter(ticket ->
                        ticket.getMovieName()
                                .equalsIgnoreCase(movieName.trim()))
                .map(Ticket::getSeatNumber)
                .sorted()
                .toList();
    }

    // map + reduce: total collection of one movie
    public BigDecimal getTotalCollection(String movieName) {

        checkNotBlank(movieName, "Movie name must not be blank");

        return tickets.stream()
                .filter(ticket ->
                        ticket.getMovieName()
                                .equalsIgnoreCase(movieName.trim()))
                .map(Ticket::getTicketPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // groupingBy + reducing: collection of every movie
    public Map<String, BigDecimal> getMovieRevenue() {

        return tickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getMovieName,
                        TreeMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Ticket::getTicketPrice,
                                BigDecimal::add)
                ));
    }

    // max + Comparator + Optional: costliest ticket
    public Ticket getCostliestTicket() {

        return tickets.stream()
                .max(Comparator.comparing(Ticket::getTicketPrice))
                .map(this::copyTicket)
                .orElseThrow(() ->
                        new TicketNotFoundException("No tickets available"));
    }

}