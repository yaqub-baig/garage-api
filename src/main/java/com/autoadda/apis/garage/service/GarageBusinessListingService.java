package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageBusinessListing;
import com.autoadda.apis.garage.entity.GarageBusinessSale;
import com.autoadda.apis.garage.entity.GarageBusinessSaleInvoice;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.exception.UnauthorizedAccessException;
import com.autoadda.apis.garage.mapper.GarageBusinessListingMapper;
import com.autoadda.apis.garage.repository.GarageBusinessListingRepository;
import com.autoadda.apis.garage.repository.GarageBusinessSaleInvoiceRepository;
import com.autoadda.apis.garage.repository.GarageBusinessSaleRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.request.AddGarageBusinessListingRequest;
import com.autoadda.apis.garage.request.FinalizeGarageSaleRequest;
import com.autoadda.apis.garage.request.UpdateGarageBusinessListingRequest;
import com.autoadda.apis.garage.response.GarageBusinessListingResponse;
import com.autoadda.apis.garage.response.GarageBusinessSaleInvoiceResponse;
import com.autoadda.apis.garage.response.GarageBusinessSaleResponse;
import com.autoadda.apis.garage.response.PagedResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.GarageListingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GarageBusinessListingService {

    public static final String LISTING_NOT_FOUND = "Garage business listing not found";
    public static final String SALE_NOT_FOUND = "Garage business sale not found";
    private static final List<GarageListingStatus> OPEN_STATUSES =
            List.of(GarageListingStatus.ACTIVE, GarageListingStatus.UNDER_NEGOTIATION);

    private final GarageBusinessListingRepository listingRepo;
    private final GarageBusinessSaleRepository saleRepo;
    private final GarageBusinessSaleInvoiceRepository invoiceRepo;
    private final GarageRepository garageRepo;
    private final AuthClient authClient;
    private final MediaFileRepository mediaFileRepository;
    private final GarageBusinessListingMapper mapper;

    @Transactional
    public GarageBusinessListingResponse createListing(String autoAddaId, AddGarageBusinessListingRequest request) {
        Integer ownerId = Integer.parseInt(autoAddaId);
        log.info("Creating garage business listing for garage: {}", request.getGarageId());

        Garage garage = garageRepo.findById(request.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with ID: " + request.getGarageId()));

        if (!garage.getGarageOwner().equals(ownerId)) {
            throw new UnauthorizedAccessException("You are not authorized to list this garage for sale");
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getAskingPrice() == null || request.getAskingPrice() <= 0) {
            throw new IllegalArgumentException("Asking price must be greater than 0");
        }

        boolean hasOpenListing = !listingRepo.findByGarageIdAndStatusIn(garage.getId(), OPEN_STATUSES).isEmpty();
        if (hasOpenListing) {
            throw new IllegalStateException("This garage already has an active listing for sale");
        }

        List<MediaFile> images = request.getListingImageIds() == null
                ? List.of()
                : mediaFileRepository.findAllById(request.getListingImageIds());

        GarageBusinessListing listing = GarageBusinessListing.builder()
                .garage(garage)
                .title(request.getTitle())
                .description(request.getDescription())
                .reasonForSelling(request.getReasonForSelling())
                .askingPrice(request.getAskingPrice())
                .isNegotiable(request.getIsNegotiable())
                .status(GarageListingStatus.ACTIVE)
                .listingImages(images)
                .build();

        listing = listingRepo.save(listing);
        log.info("Garage business listing created with ID: {}", listing.getId());

        return mapper.entityToResponse(listing);
    }

    public GarageBusinessListingResponse getListing(Integer listingId) {
        GarageBusinessListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(LISTING_NOT_FOUND));
        return mapper.entityToResponse(listing);
    }

    public List<GarageBusinessListingResponse> getListingsForGarage(Integer garageId) {
        return listingRepo.findByGarageId(garageId).stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    public PagedResponse<GarageBusinessListingResponse> browseActiveListings(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<GarageBusinessListing> result = listingRepo.findByStatus(GarageListingStatus.ACTIVE, pageable);
        return toPagedResponse(result);
    }

    public PagedResponse<GarageBusinessListingResponse> searchListings(String q, Double minPrice, Double maxPrice,
                                                                        int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<GarageBusinessListing> result = listingRepo.searchListings(
                GarageListingStatus.ACTIVE, q, minPrice, maxPrice, pageable);
        return toPagedResponse(result);
    }

    @Transactional
    public GarageBusinessListingResponse updateListing(Integer listingId, String autoAddaId, UpdateGarageBusinessListingRequest request) {
        Integer ownerId = Integer.parseInt(autoAddaId);
        GarageBusinessListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(LISTING_NOT_FOUND));

        verifyOwnership(listing, ownerId);

        if (!OPEN_STATUSES.contains(listing.getStatus())) {
            throw new IllegalStateException("Only active or under-negotiation listings can be updated");
        }

        if (request.getTitle() != null) {
            listing.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            listing.setDescription(request.getDescription());
        }
        if (request.getReasonForSelling() != null) {
            listing.setReasonForSelling(request.getReasonForSelling());
        }
        if (request.getAskingPrice() != null) {
            if (request.getAskingPrice() <= 0) {
                throw new IllegalArgumentException("Asking price must be greater than 0");
            }
            listing.setAskingPrice(request.getAskingPrice());
        }
        if (request.getIsNegotiable() != null) {
            listing.setIsNegotiable(request.getIsNegotiable());
        }
        if (request.getListingImageIds() != null) {
            listing.setListingImages(mediaFileRepository.findAllById(request.getListingImageIds()));
        }

        listing = listingRepo.save(listing);
        return mapper.entityToResponse(listing);
    }

    @Transactional
    public GarageBusinessListingResponse markUnderNegotiation(Integer listingId, String autoAddaId) {
        GarageBusinessListing listing = requireOwnedOpenListing(listingId, autoAddaId);
        listing.setStatus(GarageListingStatus.UNDER_NEGOTIATION);
        listing = listingRepo.save(listing);
        return mapper.entityToResponse(listing);
    }

    @Transactional
    public GarageBusinessListingResponse reactivateListing(Integer listingId, String autoAddaId) {
        GarageBusinessListing listing = requireOwnedOpenListing(listingId, autoAddaId);
        listing.setStatus(GarageListingStatus.ACTIVE);
        listing = listingRepo.save(listing);
        return mapper.entityToResponse(listing);
    }

    @Transactional
    public GarageBusinessListingResponse withdrawListing(Integer listingId, String autoAddaId) {
        Integer ownerId = Integer.parseInt(autoAddaId);
        GarageBusinessListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(LISTING_NOT_FOUND));
        verifyOwnership(listing, ownerId);

        if (listing.getStatus() == GarageListingStatus.SOLD) {
            throw new IllegalStateException("A sold listing cannot be withdrawn");
        }

        listing.setStatus(GarageListingStatus.WITHDRAWN);
        listing = listingRepo.save(listing);
        return mapper.entityToResponse(listing);
    }

    @Transactional
    public GarageBusinessSaleResponse finalizeSale(Integer listingId, String autoAddaId, FinalizeGarageSaleRequest request) {
        Integer ownerId = Integer.parseInt(autoAddaId);
        GarageBusinessListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(LISTING_NOT_FOUND));
        verifyOwnership(listing, ownerId);

        if (!OPEN_STATUSES.contains(listing.getStatus())) {
            throw new IllegalStateException("Only active or under-negotiation listings can be sold");
        }
        if (request.getBuyerId() == null) {
            throw new IllegalArgumentException("Buyer id is required");
        }
        if (request.getSalePrice() == null || request.getSalePrice() <= 0) {
            throw new IllegalArgumentException("Sale price must be greater than 0");
        }

        UserResponse buyer = authClient.getUser(request.getBuyerId());
        if (buyer == null) {
            throw new ResourceNotFoundException("Buyer not found with ID: " + request.getBuyerId());
        }

        GarageBusinessSale sale = GarageBusinessSale.builder()
                .listing(listing)
                .buyerId(request.getBuyerId())
                .salePrice(request.getSalePrice())
                .paymentMode(request.getPaymentMode())
                .notes(request.getNotes())
                .build();
        sale = saleRepo.save(sale);

        listing.setSale(sale);
        listing.setStatus(GarageListingStatus.SOLD);
        listingRepo.save(listing);

        log.info("Garage business listing {} sold to buyer {}", listingId, request.getBuyerId());

        return mapper.saleToResponse(sale);
    }

    public GarageBusinessSaleResponse getSale(Integer saleId) {
        GarageBusinessSale sale = saleRepo.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(SALE_NOT_FOUND));
        return mapper.saleToResponse(sale);
    }

    @Transactional
    public GarageBusinessSaleInvoiceResponse generateInvoice(Integer saleId) {
        GarageBusinessSale sale = saleRepo.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(SALE_NOT_FOUND));

        String invoiceNumber = "INV-" + saleId + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        GarageBusinessSaleInvoice invoice = GarageBusinessSaleInvoice.builder()
                .sale(sale)
                .invoiceNumber(invoiceNumber)
                .totalAmount(sale.getSalePrice())
                .paymentMethod(sale.getPaymentMode())
                .build();

        invoice = invoiceRepo.save(invoice);
        return mapper.invoiceToResponse(invoice);
    }

    public GarageBusinessSaleInvoiceResponse getInvoiceForSale(Integer saleId) {
        GarageBusinessSaleInvoice invoice = invoiceRepo.findBySaleId(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for sale: " + saleId));
        return mapper.invoiceToResponse(invoice);
    }

    private GarageBusinessListing requireOwnedOpenListing(Integer listingId, String autoAddaId) {
        Integer ownerId = Integer.parseInt(autoAddaId);
        GarageBusinessListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException(LISTING_NOT_FOUND));
        verifyOwnership(listing, ownerId);

        if (!OPEN_STATUSES.contains(listing.getStatus())) {
            throw new IllegalStateException("Only active or under-negotiation listings can change state");
        }
        return listing;
    }

    private void verifyOwnership(GarageBusinessListing listing, Integer ownerId) {
        if (!listing.getGarage().getGarageOwner().equals(ownerId)) {
            throw new UnauthorizedAccessException("You are not authorized to manage this listing");
        }
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    private PagedResponse<GarageBusinessListingResponse> toPagedResponse(Page<GarageBusinessListing> page) {
        return PagedResponse.<GarageBusinessListingResponse>builder()
                .content(page.getContent().stream().map(mapper::entityToResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}
