package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageRating;
import com.autoadda.apis.garage.entity.GarageRatingResponse;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.exception.UnauthorizedAccessException;
import com.autoadda.apis.garage.mapper.GarageRatingMapper;
import com.autoadda.apis.garage.repository.GarageRatingRepository;
import com.autoadda.apis.garage.repository.GarageRatingResponseRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.WorkOrderRepository;
import com.autoadda.apis.garage.request.CreateGarageRatingRequest;
import com.autoadda.apis.garage.request.GarageRatingResponseRequest;
import com.autoadda.apis.garage.request.UpdateGarageRatingRequest;
import com.autoadda.apis.garage.response.CanRateResponse;
import com.autoadda.apis.garage.response.GarageRatingDto;
import com.autoadda.apis.garage.response.GarageRatingSummaryResponse;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GarageRatingService {

    private final GarageRatingRepository ratingRepository;
    private final GarageRatingResponseRepository responseRepository;
    private final WorkOrderRepository workOrderRepository;
    private final GarageRepository garageRepository;
    private final GarageRatingMapper ratingMapper;

    private static final Set<WorkOrderStatus> RATABLE_STATUSES = Set.of(
            WorkOrderStatus.COMPLETED,
            WorkOrderStatus.DELIVERED_TO_CUSTOMER
    );

    @Transactional
    public GarageRatingDto createRating(CreateGarageRatingRequest request, Integer customerId) {
        log.info("Creating rating for work order {} by customer {}", request.getWorkOrderId(), customerId);

        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + request.getWorkOrderId()));

        validateCanRate(workOrder, customerId);

        if (ratingRepository.existsByWorkOrderId(request.getWorkOrderId())) {
            throw new BadRequestException("A rating already exists for this work order");
        }

        GarageRating rating = GarageRating.builder()
                .garage(workOrder.getGarage())
                .workOrder(workOrder)
                .customerId(customerId)
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .isVisible(true)
                .build();

        GarageRating savedRating = ratingRepository.save(rating);
        log.info("Rating created successfully with id {}", savedRating.getId());

        return ratingMapper.entityToDto(savedRating);
    }

    @Transactional
    public GarageRatingDto updateRating(Integer ratingId, UpdateGarageRatingRequest request, Integer customerId) {
        log.info("Updating rating {} by customer {}", ratingId, customerId);

        GarageRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        if (!rating.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this rating");
        }

        if (request.getRating() != null) {
            rating.setRating(request.getRating());
        }
        if (request.getReviewText() != null) {
            rating.setReviewText(request.getReviewText());
        }

        GarageRating updatedRating = ratingRepository.save(rating);
        log.info("Rating updated successfully");

        return ratingMapper.entityToDto(updatedRating);
    }

    @Transactional
    public void deleteRating(Integer ratingId, Integer customerId) {
        log.info("Deleting rating {} by customer {}", ratingId, customerId);

        GarageRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        if (!rating.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this rating");
        }

        ratingRepository.delete(rating);
        log.info("Rating deleted successfully");
    }

    @Transactional(readOnly = true)
    public CanRateResponse canRate(Integer workOrderId, Integer customerId) {
        log.info("Checking if customer {} can rate work order {}", customerId, workOrderId);

        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);

        if (workOrder == null) {
            return CanRateResponse.builder()
                    .canRate(false)
                    .reason("Work order not found")
                    .workOrderId(workOrderId)
                    .build();
        }

        Integer workOrderCustomerId = workOrder.getAppointment() != null
                ? workOrder.getAppointment().getCustomerId()
                : null;

        if (!customerId.equals(workOrderCustomerId)) {
            return CanRateResponse.builder()
                    .canRate(false)
                    .reason("You are not the customer for this work order")
                    .workOrderId(workOrderId)
                    .garageId(workOrder.getGarage() != null ? workOrder.getGarage().getId() : null)
                    .build();
        }

        if (!RATABLE_STATUSES.contains(workOrder.getStatus())) {
            return CanRateResponse.builder()
                    .canRate(false)
                    .reason("Work order must be completed or delivered to rate")
                    .workOrderId(workOrderId)
                    .garageId(workOrder.getGarage() != null ? workOrder.getGarage().getId() : null)
                    .build();
        }

        if (ratingRepository.existsByWorkOrderId(workOrderId)) {
            return CanRateResponse.builder()
                    .canRate(false)
                    .reason("You have already rated this work order")
                    .workOrderId(workOrderId)
                    .garageId(workOrder.getGarage() != null ? workOrder.getGarage().getId() : null)
                    .build();
        }

        return CanRateResponse.builder()
                .canRate(true)
                .reason(null)
                .workOrderId(workOrderId)
                .garageId(workOrder.getGarage() != null ? workOrder.getGarage().getId() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public GarageRatingDto getRatingById(Integer ratingId) {
        GarageRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));
        return ratingMapper.entityToDto(rating);
    }

    @Transactional(readOnly = true)
    public GarageRatingDto getRatingByWorkOrderId(Integer workOrderId) {
        GarageRating rating = ratingRepository.findByWorkOrderId(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for work order: " + workOrderId));
        return ratingMapper.entityToDto(rating);
    }

    @Transactional(readOnly = true)
    public Page<GarageRatingDto> getGarageRatings(Integer garageId, Integer stars, Pageable pageable) {
        log.info("Fetching ratings for garage {} with stars filter {}", garageId, stars);

        garageRepository.findById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));

        Page<GarageRating> ratings;
        if (stars != null && stars >= 1 && stars <= 5) {
            ratings = ratingRepository.findByGarageIdAndRatingAndIsVisibleTrue(garageId, stars, pageable);
        } else {
            ratings = ratingRepository.findByGarageIdAndIsVisibleTrue(garageId, pageable);
        }

        return ratings.map(ratingMapper::entityToDto);
    }

    @Transactional(readOnly = true)
    public List<GarageRatingDto> getCustomerRatings(Integer customerId) {
        log.info("Fetching ratings by customer {}", customerId);
        List<GarageRating> ratings = ratingRepository.findByCustomerId(customerId);
        return ratings.stream().map(ratingMapper::entityToDto).toList();
    }

    @Transactional(readOnly = true)
    public GarageRatingSummaryResponse getGarageSummary(Integer garageId) {
        log.info("Fetching rating summary for garage {}", garageId);

        garageRepository.findById(garageId)
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));

        Double averageRating = ratingRepository.calculateAverageRatingByGarageId(garageId);
        long totalRatings = ratingRepository.countByGarageIdAndIsVisibleTrue(garageId);

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        List<Object[]> distributionData = ratingRepository.getRatingDistributionByGarageId(garageId);
        for (Object[] row : distributionData) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count);
        }

        return GarageRatingSummaryResponse.builder()
                .garageId(garageId)
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : null)
                .totalRatings(totalRatings)
                .ratingDistribution(distribution)
                .build();
    }

    @Transactional
    public GarageRatingDto addOwnerResponse(GarageRatingResponseRequest request, Integer ownerId) {
        log.info("Adding owner response to rating {} by owner {}", request.getGarageRatingId(), ownerId);

        GarageRating rating = ratingRepository.findById(request.getGarageRatingId())
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + request.getGarageRatingId()));

        validateGarageOwnership(rating.getGarage(), ownerId);

        if (rating.getOwnerResponse() != null) {
            throw new BadRequestException("A response already exists for this rating");
        }

        GarageRatingResponse response = GarageRatingResponse.builder()
                .garageRating(rating)
                .responderId(ownerId)
                .responseText(request.getResponseText())
                .build();

        responseRepository.save(response);
        rating.setOwnerResponse(response);

        log.info("Owner response added successfully");
        return ratingMapper.entityToDto(rating);
    }

    @Transactional
    public GarageRatingDto updateOwnerResponse(Integer responseId, String responseText, Integer ownerId) {
        log.info("Updating owner response {} by owner {}", responseId, ownerId);

        GarageRatingResponse response = responseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Response not found with id: " + responseId));

        validateGarageOwnership(response.getGarageRating().getGarage(), ownerId);

        response.setResponseText(responseText);
        responseRepository.save(response);

        log.info("Owner response updated successfully");
        return ratingMapper.entityToDto(response.getGarageRating());
    }

    @Transactional
    public void deleteOwnerResponse(Integer responseId, Integer ownerId) {
        log.info("Deleting owner response {} by owner {}", responseId, ownerId);

        GarageRatingResponse response = responseRepository.findById(responseId)
                .orElseThrow(() -> new ResourceNotFoundException("Response not found with id: " + responseId));

        validateGarageOwnership(response.getGarageRating().getGarage(), ownerId);

        GarageRating rating = response.getGarageRating();
        rating.setOwnerResponse(null);
        responseRepository.delete(response);

        log.info("Owner response deleted successfully");
    }

    private void validateCanRate(WorkOrder workOrder, Integer customerId) {
        Integer workOrderCustomerId = workOrder.getAppointment() != null
                ? workOrder.getAppointment().getCustomerId()
                : null;

        if (!customerId.equals(workOrderCustomerId)) {
            throw new UnauthorizedAccessException("You are not authorized to rate this work order");
        }

        if (!RATABLE_STATUSES.contains(workOrder.getStatus())) {
            throw new BadRequestException("Work order must be completed or delivered to rate. Current status: " + workOrder.getStatus());
        }
    }

    private void validateGarageOwnership(Garage garage, Integer ownerId) {
        if (garage == null || !ownerId.equals(garage.getGarageOwner())) {
            throw new UnauthorizedAccessException("You are not authorized to manage responses for this garage");
        }
    }
}
