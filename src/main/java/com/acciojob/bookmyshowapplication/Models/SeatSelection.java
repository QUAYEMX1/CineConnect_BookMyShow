package com.acciojob.bookmyshowapplication.Models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_selections")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer selectionId;

    @ManyToOne
    @JoinColumn
    private Show show;

    private String seatNo;
    private String userMobNo;
    private String status; // TEMP, CONFIRMED

//    @Temporal(TemporalType.TIMESTAMP)
//    private Date createdAt;

//    @CreationTimestamp
//    @Column(updatable = false)
    private LocalDateTime createdAt;
}
