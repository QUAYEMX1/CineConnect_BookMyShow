package com.acciojob.bookmyshowapplication.Service;

import com.acciojob.bookmyshowapplication.Models.*;
import com.acciojob.bookmyshowapplication.Repository.*;
import com.acciojob.bookmyshowapplication.Requests.GetAvailableSeatsRequest;
import com.acciojob.bookmyshowapplication.Requests.SeatSelectionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private SeatSelectionRepository seatSelectionRepository;

    public Map<String, Object> getAvailableSeatsWithLayout(GetAvailableSeatsRequest request) {
        // Find the show
        Movie movie = movieRepository.findMovieByMovieName(request.getMovieName());
        Theater theater = theaterRepository.findById(request.getTheaterId()).get();
        Show show = showRepository.findShowByShowDateAndShowTimeAndMovieAndTheater(
                request.getShowDate(), request.getShowTime(), movie, theater);

        // Get all show seats
        List<ShowSeat> showSeats = showSeatRepository.findAllByShow(show);

        // Get temporarily selected seats
        //this is using version of date now new version is LocalDate
//        List<SeatSelection> tempSelections = seatSelectionRepository.findByShowAndStatusAndCreatedAtAfter(
//                show, "TEMP", new Date(System.currentTimeMillis() - 10 * 60 * 1000));
        List<SeatSelection> tempSelections = seatSelectionRepository.findByShowAndStatusAndCreatedAtAfter(
                show, "TEMP", LocalDateTime.now().minusMinutes(10)); // 10 minutes

        Set<String> tempSelectedSeats = tempSelections.stream()
                .map(SeatSelection::getSeatNo)
                .collect(Collectors.toSet());

        // Create seat layout
        Map<String, Object> seatLayout = new HashMap<>();
        List<Map<String, Object>> seats = new ArrayList<>();

        for (ShowSeat seat : showSeats) {
            Map<String, Object> seatInfo = new HashMap<>();
            seatInfo.put("seatNo", seat.getSeatNo());
            seatInfo.put("seatType", seat.getSeatType().toString());
            seatInfo.put("price", seat.getPrice());

            if (!seat.getIsAvailable()) {
                seatInfo.put("status", "BOOKED");
            } else if (tempSelectedSeats.contains(seat.getSeatNo())) {
                seatInfo.put("status", "TEMP_SELECTED");
            } else {
                seatInfo.put("status", "AVAILABLE");
            }

            seats.add(seatInfo);
        }

        seatLayout.put("seats", seats);
        seatLayout.put("showId", show.getShowId());
        seatLayout.put("movieName", movie.getMovieName());
        seatLayout.put("theaterName", theater.getName());

        return seatLayout;
    }

    @Transactional
    public Map<String, Object> selectSeats(SeatSelectionRequest request) {

        Show show = showRepository.findById(request.getShowId()).get();

        // Lock the selected seats (DB level)
        List<ShowSeat> seatsToBook = showSeatRepository.findAndLockSeatsByShowAndSeatNos(
                show, request.getSelectedSeats()
        );

        // Get already TEMP selected seats (last 10 mins)
        List<SeatSelection> existingSelections =
                seatSelectionRepository.findByShowAndStatusAndCreatedAtAfter(
                        show, "TEMP", LocalDateTime.now().minusMinutes(10)
                );

        Set<String> alreadyTempSeats = existingSelections.stream()
                .map(SeatSelection::getSeatNo)
                .collect(Collectors.toSet());

        // Check availability (BOOKED + TEMP)
        List<String> unavailableSeats = new ArrayList<>();
        int totalAmount = 0;

        for (ShowSeat seat : seatsToBook) {

            if (!seat.getIsAvailable() || alreadyTempSeats.contains(seat.getSeatNo())) {
                unavailableSeats.add(seat.getSeatNo());
            } else {
                totalAmount += seat.getPrice();
            }
        }

        // If any seat unavailable → fail
        if (!unavailableSeats.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Some seats are not available or already temporarily selected");
            response.put("unavailableSeats", unavailableSeats);
            return response;
        }

        // Clear previous TEMP selections for this user
        seatSelectionRepository.deleteByUserMobNoAndShow(request.getUserMobNo(), show);

        // Create new TEMP selections
        for (String seatNo : request.getSelectedSeats()) {
            SeatSelection selection = SeatSelection.builder()
                    .show(show)
                    .seatNo(seatNo)
                    .userMobNo(request.getUserMobNo())
                    .status("TEMP")
                    .createdAt(LocalDateTime.now())
                    .build();

            seatSelectionRepository.save(selection);
        }

        // Success response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalAmount", totalAmount);
        response.put("selectedSeats", request.getSelectedSeats());
        response.put("message", "Seats temporarily selected for 10 minutes");

        return response;
    }

    public String releaseTemporarySeats(SeatSelectionRequest request) {
        Show show = showRepository.findById(request.getShowId()).get();
        seatSelectionRepository.deleteByUserMobNoAndShow(request.getUserMobNo(), show);
        return "Temporary seat selections released";
    }
}
