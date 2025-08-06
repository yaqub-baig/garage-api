package com.service.garage.apis.service;

import com.service.garage.apis.entity.Location;
import com.service.garage.apis.repository.LocationRepository;
import com.service.garage.apis.request.LocationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

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
    public Location updateLocation(Integer id,LocationRequest location) {
        Location location1 = locationRepository.findById ( id ).orElseThrow ( () -> new NoSuchElementException ( "Location not found with id: " + id ) );
        if(location.getStreetAddress1()!=null) location1.setStreetAddress1(location.getStreetAddress1());
        if(location.getStreetAddress2()!=null) location1.setStreetAddress2(location.getStreetAddress2());
        if(location.getCity()!=null) location1.setCity(location.getCity());
        if(location.getPostalCode()!=null) location1.setPostalCode(location.getPostalCode());
        if(location.getArea()!=null) location1.setArea(location.getArea());
        if(location.getState()!=null) location1.setState(location.getState());
        if(location.getCountry()!=null) location1.setCountry(location.getCountry());
        if(location.getGmap()!=null) location1.setGmap(location.getGmap());
        return locationRepository.save ( location1 );
    }
}
