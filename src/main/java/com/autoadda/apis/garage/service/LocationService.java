package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.repository.LocationRepository;
import com.autoadda.apis.garage.request.LocationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    public Location createLocation(LocationRequest garageLocation) {
        Location location = new Location();
        location.setStreetAddress1(garageLocation.getStreetAddress1());
        location.setStreetAddress2(garageLocation.getStreetAddress2());
        location.setCity(garageLocation.getCity());
        location.setPostalCode(garageLocation.getPostalCode());
        location.setArea(garageLocation.getArea());
        location.setState(garageLocation.getState());
        location.setCountry(garageLocation.getCountry());
        location.setGmap(garageLocation.getGmap());
        locationRepository.save(location);
        return location;
    }
}
