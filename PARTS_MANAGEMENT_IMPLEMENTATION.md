# Parts Management Feature Implementation

## Overview
This document describes the complete implementation of the parts management workflow for the AutoAdda Garage API. When a technician updates a job status to `WAITING_FOR_PARTS`, they can provide part details, the customer receives a notification and chooses how to handle the parts, and the system automatically integrates part costs into invoices.

---

## Features Implemented

### 1. **Part Request Creation**
- Technicians can create part requests when updating job status to `WAITING_FOR_PARTS`
- Part details include:
  - Part name
  - Part number/code
  - Quantity
  - Description
  - Estimated price

### 2. **Customer Notification & Choice**
- Customer receives SMS notification with part details
- Customer can choose via API:
  - **Self-Provide**: Customer will bring the parts
  - **Garage Procure**: Garage will buy parts (genuine or used)

### 3. **Garage Procurement**
- When garage buys parts, they record:
  - Supplier name and contact
  - Purchase price
  - Sell price
  - Part condition (genuine/used)
  - Receipt number
  - Procurement notes

### 4. **Automatic Invoice Integration**
- When garage procures parts, they are automatically added to the invoice
- Part costs are added as line items with full details
- Invoice totals are recalculated automatically

### 5. **Part Delivery Tracking**
- For customer-provided parts: Track delivery status
- For garage-procured parts: Track procurement status
- All status transitions are logged

---

## Database Schema

### New Entities Created

#### **PartRequest**
Main entity linking to JobCard
```java
- id (Integer, PK)
- jobCard (JobCard, OneToOne)
- requestedBy (Technician)
- status (PartRequestStatus)
- requestDate (LocalDateTime)
- customerResponseDate (LocalDateTime)
- customerId (Integer)
- vehicleId (Integer)
- items (List<PartRequestItem>)
- customerResponse (CustomerPartResponse)
- notes (String)
```

#### **PartRequestItem**
Individual parts in a request
```java
- id (Integer, PK)
- partRequest (PartRequest, ManyToOne)
- partName (String)
- partNumber (String)
- quantity (Integer)
- description (String)
- estimatedPrice (Double)
- garageProcurement (GarageProcurement, OneToOne)
```

#### **CustomerPartResponse**
Customer's choice
```java
- id (Integer, PK)
- partRequest (PartRequest, OneToOne)
- choice (CustomerChoice: SELF_PROVIDE | GARAGE_PROCURE)
- partCondition (PartCondition: GENUINE | USED)
- responseDate (LocalDateTime)
- expectedDeliveryDate (LocalDate)
- customerComments (String)
```

#### **GarageProcurement**
Garage purchase details
```java
- id (Integer, PK)
- partRequestItem (PartRequestItem, OneToOne)
- supplierName (String)
- supplierContact (String)
- purchasePrice (Double)
- sellPrice (Double)
- partCondition (PartCondition)
- receiptNumber (String)
- procurementDate (LocalDateTime)
- procuredBy (Technician)
- notes (String)
```

### New Enums

#### **PartRequestStatus**
```
PENDING_CUSTOMER_RESPONSE
CUSTOMER_WILL_PROVIDE
GARAGE_WILL_PROCURE
GARAGE_PROCURING
WAITING_FOR_CUSTOMER_DELIVERY
PART_RECEIVED
ADDED_TO_INVOICE
CANCELLED
```

#### **CustomerChoice**
```
SELF_PROVIDE
GARAGE_PROCURE
```

#### **PartCondition**
```
GENUINE
USED
```

---

## API Endpoints

### **PartRequestController** (`/part-requests`)

#### 1. Create Part Request (Alternative to status update)
```http
POST /part-requests
Content-Type: application/json

{
  "jobCardId": 123,
  "technicianId": 456,
  "items": [
    {
      "partName": "Engine Oil Filter",
      "partNumber": "OE-12345",
      "quantity": 1,
      "description": "OEM oil filter for Honda City",
      "estimatedPrice": 500.00
    }
  ],
  "notes": "Urgent - customer waiting"
}
```

