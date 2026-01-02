package com.autoadda.apis.garage.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TechnicianSearchCriteria {
    private String query;                       // Universal search parameter
    private String name;                        // Technician name filter
    private List<Integer> skillIds;             // Skill IDs filter
    private List<String> expertiseCategories;   // Expertise categories filter
    private List<String> expertiseLevels;       // Expertise levels filter
    private String city;                        // Location city filter
    private String brand;                       // Vehicle brand specialization
    private String model;                       // Vehicle model specialization
}
