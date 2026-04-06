package com.acciojob.bookmyshowapplication.Service;

import com.acciojob.bookmyshowapplication.Models.Ticket;
import com.acciojob.bookmyshowapplication.Requests.BookTicketRequest;
import com.acciojob.bookmyshowapplication.Requests.SeatSelectionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class TicketServiceTests  {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SeatService seatService;

    @Test
    void testMultipleBooking() throws InterruptedException {

        // STEP 1: Both users select seat (both will succeed)
        for (String mobNo : List.of("9999999999", "888888888")) {
            SeatSelectionRequest req = new SeatSelectionRequest();
            req.setShowId(1);
            req.setUserMobNo(mobNo);
            req.setSelectedSeats(List.of("1D"));
            Map<String, Object> result =  seatService.selectSeats(req);
            System.out.println("Step1 - User " + mobNo + " → " + result); // ← print it
        }

        // STEP 2: Both try to book simultaneously
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (String mobNo : List.of("9999999999", "888888888")) {
            executor.submit(() -> {
                try {
                    BookTicketRequest request = new BookTicketRequest();
                    request.setMovieName("KGF");
                    request.setShowDate(LocalDate.of(2024, 3, 25));
                    request.setShowTime(LocalTime.of(18, 0));
                    request.setTheaterId(1);
                    request.setMobNo(mobNo);
                    request.setRequestedSeats(List.of("1D"));
                    Ticket ticket = ticketService.bookTicket(request);
                    System.out.println("User " + mobNo + " → BOOKED! " + ticket.getTicketId());
                } catch (Exception e) {
                    System.out.println("User " + mobNo + " → FAILED: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
    }

}