#### 2. Update Job Card Status with Part Request (Recommended)
```http
PUT /part-requests/jobcard/status
Content-Type: application/json

{
  "jobcardId": 123,
  "status": "WAITING_FOR_PARTS",
  "memberId": 456,
  "memberType": "TECHNICIAN",
  "comments": "Need parts to continue",
  "partRequestItems": [
    {
      "partName": "Brake Pads",
      "partNumber": "BP-67890",
      "quantity": 4,
      "description": "Front brake pads set",
      "estimatedPrice": 2500.00
    }
  ],
  "partRequestNotes": "High priority"
}
```

#### 3. Get Part Request by ID
```http
GET /part-requests/{id}
```

#### 4. Get Part Request by Job Card ID
```http
GET /part-requests/job/{jobCardId}
```

#### 5. Get Pending Part Requests for Customer
```http
GET /part-requests/customer/{customerId}/pending
```

#### 6. Customer Submits Response
```http
PUT /part-requests/{id}/customer-response
Content-Type: application/json

{
  "choice": "GARAGE_PROCURE",
  "partCondition": "GENUINE",
  "customerComments": "Please use genuine parts only"
}
```

OR for self-provide:
```json
{
  "choice": "SELF_PROVIDE",
  "expectedDeliveryDate": "2025-10-26",
  "customerComments": "I will bring the parts tomorrow"
}
```

#### 7. Add Garage Procurement (Auto-adds to Invoice)
```http
POST /part-requests/{id}/procurement
Content-Type: application/json

{
  "partRequestItemId": 789,
  "supplierName": "AutoParts Depot",
  "supplierContact": "+91-9876543210",
  "purchasePrice": 2000.00,
  "sellPrice": 2800.00,
  "partCondition": "GENUINE",
  "receiptNumber": "APD-2025-001234",
  "procuredByUserId": 456,
  "notes": "OEM part with warranty"
}
```

#### 8. Mark Part Received (Customer Delivered)
```http
PUT /part-requests/{id}/mark-received
```

#### 9. Get Part Requests by Garage
```http
GET /part-requests/garage/{garageId}
```

---

## Workflow Diagrams

### Complete Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. TECHNICIAN: Update Job Status to WAITING_FOR_PARTS          │
│    + Provide part details (name, number, qty, description)      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. SYSTEM: Create PartRequest                                   │
│    + Send SMS notification to customer                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. CUSTOMER: Opens app, views part request                      │
│    + Choose: "I'll provide" OR "Garage should buy"              │
│    + If garage: Select condition (genuine/used)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
    ┌───────────────────┐    ┌───────────────────────┐
    │ Customer Provides │    │ Garage Procures       │
    └─────────┬─────────┘    └──────────┬────────────┘
              │                          │
              ▼                          ▼
    ┌───────────────────┐    ┌───────────────────────┐
    │ Wait for delivery │    │ Garage adds:          │
    │ Confirm received  │    │ - Supplier details    │
    └─────────┬─────────┘    │ - Purchase price      │
              │              │ - Sell price          │
              │              │ - Receipt number      │
              │              └──────────┬────────────┘
              │                         │
              │                         ▼
              │              ┌───────────────────────┐
              │              │ AUTO: Add to Invoice  │
              │              └──────────┬────────────┘
              │                         │
              └────────────┬────────────┘
                           │
                           ▼
                ┌────────────────────┐
                │ Part Received      │
                │ Resume Work        │
                └────────────────────┘
