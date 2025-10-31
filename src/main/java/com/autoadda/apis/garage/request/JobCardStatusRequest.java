package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.JobCardStatus;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobCardStatusRequest {
    private Integer jobcardId;
    private JobCardStatus status;
    private Integer memberId;

    // Part request details (required when status is WAITING_FOR_PARTS)
    private List<PartRequestItemDto> partRequestItems;
    private String partRequestNotes;
}
