package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.ExpertiseCategory;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.ExpertiseCategoryRepository;
import com.autoadda.apis.garage.request.ExpertiseCategoryRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpertiseCategoryService {

    private final ExpertiseCategoryRepository expertiseCategoryRepository;

    @Transactional
    @CacheEvict(value = "expertiseCategory", allEntries = true)
    public ExpertiseCategory createCategory(ExpertiseCategoryRequest request) {
        if (expertiseCategoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AlreadyExistsException("Expertise category already exists with name: " + request.getName());
        }
        ExpertiseCategory category = ExpertiseCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();
        return expertiseCategoryRepository.save(category);
    }

    @Transactional
    @Cacheable(value = "expertiseCategory", key = "#activeOnly")
    public List<ExpertiseCategory> getAllCategories(boolean activeOnly) {
        return activeOnly
                ? expertiseCategoryRepository.findByIsActiveTrue()
                : expertiseCategoryRepository.findAll();
    }

    @Transactional
    public ExpertiseCategory getCategoryById(Integer id) {
        return expertiseCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expertise category not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = "expertiseCategory", allEntries = true)
    public ExpertiseCategory updateCategory(Integer id, ExpertiseCategoryRequest request) {
        ExpertiseCategory category = getCategoryById(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return expertiseCategoryRepository.save(category);
    }

    /**
     * Soft-deletes a category by deactivating it, preserving existing
     * technician_expertise references and referential integrity.
     */
    @Transactional
    @CacheEvict(value = "expertiseCategory", allEntries = true)
    public void deleteCategory(Integer id) {
        ExpertiseCategory category = getCategoryById(id);
        category.setIsActive(false);
        expertiseCategoryRepository.save(category);
    }
}
