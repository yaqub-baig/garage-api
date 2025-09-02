package com.autoadda.apis.garage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.garage.service.GarageFavoriteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class GarageFavoriteController {

	private final GarageFavoriteService favoriteService;

	@PostMapping("/garage")
	public ResponseEntity<?> addFavoriteGarage(@RequestParam Integer ownerId, @RequestParam Integer favGarageId) {
		return ResponseEntity.ok(favoriteService.addFavoriteGarage(ownerId, favGarageId));
	}

	@GetMapping("/garage/{ownerId}")
	public ResponseEntity<?> getFavoriteGarages(@PathVariable Integer ownerId) {
		return ResponseEntity.ok(favoriteService.getFavoriteGarages(ownerId));
	}



}
