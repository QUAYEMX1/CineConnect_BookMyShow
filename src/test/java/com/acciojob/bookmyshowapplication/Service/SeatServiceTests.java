package com.acciojob.bookmyshowapplication.Service;

import com.acciojob.bookmyshowapplication.Requests.SeatSelectionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class SeatServiceTests {

    @Autowired
    private SeatService seatService;

    @Test
    void testMultipleBooking() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        List<String> users = List.of("9999999999", "888888888");
        List<String> seatsToBook = List.of("1C");

        for (String mobNo : users) {
            executor.submit(() -> {
                SeatSelectionRequest request = new SeatSelectionRequest();
                request.setShowId(1);
                request.setUserMobNo(mobNo);
                request.setSelectedSeats(seatsToBook);
                Map<String, Object> response = seatService.selectSeats(request);
                System.out.println("User " + mobNo + " result: " + response);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
    }
}
