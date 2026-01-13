package com.autoadda.apis.garage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "garage_rating_response")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class GarageRatingResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "garage_rating_id", nullable = false, unique = true)
    @JsonIgnore
    private GarageRating garageRating;

    @Column(name = "responder_id", nullable = false)
    private Integer responderId;

    @Column(name = "response_text", nullable = false, length = 2000)
    private String responseText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
