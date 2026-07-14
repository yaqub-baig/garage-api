package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.GarageBusinessListing;
import com.autoadda.apis.garage.entity.GarageBusinessSale;
import com.autoadda.apis.garage.entity.GarageBusinessSaleInvoice;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.response.GarageBusinessListingResponse;
import com.autoadda.apis.garage.response.GarageBusinessSaleInvoiceResponse;
import com.autoadda.apis.garage.response.GarageBusinessSaleResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GarageBusinessListingMapper {

    public GarageBusinessListingResponse entityToResponse(GarageBusinessListing listing) {
        if (listing == null) {
            return null;
        }

        List<GarageBusinessListingResponse.ListingImage> images = listing.getListingImages() == null
                ? List.of()
                : listing.getListingImages().stream()
                        .map(this::mediaFileToListingImage)
                        .toList();

        return GarageBusinessListingResponse.builder()
                .id(listing.getId())
                .garageId(listing.getGarage() != null ? listing.getGarage().getId() : null)
                .garageName(listing.getGarage() != null ? listing.getGarage().getName() : null)
                .title(listing.getTitle())
                .description(listing.getDescription())
                .reasonForSelling(listing.getReasonForSelling())
                .askingPrice(listing.getAskingPrice())
                .isNegotiable(listing.getIsNegotiable())
                .status(listing.getStatus())
                .listedDate(listing.getListedDate())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .listingImages(images)
                .saleId(listing.getSale() != null ? listing.getSale().getId() : null)
                .build();
    }

    private GarageBusinessListingResponse.ListingImage mediaFileToListingImage(MediaFile mediaFile) {
        return GarageBusinessListingResponse.ListingImage.builder()
                .id(mediaFile.getId())
                .url(mediaFile.getFilePath())
                .fileName(mediaFile.getFileName())
                .build();
    }

    public GarageBusinessSaleResponse saleToResponse(GarageBusinessSale sale) {
        if (sale == null) {
            return null;
        }

        return GarageBusinessSaleResponse.builder()
                .id(sale.getId())
                .listingId(sale.getListing() != null ? sale.getListing().getId() : null)
                .buyerId(sale.getBuyerId())
                .salePrice(sale.getSalePrice())
                .paymentMode(sale.getPaymentMode())
                .saleDate(sale.getSaleDate())
                .notes(sale.getNotes())
                .build();
    }

    public GarageBusinessSaleInvoiceResponse invoiceToResponse(GarageBusinessSaleInvoice invoice) {
        if (invoice == null) {
            return null;
        }

        return GarageBusinessSaleInvoiceResponse.builder()
                .id(invoice.getId())
                .saleId(invoice.getSale() != null ? invoice.getSale().getId() : null)
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .totalAmount(invoice.getTotalAmount())
                .paymentMethod(invoice.getPaymentMethod())
                .build();
    }
}
