package com.service.garage.apis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.service.GarageFavoriteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorites")
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
