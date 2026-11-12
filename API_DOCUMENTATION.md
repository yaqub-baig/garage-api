# Garage - API Documentation

## Table of Contents
- [Appointment Management](#appointment-management)
- [Garage Management](#garage-management)
- [Garage Services](#garage-services)
- [Garage Subscriptions](#garage-subscriptions)
- [Technician Management](#technician-management)
- [Garage Team Management](#garage-team-management)
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

## Garage Team Management
**Controller:** `GarageTeamController`  
**Base Path:** `/garages/{garageId}/team`

Each garage has team members with custom roles (a set of `GaragePermission`s). Every call acts as the user in the `X-Auto-Adda-Id` header. Garage-side write endpoints across the API now check these permissions and return **403** when the caller lacks them; the garage owner always passes. Frontend guide: [GARAGE_TEAM_FRONTEND_INTEGRATION.md](GARAGE_TEAM_FRONTEND_INTEGRATION.md).

| Endpoint | Method | Permission | Description |
|----------|--------|------------|-------------|
| `/me` | GET | — | Caller's role and effective permissions |
| `/members` | GET | TEAM_VIEW | List team members |
| `/members/{memberId}/role` | PUT | TEAM_MANAGE | Change a member's role |
| `/members/{memberId}/status` | PUT | TEAM_MANAGE | Suspend / reactivate a member |
| `/members/{memberId}` | DELETE | TEAM_MANAGE | Remove a member |
| `/roles` | GET | TEAM_VIEW | List roles |
| `/roles` | POST | ROLE_MANAGE | Create a role |
| `/roles/{roleId}` | PUT | ROLE_MANAGE | Edit a role |
| `/roles/{roleId}` | DELETE | ROLE_MANAGE | Delete an unused role |
| `/invites` | POST | TEAM_MANAGE | Invite by phone with a role |
| `/invites` | GET | TEAM_VIEW | Pending invites |
| `/invites/{inviteId}` | DELETE | TEAM_MANAGE | Cancel an invite |
| `/team/invites/mine` (no garage prefix) | GET | — | Invites waiting for the caller |
| `/team/permissions` (no garage prefix) | GET | — | Permission catalogue |

Config: `garage.permissions.enforcement=ENFORCE` (default) or `LOG_ONLY` to log denials without blocking.

## Work Orders
**Controller:** `WorkOrderController`  
**Base Path:** `/work-orders`

### Parts on a work order
Every work-order response (`POST /`, `GET /workorder-by/{appointmentId}`, `PUT /update/{id}`, `GET /by-garage/{garageId}`, `GET /customer/{customerId}`) includes `partRequests`: one entry per job card that needed parts, empty when none did.

```jsonc
"partRequests": [{
  "id": 1, "jobCardId": 1, "jobCardServiceName": "Brake service",
  "status": "PART_RECEIVED", "requestedByTechnicianName": "Sara", "notes": "Pads worn", "voiceNote": null,
  "customerResponse": { "choice": "GARAGE_PROCURE", "partCondition": "GENUINE", "customerComments": "Genuine please", ... },
  "items": [{
    "id": 1, "partName": "Brake pads", "partNumber": "BP-22", "quantity": 2, "estimatedPrice": 180.0,
    "procurementStatus": "PROCURED", "addedToInvoice": false,
    "procurement": { "supplierName": "Al Futtaim Parts", "purchasePrice": 170.0, "partCondition": "GENUINE",
                     "receiptNumber": "R-881", "procuredByUserName": "Omar", "procurementDate": "…" },
    "procuredAt": "…", "receivedAt": null, "addedToInvoiceAt": null
  }],
  "timeline": [
    { "level": "REQUEST", "event": "PENDING_CUSTOMER_RESPONSE", "timestamp": "…", "description": "Parts requested, waiting for the customer to choose", "durationInMinutes": null },
    { "level": "REQUEST", "event": "GARAGE_WILL_PROCURE", "timestamp": "…", "description": "Customer asked the garage to buy the parts", "durationInMinutes": 1 },
    { "level": "ITEM", "event": "PART_PROCURED", "partRequestItemId": 1, "partName": "Brake pads", "description": "Garage bought Brake pads from Al Futtaim Parts", … },
    { "level": "REQUEST", "event": "PART_RECEIVED", "description": "All parts received", … }
  ]
}]
```

- `timeline` is oldest first. `level: REQUEST` entries are status changes of the whole request (`event` = `PartRequestStatus`). `level: ITEM` entries are per part: `PART_PROCURED`, `PART_RECEIVED_FROM_CUSTOMER`, `PART_ADDED_TO_INVOICE`.
- Part requests created before this release only show the request and the customer's choice; later steps had no recorded time.
- `vehicleDetails` is null here (the work order already has the vehicle). The same `timeline` and item timestamps are also returned by `/part-requests/**`.

### Service history
`GET /vehicle/{vehicleId}` (completed / delivered work orders) returns `garage` with `garageArabicName`, `trnNumber` and `garageLogo` (same shape as `GarageResponse.garageLogo`; image URL is `{base-url}/uploads/{filePath}`).

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

### Voice notes
Every note / comment / remark field can carry one optional voice recording. Frontend guide with per-screen changes and sample code: [VOICE_NOTES_FRONTEND_INTEGRATION.md](VOICE_NOTES_FRONTEND_INTEGRATION.md).

1. Upload the recording to `POST /mediafiles/upload` (field `files`). Audio (`audio/*`, or `.mp3 .m4a .aac .wav .ogg .opus .amr .flac`) is stored under `uploads/audios/`.
2. Send the returned media file `id` next to the text field. Only audio files are accepted (non-audio id → 400).
3. Responses return the recording as a media file object (`id`, `mediaType`, `fileUrl`, …).

On update endpoints, omitting the id keeps the existing recording.

| Text field | Request field | Response field | Where |
|------------|---------------|----------------|-------|
| `notes` | `voiceNoteId` | `voiceNote` | part request create/update, garage procurement, working tools add/update, inventory item add/update, stock add/remove/adjust, invoice generate, garage sale finalize, pickup/drop create |
| `notes` (query) | `voiceNoteId` (query) | `voiceNote` | `POST /invoices/work-order/{id}/auto-generate` |
| `comments` | `voiceNoteId` | `voiceNote` (on invoice) | invoice pay |
| `notes` | `voiceNoteId` | `resolutionVoiceNote` | inventory alert resolve/dismiss |
| `partRequestNotes` | `partRequestVoiceNoteId` | part request `voiceNote` | job card status → `WAITING_FOR_PARTS` |
| `comments` | `voiceNoteId` | `voiceNote` | appointment status update, job card assign (also via work order update), job card additional request |
| `comment` | `voiceNoteId` | `voiceNote` | vehicle received create/update |
| `comments` / `customerComments` | `voiceNoteId` / `customerVoiceNoteId` | `customerVoiceNote` | delivery checklist / customer delivery approval |
| `customerComments` | `customerVoiceNoteId` | `customerVoiceNote` | customer part response |
| `customerNegotiatedComment` / `garageNegotiatedComment` | `customerNegotiatedVoiceNoteId` / `garageNegotiatedVoiceNoteId` | `customerNegotiatedVoiceNote` / `garageNegotiatedVoiceNote` | work order update |
| `remarks` (query) | `voiceNoteId` (query) | `voiceNote` | roadside assistance status, insurance claim status (`approvalVoiceNote`) |

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
