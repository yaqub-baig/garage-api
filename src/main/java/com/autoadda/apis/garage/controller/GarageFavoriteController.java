package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.service.GarageFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class GarageFavoriteController {

    private final GarageFavoriteService favoriteService;

    @PostMapping("/garage")
    public ResponseEntity<?> addFavoriteGarage(@RequestParam Integer ownerId, @RequestParam Integer favGarageId) {
        return ResponseEntity.ok ( favoriteService.addFavoriteGarage ( ownerId, favGarageId ) );
    }

    @GetMapping("/garage/{ownerId}")
    public ResponseEntity<?> getFavoriteGarages(@PathVariable Integer ownerId) {
        return ResponseEntity.ok ( favoriteService.getFavoriteGarages ( ownerId ) );
    }

    @PostMapping("/garage/unmark")
    public  ResponseEntity<?> unmarkFavoriteGarage(@RequestParam Integer ownerId, @RequestParam Integer favGarageId){
        favoriteService.removeFavoriteGarage(ownerId,favGarageId);
        return  ResponseEntity.ok(HttpEntity.EMPTY);
    }




}
