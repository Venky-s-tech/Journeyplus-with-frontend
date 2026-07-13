# JourneyPlus - Enterprise Code Flow Documentation

This document provides a comprehensive, technical analysis of the **JourneyPlus** Corporate Travel and Expense Management Backend. It outlines the architectural design, request lifecycles, and exact code-level execution flows for every module.

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Project Folder Structure](#2-project-folder-structure)
3. [High-Level Request Flow](#3-high-level-request-flow)
4. [Authentication Flow](#4-authentication-flow)
5. [Registration Flow](#5-registration-flow)
6. [Refresh Token Flow](#6-refresh-token-flow)
7. [Module-wise Code Flow](#7-module-wise-code-flow)
8. [Every API Flow](#8-every-api-flow)
9. [Controller Explanation](#9-controller-explanation)
10. [Service Explanation](#10-service-explanation)
11. [Repository Explanation](#11-repository-explanation)
12. [Entity Explanation](#12-entity-explanation)
13. [DTO Flow](#13-dto-flow)
14. [Security Flow](#14-security-flow)
15. [Database Flow](#15-database-flow)
16. [Exception Flow](#16-exception-flow)
17. [Audit Flow](#17-audit-flow)
18. [Notification Flow](#18-notification-flow)
19. [Complete End-to-End Flow](#19-complete-end-to-end-flow)
20. [Internal Spring Boot Concepts Used](#20-internal-spring-boot-concepts-used)
21. [Sequence Diagrams](#21-sequence-diagrams)
22. [Class Relationships](#22-class-relationships)
23. [Conclusion](#23-conclusion)

---

## 1. Project Overview

* **Project Name**: JourneyPlus (Corporate Travel and Expense Management Backend)
* **Purpose**: A unified backend system that automates corporate travel requests, enforces role-based budget limits, audits expense claims with an automated policy compliance engine, processes cash advances, and handles disbursements and reimbursements.
* **Tech Stack**:
  * **Language**: Java 21 (LTS)
  * **Framework**: Spring Boot 3.2.5 (Spring MVC, Spring Security, Spring Data JPA, Spring AOP)
  * **Database**: MySQL 8.0+
  * **ORM**: Hibernate 6.4.4.Final
  * **Authentication**: Stateless JWT using Asymmetric RSA (2048-bit) Key Pairs
  * **Cryptography**: AES-256 (ECB Mode with PKCS5Padding) for transparent database column encryption
  * **Build Tool**: Maven 3.9+
* **Overall Backend Structure**: A layered (n-tier) architecture consisting of a Controller layer (REST endpoints), Service layer (business logic and transactions), Compliance/Audit engine, Repository layer (database abstractions), and an AOP aspect layer (asynchronous/synchronous auditing).

---

## 2. Project Folder Structure

The project code is located under `src/main/java/com/journeyplus/` and is divided into the following packages:

* **`advance`**: Manages cash advance requests, disbursements, and settlements.
  * *Responsibilities*: Handles business rules for requesting advances against approved trips, calculating outstanding settlements, and tracking payment states.
* **`analytics`**: Compiles and exports aggregate travel and financial reports.
  * *Responsibilities*: Gathers transactional data across trips, advances, and expenses to produce analytical reports.
* **`audit`**: Handles historical system operation tracking.
  * *Responsibilities*: Exposes endpoints for administrators to query user action logs recorded by AOP interceptors.
* **`common`**: Cross-cutting utilities and exception handling.
  * *Responsibilities*: Includes `CryptoUtils` (AES), `EncryptedBigDecimalConverter` (JPA converter), and `GlobalExceptionHandler` (centralized controller advice).
* **`compliance`**: The corporate travel policy auditing engine.
  * *Responsibilities*: Evaluates expense lines against maximum trip budgets and city daily allowance limits, flagging policy exceptions.
* **`config`**: Core application configurations.
  * *Responsibilities*: Configures Spring Security filters, RSA key loaders, OpenAPI (Swagger) setups, data seeders, and AOP aspects (`AuditAspect`).
* **`document`**: Handles file uploads and storage (e.g., receipts, visas).
  * *Responsibilities*: Manages local file system storage and registers file metadata.
* **`event`**: Event-driven application notification utilities.
  * *Responsibilities*: Defilnes custom `StatusChangeEvent` and listens for events to generate user notification records.
* **`expense`**: Manages expense claims and reimbursement payouts.
  * *Responsibilities*: Converts foreign currencies, aggregates expense lines, deducts cash advances, and records payouts.
* **`iam`**: Identity and Access Management.
  * *Responsibilities*: Handles user registration, BCrypt password hashing, role management, admin approvals, and authentication.
* **`notification`**: In-app user notifications.
  * *Responsibilities*: Manages alerts, unread statuses, and user notifications.
* **`policy`**: Corporate travel policy limits.
  * *Responsibilities*: Configures maximum limits per role and city tiers.
* **`trip`**: Trip requests, itinerary legs, and visa requirements.
  * *Responsibilities*: Manages the core travel request lifecycle, multi-leg booking states, and visa approvals.

---

## 3. High-Level Request Flow

Every incoming REST request passes through a defined path, starting at the network socket and ending at the database, before returning a JSON response.

### Execution Path
```text
Client Request (JSON + JWT)
  ↓
Filter Chain (JwtAuthenticationFilter - verifies token signature using RSA Public Key)
  ↓
Controller (Resolves endpoint, maps request JSON to DTO)
  ↓
Validation (@Valid annotations check DTO constraints; throws MethodArgumentNotValidException if invalid)
  ↓
Service (Handles business transactions, enforces domain rules, manages @Transactional boundaries)
  ↓
Repository (JPA interface triggers Hibernate session)
  ↓
Hibernate (Converts Java entities to SQL, invokes AttributeConverters for column encryption)
  ↓
Database (Executes SQL query on MySQL; returns ResultSet)
  ↓
Repository (Maps ResultSet back to Java Entity objects)
  ↓
Service (Applies post-query business logic, publishes events)
  ↓
Controller (Maps Entity to Response DTO)
  ↓
JSON Response (HTTP Response with serialized JSON payload)
```

### Spring Core Concepts in Action
* **Spring IOC & Dependency Injection**: Spring automatically instantiates and manages classes annotated with `@Component`, `@Service`, `@Repository`, or `@RestController` as **Beans**. Dependencies are injected via constructor injection or `@Autowired` fields.
* **Bean Creation**: During startup, Spring performs a component scan under `com.journeyplus`, instantiating beans, resolving dependencies, and registering them in the `ApplicationContext`.
* **Transaction Management**: The `@Transactional` annotation on service methods configures Spring's `PlatformTransactionManager` to open a database transaction. If the method completes successfully, the transaction is committed; if a `RuntimeException` is thrown, the transaction is rolled back.

---

## 4. Authentication Flow

The authentication flow validates user credentials, issues asymmetric JWT access tokens, and verifies user identity on subsequent calls.

```text
Login Request (JSON) ──> AuthController ──> AuthenticationManager ──> UserDetailsService
                                                                            │
                                                                    Loads User Entity
                                                                            │
   Response (JWT) <── AuthController <── JWT Token (Private Key) <── BCrypt Validation
```

### Classes Involved
1. **[AuthController](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/iam/controller/AuthController.java)**: Exposes the `/api/auth/login` endpoint.
2. **[AuthService](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/iam/service/AuthService.java)**: Coordinates authentication and token generation.
3. **`AuthenticationManager`**: Spring Security interface that delegates authentication to the configured `DaoAuthenticationProvider`.
4. **`BCryptPasswordEncoder`**: Hashes and verifies passwords.
5. **[JwtTokenProvider](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/config/JwtTokenProvider.java)**: Generates and parses JWTs using RSA key pairs.

### Detailed Step-by-Step Flow
1. **Client Sends Request**: `POST /api/auth/login` with `AuthRequest` containing `username` and `password`.
2. **Controller Receives**: `AuthController` calls `authService.login(authRequest)`.
3. **Spring Authentication**: `AuthService` instantiates a `UsernamePasswordAuthenticationToken` and calls `authenticationManager.authenticate(token)`.
4. **User Verification**:
   * The manager invokes `UserDetailsService` (implemented by `UserService`), which calls `UserRepository.findByUsername()`.
   * The provider compares the raw request password against the database `password_hash` using `BCryptPasswordEncoder.matches()`.
   * If valid, it returns an authenticated `Authentication` object.
5. **Token Generation**:
   * `AuthService` calls `jwtTokenProvider.generateAccessToken(userDetails)`.
   * `JwtTokenProvider` reads the **RSA Private Key** (`keys/private.pem`), signs the JWT payload containing claims (subject, username, roles, expiration), and returns the token.
6. **Response**: `AuthController` returns an `AuthResponse` containing the JWT token, username, and role with HTTP `200 OK`.

---

## 5. Registration Flow

Handles the creation and validation of new user accounts.

```text
Client ──> AuthController ──> Validations ──> AuthService ──> BCrypt ──> UserRepository ──> DB
```

### Detailed Step-by-Step Flow
1. **Client Request**: `POST /api/auth/register` with `RegisterRequest` DTO.
2. **DTO Validation**: Spring MVC intercepts the request and validates constraints:
   * `@NotBlank` on `username`, `email`, `password`, `department`.
   * `@Email` on `email`.
   * `@Size(min = 6)` on `password`.
3. **Service Processing**: `AuthController` calls `authService.register(registerRequest)`.
4. **Uniqueness Checks**:
   * Calls `UserRepository.existsByUsername(username)`. If true, throws `IllegalArgumentException` ("Username already exists").
   * Calls `UserRepository.existsByEmail(email)`. If true, throws `IllegalArgumentException` ("Email already exists").
5. **Role-Based Activation Rule**:
   * Encodes the password using `BCryptPasswordEncoder.encode()`.
   * Instantiates a new `User` entity.
   * If the requested role is `ROLE_EMPLOYEE`, sets `active` to `true` (auto-approved).
   * If the role is `ROLE_TRAVEL_ADMIN` or `ROLE_APPROVING_MANAGER`, sets `active` to `false` (requires manual admin activation).
6. **Persistence**: Calls `UserRepository.save(user)`. Hibernate generates an `INSERT` statement and executes it on the `users` table.
7. **Response**: Returns the saved `User` entity (with password omitted) with HTTP `210 Created`.

---

## 6. Refresh Token Flow

Enables clients to request a new access token without re-entering credentials.

### Detailed Step-by-Step Flow
1. **Client Request**: `POST /api/auth/refresh` with the current JWT token in the payload or header.
2. **Extraction**: `AuthController` calls `jwtTokenProvider.extractUsername(token)` and validates that the token is expired but has a valid signature.
3. **Validation**:
   * Verifies the user exists in the database via `UserRepository.findByUsername()`.
   * Verifies the user's account is `active`.
4. **Token Generation**:
   * Calls `jwtTokenProvider.generateAccessToken(userDetails)` to sign a new token using the RSA Private Key.
5. **Response**: Returns the new JWT access token in an `AuthResponse` payload.

---

## 7. Module-wise Code Flow

Each module contains a dedicated controller, service, and repository structure.

### A. User Management Module
* **APIs**:
  * `GET /api/users/me`: Retrieves the current user's profile.
  * `GET /api/admin/pending`: List pending registrations.
  * `POST /api/admin/approve/{id}`: Activate an administrator/manager account.
* **Flow**:
  ```text
  UserController/AdminController ──> UserService ──> UserRepository ──> MySQL
  ```

### B. Travel Request Module
* **APIs**:
  * `POST /api/trips`: Create a new trip request.
  * `POST /api/trips/{id}/submit`: Lock trip details and submit to manager.
  * `POST /api/trips/{id}/legs`: Add an itinerary leg.
  * `POST /api/trips/{id}/visas`: Add a visa requirement.
* **Flow**:
  ```text
  TripController ──> TripService ──> TripRequestRepository ──> MySQL
                                 ──> ItineraryLegRepository
                                 ──> VisaRequirementRepository
  ```

### C. Policy Management Module
* **APIs**:
  * `POST /api/policies`: Define role-based budget limits.
  * `POST /api/policies/city-tiers`: Define city daily allowances.
  * `GET /api/policies`: View active policies.
* **Flow**:
  ```text
  PolicyController ──> PolicyService ──> TravelPolicyRepository ──> MySQL
                                     ──> CityTierRepository
  ```

### D. Expense Management Module
* **APIs**:
  * `POST /api/expenses`: Create an expense claim draft.
  * `POST /api/expenses/{id}/lines`: Append a receipt and run compliance audits.
  * `POST /api/expenses/{id}/submit`: Submit the claim for manager review.
  * `POST /api/expenses/{id}/reimburse`: Process reimbursement payment.
* **Flow**:
  ```text
  ExpenseController ──> ExpenseService ──> ExpenseClaimRepository ──> MySQL
                                       ──> ExpenseLineRepository
                                       ──> PolicyComplianceEngine ──> PolicyExceptionRepository
                                       ──> ReimbursementRepository
  ```

### E. Approval Workflow Module
* **APIs**:
  * `POST /api/trips/{id}/approve` / `/reject`: Approve or reject a trip request.
  * `POST /api/advances/{id}/approve`: Approve a cash advance request.
  * `POST /api/expenses/{id}/approve` / `/reject`: Approve or reject an expense claim.
* **Flow**:
  * Handled inside `TripService`, `AdvanceService`, and `ExpenseService` respectively.
  * Validates that the active user's ID matches the `approver_id` on the resource.
  * Updates the status and publishes a `StatusChangeEvent` to trigger notifications.

### F. Notification Module
* **APIs**:
  * `GET /api/notifications/unread`: Retrieve unread notifications.
  * `POST /api/notifications/{id}/read`: Mark a notification as read.
* **Flow**:
  * Handled by [StatusChangeEventListener](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/event/StatusChangeEventListener.java). Listens for `StatusChangeEvent` asynchronously, creates a `Notification` entity, and saves it via `NotificationRepository`.

### G. Audit Module
* **APIs**:
  * `GET /api/audit`: Retrieve historical operations.
* **Flow**:
  * Handled by [AuditAspect](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/config/AuditAspect.java). Intercepts methods marked with `@AuditAction`, extracts the security principal, and persists an `AuditLog` entity.

---

## 8. Every API Flow

Below are the detailed execution flows for the core REST API endpoints.

### 1. User Login
* **API**: `POST /api/auth/login`
* **Purpose**: Authenticate user credentials and issue a JWT.
* **Request DTO**: `AuthRequest` (`username`, `password`).
* **Validation**: `@NotBlank` on both fields.
* **Controller**: `AuthController.loginUser()`
* **Service**: `AuthService.login()`
* **Repository**: `UserRepository.findByUsername()`
* **Database Tables**: `users`
* **Business Logic**: Compares passwords using BCrypt, generates an RSA-signed JWT.
* **Response DTO**: `AuthResponse` (`jwt`, `username`, `role`).
* **Authorization**: Public (No auth required).
* **Exceptions**: `BadCredentialsException` (HTTP 401).

### 2. Submit Trip Request
* **API**: `POST /api/trips/{id}/submit`
* **Purpose**: Submit a draft trip request to the manager.
* **Controller**: `TripController.submitTrip()`
* **Service**: `TripService.submitTripRequest()`
* **Repository**: `TripRequestRepository.findById()`, `save()`
* **Database Tables**: `trip_requests`
* **Business Logic**:
  * Verifies the trip request status is `DRAFT`.
  * Sets status to `SUBMITTED`.
  * Publishes a `StatusChangeEvent` to notify the manager.
* **Response**: `TripRequest` (HTTP 200).
* **Authorization**: Authenticated (`ROLE_EMPLOYEE`).
* **Audit Logging**: `TRIP` - `SUBMIT_TRIP`.

### 3. Add Expense Line
* **API**: `POST /api/expenses/{claimId}/lines`
* **Purpose**: Add an expense item to a claim and run compliance checks.
* **Request DTO**: `ExpenseLine`
* **Controller**: `ExpenseController.addLineItem()`
* **Service**: `ExpenseService.addExpenseLine()`
* **Repository**: `ExpenseClaimRepository.findById()`, `ExpenseLineRepository.save()`
* **Database Tables**: `expense_claims`, `expense_lines`, `compliance_audits`, `policy_exceptions`
* **Business Logic**:
  * Verifies the claim is in `DRAFT` status.
  * Converts the amount to USD using `getExchangeRateToUsd()`.
  * Saves the `ExpenseLine`.
  * Calls `PolicyComplianceEngine.runComplianceCheck()`:
    * Compares the USD amount against `TravelPolicy.maxAmountPerTrip` and `CityTier.dailyAllowanceLimit`.
    * Checks if `receiptPath` is provided.
    * If a breach occurs, sets status to `NON_COMPLIANT` and saves `ComplianceAudit` and `PolicyException` records.
  * Updates the claim total and net reimbursable amounts.
* **Response**: `ExpenseLine` (HTTP 200).
* **Authorization**: Authenticated (`ROLE_EMPLOYEE`).
* **Audit Logging**: `EXPENSE` - `ADD_EXPENSE_LINE`.

---

## 9. Controller Explanation

Controllers expose REST endpoints, handle request mapping, validate inputs, and return HTTP responses.

### 1. `AuthController`
* **Dependencies**: `AuthService`
* **Endpoints**:
  * `POST /api/auth/register`: Maps to `RegisterRequest`, returns `User` (HTTP 201).
  * `POST /api/auth/login`: Maps to `AuthRequest`, returns `AuthResponse` (HTTP 200).

### 2. `TripController`
* **Dependencies**: `TripService`, `UserRepository`
* **Endpoints**:
  * `POST /api/trips`: Maps `TripRequest` draft, returns `TripRequest` (HTTP 201).
  * `POST /api/trips/{id}/submit`: Submits a trip (HTTP 200).
  * `POST /api/trips/{id}/approve`: Approves a trip (HTTP 200).

### 3. `ExpenseController`
* **Dependencies**: `ExpenseService`
* **Endpoints**:
  * `POST /api/expenses`: Creates an expense claim (HTTP 201).
  * `POST /api/expenses/{claimId}/lines`: Appends a line item (HTTP 200).
  * `POST /api/expenses/{claimId}/reimburse`: Processes payment (HTTP 200).

---

## 10. Service Explanation

Services implement the core business logic, orchestrate database transactions, and enforce security policies.

### 1. `TripService`
* **`createTripRequest(TripRequest, List<ItineraryLeg>, List<VisaRequirement>)`**:
  * *Purpose*: Creates a new trip request draft and associates itinerary and visa details.
  * *Transactions*: `@Transactional` (Required).
  * *Exceptions*: `IllegalArgumentException` if business validations fail.
* **`submitTripRequest(Long tripId)`**:
  * *Purpose*: Submits a draft trip.
  * *Business Logic*: Verifies the current status is `DRAFT`, sets status to `SUBMITTED`, and publishes status events.

### 2. `ExpenseService`
* **`addExpenseLine(Long claimId, ExpenseLine line)`**:
  * *Purpose*: Adds a line item to a claim and triggers compliance checks.
  * *Business Logic*: Verifies the claim is in `DRAFT` status, calculates the USD equivalent, saves the line, runs the compliance engine, and updates the claim totals.
* **`payReimbursement(Long claimId, Reimbursement reimbursement)`**:
  * *Purpose*: Processes payment for an approved claim.
  * *Business Logic*: Verifies the claim is `APPROVED`, sets status to `PAID`, saves the claim, and creates a `Reimbursement` record.

---

## 11. Repository Explanation

Repositories extend `JpaRepository` to abstract database interactions and define custom queries.

### 1. `UserRepository`
* `Optional<User> findByUsername(String username)`: Used during login and filter chains.
* `boolean existsByUsername(String username)` & `existsByEmail(String email)`: Used during registration.

### 2. `TravelPolicyRepository`
* `Optional<TravelPolicy> findByEmployeeRole(Role role)`: Used by the compliance engine to retrieve role-based budget limits.

### 3. `CityTierRepository`
* `Optional<CityTier> findByCityNameIgnoreCase(String name)`: Used by the compliance engine to retrieve city-specific daily allowances.

---

## 12. Entity Explanation

Entities map Java classes to database tables. Financial columns are transparently encrypted.

### 1. `User` (Table: `users`)
* **Fields**: `id` (PK), `username` (Unique), `email` (Unique), `password_hash`, `role` (Enum), `department`, `active`.
* **Relationships**: `@OneToMany` with `TripRequest`, `@OneToMany` with `ExpenseClaim`.

### 2. `TripRequest` (Table: `trip_requests`)
* **Fields**: `id` (PK), `purpose`, `destination`, `start_date`, `end_date`, `status` (Enum), `estimated_cost` (Encrypted), `comments`.
* **Relationships**: 
  * `@ManyToOne` with `User` (`employee_id`).
  * `@ManyToOne` with `User` (`approver_id`).
  * `@OneToMany` with `ItineraryLeg` (Cascade: `ALL`, Fetch: `LAZY`).
  * `@OneToMany` with `VisaRequirement` (Cascade: `ALL`, Fetch: `LAZY`).

### 3. `ExpenseLine` (Table: `expense_lines`)
* **Fields**: `id` (PK), `expense_date`, `category`, `amount` (Encrypted), `original_currency`, `usd_equivalent` (Encrypted), `receipt_path`, `policy_compliance_status`, `compliance_remarks`.
* **Relationships**:
  * `@ManyToOne` with `ExpenseClaim` (`expense_claim_id`).

---

## 13. DTO Flow

DTOs decouple the API layer from the database model, ensuring input validation is performed before processing.

```text
Request DTO ──> Controller (@Valid Validation) ──> Service (Maps to Entity) ──> Repository (Saves to DB)
                                                                                  │
Client <── Response DTO <── Controller <── Service (Maps Entity to DTO) <────────┘
```

### Key DTOs
* **`RegisterRequest`**: Captures registration details. Enforces validation rules like email formatting and minimum password length.
* **`AuthRequest`**: Captures login credentials.
* **`ExpenseLineRequest`**: Captures expense line details, separating input validation from the persisted `ExpenseLine` entity.

---

## 14. Security Flow

Spring Security provides stateless authentication and role-based authorization.

```text
Request ──> JwtAuthenticationFilter ──> SecurityContextHolder ──> Controller
                 │
        RSA Public Key Validation
```

### Configuration
* **Stateless Session**: Configured to use `SessionCreationPolicy.STATELESS`.
* **Public Endpoints**: `/api/auth/**` and Swagger UI paths are configured with `.permitAll()`.
* **Protected Endpoints**: Mapped using role-based rules:
  * `/api/admin/**` requires `hasRole('TRAVEL_ADMIN')`.
  * `/api/compliance/**` requires `hasAnyRole('COMPLIANCE_OFFICER', 'TRAVEL_ADMIN')`.
  * Expense payments require `hasRole('FINANCE_EXECUTIVE')`.

---

## 15. Database Flow

The database flow maps entities to MySQL tables, using JPA and Hibernate to manage persistence and encryption.

```text
Repository.save(Entity) 
  ↓
JPA / Hibernate Session
  ↓
AttributeConverter.convertToDatabaseColumn() ──> CryptoUtils (AES-256)
  ↓
Generated SQL (INSERT INTO table VALUES (encrypted_value))
  ↓
MySQL Database
```

* **JPA Internals**: When `save()` is called, Hibernate inspects the entity's state. If the entity has no ID, it schedules an `INSERT` statement. If it has an ID, it schedules an `UPDATE` statement.
* **Transparent Column Encryption**: Fields annotated with `@Convert(converter = EncryptedBigDecimalConverter.class)` are intercepted. `CryptoUtils.encrypt()` is called, which encrypts the value using **AES-256 (ECB mode with PKCS5Padding)** and returns a Base64 string to be stored in the database.

---

## 16. Exception Flow

Centralized exception handling ensures consistent API error responses.

* **Validation Errors**: Uncaught `@Valid` constraint violations throw `MethodArgumentNotValidException`. The [GlobalExceptionHandler](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/common/GlobalExceptionHandler.java) catches this, formats the field errors, and returns HTTP 400.
* **Business Exceptions**: `IllegalArgumentException` and `IllegalStateException` are mapped to HTTP 400.
* **Security Exceptions**: `AccessDeniedException` is mapped to HTTP 403.
* **Error Response Format**:
  ```json
  {
    "timestamp": "2026-06-29T22:20:00.000",
    "status": 400,
    "error": "Bad Request",
    "message": "Error details",
    "path": "/api/endpoint"
  }
  ```

---

## 17. Audit Flow

Provides transparent operation auditing across the application.

```text
Service Method Call ──> @AuditAction Interceptor (AuditAspect)
                              │
                    Resolves Security Principal
                              │
                    Persists AuditLog Entity ──> MySQL
```

* **AOP Interceptor**: [AuditAspect](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/config/AuditAspect.java) intercepts methods annotated with `@AuditAction`.
* **Processing**:
  * Resolves the authenticated user from the `SecurityContextHolder`.
  * Extracts the module and action details from the `@AuditAction` annotation.
  * Creates an `AuditLog` record containing the user details, timestamp, action, and method signature.
  * Saves the log to the database.

---

## 18. Notification Flow

An event-driven notification system notifies users of status changes.

```text
Service Action ──> Publish StatusChangeEvent ──> StatusChangeEventListener
                                                       │
                                            Saves Notification Entity
```

* **Publishing**: Services publish a `StatusChangeEvent` containing the target user ID, title, message, and actor details.
* **Listening**: `StatusChangeEventListener` catches the event, resolves the target `User` entity, creates a `Notification` record, and saves it to the database.

---

## 19. Complete End-to-End Flow

### 1. Travel Request Submission & Approval
```text
Employee                      Manager                   Database
   │                             │                         │
   │──> Create Trip (Draft) ────>│────────────────────────>│ (Save Draft)
   │                             │                         │
   │──> Submit Trip ────────────>│────────────────────────>│ (Status: SUBMITTED)
   │                             │                         │
   │                             │──> Approve Trip ───────>│ (Status: APPROVED)
   │                             │                         │
   │<── Notification (Approved) ─│                         │
```

### 2. Expense Claim Audit & Payment
```text
Employee                    Compliance Engine            Finance Executive
   │                             │                               │
   │──> Add Expense Line ───────>│                               │
   │                             │──> Run Compliance Check ────> │ (Saves Audit/Exception)
   │                             │                               │
   │──> Submit Claim ───────────>│─────────────────────────────> │ (Status: SUBMITTED)
   │                             │                               │
   │                             │                               │──> Process Payment
   │                             │                               │      (Status: PAID)
```

---

## 20. Internal Spring Boot Concepts Used

* **Inversion of Control (IoC)**: The Spring container manages the lifecycle and configuration of application beans.
* **Dependency Injection (DI)**: Dependencies are injected into beans, decoupling class implementations.
* **Component Scan**: Spring scans `com.journeyplus` at startup to discover and register beans.
* **Spring MVC**: Maps HTTP requests to controller endpoints and handles serialization.
* **Spring Data JPA**: Simplifies data access by generating repository implementations at runtime.
* **Spring Security**: Provides authentication and authorization filters.
* **Aspect-Oriented Programming (AOP)**: Separates cross-cutting concerns like auditing and logging from business logic.
* **Transaction Management**: Manages database transactions via `@Transactional` boundaries.

---

## 21. Sequence Diagrams

### 1. User Login Sequence
```text
Client            AuthController          AuthService        JwtTokenProvider        Database
  │                     │                      │                     │                   │
  │── POST /login ─────>│                      │                     │                   │
  │                     │── login() ──────────>│                     │                   │
  │                     │                      │── findByUsername() ────────────────────>│
  │                     │                      │<── User Entity ─────────────────────────│
  │                     │                      │                     │                   │
  │                     │                      │── generateToken() ─>│                   │
  │                     │                      │                     │── Signs JWT       │
  │                     │                      │                     │     with RSA      │
  │                     │                      │<── Signed Token ────│                   │
  │                     │<── AuthResponse ─────│                     │                   │
  │<── Token (200 OK) ──│                      │                     │                   │
```

### 2. Expense Line Compliance Check
```text
Client          ExpenseController       ExpenseService      ComplianceEngine         Database
  │                     │                      │                    │                    │
  │── POST /lines ─────>│                      │                    │                    │
  │                     │── addExpenseLine() ─>│                    │                    │
  │                     │                      │── Save Line ───────────────────────────>│
  │                     │                      │── runCheck() ─────>│                    │
  │                     │                      │                    │── Check Policy ───>│
  │                     │                      │                    │<── Policy Limits ─│
  │                     │                      │                    │                    │
  │                     │                      │                    │── Save Exception ─>│
  │                     │                      │<── Return Line ────│                    │
  │                     │                      │── Save Claim ──────────────────────────>│
  │<── Line (200 OK) ───│                      │                    │                    │
```

---

## 22. Class Relationships

```text
+-------------------+      +-------------------+      +-------------------+      +------------------+
|  TripController   | ---> |    TripService    | ---> |TripRequestRepository| ---> |   TripRequest    |
+-------------------+      +-------------------+      +-------------------+      +------------------+
                                     │                                                     │
                                     v                                                     v
                         +-----------------------+                              +-------------------+
                         |  StatusChangeEvent    |                              |   ItineraryLeg    |
                         +-----------------------+                              +-------------------+
                                     │                                                     │
                                     v                                                     v
                         +-----------------------+                              +-------------------+
                         |StatusChangeEventListener|                            |  VisaRequirement  |
                         +-----------------------+                              +-------------------+
```

---

## 23. Conclusion

The **JourneyPlus** backend is built on a clean, layered architecture that separates concerns across controllers, services, and repositories.

### Key Takeaways
* **Security**: Stateless authentication is implemented using asymmetric RSA-signed JWTs, and financial data is encrypted at rest using transparent AES-256 database converters.
* **Compliance**: An automated policy engine audits expenses in real-time, flagging policy exceptions.
* **Auditability**: Spring AOP intercepts service operations to write audit logs without polluting business logic.
* **Maintainability**: The codebase leverages Spring Boot auto-configuration, dependency injection, and transaction management to deliver a scalable corporate travel solution.
