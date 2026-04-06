package com.acciojob.bookmyshowapplication.Repository;

import com.acciojob.bookmyshowapplication.Models.Show;
import com.acciojob.bookmyshowapplication.Models.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat,Integer> {

    public List<ShowSeat> findAllByShow(Show show); //Inbuilt method invoking

    @Lock(LockModeType.PESSIMISTIC_WRITE) // <-- This ensures row is locked for this transaction
    @Query("SELECT s FROM ShowSeat s WHERE s.show = :show AND s.seatNo IN :seatNos")
    List<ShowSeat> findAndLockSeatsByShowAndSeatNos(Show show, List<String> seatNos);
}
