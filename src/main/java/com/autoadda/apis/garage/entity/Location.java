package com.autoadda.apis.garage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "street_address_1")
    private String streetAddress1;

    @Column(name = "street_address_2")
    private String streetAddress2;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;
    @Column(name = "area")
    private String area;
    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;
    @Column(name = "gmap", length = 2048)
    private String gmap;

    @Column(name = "type")
    private String type;

    @Override
    public String toString() {
        StringBuilder address = new StringBuilder();

        if (streetAddress1 != null && !streetAddress1.isBlank()) {
            address.append(streetAddress1).append("\n");
        }
        if (streetAddress2 != null && !streetAddress2.isBlank()) {
            address.append(streetAddress2).append("\n");
        }
        if (area != null && !area.isBlank()) {
            address.append(area).append("\n");
        }

        address.append(city);

        if (state != null && !state.isBlank()) {
            address.append(", ").append(state);
        }

        if (postalCode != null && !postalCode.isBlank()) {
            address.append(" - ").append(postalCode);
        }

        address.append("\n").append(country);

        return address.toString();
    }

}