# garage-api
    
A comprehensive **Garage Management System** API built with Spring Boot. This application provides RESTful endpoints for managing garages, technicians, appointments, and job cards in an automotive service environment.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Key Entities](#key-entities)
- [API Endpoints](#api-endpoints)
- [Entity Relationships](#entity-relationships)
- [Database Schema](#database-schema)
- [Prerequisites](#prerequisites)
- [Running the Application](#running-the-application)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Migrations](#migrations)
- [Deployment](#deployment)

## Overview

The Garage Management System is a robust platform designed to streamline automotive garage operations. It enables:

- **Garage Management** - Create, update, verify, and search garages
- **Technician Onboarding** - Maintain technician profiles with skills and expertise
- **Appointment Scheduling** - Book and manage vehicle service appointments
- **Job Card Management** - Assign technicians to services and track job progress

## Architecture

The application follows a **Clean Architecture** pattern with distinct layers:

```
Controller Layer (REST API)
       ↓
Service Layer (Business Logic)
       ↓
Repository Layer (Data Access)
       ↓
Entity Layer (Database Models)
```

### Project Structure

```
src/main/java/com/autoadda/apis/garage/
├── controller/          # REST API endpoints
│   ├── GarageController.java
│   ├── JobCardController.java
│   ├── AppointmentController.java
│   ├── TechnicianController.java
│   └── ...
├── service/             # Business logic
│   ├── GarageManagementService.java
│   ├── JobCardService.java
│   ├── AppointmentService.java
│   ├── TechnicianService.java
│   └── ...
├── entity/              # JPA entities
│   ├── Garage.java
│   ├── Technician.java
│   ├── JobCard.java
│   ├── AppointmentDetails.java
│   └── ...
├── repository/          # JPA repositories
├── mapper/              # DTO mapping
├── request/             # Request DTOs
├── response/            # Response DTOs
├── clients/             # Feign clients (identity-api, vehicle-api, part-api)
├── config/              # CORS, file storage, OpenAPI config
├── exception/           # Custom exceptions & GlobalExceptionHandler
├── scheduler/           # Scheduled jobs (AutoAppointmentScheduler)
└── util/                # Utility enums and helpers
```

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.5.9 | Framework |
| Spring Cloud | 2025.0.0 | OpenFeign clients, Eureka client |
| Spring Data JPA | - | ORM & Database access |
| Spring Web | - | REST API |
| MySQL | 8 (connector-j 8.4.0) | Database |
| Lombok | - | Boilerplate reduction |
| MapStruct | 1.5.5.Final | Object mapping |
| springdoc-openapi | 2.8.14 | Swagger UI / OpenAPI docs |
| Spring Boot Actuator | - | Health checks |
| Maven | - | Build |
| Docker | - | Containerization |

## Key Entities

### Garage

The central entity representing an automotive service garage.

**Fields:**
- `id` - Unique identifier
- `garageBrandName` - Brand name of the garage
- `name` - Display name
- `tradeLicenceNumber` - Legal licence (unique)
- `email`, `phoneNumber`, `mobileNumber` - Contact information
- `website` - Website URL (supports up to 2048 chars)
- `garageLocation` - Location entity reference
- `accountStatus` - Verification status (`PENDING`, `APPROVED`, `REJECTED`)
- `tamaraPaymentAvailable` - Payment method availability
- `openingTime`, `closingTime` - Working hours
- `isActive` - Active status flag
- `garageLogo`, `tradeLicenseAttachment`, `vatCertificate` - Media files
- `services` - List of offered services
- `technicians` - List of affiliated technicians

### Technician

Represents a technician working at one or more garages.

**Core Fields:**
- `id` - Unique identifier
- `emiratesId` - Government ID
- `autoAddaId` - System user ID
- `firstName`, `lastName` - Name fields
- `emailId`, `mobileNumber` - Contact information
- `yearOfExperience` - Years of experience
- `city` - Location
- `isAvailableForService` - Current availability status
- `profileImg` - Profile image URL
- `employeeRole` - Role type (`OWNER`, `TECHNICIAN`, `SUPERVISOR`)
- `status` - Operational status (`ACTIVE`, `INACTIVE`, `PENDING_VERIFICATION`, etc.)
- `brand`, `model`, `description` - Technical description
- `isDeleted` - Soft delete flag

**Relationships:**
- `skillSet` - Services the technician can perform
- `expertise` - Expertise categories with proficiency levels
- `garages` - Garages the technician works at

### AppointmentDetails

Represents a service appointment for a vehicle.

**Fields:**
- `id` - Unique identifier
- `appointmentReferenceId` - Reference number
- `scheduleDate` - Date of service
- `serviceStartTime`, `serviceEndTime` - Time slots
- `deliveryTime` - Estimated delivery time
- `status` - Current status (`CONFIRMED`, `CANCELLED`, `COMPLETED`, etc.)
- `comments` - Additional notes
- `customerId` - Customer reference
- `vehicleId` - Vehicle reference
- `garageId` - Garage association
- `slotId` - Time slot reference
- `technicianId` - Assigned technician (optional)

### JobCard

Represents an individual service job assigned to a technician.

**Fields:**
- `id` - Unique identifier
- `serviceStartTime`, `serviceEndTime` - Time tracking
- `status` - Job status (`CREATED`, `IN_PROGRESS`, `COMPLETED`, etc.)
- `createdAt`, `completedAt` - Timestamps
- `comments`, `qualityComments` - Notes
- `assignedAt`, `startedAt`, `qualityCheckedAt` - Workflow timestamps
- `statusTimestamps` - Map tracking status changes over time

**Relationships:**
- `garage` - Associated garage
- `technician` - Assigned technician
- `appointment` - Linked appointment
- `workOrder` - Work order reference

## API Endpoints

### Garage Endpoints (`/garages`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create a new garage |
| GET | `all` | Get all garages (paginated) |
| GET | `/` | Get garages by owner ID |
| GET | `/{id}` | Get garage by ID |
| PUT | `/{id}` | Update garage details |
| PATCH | `/{id}` | Update verification status |
| DELETE | `/{id}` | Delete a garage |
| POST | `/{id}/resubmit` | Resubmit rejected garage |
| GET | `/search` | Search garages (universal) |
| GET | `/search/paginated` | Paginated search |
| GET | `/brandname/{name}` | Search by legal name |
| GET | `/service/{serviceId}` | Get garages by service |

### Technician Endpoints (`/technicians`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Add a new technician |
| GET | `/garage/{garageId}` | Get technicians by garage |
| GET | `/email/{email}` | Get by email |
| GET | `/skill/{skillId}` | Get by skill set |
| GET | `/status/{status}` | Get by status |
| GET | `/available` | Get available technicians |
| DELETE | `/{id}/delete` | Delete technician |
| PUT | `/{id}/update` | Update technician |
| GET | `/search` | Search technicians |
| GET | `/search/paginated` | Paginated search |

### Appointment Endpoints (`/appointments`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create customer appointment |
| POST | `/{garageId}/book` | Book an appointment slot |
| GET | `/garage/{garageId}` | Get garage appointments |
| PUT | `/status` | Update appointment status |
| DELETE | `/{appointmentId}/cancel` | Cancel appointment |
| PUT | `/edit/appointment` | Edit appointment |

### Job Card Endpoints (`/jobcards`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/assign` | Assign technician to a job |
| PUT | `/status/{jobCardId}` | Update job card status |
| GET | `/technician/{technicianId}` | Get technician's job cards |
| GET | `/garage/{garageId}` | Get garage job cards |
| GET | `/{jobCardId}` | Get job card by ID |
| GET | `/{jobCardId}/timeline` | Get status timeline |

## Entity Relationships

### Database Relationship Diagram

```
┌─────────┐         ┌─────────┐
│  Garage │◄────────┤Customer │
└────┬────┘    has  └─────────┘
     │
     │ manages
     │
┌────▼────┐         ┌─────────────┐
│Technician├───────◄┤JobCard      │
└────┬────┘    for  └─────┬───────┘
     │                  │
     │ works at         │ belongs to
     │                  │
┌────▼────┐         ┌───▼──────────┐
│GarageService │   │Appointment  │
└───────────┘     └──────────────┘
     ▲
     │ offers
     │
┌────▼────────────┐
│AvailableService │
└────────────────┘
```

### Junction Tables

| Junction Table | Purpose |
|----------------|---------|
| `technician_garage` | Many-to-many: Which garages a technician works at |
| `technician_skill_set` | Many-to-many: Which services a technician can perform |
| `technician_expertise` | One-to-many: Expertise categories with proficiency levels |
| `garage_services` | Many-to-many: Services offered by each garage |

## Database Schema

### Core Tables

| Table | Description |
|-------|-------------|
| `garage` | Main garage information |
| `technician` | Technician profiles |
| `appointment_details` | Service appointments |
| `job_card` | Individual job tracking |
| `available_service` | Service catalog |
| `garage_service` | Services offered by each garage |
| `expertise_category` | Skill/Expertise categories |
| `technician_expertise` | Technician expertise with levels |
| `location` | Geographic location data |
| `media_file` | Attached media (logos, certificates) |
| `work_order` | Work order records |
| `lot_number` | Vehicle lot tracking |

## Prerequisites

Before running this application, ensure you have:

- **Java 21**
- **Maven 3.8+** (for building)
- **MySQL 8** (if running without Docker)
- **Docker** and **docker-compose** (for containerized deployment)

## Running the Application

The API is served on port **8020** under the context path **`/api/v1`**, e.g. `http://localhost:8020/api/v1/garages`.

### Locally without Docker

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run the JAR
java -jar target/GarageApi-0.1.jar
```

### Database Setup

The datasource URL includes `createDatabaseIfNotExist=true`, so the `auto_adda_garage` database is created automatically on first start as long as the configured MySQL user has permission to do so. Hibernate (`ddl-auto=update`) then creates and updates the tables.

### Using Docker

`docker-compose.yaml` builds the whole AutoAdda stack (MySQL 8, identity-api, garage-api, vehicle-api, part-api). Its build contexts (`./garage-api`, `./identity-api`, ...) are relative to the **parent directory** that contains all service repos, so run it from there:

```bash
cd ..   # directory containing garage-api/, identity-api/, vehicle-api/, ...
docker-compose -f garage-api/docker-compose.yaml up --build
```

MySQL is exposed on host port `3308`, and `mysql-init/init.sql` runs on first start. To build only this service, use the `Dockerfile` (multi-stage, Java 21; exposes 8020 with a health check on `/api/v1/actuator/health`).

## Configuration

Configuration lives in `src/main/resources/application.properties`, with local overrides in `application-local.properties` (the `local` profile is active by default). Key properties:

```properties
server.port=8020
server.servlet.contextPath=/api/v1

spring.datasource.url=jdbc:mysql://localhost:3306/auto_adda_garage?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.jpa.hibernate.ddl-auto=update

# File uploads
file.upload-dir=../uploaded-files
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=50MB

# Eureka (currently disabled)
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### External Services

Feign clients in `clients/` call other AutoAdda services (URLs are currently hardcoded in the `@FeignClient` annotations):

| Client | Service | URL |
|--------|---------|-----|
| `AuthClient` | identity-api | `http://localhost:8010/api/v1` |
| `VehicleClient` | vehicle-api | `http://localhost:8030/api/v1` |
| `PartClient` | part-api | `http://localhost:8060/api/v1` |

Swagger UI is available at `http://localhost:8020/api/v1/swagger-ui.html`.

## API Documentation

### Request Headers

Some endpoints require the `X-Auto-Adda-Id` header for user context:

```http
X-Auto-Adda-Id: 12345
Content-Type: application/json
```

### Common Response Formats

Most endpoints wrap their payload in `GenericResponse`:

**Success Response:**
```json
{
  "status": "success",
  "code": "200",
  "message": "success",
  "body": { ... }
}
```

**Error Response** (from `GlobalExceptionHandler`):
```json
{
  "status": "failed",
  "code": "400",
  "message": "Error description",
  "body": null
}
```

## Testing

Run the test suite:

```bash
mvn test
```

Integration tests are located in:
- `src/test/java/com/autoadda/apis/garage/apis/garage/`
- `src/test/java/com/autoadda/apis/garage/service/`

## Migrations

There is no migration tool (Flyway/Liquibase). Hibernate's `ddl-auto=update` creates new tables and columns automatically on startup. Changes it cannot handle (column type changes, renames, drops, data backfills) are written as SQL scripts in the repository root and **applied manually** to each environment:

```
migration_add_garage_tamara_payment_flag.sql
migration_drop_other_service_module.sql
migration_expertise_category_dynamic.sql
migration_garage_brands_models_longtext.sql
migration_garage_business_listing.sql
migration_increase_website_column_length.sql
migration_technician_brand_model_longtext.sql
migration_working_tools.sql
```

## Deployment

`.github/workflows/deploy.yml` runs on every push to `main` or `production`. It builds the JAR with `mvn clean package -DskipTests` and deploys it to the server over SSH, keeping the last 5 JARs as rollback backups.

## License

This project is proprietary software of AutoAdda.