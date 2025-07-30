package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.entity.OtherService;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.OtherServiceRepository;
import com.autoadda.apis.garage.request.OtherServiceRequest;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtherServiceManagementServiceImpl implements OtherServiceManagementService {

    private final OtherServiceRepository otherServiceRepository;
    private final LocationService locationService;
    private final MediaFileService mediaFileService;

    private OtherService mapToEntity(OtherServiceRequest request) {
        OtherService otherService = new OtherService();
        otherService.setCompanyBrandName(request.getCompanyBrandName());
        otherService.setName(request.getName());
        otherService.setEmail(request.getEmail());
        otherService.setPhoneNumber(request.getPhoneNumber());
        otherService.setMobileNumber(request.getMobileNumber());
        otherService.setTrnNumber(request.getTrnNumber());
        otherService.setWebSite(request.getWebSite());
        otherService.setAccountStatus(Status.PENDING); // Default status
        otherService.setServiceType(request.getServiceType());
        otherService.setCompanyArabicName(request.getCompanyArabicName());
        otherService.setCreatedAt(LocalDateTime.now());
        return otherService;
    }

    @Override
    @Transactional
    public OtherService createOtherService(OtherServiceRequest otherServiceRequest,
                                         String userId,
                                         String userRole,
                                         String autoAddaId,
                                         MultipartFile companyLogo,
                                         List<MultipartFile> vatCertificate,
                                         List<MultipartFile> tradeLicenseAttachment,
                                         List<MultipartFile> companyImages) throws IOException {
        
        OtherService otherService = mapToEntity(otherServiceRequest);
        
        // Set location
        Location location = locationService.createLocation(otherServiceRequest.getCompanyLocation());
        otherService.setCompanyLocation(location);
        otherService.setCompanyOwner(Integer.parseInt(autoAddaId));
        
        // Handle company logo
        List<MediaFile> logo = mediaFileService.upload(Collections.singletonList(companyLogo));
        MediaFile logoMediaFile = logo.get(0);
        otherService.setCompanyLogo(logoMediaFile);
        
        // Handle VAT certificates
        if (vatCertificate != null && !vatCertificate.isEmpty()) {
            List<MediaFile> vatCertificates = mediaFileService.upload(vatCertificate);
            otherService.setVatCertificate( vatCertificates );
        }
        
        // Handle trade license attachments
        if (tradeLicenseAttachment != null ) {
            List<MediaFile> tradeLicenseAttachments = mediaFileService.upload(tradeLicenseAttachment);
            otherService.setTradeLicenseAttachment( tradeLicenseAttachments );
        }
        
        // Handle company images
        if (companyImages != null ) {
            List<MediaFile> uploadedCompanyImages = mediaFileService.upload(companyImages);
            otherService.setCompanyImages(uploadedCompanyImages);
        }
        
        return otherServiceRepository.save(otherService);
    }

    @Override
    public List<OtherService> getAllOtherServices() {
        return otherServiceRepository.findAll().stream()
                .filter(OtherService::isActive)
                .toList();
    }

    @Override
    public List<OtherService> getAllOtherServicesByOwnerId(String autoAddaId) {
        return otherServiceRepository.findByCompanyOwner(Integer.parseInt(autoAddaId)).stream()
                .filter(OtherService::isActive)
                .toList();
    }

    @Override
    public Optional<OtherService> getOtherServiceById(Integer id) {
        return otherServiceRepository.findById(id)
                .filter(OtherService::isActive);
    }

    @Override
    @Transactional
    public OtherService updateOtherService(Integer id, Map<String, Object> request) throws IOException {
        OtherService existing = otherServiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Other service not found with id: " + id));
                
        if (request.containsKey("data")) {
            OtherServiceRequest data = (OtherServiceRequest) request.get("data");
            
            // Update location if provided
            if (data.getCompanyLocation() != null) {
                Location existingLocation = existing.getCompanyLocation();
                Location location = locationService.updateLocation(existingLocation.getId(), data.getCompanyLocation());
                existing.setCompanyLocation(location);
            }
            
            // Update other fields if provided
            if (data.getServiceType() != null) {
                existing.setServiceType(data.getServiceType());
            }
            if (data.getCompanyArabicName() != null) {
                existing.setCompanyArabicName(data.getCompanyArabicName());
            }
            if (data.getCompanyBrandName() != null) {
                existing.setCompanyBrandName(data.getCompanyBrandName());
            }
            if (data.getTrnNumber() != null) {
                existing.setTrnNumber(data.getTrnNumber());
            }
            if (data.getPhoneNumber() != null) {
                existing.setPhoneNumber(data.getPhoneNumber());
            }
            if (data.getWebSite() != null) {
                existing.setWebSite(data.getWebSite());
            }
            if (data.getName() != null) {
                existing.setName(data.getName());
            }
            if (data.getMobileNumber() != null) {
                existing.setMobileNumber(data.getMobileNumber());
            }
            if (data.getEmail() != null) {
                existing.setEmail(data.getEmail());
            }
        }
        
        // Handle company logo update
        if (request.containsKey("companyLogo")) {
            // Schedule old logo for cleanup
            if (existing.getCompanyLogo() != null) {
                scheduleMediaFileCleanup(Collections.singletonList(existing.getCompanyLogo().getId()));
            }
            
            // Upload and set new logo
            MultipartFile companyLogo = (MultipartFile) request.get("companyLogo");
            List<MediaFile> logo = mediaFileService.upload(Collections.singletonList(companyLogo));
            existing.setCompanyLogo(logo.get(0));
        }
        
        // Handle VAT certificate update
        if (request.containsKey("vatCertificate")) {
            // Schedule old VAT certificates for cleanup
            List<Long> oldVatCertIds = existing.getVatCertificate().stream()
                    .map(MediaFile::getId)
                    .collect(Collectors.toList());
            if (!oldVatCertIds.isEmpty()) {
                scheduleMediaFileCleanup(oldVatCertIds);
            }
            
            // Upload and set new VAT certificates
            List<MultipartFile> vatCertificates = (List<MultipartFile>) request.get("vatCertificate");
            existing.getVatCertificate().clear();
            if (vatCertificates != null && !vatCertificates.isEmpty()) {
                List<MediaFile> newVatCertificates = mediaFileService.upload(vatCertificates);
                existing.getVatCertificate().addAll(newVatCertificates);
            }
        }
        
        // Handle trade license update
        if (request.containsKey("tradeLicenseAttachment")) {
            // Schedule old trade licenses for cleanup
            List<Long> oldTradeLicenseIds = existing.getTradeLicenseAttachment().stream()
                    .map(MediaFile::getId)
                    .collect(Collectors.toList());
            if (!oldTradeLicenseIds.isEmpty()) {
                scheduleMediaFileCleanup(oldTradeLicenseIds);
            }
            
            // Upload and set new trade licenses
            List<MultipartFile> tradeLicenses = (List<MultipartFile>) request.get("tradeLicenseAttachment");
            existing.getTradeLicenseAttachment().clear();
            if (tradeLicenses != null && !tradeLicenses.isEmpty()) {
                List<MediaFile> newTradeLicenses = mediaFileService.upload(tradeLicenses);
                existing.getTradeLicenseAttachment().addAll(newTradeLicenses);
            }
        }
        
        // Handle company images update
        if (request.containsKey("companyImages")) {
            // Schedule old company images for cleanup
            List<Long> oldCompanyImageIds = existing.getCompanyImages().stream()
                    .map(MediaFile::getId)
                    .collect(Collectors.toList());
            if (!oldCompanyImageIds.isEmpty()) {
                scheduleMediaFileCleanup(oldCompanyImageIds);
            }
            
            // Upload and set new company images
            List<MultipartFile> companyImages = (List<MultipartFile>) request.get("companyImages");
            existing.getCompanyImages().clear();
            if (companyImages != null && !companyImages.isEmpty()) {
                List<MediaFile> newCompanyImages = mediaFileService.upload(companyImages);
                existing.getCompanyImages().addAll(newCompanyImages);
            }
        }
        
        return otherServiceRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteOtherService(Integer id) {
        OtherService otherService = otherServiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Other service not found with id: " + id));
        
        // Mark as inactive instead of deleting
        otherService.setActive(false);
        otherServiceRepository.save(otherService);
        
        // Schedule media files for cleanup
        List<Long> mediaFileIds = new ArrayList<>();
        
        if (otherService.getCompanyLogo() != null) {
            mediaFileIds.add(otherService.getCompanyLogo().getId());
        }
        
        mediaFileIds.addAll(otherService.getVatCertificate().stream()
                .map(MediaFile::getId)
                .collect(Collectors.toList()));
                
        mediaFileIds.addAll(otherService.getTradeLicenseAttachment().stream()
                .map(MediaFile::getId)
                .collect(Collectors.toList()));
                
        mediaFileIds.addAll(otherService.getCompanyImages().stream()
                .map(MediaFile::getId)
                .collect(Collectors.toList()));
        
        if (!mediaFileIds.isEmpty()) {
            scheduleMediaFileCleanup(mediaFileIds);
        }
    }

    @Override
    public List<OtherService> findOtherServicesByBrandName(String name) {
        return otherServiceRepository.findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue(name);
    }

    @Override
    public List<OtherService> findOtherServicesByServiceType(String serviceType) {
        return otherServiceRepository.findByServiceTypeAndIsActiveTrue(serviceType);
    }

    @Override
    public List<OtherService> findOtherServicesByStatus(Status status) {
        return otherServiceRepository.findByAccountStatusAndIsActiveTrue(status);
    }

//    @Override
//    public List<OtherService> findOtherServicesByChainId(Integer chainId) {
//        return otherServiceRepository.findByChainIdAndIsActiveTrue(chainId);
//    }

    @Override
    public Optional<OtherService> findOtherServiceByEmail(String email) {
        return otherServiceRepository.findByEmailAndIsActiveTrue(email);
    }

    @Override
    public Optional<OtherService> findOtherServiceByContact(String contact) {
        return otherServiceRepository.findByPhoneNumberOrMobileNumberAndIsActiveTrue(contact)
                .stream().findFirst();
    }
    
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        for (Long mediaFileId : mediaFileIds) {
            try {
                if (!isMediaFileStillReferenced(mediaFileId)) {
                    mediaFileService.delete(mediaFileId);
                }
            } catch (Exception e) {
                log.error("Error cleaning up media file with id: " + mediaFileId, e);
            }
        }
    }
    
    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        // Check if the media file is still referenced by any other entity
        return otherServiceRepository.countByCompanyLogoId(mediaFileId) > 0 ||
               otherServiceRepository.countByVatCertificateId(mediaFileId) > 0 ||
               otherServiceRepository.countByTradeLicenseAttachmentId(mediaFileId) > 0 ||
               otherServiceRepository.countByCompanyImagesId(mediaFileId) > 0;
    }
}
