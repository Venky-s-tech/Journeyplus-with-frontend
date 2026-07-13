# JourneyPlus - API Documentation

## 1. Document Information

* **Document Title**: REST API Documentation
* **Project Name**: JourneyPlus - Corporate Travel and Expense Management Backend
* **Version**: 1.0.0
* **Author**: Software & API Architect
* **Date**: June 29, 2026
* **Purpose of the Document**: This document describes all REST API endpoints exposed by the JourneyPlus backend. It helps frontend developers, QA engineers, and interviewers understand how to interact with the system.

---

## 2. Table of Contents
1. [Document Information](#1-document-information)
2. [Table of Contents](#2-table-of-contents)
3. [Introduction](#3-introduction)
4. [API Overview](#4-api-overview)
5. [Authentication](#5-authentication)
6. [Common HTTP Status Codes](#6-common-http-status-codes)
7. [Common Response Format](#7-common-response-format)
8. [API Modules](#8-api-modules)
9. [Endpoint Documentation](#9-endpoint-documentation)
10. [API Flow](#10-api-flow)
11. [Validation Rules](#11-validation-rules)
12. [Security](#12-security)
13. [Swagger](#13-swagger)
14. [Error Handling](#14-error-handling)
15. [API Testing](#15-api-testing)
16. [Best Practices](#16-best-practices)
17. [Summary](#17-summary)

---

## 3. Introduction

### What is an API?
* An **API (Application Programming Interface)** is a set of rules that lets different software programs talk to each other.
* In this project, the backend exposes APIs so that web or mobile frontend applications can save and retrieve data.

### Why this project uses REST APIs?
* **REST (Representational State Transfer)** is a standard way to design APIs.
* It uses standard HTTP methods (like `GET`, `POST`, `PUT`, `DELETE`) to perform actions on resources.
* It is simple, fast, and supported by almost all platforms.

### How clients communicate with the backend
* The client sends an HTTP request to the backend.
* The request contains a URL, an HTTP method, headers, and sometimes a JSON body.
* The backend processes the request and sends back an HTTP response.

### Response format used
* This project uses **JSON (JavaScript Object Notation)** for all request and response bodies.
* JSON is a lightweight text format that is easy for both humans and computers to read.

---

## 4. API Overview

| Property | Value |
| :--- | :--- |
| **Base URL** | `/api` |
| **API Version** | `v1.0` |
| **Request Format** | `JSON` |
| **Response Format** | `JSON` |
| **Authentication Method** | `JWT (JSON Web Token)` |
| **Content Type** | `application/json` |

---

## 5. Authentication

Most APIs in JourneyPlus are protected and require authentication.

### JWT Authentication
* We use stateless **JWT (JSON Web Token)** authentication.
* When you login successfully, the server sends back an **Access Token**.
* You must include this token in the header of every subsequent request.

### Authorization Header
* Pass the access token in the `Authorization` header using the `Bearer` scheme.

**Example**:
```text
Authorization: Bearer <your_jwt_access_token>
```

---

## 6. Common HTTP Status Codes

| Code | Meaning | Description |
| :--- | :--- | :--- |
| **200** | OK | The request was successful, and the server returned the requested data. |
| **210** | Created | The request was successful, and a new resource was created (e.g., registration). |
| **400** | Bad Request | The request was invalid (e.g., missing required fields). |
| **401** | Unauthorized | The user is not logged in or the JWT token is invalid. |
| **403** | Forbidden | The user is logged in but does not have the required role to access this API. |
| **404** | Not Found | The requested resource does not exist. |
| **500** | Internal Server Error | Something went wrong on the server. |

---

## 7. Common Response Format

### Success Response
Successful requests return the requested resource directly as a JSON object or array.

**Example**:
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@company.com",
  "role": "ROLE_EMPLOYEE",
  "department": "Engineering",
  "active": true
}
```

### Error Response
When an error occurs, the server returns a structured error object.

**Example**:
```json
{
  "timestamp": "2026-06-29T22:40:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists",
  "path": "/api/auth/register"
}
```

### Validation Error Response
If input validation fails, the server returns the specific field errors.

**Example**:
```json
{
  "timestamp": "2026-06-29T22:41:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: email must be a well-formed email address",
  "path": "/api/auth/register"
}
```

---

## 8. API Modules

The APIs are grouped into the following modules:

1. **Authentication**: Registering users, logging in, and refreshing tokens.
2. **User Management**: Viewing profiles and approving pending accounts.
3. **Travel**: Managing trip drafts, submitting trips, and adding itinerary legs or visas.
4. **Policy**: Defining travel policies and city tier limits.
5. **Expense**: Managing expense claims, adding receipts, and paying reimbursements.
6. **Approval**: Reviewing and approving trips or cash advances.
7. **Notification**: Checking unread alerts.
8. **Audit**: Reviewing historical logs.

---

## 9. Endpoint Documentation

Below is the detailed documentation for the core endpoints in each module.

---

### Module 1: Authentication

#### 1. Register User
* **HTTP Method**: `POST`
* **URL**: `/api/auth/register`
* **Description**: Creates a new user account. Employees are activated immediately. Managers and admins require admin approval.
* **Authentication Required**: No
* **Request Body**:
  ```json
  {
    "username": "alice_smith",
    "email": "alice@company.com",
    "password": "securepassword",
    "role": "ROLE_EMPLOYEE",
    "department": "Sales"
  }
  ```
* **Request Fields**:
  | Field | Type | Required | Description |
  | :--- | :--- | :--- | :--- |
  | `username` | String | Yes | Unique name between 3 and 50 characters. |
  | `email` | String | Yes | Valid email address. |
  | `password` | String | Yes | Minimum 6 characters. |
  | `role` | String | Yes | Target role (e.g., `ROLE_EMPLOYEE`). |
  | `department` | String | Yes | User's department. |
* **Success Response (HTTP 201)**:
  ```json
  {
    "id": 2,
    "username": "alice_smith",
    "email": "alice@company.com",
    "role": "ROLE_EMPLOYEE",
    "department": "Sales",
    "active": true
  }
  ```
* **Error Responses**:
  * `400 Bad Request`: Username or email already exists.

#### 2. User Login
* **HTTP Method**: `POST`
* **URL**: `/api/auth/login`
* **Description**: Authenticates credentials and returns a JWT token.
* **Authentication Required**: No
* **Request Body**:
  ```json
  {
    "username": "alice_smith",
    "password": "securepassword"
  }
  ```
* **Success Response (HTTP 200)**:
  ```json
  {
    "jwt": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "alice_smith",
    "role": "ROLE_EMPLOYEE"
  }
  ```
* **Error Responses**:
  * `401 Unauthorized`: Invalid username or password.

---

### Module 2: Travel Management

#### 1. Create Trip Request
* **HTTP Method**: `POST`
* **URL**: `/api/trips`
* **Description**: Creates a new trip request draft.
* **Access Role**: `ROLE_EMPLOYEE`
* **Authentication Required**: Yes
* **Request Body**:
  ```json
  {
    "purpose": "Client meeting",
    "destination": "New York",
    "departureDate": "2026-07-10",
    "returnDate": "2026-07-15",
    "travelType": "DOMESTIC",
    "estimatedCost": 1500.00,
    "comments": "Visiting client headquarters"
  }
  ```
* **Success Response (HTTP 201)**:
  ```json
  {
    "id": 10,
    "purpose": "Client meeting",
    "destination": "New York",
    "departureDate": "2026-07-10",
    "returnDate": "2026-07-15",
    "travelType": "DOMESTIC",
    "estimatedCost": 1500.00,
    "status": "DRAFT",
    "comments": "Visiting client headquarters"
  }
  ```

#### 2. Submit Trip Request
* **HTTP Method**: `POST`
* **URL**: `/api/trips/{id}/submit`
* **Description**: Locks the trip request and submits it to the manager for review.
* **Access Role**: `ROLE_EMPLOYEE`
* **Authentication Required**: Yes
* **Path Parameters**:
  | Name | Type | Description |
  | :--- | :--- | :--- |
  | `id` | Long | The unique ID of the trip request. |
* **Success Response (HTTP 200)**:
  ```json
  {
    "id": 10,
    "status": "SUBMITTED"
  }
  ```

---

### Module 3: Expense Management

#### 1. Add Expense Line
* **HTTP Method**: `POST`
* **URL**: `/api/expenses/{claimId}/lines`
* **Description**: Adds a receipt line item to a draft claim. Triggers the compliance engine.
* **Access Role**: `ROLE_EMPLOYEE`
* **Authentication Required**: Yes
* **Path Parameters**:
  | Name | Type | Description |
  | :--- | :--- | :--- |
  | `claimId` | Long | The unique ID of the expense claim. |
* **Request Body**:
  ```json
  {
    "expenseDate": "2026-07-12",
    "category": "MEALS",
    "amount": 120.00,
    "originalCurrency": "USD",
    "receiptPath": "/receipts/meal_101.png"
  }
  ```
* **Success Response (HTTP 200)**:
  ```json
  {
    "id": 25,
    "expenseDate": "2026-07-12",
    "category": "MEALS",
    "amount": 120.00,
    "originalCurrency": "USD",
    "usdEquivalent": 120.00,
    "receiptPath": "/receipts/meal_101.png",
    "policyComplianceStatus": "COMPLIANT",
    "complianceRemarks": "Automated check passed. All limits satisfied."
  }
  ```

---

### Module 4: Cash Advances

#### 1. Request Cash Advance
* **HTTP Method**: `POST`
* **URL**: `/api/advances`
* **Description**: Requests a cash advance for an approved trip.
* **Access Role**: `ROLE_EMPLOYEE`
* **Authentication Required**: Yes
* **Request Body**:
  ```json
  {
    "tripRequest": {
      "id": 10
    },
    "requestedAmount": 500.00,
    "currency": "USD",
    "purposeDetails": "Daily local transport and meals"
  }
  ```
* **Success Response (HTTP 201)**:
  ```json
  {
    "id": 5,
    "requestedAmount": 500.00,
    "currency": "USD",
    "status": "REQUESTED",
    "purposeDetails": "Daily local transport and meals"
  }
  ```

---

## 10. API Flow

Here is how a request travels through the application:

```text
[Client Browser]
       │  1. Sends HTTP Request with JWT
       ▼
[Security Filter]
       │  2. Validates Token using RSA Public Key
       ▼
[Controller]
       │  3. Validates DTO Fields
       ▼
[Service]
       │  4. Runs Business Rules (e.g. Compliance Checks)
       ▼
[Repository]
       │  5. Maps Entities to Database Tables
       ▼
[MySQL Database]
```

---

## 11. Validation Rules

The application validates incoming request bodies using the following rules:

| DTO Class | Field | Validation Annotations | Description |
| :--- | :--- | :--- | :--- |
| `RegisterRequest` | `username` | `@NotBlank`, `@Size(min=3, max=50)` | Must be unique and 3-50 chars. |
| `RegisterRequest` | `email` | `@NotBlank`, `@Email` | Must be a valid email format. |
| `RegisterRequest` | `password` | `@NotBlank`, `@Size(min=6)` | Must be at least 6 chars. |
| `TripRequest` | `purpose` | `@NotBlank` | Cannot be empty. |
| `TripRequest` | `estimatedCost` | `@NotNull`, `@Min(1)` | Must be a positive amount. |
| `ExpenseLine` | `amount` | `@NotNull`, `@Min(1)` | Must be a positive amount. |

---

## 12. Security

* **JWT Verification**: Validates the signature on every request using the RSA Public Key.
* **Password Hashing**: Encrypts user passwords using the **BCrypt** algorithm.
* **Role-Based Access**: Restricts endpoints by role. For example, only users with `ROLE_TRAVEL_ADMIN` can access `/api/admin/**` and `/api/policies`.

---

## 13. Swagger

### Swagger UI URL
You can view and test all APIs interactively by visiting:
```text
http://localhost:8080/swagger-ui.html
```

### Testing with Swagger:
1. Click on the **Authorize** button.
2. Enter your JWT token in the format: `Bearer <your_token>`.
3. Click **Authorize** and close the dialog.
4. Expand any API endpoint, click **Try it out**, fill in the parameters, and click **Execute**.

---

## 14. Error Handling

If an error occurs, the [GlobalExceptionHandler](file:///c:/Users/venky/OneDrive/Desktop/finall-Backend/final%20backend/Testing_4_Modules/src/main/java/com/journeyplus/common/GlobalExceptionHandler.java) intercepts the exception and returns a structured JSON error response.

**Example Validation Error Response**:
```json
{
  "timestamp": "2026-06-29T22:42:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: purpose must not be blank",
  "path": "/api/trips"
}
```

---

## 15. API Testing

### Testing with Postman
1. **Login**: Send a `POST` request to `/api/auth/login` with your username and password.
2. **Extract Token**: Copy the `jwt` string from the response.
3. **Configure Headers**: In your next request, go to the **Headers** tab and add:
   * **Key**: `Authorization`
   * **Value**: `Bearer <copied_jwt_token>`
4. **Send Request**: Submit the request to access protected endpoints.

---

## 16. Best Practices

* **RESTful URLs**: Resources are named using plural nouns (e.g., `/api/trips`, `/api/expenses`).
* **HTTP Methods**: Uses `POST` to create, `GET` to read, and `PUT` to update resources.
* **Input Validation**: Validates payloads on the server side before running business logic.
* **Stateless Auth**: Uses JWTs to eliminate server-side session storage.

---

## 17. Summary

* **API Organization**: Endpoints are grouped logically by module (Auth, Trips, Expenses, Advances).
* **Security**: Enforced using asymmetric JWT verification and role-based filters.
* **Ease of Use**: Integrates Swagger UI to let developers test APIs directly from their browsers.
