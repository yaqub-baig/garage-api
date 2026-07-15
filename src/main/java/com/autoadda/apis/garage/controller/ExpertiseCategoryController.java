package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.ExpertiseCategory;
import com.autoadda.apis.garage.request.ExpertiseCategoryRequest;
import com.autoadda.apis.garage.service.ExpertiseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/expertise-categories")
@RequiredArgsConstructor
public class ExpertiseCategoryController {

    private final ExpertiseCategoryService expertiseCategoryService;

    @PostMapping
    public ResponseEntity<ExpertiseCategory> create(@RequestBody ExpertiseCategoryRequest request) {
        return ResponseEntity.ok(expertiseCategoryService.createCategory(request));
    }

    @GetMapping
    public ResponseEntity<List<ExpertiseCategory>> getAll(
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(expertiseCategoryService.getAllCategories(activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpertiseCategory> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(expertiseCategoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpertiseCategory> update(@PathVariable Integer id,
                                                      @RequestBody ExpertiseCategoryRequest request) {
        return ResponseEntity.ok(expertiseCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        expertiseCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
