# Garage - API Documentation

## Table of Contents
- [Appointment Management](#appointment-management)
- [Garage Management](#garage-management)
- [Garage Services](#garage-services)
- [Garage Subscriptions](#garage-subscriptions)
- [Technician Management](#technician-management)
- [Roadside Assistance](#roadside-assistance)
- [Pickup & Drop](#pickup--drop)
- [Invoicing](#invoicing)
- [Loyalty Program](#loyalty-program)
- [Favorites](#favorites)
- [Media Files](#media-files)
- [Communication Preferences](#communication-preferences)
- [Referral System](#referral-system)

## Appointment Management
**Controller:** `AppointmentController`  
**Base Path:** `/appointments`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Create a new customer appointment |
| `/status` | PUT | Update appointment status |
| `/{appointmentId}` | GET | Get appointment details |

## Garage Management
**Controller:** `GarageController`  
**Base Path:** `/garages`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Create a new garage |
| `/{id}` | GET | Get garage by ID |
| `/{id}` | PUT | Update garage details |
| `/{id}` | DELETE | Delete a garage |
| `/brandname/{name}` | GET | Search garages by legal name |
| `/service/{serviceId}` | GET | Get garages offering a specific service |

## Garage Services
**Controller:** `GarageServiceController`  
**Base Path:** `/garage-services`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Create a new garage service |
| `/{id}` | GET | Get service by ID |
| `/{id}` | PUT | Update service details |
| `/{id}` | DELETE | Delete a service |
| `/` | GET | Get all services |
| `/garage/{garageId}` | GET | Get services by garage |

## Garage Subscriptions
**Controller:** `GarageSubscriptionController`  
**Base Path:** `/garage-subscriptions`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Create a new subscription plan |
| `/garage/{garageId}` | GET | Get subscriptions by garage |
| `/{id}` | GET | Get subscription by ID |
| `/{id}` | PUT | Update subscription |
| `/{id}` | DELETE | Delete subscription |
| `/search` | GET | Search subscriptions |
| `/all` | GET | Get all subscriptions |

## Technician Management
**Controller:** `TechnicianController`  
**Base Path:** `/technicians`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Onboard a new technician |
| `/{id}` | GET | Get technician by ID |
| `/{id}/delete` | DELETE | Delete a technician |

## Roadside Assistance
**Controller:** `RoadsideAssistanceController`  
**Base Path:** `/api/roadside-assistance`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Create roadside assistance request |
| `/owner/{ownerId}` | GET | Get requests by owner |
| `/{id}/status` | PUT | Update request status |
| `/{id}` | GET | Get request by ID |

## Pickup & Drop
**Controller:** `PickupDropController`  
**Base Path:** `/api/pickup-drop`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Create pickup/drop request |
| `/{id}/assign-driver/{driverId}` | PUT | Assign driver to request |
| `/{id}/status/{status}` | PUT | Update request status |

## Invoicing
**Controller:** `InvoiceController`  
**Base Path:** `/invoices`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/prepare` | POST | Prepare an invoice |
| `/pay` | PUT | Mark invoice as paid |

## Loyalty Program
**Controller:** `LoyaltyController`  
**Base Path:** `/loyalty`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/earn` | POST | Earn loyalty points |
| `/redeem` | POST | Redeem loyalty points |
| `/status` | GET | Get loyalty status |
| `/history` | GET | Get transaction history |
| `/history/garage/{garageId}/owner/{ownerId}` | GET | Get history by garage and owner |

## Favorites
**Controller:** `GarageFavoriteController` & `VehicleFavoriteController`  
**Base Path:** `/api/favorites`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/garage` | POST | Add garage to favorites |
| `/garage/{ownerId}` | GET | Get favorite garages |
| `/vehicle` | POST | Add vehicle to favorites |
| `/vehicle/{ownerId}` | GET | Get favorite vehicles |

## Media Files
**Controller:** `MediaFileController`  
**Base Path:** `/mediafiles`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/upload` | POST | Upload media files |
| `/delete-image/{id}` | DELETE | Delete a media file |

## Communication Preferences
**Controller:** `CommunicationPreferenceController`  
**Base Path:** `/preferences`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | POST | Set communication preferences |
| `/{customerId}` | GET | Get preferences by customer |
| `/update` | POST | Update preferences |
| `/update/{id}/{customerId}` | GET | Update by ID and customer ID |

## Referral System
**Controller:** `ReferralController`  
**Base Path:** `/api/referrals`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/generate` | POST | Generate a referral code |
| `/redeem` | POST | Redeem a referral code |

## Additional Features

### Technician Availability
**Controller:** `TechnicianAvailabilityController`  
**Base Path:** `/technicians/availability`

### Garage Analytics
**Controller:** `GarageAnalyticsController`  
**Base Path:** `/garage-analytics`

### Additional Requests
**Controller:** `AdditionalRequestController`  
**Base Path:** `/additional-requests`
