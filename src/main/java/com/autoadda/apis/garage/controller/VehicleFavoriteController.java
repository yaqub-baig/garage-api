package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.VehicleFavorite;
import com.autoadda.apis.garage.service.VehicleFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class VehicleFavoriteController {

    private final VehicleFavoriteService favoriteService;

    @PostMapping("/vehicle")
    public ResponseEntity<VehicleFavorite> addFavoriteVehicle(@RequestParam Integer ownerId, @RequestParam Integer favVehicleId) {
        return ResponseEntity.ok(favoriteService.addFavoriteVehicle(ownerId, favVehicleId));
    }

    @GetMapping("/vehicle/{ownerId}")
    public ResponseEntity<List<VehicleFavorite>> getFavoriteVehicles(@PathVariable Integer ownerId) {
        return ResponseEntity.ok(favoriteService.getFavoriteVehicles(ownerId));
    }
}