```

---

## Code Structure

### Files Created

#### Entities
- `src/main/java/com/autoadda/apis/garage/entity/PartRequest.java`
- `src/main/java/com/autoadda/apis/garage/entity/PartRequestItem.java`
- `src/main/java/com/autoadda/apis/garage/entity/CustomerPartResponse.java`
- `src/main/java/com/autoadda/apis/garage/entity/GarageProcurement.java`

#### Enums
- `src/main/java/com/autoadda/apis/garage/util/PartRequestStatus.java`
- `src/main/java/com/autoadda/apis/garage/util/CustomerChoice.java`
- `src/main/java/com/autoadda/apis/garage/util/PartCondition.java`

#### DTOs/Requests
- `src/main/java/com/autoadda/apis/garage/request/PartRequestItemDto.java`
- `src/main/java/com/autoadda/apis/garage/request/CreatePartRequestRequest.java`
- `src/main/java/com/autoadda/apis/garage/request/CustomerPartResponseRequest.java`
- `src/main/java/com/autoadda/apis/garage/request/GarageProcurementRequest.java`

#### Responses
- `src/main/java/com/autoadda/apis/garage/response/PartRequestResponse.java`
- `src/main/java/com/autoadda/apis/garage/response/PartRequestItemResponse.java`
- `src/main/java/com/autoadda/apis/garage/response/CustomerPartResponseResponse.java`
- `src/main/java/com/autoadda/apis/garage/response/GarageProcurementResponse.java`

#### Repositories
- `src/main/java/com/autoadda/apis/garage/repository/PartRequestRepository.java`
- `src/main/java/com/autoadda/apis/garage/repository/PartRequestItemRepository.java`
- `src/main/java/com/autoadda/apis/garage/repository/CustomerPartResponseRepository.java`
- `src/main/java/com/autoadda/apis/garage/repository/GarageProcurementRepository.java`

#### Services
- `src/main/java/com/autoadda/apis/garage/service/PartRequestService.java`
- Enhanced: `src/main/java/com/autoadda/apis/garage/service/JobCardService.java`

#### Controllers
- `src/main/java/com/autoadda/apis/garage/controller/PartRequestController.java`

### Files Modified

#### Enhanced for Part Requests
- `src/main/java/com/autoadda/apis/garage/request/JobCardStatusRequest.java` - Added part request fields
- `src/main/java/com/autoadda/apis/garage/service/JobCardService.java` - Added updateJobCardStatusWithPartRequest() method
- `src/main/java/com/autoadda/apis/garage/exception/GlobalExceptionHandler.java` - Enhanced error handling

---

## SMS Notifications

### Part Request Notification
Sent to customer when technician creates part request:
```
🔧 PARTS REQUIRED - AutoAdda

Service: Oil Change
Vehicle: Honda City
Reg No: KA01AB1234

Parts needed:
• Engine Oil Filter (Qty: 1)
• Oil Drain Plug Washer (Qty: 1)
...and 1 more

Please open the AutoAdda app to choose:
1. You'll provide the parts
2. Garage should buy (genuine/used)
```

### Response Confirmation
Sent after customer makes a choice:
```
✅ RESPONSE CONFIRMED - AutoAdda

Service: Oil Change
Your choice: Garage will purchase

