package entelect.training.incubator.spring.booking.controller;

import entelect.training.incubator.spring.booking.model.Booking;
import entelect.training.incubator.spring.booking.service.BookingService;
import entelect.training.incubator.spring.customer.model.Customer;
import entelect.training.incubator.spring.flight.model.Flight;
import entelect.training.incubator.spring.notification.sms.client.impl.MoloCellSmsClient;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("bookings")
public class BookingController {

    private final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    private final RestTemplate restTemplate;

    private final MoloCellSmsClient moloCellSmsClient;


    public BookingController(BookingService bookingService, RestTemplate restTemplate, MoloCellSmsClient moloCellSmsClient) {
        this.bookingService = bookingService;
        this.restTemplate = restTemplate;
        this.moloCellSmsClient = moloCellSmsClient;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String plainCreds = "admin:is_a_lie";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            Customer customer = restTemplate.exchange("http://localhost:8201/customers/" + booking.getCustomerId(), HttpMethod.GET, entity, Customer.class).getBody();
            Flight flight = restTemplate.exchange("http://localhost:8202/flights/" + booking.getFlightId(), HttpMethod.GET, entity, Flight.class).getBody();
            final Booking savedBooking  = bookingService.createBooking(booking);

            //BigDecimal balance = rewardsService.getBalance(customer.getPassportNumber());
//            balance = balance.add(BigDecimal.valueOf(100));
//            rewardsService.updateBalance(customer.getPassportNumber(), balance);

            moloCellSmsClient.sendSms(customer.getPhoneNumber(), "Molo Air: Confirming flight " + flight.getFlightNumber() +
                    " booked for " + customer.getFirstName() + " " + customer.getFirstName() + " on " + flight.getDepartureTime() + ".");

            return new ResponseEntity<>(savedBooking, HttpStatus.OK);
        }
        catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<?> getBookings() {
        LOGGER.info("Fetching all bookings");
        List<Booking> bookings = bookingService.getBookings();

        if (!bookings.isEmpty()) {
            LOGGER.trace("Found bookings");
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        }

        LOGGER.info("No bookings could be found");
        return ResponseEntity.notFound().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Integer id) {
        LOGGER.info("Processing booking search request for booking id={}", id);
        Booking booking = this.bookingService.getBooking(id);

        if (booking != null) {
            LOGGER.trace("Found booking");
            return new ResponseEntity<>(booking, HttpStatus.OK);
        }

        LOGGER.info("No booking could be found for id={}", id);
        return ResponseEntity.notFound().build();
    }
}
