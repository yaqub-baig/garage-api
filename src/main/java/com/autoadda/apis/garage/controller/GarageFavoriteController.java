package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.response.GarageFavoriteResponse;
import com.autoadda.apis.garage.service.GarageFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class GarageFavoriteController {

    private final GarageFavoriteService favoriteService;

    @PostMapping("/garage")
    public ResponseEntity<GarageFavoriteResponse> addFavoriteGarage(@RequestParam Integer ownerId, @RequestParam Integer favGarageId) {
        return ResponseEntity.ok(favoriteService.addFavoriteGarage(ownerId, favGarageId));
    }

    @GetMapping("/garage/{ownerId}")
    public ResponseEntity<List<GarageFavoriteResponse>> getFavoriteGarages(@PathVariable Integer ownerId) {
        return ResponseEntity.ok(favoriteService.getFavoriteGarages(ownerId));
    }

    @PostMapping("/garage/unmark")
    public ResponseEntity<Void> unmarkFavoriteGarage(@RequestParam Integer ownerId, @RequestParam Integer favGarageId) {
        favoriteService.removeFavoriteGarage(ownerId, favGarageId);
        return ResponseEntity.ok().build();
    }


}
