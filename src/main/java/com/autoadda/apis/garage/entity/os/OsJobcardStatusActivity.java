package com.autoadda.apis.garage.entity.os;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OsJobcardStatusActivity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "jobcard_id")
    private Integer jobcardId;

    @Column(name = "status")
    private Status status;

    @Column(name = "comments")
    private String comments;

    @Column(name = "insert_tms")
    private LocalDate insert_tms;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "member_type")
    private String memberType;

}