The garage will procure the parts.
Part cost will be added to your invoice.
```

---

## Key Features & Design Decisions

### 1. **Automatic Invoice Integration**
- When garage procures parts, they're automatically added as invoice line items
- No manual intervention required
- Invoice totals recalculated automatically
- Transaction-safe with rollback on failures

### 2. **SMS Notifications**
- Customer notified immediately when parts are needed
- Confirmation sent after customer responds
- Uses existing SMS infrastructure via AuthClient

### 3. **Flexible Part Procurement**
- Supports both customer-provided and garage-procured parts
- Tracks complete procurement lifecycle
- Records supplier information for accountability

### 4. **Status Tracking**
- Every status change is logged
- Clear workflow from request to completion
- Easy to track where parts are in the process

### 5. **Error Handling**
- Comprehensive validation at each step
- Clear error messages for invalid operations
- Global exception handling for consistency
- Graceful failures (e.g., if invoice not yet created, parts marked for later addition)

### 6. **Data Integrity**
- Foreign key relationships ensure referential integrity
- Cascade operations for related entities
- Transaction boundaries prevent partial updates

---

## Testing Guide

### Test Scenario 1: Complete Flow (Garage Procures)
1. Technician updates job to WAITING_FOR_PARTS with part details
2. Verify SMS sent to customer
3. Customer selects "Garage should buy" with condition
4. Verify status updated to GARAGE_WILL_PROCURE
5. Garage adds procurement details
6. Verify part added to invoice
7. Verify status updated to ADDED_TO_INVOICE

### Test Scenario 2: Customer Provides Parts
1. Technician updates job to WAITING_FOR_PARTS with part details
2. Customer selects "I'll provide" with expected date
3. Verify status updated to WAITING_FOR_CUSTOMER_DELIVERY
4. Garage marks part as received
5. Verify status updated to PART_RECEIVED

### Test Scenario 3: Error Cases
1. Try updating to WAITING_FOR_PARTS without part details → Should fail
2. Try customer response when already responded → Should fail
3. Try garage procurement when customer said they'll provide → Should fail
4. Try marking received when status is wrong → Should fail

---

## Database Migration

### Run these SQL commands to create the tables:

```sql
-- Create part_request table
CREATE TABLE part_request (
    id INT AUTO_INCREMENT PRIMARY KEY,
    job_card_id INT NOT NULL UNIQUE,
    requested_by_technician_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    request_date TIMESTAMP NOT NULL,
    customer_response_date TIMESTAMP,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    notes VARCHAR(1000),
    FOREIGN KEY (job_card_id) REFERENCES job_card(id),
    FOREIGN KEY (requested_by_technician_id) REFERENCES technician(id)
);

-- Create part_request_item table
CREATE TABLE part_request_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    part_request_id INT NOT NULL,
    part_name VARCHAR(255) NOT NULL,
    part_number VARCHAR(100),
    quantity INT NOT NULL,
    description VARCHAR(1000),
    estimated_price DOUBLE,
    FOREIGN KEY (part_request_id) REFERENCES part_request(id) ON DELETE CASCADE
);

-- Create customer_part_response table
CREATE TABLE customer_part_response (
    id INT AUTO_INCREMENT PRIMARY KEY,
    part_request_id INT NOT NULL UNIQUE,
    choice VARCHAR(50) NOT NULL,
    part_condition VARCHAR(50),
    response_date TIMESTAMP NOT NULL,
    expected_delivery_date DATE,
    customer_comments VARCHAR(1000),
    FOREIGN KEY (part_request_id) REFERENCES part_request(id) ON DELETE CASCADE
);

-- Create garage_procurement table
CREATE TABLE garage_procurement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    part_request_item_id INT NOT NULL UNIQUE,
    supplier_name VARCHAR(255) NOT NULL,
    supplier_contact VARCHAR(50),
    purchase_price DOUBLE NOT NULL,
    sell_price DOUBLE NOT NULL,
    part_condition VARCHAR(50) NOT NULL,
    receipt_number VARCHAR(100),
    procurement_date TIMESTAMP NOT NULL,
    procured_by_user_id INT,
    notes VARCHAR(1000),
    FOREIGN KEY (part_request_item_id) REFERENCES part_request_item(id) ON DELETE CASCADE,
    FOREIGN KEY (procured_by_user_id) REFERENCES technician(id)
);
```

---

## Next Steps / Future Enhancements

1. **Parts Inventory Integration**: Link to a parts inventory system
2. **Multiple Suppliers**: Allow comparing prices from multiple suppliers
3. **Approval Workflow**: Add manager approval for high-value parts
4. **Part Warranty Tracking**: Track warranty information for parts
5. **Analytics**: Report on part usage, supplier performance, etc.
6. **Push Notifications**: Add push notifications in addition to SMS
7. **Part Photos**: Allow attaching photos of parts
8. **Barcode Scanning**: Scan part barcodes for quick entry

---

## Support

For questions or issues with this implementation, please contact the development team or refer to the main project documentation.

**Implementation Date**: October 2025
**Version**: 1.0
**Status**: ✅ Complete & Ready for Testing
