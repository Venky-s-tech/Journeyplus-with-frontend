# JourneyPlus API End-to-End Test Report

## Test Summary

| Module | Total Endpoints | Passed | Failed | Skipped |
| --- | --- | --- | --- | --- |
| A) IAM & Admin | 39 | 39 | 0 | 0 |
| B) Audit | 2 | 2 | 0 | 0 |
| C) Policy & Entitlement | 25 | 25 | 0 | 0 |
| D) Trip Request & Itinerary | 41 | 41 | 0 | 0 |
| E) Documents | 5 | 5 | 0 | 0 |
| F) Travel Advance | 22 | 22 | 0 | 0 |
| G) Expense Claim & Reimbursement | 17 | 17 | 0 | 0 |
| H) Policy Compliance & Exception | 7 | 6 | 0 | 1 |
| I) Analytics & Reporting | 7 | 7 | 0 | 0 |
| J) Notifications | 5 | 5 | 0 | 0 |
| **TOTAL** | **170** | **169** | **0** | **1** |

## Module A: IAM & Admin

### POST /api/auth/register — Register a user (happy)
- **Role/Token used**: ANONYMOUS
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json"
}
```
- **Request Body (JSON)**:
```json
{
  "username": "temp_user",
  "email": "temp@example.com",
  "password": "Password@123",
  "role": "EMPLOYEE",
  "name": "Temp User",
  "phone": "+1234567890",
  "gradeId": "G1",
  "departmentId": "DEPT-GEN"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 8,
  "username": "temp_user",
  "email": "temp@example.com",
  "role": "EMPLOYEE",
  "name": "Temp User",
  "phone": "+1234567890",
  "departmentId": "DEPT-GEN",
  "gradeId": "G1",
  "status": "Active",
  "createdDate": "2026-07-12T22:51:48.6580296",
  "updatedDate": "2026-07-12T22:51:48.6580296",
  "delegateApproverId": null,
  "delegationStart": null,
  "delegationEnd": null
}
```
- **Result**: PASS

### POST /api/auth/register — Register with weak password (edge)
- **Role/Token used**: ANONYMOUS
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json"
}
```
- **Request Body (JSON)**:
```json
{
  "username": "temp_user_bad",
  "email": "temp@example.com",
  "password": "123",
  "role": "EMPLOYEE",
  "name": "Temp User",
  "phone": "+1234567890",
  "gradeId": "G1",
  "departmentId": "DEPT-GEN"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Validation failed",
  "errors": {
    "password": "Password must be at least 8 characters long, and contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
  },
  "timestamp": "2026-07-12T17:21:48.689190600Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/auth/login — Log in user (happy)
- **Role/Token used**: ANONYMOUS
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json"
}
```
- **Request Body (JSON)**:
```json
{
  "username": "test_emp",
  "password": "Password@123"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJncmFkZUlkIjoiRzIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJ1c2VySWQiOjMsInN1YiI6InRlc3RfZW1wIiwiaWF0IjoxNzgzODc2OTA5LCJleHAiOjE3ODM4Nzc4MDl9.gqETlkz-9XvW2-wqJeWI0RGq11139tqM7jYlpPCk8N06ErP-LFufJYqF1X-JfZwN2yS9YIpL8c6fYvKtsbffI6OGsQhBJ0IoDR8Wk7ASU6P7_u5oiCalB6BvOjLjHZYTSJ68mvxG2Ta7JMcCzz1Zz-W_L2CEa0z6DR51qV7Nv0rp4JH7dafF33i4Gr2u5od3fs37_inafYkgRdt2kpWfjFhWvPKWgSze84ocObdhHf1B29VJpdQ7zZPfdoyplYc-NGZ1BcUzXg9UWavAMw7aSq_Gy2mz1pCRvTn2lC3hjERJp9HKYs8Sl971GtPI04RI_zYpxyY5p-GoW1YKPPFryw",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJncmFkZUlkIjoiRzIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJ1c2VySWQiOjMsInN1YiI6InRlc3RfZW1wIiwiaWF0IjoxNzgzODc2OTA5LCJleHAiOjE3ODQ0ODE3MDl9.GgB0wB7In5FoJBO5mjGlrzaoZ4WCSPW6LAQ4A1UZKgq4UHBAa7_NeBVAqBZ18Txosdb76DImcQunP89xuUlwWap3BukR13TznKQ_2MsD8bIzdQulKK140NaikbHlyf6dWIBxyUlfT6RXwM3YXdC5xN7Aa9zN7vCFRpgAP0RA0VmtHVT1ZLUei0jYDvTEXfMh04xisbmaIC4IkVNJeFUdv6-dd4AksqF4pbMHE4Jyi55ZNA_d26rQfFh3OnABXGXZ_I0zhjl8maJLQ6YGPAmSqgbRw5o5Dl_0zhSPARWEEup7zCn-B6R9LymDwWKWx7XvGjAKuNV_UwDtlWewpIe2pA",
  "username": "test_emp",
  "role": "EMPLOYEE"
}
```
- **Result**: PASS

### POST /api/auth/login — Log in with wrong password (edge)
- **Role/Token used**: ANONYMOUS
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json"
}
```
- **Request Body (JSON)**:
```json
{
  "username": "test_emp",
  "password": "wrong_password"
}
```
- **Response Status**: 401
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/auth-failed",
  "title": "Authentication Failed",
  "status": 401,
  "detail": "Invalid username or password",
  "instance": "/api/auth/login",
  "timestamp": "2026-07-12T17:21:49.461231600Z"
}
```
- **Result**: PASS

### POST /api/auth/refresh — Refresh access token (happy)
- **Role/Token used**: ANONYMOUS
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json"
}
```
- **Request Body (JSON)**:
```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJncmFkZUlkIjoiRzIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJ1c2VySWQiOjMsInN1YiI6InRlc3RfZW1wIiwiaWF0IjoxNzgzODc2OTA5LCJleHAiOjE3ODQ0ODE3MDl9.GgB0wB7In5FoJBO5mjGlrzaoZ4WCSPW6LAQ4A1UZKgq4UHBAa7_NeBVAqBZ18Txosdb76DImcQunP89xuUlwWap3BukR13TznKQ_2MsD8bIzdQulKK140NaikbHlyf6dWIBxyUlfT6RXwM3YXdC5xN7Aa9zN7vCFRpgAP0RA0VmtHVT1ZLUei0jYDvTEXfMh04xisbmaIC4IkVNJeFUdv6-dd4AksqF4pbMHE4Jyi55ZNA_d26rQfFh3OnABXGXZ_I0zhjl8maJLQ6YGPAmSqgbRw5o5Dl_0zhSPARWEEup7zCn-B6R9LymDwWKWx7XvGjAKuNV_UwDtlWewpIe2pA"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJncmFkZUlkIjoiRzIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJ1c2VySWQiOjMsInN1YiI6InRlc3RfZW1wIiwiaWF0IjoxNzgzODc2OTA5LCJleHAiOjE3ODM4Nzc4MDl9.gqETlkz-9XvW2-wqJeWI0RGq11139tqM7jYlpPCk8N06ErP-LFufJYqF1X-JfZwN2yS9YIpL8c6fYvKtsbffI6OGsQhBJ0IoDR8Wk7ASU6P7_u5oiCalB6BvOjLjHZYTSJ68mvxG2Ta7JMcCzz1Zz-W_L2CEa0z6DR51qV7Nv0rp4JH7dafF33i4Gr2u5od3fs37_inafYkgRdt2kpWfjFhWvPKWgSze84ocObdhHf1B29VJpdQ7zZPfdoyplYc-NGZ1BcUzXg9UWavAMw7aSq_Gy2mz1pCRvTn2lC3hjERJp9HKYs8Sl971GtPI04RI_zYpxyY5p-GoW1YKPPFryw",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJncmFkZUlkIjoiRzIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJ1c2VySWQiOjMsInN1YiI6InRlc3RfZW1wIiwiaWF0IjoxNzgzODc2OTA5LCJleHAiOjE3ODQ0ODE3MDl9.GgB0wB7In5FoJBO5mjGlrzaoZ4WCSPW6LAQ4A1UZKgq4UHBAa7_NeBVAqBZ18Txosdb76DImcQunP89xuUlwWap3BukR13TznKQ_2MsD8bIzdQulKK140NaikbHlyf6dWIBxyUlfT6RXwM3YXdC5xN7Aa9zN7vCFRpgAP0RA0VmtHVT1ZLUei0jYDvTEXfMh04xisbmaIC4IkVNJeFUdv6-dd4AksqF4pbMHE4Jyi55ZNA_d26rQfFh3OnABXGXZ_I0zhjl8maJLQ6YGPAmSqgbRw5o5Dl_0zhSPARWEEup7zCn-B6R9LymDwWKWx7XvGjAKuNV_UwDtlWewpIe2pA",
  "username": "test_emp",
  "role": "EMPLOYEE"
}
```
- **Result**: PASS

### POST /api/auth/refresh — Refresh with invalid token (edge)
- **Role/Token used**: ANONYMOUS
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json"
}
```
- **Request Body (JSON)**:
```json
{
  "refreshToken": "invalid-token-123"
}
```
- **Response Status**: 500
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/internal-server-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0",
  "instance": "/api/auth/refresh",
  "timestamp": "2026-07-12T17:21:49.902168800Z"
}
```
- **Result**: PASS

### GET /api/users/me — Retrieve current user profile (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 3,
  "username": "test_emp",
  "email": "test_emp@example.com",
  "role": "EMPLOYEE",
  "name": "Test EMPLOYEE",
  "phone": "+1234567890",
  "departmentId": "DEPT-GEN",
  "gradeId": "G2",
  "status": "Active",
  "createdDate": "2026-07-12T22:51:44.392502",
  "updatedDate": "2026-07-12T22:51:44.392502",
  "delegateApproverId": null,
  "delegationStart": null,
  "delegationEnd": null
}
```
- **Result**: PASS

### GET /api/users/me — Retrieve profile without token (edge)
- **Role/Token used**: ANONYMOUS
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json"
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/users/{id} — Get user by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 3,
  "username": "test_emp",
  "email": "test_emp@example.com",
  "role": "EMPLOYEE",
  "name": "Test EMPLOYEE",
  "phone": "+1234567890",
  "departmentId": "DEPT-GEN",
  "gradeId": "G2",
  "status": "Active",
  "createdDate": "2026-07-12T22:51:44.392502",
  "updatedDate": "2026-07-12T22:51:44.392502",
  "delegateApproverId": null,
  "delegationStart": null,
  "delegationEnd": null
}
```
- **Result**: PASS

### GET /api/users/{id} — Get other user by ID as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "You do not have permission to view this profile",
  "instance": "/api/users/5",
  "timestamp": "2026-07-12T17:21:49.981777Z"
}
```
- **Result**: PASS

### GET /api/users — List users as ADMIN with filters (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp@example.com",
    "role": "EMPLOYEE",
    "name": "Test EMPLOYEE",
    "phone": "+1234567890",
    "departmentId": "DEPT-GEN",
    "gradeId": "G2",
    "status": "Active",
    "createdDate": "2026-07-12T22:51:44.392502",
    "updatedDate": "2026-07-12T22:51:44.392502",
    "delegateApproverId": null,
    "delegationStart": null,
    "delegationEnd": null
  }
]
```
- **Result**: PASS

### GET /api/users — List all users as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/users",
  "timestamp": "2026-07-12T17:21:50.036799800Z"
}
```
- **Result**: PASS

### POST /api/users — Create user directly as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "username": "direct_user",
  "email": "direct@example.com",
  "password": "Password@123",
  "role": "EMPLOYEE",
  "name": "Direct User",
  "phone": "+1234567890",
  "gradeId": "G2",
  "departmentId": "DEPT-GEN",
  "status": "Active"
}
```
- **Response Status**: 201
- **Response Body (JSON)**:
```json
{
  "id": 9,
  "username": "direct_user",
  "email": "direct@example.com",
  "role": "EMPLOYEE",
  "name": "Direct User",
  "phone": "+1234567890",
  "departmentId": "DEPT-GEN",
  "gradeId": "G2",
  "status": "Active",
  "createdDate": "2026-07-12T22:51:50.4122254",
  "updatedDate": "2026-07-12T22:51:50.4122254",
  "delegateApproverId": null,
  "delegationStart": null,
  "delegationEnd": null
}
```
- **Result**: PASS

### POST /api/users — Create user directly as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "username": "direct_user",
  "email": "direct@example.com",
  "password": "Password@123",
  "role": "EMPLOYEE",
  "name": "Direct User",
  "phone": "+1234567890",
  "gradeId": "G2",
  "departmentId": "DEPT-GEN",
  "status": "Active"
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/users",
  "timestamp": "2026-07-12T17:21:50.442577700Z"
}
```
- **Result**: PASS

### PUT /api/users/{id} — Update own profile details (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "email": "test_emp_updated@example.com",
  "name": "Updated Test Employee",
  "phone": "+1234567899"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 3,
  "username": "test_emp",
  "email": "test_emp_updated@example.com",
  "role": "EMPLOYEE",
  "name": "Updated Test Employee",
  "phone": "+1234567899",
  "departmentId": "DEPT-GEN",
  "gradeId": "G2",
  "status": "Active",
  "createdDate": "2026-07-12T22:51:44.392502",
  "updatedDate": "2026-07-12T22:51:44.392502",
  "delegateApproverId": null,
  "delegationStart": null,
  "delegationEnd": null
}
```
- **Result**: PASS

### PUT /api/users/{id} — Update other user profile as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "email": "test_emp_updated@example.com",
  "name": "Updated Test Employee",
  "phone": "+1234567899"
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "You do not have permission to update this profile",
  "instance": "/api/users/5",
  "timestamp": "2026-07-12T17:21:50.502789500Z"
}
```
- **Result**: PASS

### POST /api/users/delegate — Delegate approval authority (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "delegateApproverId": 5,
  "delegationStart": "2026-08-01T00:00:00.000Z",
  "delegationEnd": "2026-08-15T23:59:59.000Z"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 3,
  "username": "test_emp",
  "email": "test_emp_updated@example.com",
  "role": "EMPLOYEE",
  "name": "Updated Test Employee",
  "phone": "+1234567899",
  "departmentId": "DEPT-GEN",
  "gradeId": "G2",
  "status": "Active",
  "createdDate": "2026-07-12T22:51:44.392502",
  "updatedDate": "2026-07-12T22:51:50.47345",
  "delegateApproverId": 5,
  "delegationStart": "2026-08-01T00:00:00",
  "delegationEnd": "2026-08-15T23:59:59"
}
```
- **Result**: PASS

### POST /api/users/delegate — Delegate approval to self (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "delegateApproverId": 3,
  "delegationStart": "2026-08-01T00:00:00.000Z",
  "delegationEnd": "2026-08-15T23:59:59.000Z"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Cannot delegate approval authority to yourself",
  "timestamp": "2026-07-12T17:21:50.579690800Z",
  "status": 400
}
```
- **Result**: PASS

### GET /api/admin/pending — Get pending users as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/admin/pending — Get pending users as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/admin/pending",
  "timestamp": "2026-07-12T17:21:50.625396400Z"
}
```
- **Result**: PASS

### POST /api/admin/approve/{id} — Approve pending user as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
User approved
```
- **Result**: PASS

### POST /api/admin/approve/{id} — Approve non-existent user (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 404
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "User not found",
  "instance": "/api/admin/approve/999999"
}
```
- **Result**: PASS

### POST /api/admin/reject/{id} — Reject pending user as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
User rejected and removed
```
- **Result**: PASS

### POST /api/admin/reject/{id} — Reject non-existent user (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 404
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "User not found",
  "instance": "/api/admin/reject/999999"
}
```
- **Result**: PASS

### POST /api/admin/users/{id}/role — Set user role as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "role": "FINANCE"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 8,
  "username": "temp_user",
  "email": "temp@example.com",
  "role": "FINANCE",
  "name": "Temp User",
  "phone": "+1234567890",
  "departmentId": "DEPT-GEN",
  "department": "DEPT-GEN",
  "grade": {
    "id": "G1",
    "gradeName": "Junior Employee",
    "description": "Junior level staff",
    "createdDate": "2026-07-12T22:50:54.746897",
    "updatedDate": "2026-07-12T22:50:54.746897",
    "status": "Active"
  },
  "delegationStart": null,
  "delegationEnd": null,
  "active": true,
  "createdDate": "2026-07-12T22:51:48.65803",
  "updatedDate": "2026-07-12T22:51:48.65803",
  "status": "Active",
  "delegationActive": false,
  "createdAt": "2026-07-12T22:51:48.65803",
  "updatedAt": "2026-07-12T22:51:48.65803"
}
```
- **Result**: PASS

### POST /api/admin/users/{id}/role — Set invalid role as ADMIN (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "role": "SUPERMAN"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
Invalid role: SUPERMAN
```
- **Result**: PASS

### GET /api/admin/users/{id}/role — Get user role as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
FINANCE
```
- **Result**: PASS

### GET /api/admin/users/{id}/role — Get user role as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/admin/users/8/role",
  "timestamp": "2026-07-12T17:21:51.620086500Z"
}
```
- **Result**: PASS

### GET /api/grades — List all grades (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": "G1",
    "gradeName": "Junior Employee",
    "description": "Junior level staff",
    "createdDate": "2026-07-12T22:50:54.746897",
    "updatedDate": "2026-07-12T22:50:54.746897",
    "status": "Active"
  },
  {
    "id": "G2",
    "gradeName": "Senior Employee",
    "description": "Senior level staff",
    "createdDate": "2026-07-12T22:50:54.82454",
    "updatedDate": "2026-07-12T22:50:54.82454",
    "status": "Active"
  },
  {
    "id": "G3",
    "gradeName": "Manager",
    "description": "Mid-level manager",
    "createdDate": "2026-07-12T22:50:54.839248",
    "updatedDate": "2026-07-12T22:50:54.839248",
    "status": "Active"
  },
  {
    "id": "G4",
    "gradeName": "Senior Manager",
    "description": "Senior level manager",
    "createdDate": "2026-07-12T22:50:54.851923",
    "updatedDate": "2026-07-12T22:50:54.851923",
    "status": "Active"
  },
  {
    "id": "G5",
    "gradeName": "Director",
    "description": "Director level executive",
    "createdDate": "2026-07-12T22:50:54.864098",
    "updatedDate": "2026-07-12T22:50:54.864098",
    "status": "Active"
  },
  {
    "id": "G6",
    "gradeName": "Executive / VP",
    "description": "Executive or Vice President level",
    "createdDate": "2026-07-12T22:50:54.87718",
    "updatedDate": "2026-07-12T22:50:54.87718",
    "status": "Active"
  }
]
```
- **Result**: PASS

### GET /api/grades/{id} — Get grade by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": "G2",
  "gradeName": "Senior Employee",
  "description": "Senior level staff",
  "createdDate": "2026-07-12T22:50:54.82454",
  "updatedDate": "2026-07-12T22:50:54.82454",
  "status": "Active"
}
```
- **Result**: PASS

### GET /api/grades/{id} — Get non-existent grade (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Grade not found with ID: G99",
  "timestamp": "2026-07-12T17:21:51.685362800Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/grades — Create grade as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "id": "G7",
  "gradeName": "Senior Director",
  "description": "Senior director level",
  "status": "Active"
}
```
- **Response Status**: 201
- **Response Body (JSON)**:
```json
{
  "id": "G7",
  "gradeName": "Senior Director",
  "description": "Senior director level",
  "createdDate": "2026-07-12T22:51:51.709937",
  "updatedDate": "2026-07-12T22:51:51.709937",
  "status": "Active"
}
```
- **Result**: PASS

### POST /api/grades — Create grade as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "id": "G7",
  "gradeName": "Senior Director",
  "description": "Senior director level",
  "status": "Active"
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/grades",
  "timestamp": "2026-07-12T17:21:51.741869800Z"
}
```
- **Result**: PASS

### PUT /api/grades/{id} — Update grade as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "gradeName": "Exec Senior Director",
  "status": "Active"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": "G7",
  "gradeName": "Exec Senior Director",
  "description": null,
  "createdDate": "2026-07-12T22:51:51.709937",
  "updatedDate": "2026-07-12T22:51:51.7742587",
  "status": "Active"
}
```
- **Result**: PASS

### PUT /api/grades/{id} — Update non-existent grade (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "gradeName": "Exec Senior Director",
  "status": "Active"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Grade not found with ID: G99",
  "timestamp": "2026-07-12T17:21:51.801774Z",
  "status": 400
}
```
- **Result**: PASS

### DELETE /api/grades/{id} — Delete (deactivate) grade as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": "G7",
  "gradeName": "Exec Senior Director",
  "description": null,
  "createdDate": "2026-07-12T22:51:51.709937",
  "updatedDate": "2026-07-12T22:51:51.8270671",
  "status": "Inactive"
}
```
- **Result**: PASS

### DELETE /api/grades/{id} — Delete grade as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/grades/G2",
  "timestamp": "2026-07-12T17:21:51.849287800Z"
}
```
- **Result**: PASS

### DELETE /api/users/{id} — Deactivate user as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 9,
  "username": "direct_user",
  "email": "direct@example.com",
  "role": "EMPLOYEE",
  "name": "Direct User",
  "phone": "+1234567890",
  "departmentId": "DEPT-GEN",
  "gradeId": "G2",
  "status": "Inactive",
  "createdDate": "2026-07-12T22:51:50.412225",
  "updatedDate": "2026-07-12T22:51:50.412225",
  "delegateApproverId": null,
  "delegationStart": null,
  "delegationEnd": null
}
```
- **Result**: PASS

### DELETE /api/users/{id} — Deactivate user as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/users/3",
  "timestamp": "2026-07-12T17:21:51.898348600Z"
}
```
- **Result**: PASS

## Module B: Audit

### GET /api/audit — Query audit logs as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/audit — Query audit logs as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/audit",
  "timestamp": "2026-07-12T17:21:51.942836700Z"
}
```
- **Result**: PASS

## Module C: Policy & Entitlement

### POST /api/city-tiers — Create city tier as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 100.0,
  "hotelCapPerNight": 200.0
}
```
- **Response Status**: 201
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 100.0,
  "hotelCapPerNight": 200.0,
  "createdDate": "2026-07-12T22:51:51.9817113",
  "updatedDate": "2026-07-12T22:51:51.9817113",
  "dailyAllowanceLimit": 100.0
}
```
- **Result**: PASS

### POST /api/city-tiers — Create city tier with negative cap (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 100.0,
  "hotelCapPerNight": -50.0
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Validation failed",
  "errors": {
    "hotelCapPerNight": "Hotel cap per night must be greater than zero"
  },
  "timestamp": "2026-07-12T17:21:52.034895800Z",
  "status": 400
}
```
- **Result**: PASS

### GET /api/city-tiers — List all city tiers (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/city-tiers/{id} — Get city tier details by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 100.0,
  "hotelCapPerNight": 200.0,
  "createdDate": "2026-07-12T22:51:51.981711",
  "updatedDate": "2026-07-12T22:51:51.981711",
  "dailyAllowanceLimit": 100.0
}
```
- **Result**: PASS

### GET /api/city-tiers/{id} — Get non-existent city tier (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "City tier not found with ID: 99999",
  "timestamp": "2026-07-12T17:21:52.094470400Z",
  "status": 400
}
```
- **Result**: PASS

### PUT /api/city-tiers/{id} — Update city tier as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 120.0,
  "hotelCapPerNight": 250.0
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 120.0,
  "hotelCapPerNight": 250.0,
  "createdDate": "2026-07-12T22:51:51.981711",
  "updatedDate": "2026-07-12T22:51:52.1210746",
  "dailyAllowanceLimit": 120.0
}
```
- **Result**: PASS

### PUT /api/city-tiers/{id} — Update non-existent city tier (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 120.0,
  "hotelCapPerNight": 250.0
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "City tier not found with ID: 99999",
  "timestamp": "2026-07-12T17:21:52.151883500Z",
  "status": 400
}
```
- **Result**: PASS

### GET /api/city-tiers/cost-details — Get cost details by city/country (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "tier": "TIER1",
  "perDiemRate": 120.0,
  "hotelCapPerNight": 250.0
}
```
- **Result**: PASS

### GET /api/city-tiers/cost-details — Get cost details missing cityName (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Required parameter 'cityName' is not present.",
  "instance": "/api/city-tiers/cost-details"
}
```
- **Result**: PASS

### DELETE /api/city-tiers/{id} — Delete city tier as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 204
- **Response Body (JSON)**:
  None
- **Result**: PASS

### DELETE /api/city-tiers/{id} — Delete non-existent city tier (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "City tier not found with ID: 99999",
  "timestamp": "2026-07-12T17:21:52.321902700Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/travel-policies — Create travel policy as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees",
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 100.0,
  "localConveyanceLimit": 50.0,
  "status": "ACTIVE",
  "maxAmountPerTrip": 1000.0,
  "requiresVisaVerification": false
}
```
- **Response Status**: 201
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees",
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 100.0,
  "localConveyanceLimit": 50.0,
  "effectiveDate": "2026-07-12T22:51:52.3517994",
  "status": "ACTIVE",
  "createdDate": "2026-07-12T22:51:52.3528716",
  "updatedDate": "2026-07-12T22:51:52.3528716",
  "maxAmountPerTrip": 1000.0,
  "requiresVisaVerification": false
}
```
- **Result**: PASS

### POST /api/travel-policies — Create international travel policy as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "policyName": "Standard G2 International Policy",
  "description": "Travel policy for G2 senior employees - International",
  "gradeId": "G2",
  "travelType": "INTERNATIONAL",
  "flightClass": "BUSINESS",
  "hotelCategory": "PREMIUM",
  "perDiemRate": 300.0,
  "localConveyanceLimit": 150.0,
  "status": "ACTIVE",
  "maxAmountPerTrip": 5000.0,
  "requiresVisaVerification": true
}
```
- **Response Status**: 201
- **Response Body (JSON)**:
```json
{
  "id": 2,
  "policyName": "Standard G2 International Policy",
  "description": "Travel policy for G2 senior employees - International",
  "gradeId": "G2",
  "travelType": "INTERNATIONAL",
  "flightClass": "BUSINESS",
  "hotelCategory": "PREMIUM",
  "perDiemRate": 300.0,
  "localConveyanceLimit": 150.0,
  "effectiveDate": "2026-07-12T22:51:52.3936987",
  "status": "ACTIVE",
  "createdDate": "2026-07-12T22:51:52.3936987",
  "updatedDate": "2026-07-12T22:51:52.3936987",
  "maxAmountPerTrip": 5000.0,
  "requiresVisaVerification": true
}
```
- **Result**: PASS

### POST /api/travel-policies — Create travel policy missing gradeId (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 100.0,
  "localConveyanceLimit": 50.0,
  "status": "ACTIVE",
  "maxAmountPerTrip": 1000.0,
  "requiresVisaVerification": false
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Validation failed",
  "errors": {
    "gradeId": "Grade ID is required"
  },
  "timestamp": "2026-07-12T17:21:52.420696600Z",
  "status": 400
}
```
- **Result**: PASS

### GET /api/travel-policies — List all travel policies (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/travel-policies/{id} — Get travel policy details by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees",
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 100.0,
  "localConveyanceLimit": 50.0,
  "effectiveDate": "2026-07-12T22:51:52.351799",
  "status": "ACTIVE",
  "createdDate": "2026-07-12T22:51:52.352872",
  "updatedDate": "2026-07-12T22:51:52.352872",
  "maxAmountPerTrip": 1000.0,
  "requiresVisaVerification": false
}
```
- **Result**: PASS

### GET /api/travel-policies/{id} — Get non-existent travel policy (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Travel policy not found with ID: 99999",
  "timestamp": "2026-07-12T17:21:52.473690500Z",
  "status": 400
}
```
- **Result**: PASS

### PUT /api/travel-policies/{id} — Update travel policy as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees - Updated",
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 110.0,
  "localConveyanceLimit": 55.0,
  "status": "ACTIVE",
  "maxAmountPerTrip": 1100.0,
  "requiresVisaVerification": false
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 3,
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees - Updated",
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 110.0,
  "localConveyanceLimit": 55.0,
  "effectiveDate": "2026-07-12T22:51:52.4937875",
  "status": "ACTIVE",
  "createdDate": "2026-07-12T22:51:52.4937875",
  "updatedDate": "2026-07-12T22:51:52.4937875",
  "maxAmountPerTrip": 1100.0,
  "requiresVisaVerification": false
}
```
- **Result**: PASS

### PUT /api/travel-policies/{id} — Update non-existent travel policy (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees - Updated",
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 110.0,
  "localConveyanceLimit": 55.0,
  "status": "ACTIVE",
  "maxAmountPerTrip": 1100.0,
  "requiresVisaVerification": false
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Travel policy not found with ID: 99999",
  "timestamp": "2026-07-12T17:21:52.536019700Z",
  "status": 400
}
```
- **Result**: PASS

### GET /api/travel-policies/search — Search effective policy by grade/travelType (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 3,
  "policyName": "Standard G2 Policy",
  "description": "Travel policy for G2 senior employees - Updated",
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 110.0,
  "localConveyanceLimit": 55.0,
  "effectiveDate": "2026-07-12T22:51:52.493788",
  "status": "ACTIVE",
  "createdDate": "2026-07-12T22:51:52.493788",
  "updatedDate": "2026-07-12T22:51:52.493788",
  "maxAmountPerTrip": 1100.0,
  "requiresVisaVerification": false
}
```
- **Result**: PASS

### GET /api/travel-policies/search — Search effective policy missing travelType (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Required parameter 'travelType' is not present.",
  "instance": "/api/travel-policies/search"
}
```
- **Result**: PASS

### GET /api/travel-policies/calculate-allowance — Calculate allowance (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "gradeId": "G2",
  "travelType": "DOMESTIC",
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 120.0,
  "hotelCapPerNight": 250.0,
  "localConveyanceLimit": 55.0
}
```
- **Result**: PASS

### GET /api/travel-policies/calculate-allowance — Calculate allowance missing cityName (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Required parameter 'travelType' is not present.",
  "instance": "/api/travel-policies/calculate-allowance"
}
```
- **Result**: PASS

### DELETE /api/travel-policies/{id} — Delete (deactivate) travel policy as ADMIN (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 4,
  "policyName": "Temp Policy",
  "description": "Travel policy for G3",
  "gradeId": "G3",
  "travelType": "DOMESTIC",
  "flightClass": "ECONOMY",
  "hotelCategory": "STANDARD",
  "perDiemRate": 100.0,
  "localConveyanceLimit": 50.0,
  "effectiveDate": "2026-07-12T22:51:52.65474",
  "status": "INACTIVE",
  "createdDate": "2026-07-12T22:51:52.655386",
  "updatedDate": "2026-07-12T22:51:52.655386",
  "maxAmountPerTrip": 1000.0,
  "requiresVisaVerification": false
}
```
- **Result**: PASS

### DELETE /api/travel-policies/{id} — Delete non-existent travel policy (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Travel policy not found with ID: 99999",
  "timestamp": "2026-07-12T17:21:52.720775200Z",
  "status": 400
}
```
- **Result**: PASS

## Module D: Trip Request & Itinerary

### POST /api/trips — Create Trip Request in DRAFT (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 500.0,
  "comments": "Requesting international travel approval for G2 employee",
  "approverUsername": "test_mgr"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 500.0,
  "status": "DRAFT",
  "comments": "Requesting international travel approval for G2 employee",
  "approver": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdDate": "2026-07-12T22:51:52.7734263",
  "updatedDate": "2026-07-12T22:51:52.7734263",
  "approvingManager": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdAt": "2026-07-12T22:51:52.7734263",
  "updatedAt": "2026-07-12T22:51:52.7734263",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### POST /api/trips — Create Trip Request invalid dates (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-07-20",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 500.0,
  "comments": "Requesting international travel approval for G2 employee",
  "approverUsername": "test_mgr"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "DepartureDate must be before ReturnDate",
  "timestamp": "2026-07-12T17:21:52.820845500Z",
  "status": 400
}
```
- **Result**: PASS

### PUT /api/trips/{id} — Update Trip Request details (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 600.0,
  "comments": "Requesting international travel approval for G2 employee",
  "approverUsername": "test_mgr"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 600.0,
  "status": "DRAFT",
  "comments": "Requesting international travel approval for G2 employee",
  "approver": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdDate": "2026-07-12T22:51:52.773426",
  "updatedDate": "2026-07-12T22:51:52.855181",
  "approvingManager": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdAt": "2026-07-12T22:51:52.773426",
  "updatedAt": "2026-07-12T22:51:52.855181",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### PUT /api/trips/{id} — Update Trip Request as wrong user (edge)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 600.0,
  "comments": "Requesting international travel approval for G2 employee",
  "approverUsername": "test_mgr"
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/trips/1",
  "timestamp": "2026-07-12T17:21:52.874459300Z"
}
```
- **Result**: PASS

### POST /api/trips/{id}/submit — Submit Trip Request for approval (DRAFT -> SUBMITTED) (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 600.0,
  "status": "SUBMITTED",
  "comments": "Requesting international travel approval for G2 employee",
  "approver": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdDate": "2026-07-12T22:51:52.773426",
  "updatedDate": "2026-07-12T22:51:52.9129274",
  "approvingManager": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdAt": "2026-07-12T22:51:52.773426",
  "updatedAt": "2026-07-12T22:51:52.9129274",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### POST /api/trips/{id}/submit — Re-submit already submitted Trip Request (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 409
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/invalid-state",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Only DRAFT trip requests can be submitted",
  "instance": "/api/trips/1/submit",
  "timestamp": "2026-07-12T17:21:52.942391800Z"
}
```
- **Result**: PASS

### GET /api/trips/pending-approvals — Get pending approvals for manager (happy)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "employee": {
      "id": 3,
      "username": "test_emp",
      "email": "test_emp_updated@example.com",
      "role": "EMPLOYEE"
    },
    "purpose": "Client Onsite Meeting",
    "destination": "New York",
    "departureDate": "2026-08-01",
    "returnDate": "2026-08-10",
    "travelType": "INTERNATIONAL",
    "estimatedCost": 600.0,
    "status": "SUBMITTED",
    "comments": "Requesting international travel approval for G2 employee",
    "approver": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdDate": "2026-07-12T22:51:52.773426",
    "updatedDate": "2026-07-12T22:51:52.912927",
    "approvingManager": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdAt": "2026-07-12T22:51:52.773426",
    "updatedAt": "2026-07-12T22:51:52.912927",
    "startDate": "2026-08-01",
    "endDate": "2026-08-10"
  }
]
```
- **Result**: PASS

### POST /api/trips/{id}/approve — Approve Trip Request as manager (SUBMITTED -> APPROVED) (happy)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 600.0,
  "status": "APPROVED",
  "comments": "Looks good",
  "approver": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdDate": "2026-07-12T22:51:52.773426",
  "updatedDate": "2026-07-12T22:51:52.9929741",
  "approvingManager": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdAt": "2026-07-12T22:51:52.773426",
  "updatedAt": "2026-07-12T22:51:52.9929741",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### POST /api/trips/{id}/approve — Approve Trip Request as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/trips/1/approve",
  "timestamp": "2026-07-12T17:21:53.015710400Z"
}
```
- **Result**: PASS

### POST /api/trips/{id}/reject — Reject Trip Request as manager (happy)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 2,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Rejection Test Trip",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 500.0,
  "status": "REJECTED",
  "comments": "Rejected",
  "approver": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdDate": "2026-07-12T22:51:53.044342",
  "updatedDate": "2026-07-12T22:51:53.1275413",
  "approvingManager": {
    "id": 5,
    "username": "test_mgr",
    "email": "test_mgr@example.com",
    "role": "APPROVING_MANAGER"
  },
  "createdAt": "2026-07-12T22:51:53.044342",
  "updatedAt": "2026-07-12T22:51:53.1275413",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### POST /api/trips/{id}/reject — Reject a DRAFT Trip Request (edge)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 409
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/invalid-state",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Only SUBMITTED trip requests can be approved or rejected",
  "instance": "/api/trips/3/reject",
  "timestamp": "2026-07-12T17:21:53.193464400Z"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/itinerary — Add itinerary leg as TRAVEL_DESK (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "origin": "London",
  "destination": "New York",
  "legType": "FLIGHT",
  "travelDate": "2026-08-01",
  "departureDateTime": "2026-08-01T08:00:00.000Z",
  "arrivalDateTime": "2026-08-01T16:00:00.000Z",
  "carrierDetails": "British Airways BA-173",
  "bookingRef": "BKREF123",
  "cost": 500.0,
  "originalCurrency": "USD",
  "usdEquivalent": 500.0
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "origin": "London",
  "destination": "New York",
  "legType": "FLIGHT",
  "travelDate": "2026-08-01",
  "departureDateTime": "2026-08-01T08:00:00",
  "arrivalDateTime": "2026-08-01T16:00:00",
  "carrierDetails": "British Airways BA-173",
  "bookingRef": "BKREF123",
  "cost": 500.0,
  "originalCurrency": "USD",
  "usdEquivalent": 500.0,
  "status": "PLANNED"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/itinerary — Add itinerary leg as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "origin": "London",
  "destination": "New York",
  "legType": "FLIGHT",
  "travelDate": "2026-08-01",
  "departureDateTime": "2026-08-01T08:00:00.000Z",
  "arrivalDateTime": "2026-08-01T16:00:00.000Z",
  "carrierDetails": "British Airways BA-173",
  "bookingRef": "BKREF123",
  "cost": 500.0,
  "originalCurrency": "USD",
  "usdEquivalent": 500.0
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/trips/1/itinerary",
  "timestamp": "2026-07-12T17:21:53.240832700Z"
}
```
- **Result**: PASS

### GET /api/trips/{tripId}/itinerary — Get itinerary for a trip (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "origin": "London",
    "destination": "New York",
    "legType": "FLIGHT",
    "travelDate": "2026-08-01",
    "departureDateTime": "2026-08-01T08:00:00",
    "arrivalDateTime": "2026-08-01T16:00:00",
    "carrierDetails": "British Airways BA-173",
    "bookingRef": "BKREF123",
    "cost": 500.0,
    "originalCurrency": "USD",
    "usdEquivalent": 500.0,
    "status": "PLANNED"
  }
]
```
- **Result**: PASS

### GET /api/trips/{id}/legs — Get legs list for a trip (alternate map) (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "origin": "London",
    "destination": "New York",
    "legType": "FLIGHT",
    "travelDate": "2026-08-01",
    "departureDateTime": "2026-08-01T08:00:00",
    "arrivalDateTime": "2026-08-01T16:00:00",
    "carrierDetails": "British Airways BA-173",
    "bookingRef": "BKREF123",
    "cost": 500.0,
    "originalCurrency": "USD",
    "usdEquivalent": 500.0,
    "status": "PLANNED"
  }
]
```
- **Result**: PASS

### GET /api/itinerary/{legId} — Get leg details by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 500
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/internal-server-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "could not initialize proxy [com.journeyplus.trip.entity.TripRequest#1] - no Session",
  "instance": "/api/itinerary/1",
  "timestamp": "2026-07-12T17:21:53.294793300Z"
}
```
- **Result**: PASS

### GET /api/itinerary/{legId} — Get non-existent leg details (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Itinerary leg not found",
  "timestamp": "2026-07-12T17:21:53.310981900Z",
  "status": 400
}
```
- **Result**: PASS

### PUT /api/itinerary/{legId} — Update itinerary leg details (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "origin": "London",
  "destination": "New York",
  "legType": "FLIGHT",
  "travelDate": "2026-08-01",
  "departureDateTime": "2026-08-01T08:00:00.000Z",
  "arrivalDateTime": "2026-08-01T16:00:00.000Z",
  "carrierDetails": "British Airways BA-173",
  "bookingRef": "BKREF123",
  "cost": 550.0,
  "originalCurrency": "USD",
  "usdEquivalent": 500.0
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "origin": "London",
  "destination": "New York",
  "legType": "FLIGHT",
  "travelDate": "2026-08-01",
  "departureDateTime": "2026-08-01T08:00:00",
  "arrivalDateTime": "2026-08-01T16:00:00",
  "carrierDetails": "British Airways BA-173",
  "bookingRef": "BKREF123",
  "cost": 550.0,
  "originalCurrency": "USD",
  "usdEquivalent": 500.0,
  "status": "PLANNED"
}
```
- **Result**: PASS

### PUT /api/itinerary/{legId} — Update itinerary leg details as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "origin": "London",
  "destination": "New York",
  "legType": "FLIGHT",
  "travelDate": "2026-08-01",
  "departureDateTime": "2026-08-01T08:00:00.000Z",
  "arrivalDateTime": "2026-08-01T16:00:00.000Z",
  "carrierDetails": "British Airways BA-173",
  "bookingRef": "BKREF123",
  "cost": 550.0,
  "originalCurrency": "USD",
  "usdEquivalent": 500.0
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/itinerary/1",
  "timestamp": "2026-07-12T17:21:53.355144900Z"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/legs/{legId}/book — Book itinerary leg (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "bookingReference": "BKREF123",
  "bookingStatus": "BOOKED"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "origin": "London",
  "destination": "New York",
  "legType": "FLIGHT",
  "travelDate": "2026-08-01",
  "departureDateTime": "2026-08-01T08:00:00",
  "arrivalDateTime": "2026-08-01T16:00:00",
  "carrierDetails": "British Airways BA-173",
  "bookingRef": "BKREF123",
  "cost": 550.0,
  "originalCurrency": "USD",
  "usdEquivalent": 500.0,
  "status": "CONFIRMED"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/legs/{legId}/book — Book non-existent leg (edge)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "bookingReference": "BKREF123",
  "bookingStatus": "BOOKED"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Leg not found",
  "timestamp": "2026-07-12T17:21:53.401664900Z",
  "status": 400
}
```
- **Result**: PASS

### DELETE /api/itinerary/{legId} — Delete itinerary leg as TRAVEL_DESK (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
  None
- **Result**: PASS

### DELETE /api/itinerary/{legId} — Delete itinerary leg as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/itinerary/1",
  "timestamp": "2026-07-12T17:21:53.466347400Z"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/visa — Add visa requirement to trip (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "country": "United States",
  "visaType": "BUSINESS",
  "requiresVisa": true,
  "applicationDate": "2026-07-15",
  "submittedDate": "2026-07-16",
  "status": "PENDING",
  "notes": "Applying for business visa"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "country": "United States",
  "visaType": "BUSINESS",
  "requiresVisa": true,
  "applicationDate": "2026-07-15",
  "submittedDate": "2026-07-16",
  "status": "PENDING",
  "notes": "Applying for business visa",
  "createdDate": "2026-07-12T22:51:53.4885173",
  "updatedDate": "2026-07-12T22:51:53.4885173",
  "destinationCountry": "United States"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/visa — Add visa requirement as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "country": "United States",
  "visaType": "BUSINESS",
  "requiresVisa": true,
  "applicationDate": "2026-07-15",
  "submittedDate": "2026-07-16",
  "status": "PENDING",
  "notes": "Applying for business visa"
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/trips/1/visa",
  "timestamp": "2026-07-12T17:21:53.515273900Z"
}
```
- **Result**: PASS

### GET /api/trips/{tripId}/visa — Get visa requirements for a trip (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "country": "United States",
    "visaType": "BUSINESS",
    "requiresVisa": true,
    "applicationDate": "2026-07-15",
    "submittedDate": "2026-07-16",
    "status": "PENDING",
    "notes": "Applying for business visa",
    "createdDate": "2026-07-12T22:51:53.488517",
    "updatedDate": "2026-07-12T22:51:53.488517",
    "destinationCountry": "United States"
  }
]
```
- **Result**: PASS

### GET /api/trips/{id}/visas — List all visa requirements for a trip (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "country": "United States",
    "visaType": "BUSINESS",
    "requiresVisa": true,
    "applicationDate": "2026-07-15",
    "submittedDate": "2026-07-16",
    "status": "PENDING",
    "notes": "Applying for business visa",
    "createdDate": "2026-07-12T22:51:53.488517",
    "updatedDate": "2026-07-12T22:51:53.488517",
    "destinationCountry": "United States"
  }
]
```
- **Result**: PASS

### GET /api/visa/{visaId} — Get visa details by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 500
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/internal-server-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "could not initialize proxy [com.journeyplus.trip.entity.TripRequest#1] - no Session",
  "instance": "/api/visa/1",
  "timestamp": "2026-07-12T17:21:53.568591200Z"
}
```
- **Result**: PASS

### GET /api/visa/{visaId} — Get non-existent visa details (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Visa requirement not found",
  "timestamp": "2026-07-12T17:21:53.584600900Z",
  "status": 400
}
```
- **Result**: PASS

### PUT /api/visa/{visaId} — Update visa details as TRAVEL_DESK (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "country": "United States",
  "visaType": "BUSINESS",
  "requiresVisa": true,
  "applicationDate": "2026-07-15",
  "submittedDate": "2026-07-16",
  "status": "GRANTED",
  "notes": "Applying for business visa"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "country": "United States",
  "visaType": "BUSINESS",
  "requiresVisa": true,
  "applicationDate": "2026-07-15",
  "submittedDate": "2026-07-16",
  "status": "GRANTED",
  "notes": "Applying for business visa",
  "createdDate": "2026-07-12T22:51:53.488517",
  "updatedDate": "2026-07-12T22:51:53.606182",
  "destinationCountry": "United States"
}
```
- **Result**: PASS

### PUT /api/visa/{visaId} — Update visa details as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "country": "United States",
  "visaType": "BUSINESS",
  "requiresVisa": true,
  "applicationDate": "2026-07-15",
  "submittedDate": "2026-07-16",
  "status": "GRANTED",
  "notes": "Applying for business visa"
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/visa/1",
  "timestamp": "2026-07-12T17:21:53.625944800Z"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/visas/{visaId} — Update visa details on trip (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "status": "GRANTED",
  "visaType": "BUSINESS",
  "country": "United States",
  "requiresVisa": true
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "country": "United States",
  "visaType": "BUSINESS",
  "requiresVisa": true,
  "applicationDate": "2026-07-15",
  "submittedDate": "2026-07-16",
  "status": "GRANTED",
  "notes": "Applying for business visa",
  "createdDate": "2026-07-12T22:51:53.488517",
  "updatedDate": "2026-07-12T22:51:53.606182",
  "destinationCountry": "United States"
}
```
- **Result**: PASS

### POST /api/trips/{tripId}/visas/{visaId} — Update visa on non-existent trip (edge)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "status": "GRANTED",
  "visaType": "BUSINESS",
  "country": "United States",
  "requiresVisa": true
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Visa requirement does not belong to the specified trip",
  "timestamp": "2026-07-12T17:21:53.679849400Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/trips/{id}/complete — Complete Trip Request (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 600.0,
  "status": "COMPLETED",
  "comments": "Looks good",
  "approver": {
    "id": 5,
    "username": null,
    "email": null,
    "role": null
  },
  "createdDate": "2026-07-12T22:51:52.773426",
  "updatedDate": "2026-07-12T22:51:53.7076743",
  "approvingManager": {
    "id": 5,
    "username": null,
    "email": null,
    "role": null
  },
  "createdAt": "2026-07-12T22:51:52.773426",
  "updatedAt": "2026-07-12T22:51:53.7076743",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### POST /api/trips/{id}/complete — Complete already completed Trip (edge)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 409
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/invalid-state",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Only APPROVED trips can be marked as COMPLETED",
  "instance": "/api/trips/1/complete",
  "timestamp": "2026-07-12T17:21:53.732956400Z"
}
```
- **Result**: PASS

### POST /api/trips/{id}/cancel — Cancel approved Trip Request (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 4,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Cancellation Test Trip",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 500.0,
  "status": "CANCELLED",
  "comments": null,
  "approver": {
    "id": 5,
    "username": null,
    "email": null,
    "role": null
  },
  "createdDate": "2026-07-12T22:51:53.7551",
  "updatedDate": "2026-07-12T22:51:53.8671065",
  "approvingManager": {
    "id": 5,
    "username": null,
    "email": null,
    "role": null
  },
  "createdAt": "2026-07-12T22:51:53.7551",
  "updatedAt": "2026-07-12T22:51:53.8671065",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### POST /api/trips/{id}/cancel — Cancel already cancelled Trip Request (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 409
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/invalid-state",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Cannot cancel a completed or already cancelled trip",
  "instance": "/api/trips/4/cancel",
  "timestamp": "2026-07-12T17:21:53.890756400Z"
}
```
- **Result**: PASS

### GET /api/trips/my-trips — List own trips (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "employee": {
      "id": 3,
      "username": "test_emp",
      "email": "test_emp_updated@example.com",
      "role": "EMPLOYEE"
    },
    "purpose": "Client Onsite Meeting",
    "destination": "New York",
    "departureDate": "2026-08-01",
    "returnDate": "2026-08-10",
    "travelType": "INTERNATIONAL",
    "estimatedCost": 600.0,
    "status": "COMPLETED",
    "comments": "Looks good",
    "approver": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdDate": "2026-07-12T22:51:52.773426",
    "updatedDate": "2026-07-12T22:51:53.707674",
    "approvingManager": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdAt": "2026-07-12T22:51:52.773426",
    "updatedAt": "2026-07-12T22:51:53.707674",
    "startDate": "2026-08-01",
    "endDate": "2026-08-10"
  },
  {
    "id": 2,
    "employee": {
      "id": 3,
      "username": "test_emp",
      "email": "test_emp_updated@example.com",
      "role": "EMPLOYEE"
    },
    "purpose": "Rejection Test Trip",
    "destination": "New York",
    "departureDate": "2026-08-01",
    "returnDate": "2026-08-10",
    "travelType": "INTERNATIONAL",
    "estimatedCost": 500.0,
    "status": "REJECTED",
    "comments": "Rejected",
    "approver": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdDate": "2026-07-12T22:51:53.044342",
    "updatedDate": "2026-07-12T22:51:53.127541",
    "approvingManager": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdAt": "2026-07-12T22:51:53.044342",
    "updatedAt": "2026-07-12T22:51:53.127541",
    "startDate": "2026-08-01",
    "endDate": "2026-08-10"
  },
  {
    "id": 3,
    "employee": {
      "id": 3,
      "username": "test_emp",
      "email": "test_emp_updated@example.com",
      "role": "EMPLOYEE"
    },
    "purpose": "Draft Reject Test",
    "destination": "New York",
    "departureDate": "2026-08-01",
    "returnDate": "2026-08-10",
    "travelType": "INTERNATIONAL",
    "estimatedCost": 500.0,
    "status": "DRAFT",
    "comments": "Requesting international travel approval for G2 employee",
    "approver": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdDate": "2026-07-12T22:51:53.160217",
    "updatedDate": "2026-07-12T22:51:53.160217",
    "approvingManager": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdAt": "2026-07-12T22:51:53.160217",
    "updatedAt": "2026-07-12T22:51:53.160217",
    "startDate": "2026-08-01",
    "endDate": "2026-08-10"
  },
  {
    "id": 4,
    "employee": {
      "id": 3,
      "username": "test_emp",
      "email": "test_emp_updated@example.com",
      "role": "EMPLOYEE"
    },
    "purpose": "Cancellation Test Trip",
    "destination": "New York",
    "departureDate": "2026-08-01",
    "returnDate": "2026-08-10",
    "travelType": "INTERNATIONAL",
    "estimatedCost": 500.0,
    "status": "CANCELLED",
    "comments": null,
    "approver": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdDate": "2026-07-12T22:51:53.7551",
    "updatedDate": "2026-07-12T22:51:53.867107",
    "approvingManager": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdAt": "2026-07-12T22:51:53.7551",
    "updatedAt": "2026-07-12T22:51:53.867107",
    "startDate": "2026-08-01",
    "endDate": "2026-08-10"
  }
]
```
- **Result**: PASS

### GET /api/trips/{id} — Get Trip Request detail by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "employee": {
    "id": 3,
    "username": "test_emp",
    "email": "test_emp_updated@example.com",
    "role": "EMPLOYEE"
  },
  "purpose": "Client Onsite Meeting",
  "destination": "New York",
  "departureDate": "2026-08-01",
  "returnDate": "2026-08-10",
  "travelType": "INTERNATIONAL",
  "estimatedCost": 600.0,
  "status": "COMPLETED",
  "comments": "Looks good",
  "approver": {
    "id": 5,
    "username": null,
    "email": null,
    "role": null
  },
  "createdDate": "2026-07-12T22:51:52.773426",
  "updatedDate": "2026-07-12T22:51:53.707674",
  "approvingManager": {
    "id": 5,
    "username": null,
    "email": null,
    "role": null
  },
  "createdAt": "2026-07-12T22:51:52.773426",
  "updatedAt": "2026-07-12T22:51:53.707674",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10"
}
```
- **Result**: PASS

### GET /api/trips — List all Trip Requests as TRAVEL_DESK (happy)
- **Role/Token used**: TRAVEL_DESK
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/trips/{id}/summary — Get Trip summary by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "tripDetails": {
    "id": 1,
    "employee": {
      "id": 3,
      "username": "test_emp",
      "email": "test_emp_updated@example.com",
      "role": "EMPLOYEE"
    },
    "purpose": "Client Onsite Meeting",
    "destination": "New York",
    "departureDate": "2026-08-01",
    "returnDate": "2026-08-10",
    "travelType": "INTERNATIONAL",
    "estimatedCost": 600.0,
    "status": "COMPLETED",
    "comments": "Looks good",
    "approver": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdDate": "2026-07-12T22:51:52.773426",
    "updatedDate": "2026-07-12T22:51:53.707674",
    "approvingManager": {
      "id": 5,
      "username": null,
      "email": null,
      "role": null
    },
    "createdAt": "2026-07-12T22:51:52.773426",
    "updatedAt": "2026-07-12T22:51:53.707674",
    "startDate": "2026-08-01",
    "endDate": "2026-08-10"
  },
  "itineraryLegs": [
    {
      "id": 1,
      "origin": "London",
      "destination": "New York",
      "legType": "FLIGHT",
      "travelDate": "2026-08-01",
      "departureDateTime": "2026-08-01T08:00:00",
      "arrivalDateTime": "2026-08-01T16:00:00",
      "carrierDetails": "British Airways BA-173",
      "bookingRef": "BKREF123",
      "cost": 550.0,
      "originalCurrency": "USD",
      "usdEquivalent": 500.0,
      "status": "CONFIRMED"
    }
  ],
  "visaDetails": [
    {
      "id": 1,
      "country": "United States",
      "visaType": "BUSINESS",
      "requiresVisa": true,
      "applicationDate": "2026-07-15",
      "submittedDate": "2026-07-16",
      "status": "GRANTED",
      "notes": "Applying for business visa",
      "createdDate": "2026-07-12T22:51:53.488517",
      "updatedDate": "2026-07-12T22:51:53.606182",
      "destinationCountry": "United States"
    }
  ],
  "totalEstimatedCost": 550.0
}
```
- **Result**: PASS

## Module E: Documents

### POST /api/documents/upload — Upload document receipt (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "filename": "test_receipt.jpg",
  "contentType": "image/jpeg",
  "path": "data\\documents\\1783876914005_test_receipt.jpg",
  "ownerId": 3,
  "createdAt": "2026-07-12T22:51:54.010912"
}
```
- **Result**: PASS

### POST /api/documents/upload — Upload document missing file (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 415
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Unsupported Media Type",
  "status": 415,
  "detail": "Content-Type 'null' is not supported.",
  "instance": "/api/documents/upload"
}
```
- **Result**: PASS

### GET /api/documents/{id} — Download/view document details (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
Dummy Receipt Content
```
- **Result**: PASS

### GET /api/documents/{id} — Download non-existent document (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 404
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/documents — List own uploaded documents (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "filename": "test_receipt.jpg",
    "contentType": "image/jpeg",
    "path": "data\\documents\\1783876914005_test_receipt.jpg",
    "ownerId": 3,
    "createdAt": "2026-07-12T22:51:54.010912"
  }
]
```
- **Result**: PASS

## Module F: Travel Advance

### POST /api/advances — Request travel advance (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "tripRequestId": 5,
  "requestedAmount": 300.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "tripRequestId": 5,
  "employeeId": 3,
  "requestedAmount": 300.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0,
  "status": "REQUESTED",
  "approvedById": null,
  "createdDate": "2026-07-12T22:51:54.2222467",
  "updatedDate": "2026-07-12T22:51:54.2228376",
  "disbursementDate": null
}
```
- **Result**: PASS

### POST /api/advances — Request advance on DRAFT trip (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "tripRequestId": 6,
  "requestedAmount": 300.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Advance request can only be created against an APPROVED trip",
  "timestamp": "2026-07-12T17:21:54.313645400Z",
  "status": 400
}
```
- **Result**: PASS

### PUT /api/advances/{id} — Update advance details (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "tripRequestId": 5,
  "requestedAmount": 350.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "tripRequestId": 5,
  "employeeId": 3,
  "requestedAmount": 350.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0,
  "status": "REQUESTED",
  "approvedById": null,
  "createdDate": "2026-07-12T22:51:54.222247",
  "updatedDate": "2026-07-12T22:51:54.3322644",
  "disbursementDate": null
}
```
- **Result**: PASS

### PUT /api/advances/{id} — Update non-existent advance (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "tripRequestId": 5,
  "requestedAmount": 350.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Advance request not found",
  "timestamp": "2026-07-12T17:21:54.352071600Z",
  "status": 400
}
```
- **Result**: PASS

### GET /api/advances/pending-approvals — Get pending advance approvals as manager (happy)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "tripRequestId": 5,
    "employeeId": 3,
    "requestedAmount": 350.0,
    "currency": "USD",
    "purposeDetails": "Hotel and food pocket allowance",
    "usdEquivalent": 300.0,
    "status": "REQUESTED",
    "approvedById": null,
    "createdDate": "2026-07-12T22:51:54.222247",
    "updatedDate": "2026-07-12T22:51:54.332264",
    "disbursementDate": null
  }
]
```
- **Result**: PASS

### POST /api/advances/{id}/approve — Approve travel advance as manager (happy)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "tripRequestId": 5,
  "employeeId": 3,
  "requestedAmount": 350.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0,
  "status": "APPROVED",
  "approvedById": 5,
  "createdDate": "2026-07-12T22:51:54.222247",
  "updatedDate": "2026-07-12T22:51:54.4003492",
  "disbursementDate": null
}
```
- **Result**: PASS

### POST /api/advances/{id}/approve — Approve advance as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/advances/1/approve",
  "timestamp": "2026-07-12T17:21:54.418174800Z"
}
```
- **Result**: PASS

### GET /api/advances/pending-disbursements — Get pending disbursements as FINANCE (happy)
- **Role/Token used**: FINANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "tripRequestId": 5,
    "employeeId": 3,
    "requestedAmount": 350.0,
    "currency": "USD",
    "purposeDetails": "Hotel and food pocket allowance",
    "usdEquivalent": 300.0,
    "status": "APPROVED",
    "approvedById": 5,
    "createdDate": "2026-07-12T22:51:54.222247",
    "updatedDate": "2026-07-12T22:51:54.400349",
    "disbursementDate": null
  }
]
```
- **Result**: PASS

### POST /api/advances/{id}/disburse — Disburse advance as FINANCE (happy)
- **Role/Token used**: FINANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "tripRequestId": 5,
  "employeeId": 3,
  "requestedAmount": 350.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0,
  "status": "DISBURSED",
  "approvedById": 5,
  "createdDate": "2026-07-12T22:51:54.222247",
  "updatedDate": "2026-07-12T22:51:54.4682994",
  "disbursementDate": "2026-07-12"
}
```
- **Result**: PASS

### POST /api/advances/{id}/disburse — Disburse advance as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/advances/1/disburse",
  "timestamp": "2026-07-12T17:21:54.485175700Z"
}
```
- **Result**: PASS

### POST /api/advances/{advanceId}/settlements — Add settlement details for advance (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "amountUtilised": 250.0,
  "amountReturned": 50.0,
  "remarks": "Utilised 250, returning 50"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "advanceRequestId": 1,
  "amountUtilised": 250.0,
  "amountReturned": 50.0,
  "settlementDate": "2026-07-12",
  "status": "PARTIALLY_SETTLED",
  "remarks": "Utilised 250, returning 50",
  "createdDate": "2026-07-12T22:51:54.5039841",
  "updatedDate": "2026-07-12T22:51:54.5039841",
  "returnedAmount": 50.0,
  "actualSpent": 250.0
}
```
- **Result**: PASS

### POST /api/advances/{advanceId}/settlements — Add settlement for non-existent advance (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "amountUtilised": 250.0,
  "amountReturned": 50.0,
  "remarks": "Utilised 250, returning 50"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Advance request not found",
  "timestamp": "2026-07-12T17:21:54.552319Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/advances/{id}/settle — Settle travel advance request (happy)
- **Role/Token used**: FINANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "amountUtilised": 250.0,
  "amountReturned": 50.0,
  "remarks": "Utilised 250, returning 50"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 2,
  "tripRequestId": 7,
  "employeeId": 3,
  "requestedAmount": 300.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0,
  "status": "SETTLED",
  "approvedById": 5,
  "createdDate": "2026-07-12T22:51:54.671851",
  "updatedDate": "2026-07-12T22:51:54.818563",
  "disbursementDate": "2026-07-12"
}
```
- **Result**: PASS

### POST /api/advances/{id}/forfeit — Forfeit travel advance as FINANCE (happy)
- **Role/Token used**: FINANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 3,
  "tripRequestId": 8,
  "employeeId": 3,
  "requestedAmount": 100.0,
  "currency": "USD",
  "purposeDetails": "Pocket money",
  "usdEquivalent": 100.0,
  "status": "FORFEITED",
  "approvedById": 5,
  "createdDate": "2026-07-12T22:51:54.941401",
  "updatedDate": "2026-07-12T22:51:55.0832949",
  "disbursementDate": "2026-07-12"
}
```
- **Result**: PASS

### POST /api/advances/{id}/forfeit — Forfeit advance as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/advances/1/forfeit",
  "timestamp": "2026-07-12T17:21:55.103204700Z"
}
```
- **Result**: PASS

### GET /api/advances — List all travel advances as FINANCE (happy)
- **Role/Token used**: FINANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
  None
- **Result**: PASS

### GET /api/advances/{id} — Get advance details by ID (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "tripRequestId": 5,
  "employeeId": 3,
  "requestedAmount": 350.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0,
  "status": "SETTLED",
  "approvedById": 5,
  "createdDate": "2026-07-12T22:51:54.222247",
  "updatedDate": "2026-07-12T22:51:54.527316",
  "disbursementDate": "2026-07-12"
}
```
- **Result**: PASS

### GET /api/advances/{advanceId}/settlements — List settlements for advance (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "advanceRequestId": 1,
    "amountUtilised": 250.0,
    "amountReturned": 50.0,
    "settlementDate": "2026-07-12",
    "status": "PARTIALLY_SETTLED",
    "remarks": "Utilised 250, returning 50",
    "createdDate": "2026-07-12T22:51:54.503984",
    "updatedDate": "2026-07-12T22:51:54.503984",
    "returnedAmount": 50.0,
    "actualSpent": 250.0
  }
]
```
- **Result**: PASS

### GET /api/settlements/{id} — Get specific settlement detail (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 500
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/internal-server-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "could not initialize proxy [com.journeyplus.advance.entity.AdvanceRequest#1] - no Session",
  "instance": "/api/settlements/1",
  "timestamp": "2026-07-12T17:21:55.164471600Z"
}
```
- **Result**: PASS

### PUT /api/settlements/{id} — Update specific settlement details (happy)
- **Role/Token used**: FINANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "amountUtilised": 250.0,
  "amountReturned": 50.0,
  "remarks": "Utilised 250, returning 50"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "advanceRequestId": 1,
  "amountUtilised": 250.0,
  "amountReturned": 50.0,
  "settlementDate": "2026-07-12",
  "status": "PARTIALLY_SETTLED",
  "remarks": "Utilised 250, returning 50",
  "createdDate": "2026-07-12T22:51:54.503984",
  "updatedDate": "2026-07-12T22:51:54.503984",
  "returnedAmount": 50.0,
  "actualSpent": 250.0
}
```
- **Result**: PASS

### GET /api/advances/my-advances — List own advances (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "tripRequestId": 5,
    "employeeId": 3,
    "requestedAmount": 350.0,
    "currency": "USD",
    "purposeDetails": "Hotel and food pocket allowance",
    "usdEquivalent": 300.0,
    "status": "SETTLED",
    "approvedById": 5,
    "createdDate": "2026-07-12T22:51:54.222247",
    "updatedDate": "2026-07-12T22:51:54.527316",
    "disbursementDate": "2026-07-12"
  },
  {
    "id": 2,
    "tripRequestId": 7,
    "employeeId": 3,
    "requestedAmount": 300.0,
    "currency": "USD",
    "purposeDetails": "Hotel and food pocket allowance",
    "usdEquivalent": 300.0,
    "status": "SETTLED",
    "approvedById": 5,
    "createdDate": "2026-07-12T22:51:54.671851",
    "updatedDate": "2026-07-12T22:51:54.818563",
    "disbursementDate": "2026-07-12"
  },
  {
    "id": 3,
    "tripRequestId": 8,
    "employeeId": 3,
    "requestedAmount": 100.0,
    "currency": "USD",
    "purposeDetails": "Pocket money",
    "usdEquivalent": 100.0,
    "status": "FORFEITED",
    "approvedById": 5,
    "createdDate": "2026-07-12T22:51:54.941401",
    "updatedDate": "2026-07-12T22:51:55.083295",
    "disbursementDate": "2026-07-12"
  }
]
```
- **Result**: PASS

### GET /api/advances/{id}/summary — Get advance summary (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "advanceDetails": {
    "id": 1,
    "tripRequestId": 5,
    "employeeId": 3,
    "requestedAmount": 350.0,
    "currency": "USD",
    "purposeDetails": "Hotel and food pocket allowance",
    "usdEquivalent": 300.0,
    "status": "SETTLED",
    "approvedById": 5,
    "createdDate": "2026-07-12T22:51:54.222247",
    "updatedDate": "2026-07-12T22:51:54.527316",
    "disbursementDate": "2026-07-12"
  },
  "settlementDetails": [
    {
      "id": 1,
      "advanceRequestId": 1,
      "amountUtilised": 250.0,
      "amountReturned": 50.0,
      "settlementDate": "2026-07-12",
      "status": "PARTIALLY_SETTLED",
      "remarks": "Utilised 250, returning 50",
      "createdDate": "2026-07-12T22:51:54.503984",
      "updatedDate": "2026-07-12T22:51:54.503984",
      "returnedAmount": 50.0,
      "actualSpent": 250.0
    }
  ],
  "totalUtilisedAmount": 250.0,
  "totalReturnedAmount": 50.0,
  "outstandingAmount": 50.0,
  "currentStatus": "SETTLED"
}
```
- **Result**: PASS

## Module G: Expense Claim & Reimbursement

### POST /api/expenses — Create Expense Claim (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-08-11",
  "totalAmount": 0.0,
  "originalCurrency": "USD",
  "expenseLines": []
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-08-11",
  "totalAmount": 0,
  "originalCurrency": "USD",
  "usdEquivalent": 0,
  "advanceAdjusted": 0.0,
  "netReimbursable": 0.0,
  "status": "DRAFT",
  "managerComments": null,
  "financeComments": null,
  "approver": null,
  "tripRequestId": 1,
  "employeeId": 3
}
```
- **Result**: PASS

### POST /api/expenses — Create Expense Claim on DRAFT trip (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-08-11",
  "totalAmount": 0.0,
  "originalCurrency": "USD",
  "expenseLines": []
}
```
- **Response Status**: 409
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/invalid-state",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Expense claims can only be raised against COMPLETED trip requests",
  "instance": "/api/expenses",
  "timestamp": "2026-07-12T17:21:55.281930300Z"
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/lines — Add compliant expense line to claim (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "expenseDate": "2026-08-02",
  "category": "MEALS",
  "amount": 80.0,
  "originalCurrency": "USD",
  "receiptPath": "/receipts/meal1.jpg"
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "expenseDate": "2026-08-02",
  "category": "MEALS",
  "amount": 80.0,
  "originalCurrency": "USD",
  "usdEquivalent": 80.0,
  "receiptPath": "/receipts/meal1.jpg",
  "policyComplianceStatus": "COMPLIANT",
  "complianceRemarks": "Automated check passed. All limits satisfied.",
  "policyCompliant": true,
  "status": "INCLUDED"
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/lines — Add expense line to non-existent claim (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "expenseDate": "2026-08-02",
  "category": "MEALS",
  "amount": 80.0,
  "originalCurrency": "USD",
  "receiptPath": "/receipts/meal1.jpg"
}
```
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Expense claim not found",
  "timestamp": "2026-07-12T17:21:55.396674600Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/lines/{lineId}/submit — Submit expense line for compliance check (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "expenseDate": "2026-08-02",
  "category": "MEALS",
  "amount": 80.0,
  "originalCurrency": "USD",
  "usdEquivalent": 80.0,
  "receiptPath": "/receipts/meal1.jpg",
  "policyComplianceStatus": "COMPLIANT",
  "complianceRemarks": "Automated check passed. All limits satisfied.",
  "policyCompliant": true,
  "status": "INCLUDED"
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/lines/{lineId}/submit — Submit non-existent expense line (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Expense line not found",
  "timestamp": "2026-07-12T17:21:55.448249500Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/submit — Submit entire Expense Claim (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-07-12",
  "totalAmount": 80.0,
  "originalCurrency": "USD",
  "usdEquivalent": 80.0,
  "advanceAdjusted": 0.0,
  "netReimbursable": 80.0,
  "status": "SUBMITTED",
  "managerComments": null,
  "financeComments": null,
  "approver": null,
  "tripRequestId": 1,
  "employeeId": 3
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/submit — Submit already submitted claim (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 409
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/invalid-state",
  "title": "Invalid State Transition",
  "status": 409,
  "detail": "Only DRAFT claims can be submitted",
  "instance": "/api/expenses/1/submit",
  "timestamp": "2026-07-12T17:21:55.538250900Z"
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/approve — Approve Expense Claim as manager (happy)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-07-12",
  "totalAmount": 80.0,
  "originalCurrency": "USD",
  "usdEquivalent": 80.0,
  "advanceAdjusted": 0.0,
  "netReimbursable": 80.0,
  "status": "APPROVED",
  "managerComments": "Approved claim",
  "financeComments": null,
  "approver": null,
  "tripRequestId": 1,
  "employeeId": 3
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/approve — Approve claim as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/expenses/1/approve",
  "timestamp": "2026-07-12T17:21:55.590442300Z"
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/reject — Reject Expense Claim as manager (happy)
- **Role/Token used**: APPROVING_MANAGER
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 2,
  "claimTitle": "Claim Reject Test",
  "submittedDate": "2026-07-12",
  "totalAmount": 80.0,
  "originalCurrency": "USD",
  "usdEquivalent": 80.0,
  "advanceAdjusted": 0.0,
  "netReimbursable": 80.0,
  "status": "REJECTED",
  "managerComments": "Denied",
  "financeComments": null,
  "approver": null,
  "tripRequestId": 9,
  "employeeId": 3
}
```
- **Result**: PASS

### POST /api/expenses/{claim_rej_id}/reject — Reject claim as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/expenses/2/reject",
  "timestamp": "2026-07-12T17:21:55.919497700Z"
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/reimburse — Disburse reimbursement as FINANCE (happy)
- **Role/Token used**: FINANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "paymentMethod": "BANK_TRANSFER",
  "transactionReference": "TXN987654",
  "amount": 80.0
}
```
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-07-12",
  "totalAmount": 80.0,
  "originalCurrency": "USD",
  "usdEquivalent": 80.0,
  "advanceAdjusted": 0.0,
  "netReimbursable": 80.0,
  "status": "PAID",
  "managerComments": "Approved claim",
  "financeComments": null,
  "approver": null,
  "tripRequestId": 1,
  "employeeId": 3
}
```
- **Result**: PASS

### POST /api/expenses/{claimId}/reimburse — Disburse reimbursement as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
```json
{
  "paymentMethod": "BANK_TRANSFER",
  "transactionReference": "TXN987654",
  "amount": 80.0
}
```
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/expenses/1/reimburse",
  "timestamp": "2026-07-12T17:21:55.984085300Z"
}
```
- **Result**: PASS

### GET /api/expenses/my-claims — List own claims (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "claimTitle": "Trip 1 Expenses Claim",
    "submittedDate": "2026-07-12",
    "totalAmount": 80.0,
    "originalCurrency": "USD",
    "usdEquivalent": 80.0,
    "advanceAdjusted": 0.0,
    "netReimbursable": 80.0,
    "status": "PAID",
    "managerComments": "Approved claim",
    "financeComments": null,
    "approver": null,
    "tripRequestId": 1,
    "employeeId": 3
  },
  {
    "id": 2,
    "claimTitle": "Claim Reject Test",
    "submittedDate": "2026-07-12",
    "totalAmount": 80.0,
    "originalCurrency": "USD",
    "usdEquivalent": 80.0,
    "advanceAdjusted": 0.0,
    "netReimbursable": 80.0,
    "status": "REJECTED",
    "managerComments": "Denied",
    "financeComments": null,
    "approver": null,
    "tripRequestId": 9,
    "employeeId": 3
  }
]
```
- **Result**: PASS

### GET /api/expenses/{claimId} — Get specific claim details (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-07-12",
  "totalAmount": 80.0,
  "originalCurrency": "USD",
  "usdEquivalent": 80.0,
  "advanceAdjusted": 0.0,
  "netReimbursable": 80.0,
  "status": "PAID",
  "managerComments": "Approved claim",
  "financeComments": null,
  "approver": null,
  "tripRequestId": 1,
  "employeeId": 3
}
```
- **Result**: PASS

### GET /api/expenses/{claimId}/lines — List lines for a claim (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "expenseDate": "2026-08-02",
    "category": "MEALS",
    "amount": 80.0,
    "originalCurrency": "USD",
    "usdEquivalent": 80.0,
    "receiptPath": "/receipts/meal1.jpg",
    "policyComplianceStatus": "COMPLIANT",
    "complianceRemarks": "Automated check passed. All limits satisfied.",
    "policyCompliant": true,
    "status": "INCLUDED"
  },
  {
    "id": 2,
    "expenseDate": "2026-08-03",
    "category": "MEALS",
    "amount": 400.0,
    "originalCurrency": "USD",
    "usdEquivalent": 400.0,
    "receiptPath": "/receipts/meal_large.jpg",
    "policyComplianceStatus": "NON_COMPLIANT",
    "complianceRemarks": "Policy per diem limit of 300.0 USD exceeded by 100.00 USD. ",
    "policyCompliant": false,
    "status": "FLAGGED"
  }
]
```
- **Result**: PASS

## Module H: Policy Compliance & Exception

### GET /api/compliance/exceptions — List all exceptions as COMPLIANCE (happy)
- **Role/Token used**: COMPLIANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 500
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Failed to write request",
  "instance": "/api/compliance/exceptions"
}
```
- **Result**: PASS

### GET /api/compliance/exceptions — List exceptions as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/compliance/exceptions",
  "timestamp": "2026-07-12T17:21:56.105317300Z"
}
```
- **Result**: PASS

### GET /api/compliance/exceptions?status=PENDING — List pending exceptions (happy)
- **Role/Token used**: COMPLIANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 500
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Failed to write request",
  "instance": "/api/compliance/exceptions"
}
```
- **Result**: PASS

### POST /api/compliance/exceptions/{id}/resolve — Resolve exception as COMPLIANCE
- **Role/Token used**: COMPLIANCE
- **Preconditions**: N/A
- **Request Headers**:
  None
- **Request Body (JSON)**:
  None
- **Response Status**: None
- **Response Body (JSON)**:
  None
- **Result**: SKIPPED — No exception ID generated

### POST /api/compliance/exceptions/{id}/resolve — Resolve non-existent exception (edge)
- **Role/Token used**: COMPLIANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "message": "Exception not found",
  "timestamp": "2026-07-12T17:21:56.135821500Z",
  "status": 400
}
```
- **Result**: PASS

### POST /api/compliance/claims/{claimId}/audit — Audit claim as COMPLIANCE (happy)
- **Role/Token used**: COMPLIANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 6,
  "expenseLine": null,
  "claim": {
    "id": 1,
    "claimTitle": "Trip 1 Expenses Claim",
    "submittedDate": "2026-07-12",
    "totalAmount": 80.0,
    "originalCurrency": "USD",
    "usdEquivalent": 80.0,
    "advanceAdjusted": 0.0,
    "netReimbursable": 80.0,
    "status": "PAID",
    "managerComments": "Approved claim",
    "financeComments": null,
    "approver": null,
    "tripRequestId": 1,
    "employeeId": 3
  },
  "auditor": {
    "id": 7,
    "username": "test_comp",
    "email": "test_comp@example.com",
    "role": "COMPLIANCE",
    "name": "Test COMPLIANCE",
    "phone": "+1234567890",
    "departmentId": "DEPT-GEN",
    "department": "DEPT-GEN",
    "grade": {
      "id": "G3",
      "gradeName": "Manager",
      "description": "Mid-level manager",
      "createdDate": "2026-07-12T22:50:54.839248",
      "updatedDate": "2026-07-12T22:50:54.839248",
      "status": "Active"
    },
    "delegationStart": null,
    "delegationEnd": null,
    "active": true,
    "createdDate": "2026-07-12T22:51:47.845081",
    "updatedDate": "2026-07-12T22:51:47.910383",
    "status": "Active",
    "delegationActive": false,
    "createdAt": "2026-07-12T22:51:47.845081",
    "updatedAt": "2026-07-12T22:51:47.910383"
  },
  "auditDate": "2026-07-12T22:51:56.1517055",
  "complianceStatus": "PASSED",
  "violationsFound": null,
  "auditNotes": null,
  "findings": "Clean claim",
  "auditOutcome": "Clean",
  "status": "Completed"
}
```
- **Result**: PASS

### POST /api/compliance/claims/{claimId}/audit — Audit claim as EMPLOYEE (edge)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access Denied",
  "instance": "/api/compliance/claims/1/audit",
  "timestamp": "2026-07-12T17:21:56.181288600Z"
}
```
- **Result**: PASS

## Module I: Analytics & Reporting

### POST /api/reports — Generate custom travel report (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 1,
  "title": "Q3_Travel_Report",
  "reportType": "TravelSummary",
  "parameters": null,
  "generatedBy": "admin1",
  "generatedAt": "2026-07-12T22:51:56.1954071",
  "filePath": "c:/journeyplus/reports/report_travelsummary_2b60ff6e.csv",
  "scope": null,
  "scopeValue": null,
  "tripCount": null,
  "totalSpend": null,
  "avgCostPerTrip": null,
  "advanceSettlementRate": null,
  "policyExceptionRate": null,
  "budgetUtilisation": null,
  "generatedDate": "2026-07-12T22:51:56.1954071"
}
```
- **Result**: PASS

### POST /api/reports — Generate report missing title (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Required parameter 'title' is not present.",
  "instance": "/api/reports"
}
```
- **Result**: PASS

### POST /api/reports/metrics — Generate metrics report (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "id": 2,
  "title": "Travel Spend Report - Department (DEPT-GEN)",
  "reportType": "METRICS_DEPARTMENT",
  "parameters": "scopeValue=DEPT-GEN",
  "generatedBy": "admin1",
  "generatedAt": "2026-07-12T22:51:56.2731593",
  "filePath": "N/A - Database Record",
  "scope": "Department",
  "scopeValue": "DEPT-GEN",
  "tripCount": 9,
  "totalSpend": 80.0,
  "avgCostPerTrip": 8.89,
  "advanceSettlementRate": 0.7738,
  "policyExceptionRate": 0.3333,
  "budgetUtilisation": 0.0008,
  "generatedDate": "2026-07-12T22:51:56.2731593"
}
```
- **Result**: PASS

### POST /api/reports/metrics — Generate metrics with invalid scope (edge)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 400
- **Response Body (JSON)**:
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Failed to convert 'scope' with value: 'INVALID'",
  "instance": "/api/reports/metrics"
}
```
- **Result**: PASS

### GET /api/reports/top-travellers — Get top travellers (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 500
- **Response Body (JSON)**:
```json
{
  "type": "https://journeyplus.com/errors/internal-server-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "could not initialize proxy [com.journeyplus.iam.entity.User#3] - no Session",
  "instance": "/api/reports/top-travellers",
  "timestamp": "2026-07-12T17:21:56.316899700Z"
}
```
- **Result**: PASS

### GET /api/reports — List all reports (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "title": "Q3_Travel_Report",
    "reportType": "TravelSummary",
    "parameters": null,
    "generatedBy": "admin1",
    "generatedAt": "2026-07-12T22:51:56.195407",
    "filePath": "c:/journeyplus/reports/report_travelsummary_2b60ff6e.csv",
    "scope": null,
    "scopeValue": null,
    "tripCount": null,
    "totalSpend": null,
    "avgCostPerTrip": null,
    "advanceSettlementRate": null,
    "policyExceptionRate": null,
    "budgetUtilisation": null,
    "generatedDate": "2026-07-12T22:51:56.195407"
  },
  {
    "id": 2,
    "title": "Travel Spend Report - Department (DEPT-GEN)",
    "reportType": "METRICS_DEPARTMENT",
    "parameters": "scopeValue=DEPT-GEN",
    "generatedBy": "admin1",
    "generatedAt": "2026-07-12T22:51:56.273159",
    "filePath": "N/A - Database Record",
    "scope": "Department",
    "scopeValue": "DEPT-GEN",
    "tripCount": 9,
    "totalSpend": 80.0,
    "avgCostPerTrip": 8.89,
    "advanceSettlementRate": 0.77,
    "policyExceptionRate": 0.33,
    "budgetUtilisation": 0.0,
    "generatedDate": "2026-07-12T22:51:56.273159"
  }
]
```
- **Result**: PASS

### GET /api/reports/type/{type} — List reports by type (happy)
- **Role/Token used**: ADMIN
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "title": "Q3_Travel_Report",
    "reportType": "TravelSummary",
    "parameters": null,
    "generatedBy": "admin1",
    "generatedAt": "2026-07-12T22:51:56.195407",
    "filePath": "c:/journeyplus/reports/report_travelsummary_2b60ff6e.csv",
    "scope": null,
    "scopeValue": null,
    "tripCount": null,
    "totalSpend": null,
    "avgCostPerTrip": null,
    "advanceSettlementRate": null,
    "policyExceptionRate": null,
    "budgetUtilisation": null,
    "generatedDate": "2026-07-12T22:51:56.195407"
  }
]
```
- **Result**: PASS

## Module J: Notifications

### GET /api/notifications — Get notifications for employee (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "message": "Notifications retrieved",
  "count": 41,
  "notifications": [
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.955955",
      "read": false,
      "id": 77,
      "notificationType": "IN_APP",
      "title": "Expense Claim PAID",
      "message": "Your expense claim 'Trip 1 Expenses Claim' has been paid via BANK_TRANSFER. Status: paid",
      "category": "ExpenseClaim",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:55.892425",
      "read": false,
      "id": 76,
      "notificationType": "IN_APP",
      "title": "Expense Claim REJECTED",
      "message": "Your expense claim 'Claim Reject Test' has been rejected.",
      "category": "ExpenseClaim",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.730541",
      "read": false,
      "id": 74,
      "notificationType": "IN_APP",
      "title": "Trip Request COMPLETED",
      "message": "Your trip request to New York is now completed.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:55.695225",
      "read": false,
      "id": 73,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:55.660963",
      "read": false,
      "id": 72,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:55.55884",
      "read": false,
      "id": 70,
      "notificationType": "IN_APP",
      "title": "Expense Claim APPROVED",
      "message": "Your expense claim 'Trip 1 Expenses Claim' has been approved.",
      "category": "ExpenseClaim",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "System Automated Compliance"
      },
      "createdAt": "2026-07-12T22:51:55.476803",
      "read": false,
      "id": 68,
      "notificationType": "IN_APP",
      "title": "Compliance Policy Exception Flagged",
      "message": "A policy compliance exception has been flagged on your expense claim: Policy per diem limit of 300.0 USD exceeded by 100.00 USD. ",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "System Automated Compliance"
      },
      "createdAt": "2026-07-12T22:51:55.364969",
      "read": false,
      "id": 67,
      "notificationType": "IN_APP",
      "title": "Compliance Policy Exception Flagged",
      "message": "A policy compliance exception has been flagged on your expense claim: Policy per diem limit of 300.0 USD exceeded by 100.00 USD. ",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.065869",
      "read": false,
      "id": 64,
      "notificationType": "IN_APP",
      "title": "Advance Request Forfeited",
      "message": "Your cash advance request of 100.0 USD has been forfeited.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.023843",
      "read": false,
      "id": 61,
      "notificationType": "IN_APP",
      "title": "Advance Disbursed",
      "message": "Your cash advance of 100.0 USD has been disbursed to your account.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.984163",
      "read": false,
      "id": 58,
      "notificationType": "IN_APP",
      "title": "Advance Request Approved",
      "message": "Your cash advance request for 100.0 USD has been approved by test_mgr.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.947663",
      "read": false,
      "id": 55,
      "notificationType": "IN_APP",
      "title": "Advance Request Submitted",
      "message": "An advance request of 100.0 USD has been submitted.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.910828",
      "read": false,
      "id": 53,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:54.881961",
      "read": false,
      "id": 52,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.806712",
      "read": false,
      "id": 48,
      "notificationType": "IN_APP",
      "title": "Advance Request Settled",
      "message": "Your cash advance of 300.0 USD has been successfully settled.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.753935",
      "read": false,
      "id": 45,
      "notificationType": "IN_APP",
      "title": "Advance Disbursed",
      "message": "Your cash advance of 300.0 USD has been disbursed to your account.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.718052",
      "read": false,
      "id": 42,
      "notificationType": "IN_APP",
      "title": "Advance Request Approved",
      "message": "Your cash advance request for 300.0 USD has been approved by test_mgr.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.678469",
      "read": false,
      "id": 39,
      "notificationType": "IN_APP",
      "title": "Advance Request Submitted",
      "message": "An advance request of 300.0 USD has been submitted.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.636917",
      "read": false,
      "id": 37,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:54.607256",
      "read": false,
      "id": 36,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.512299",
      "read": false,
      "id": 32,
      "notificationType": "IN_APP",
      "title": "Advance Request Settled",
      "message": "Your cash advance of 350.0 USD has been successfully settled.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.447299",
      "read": false,
      "id": 29,
      "notificationType": "IN_APP",
      "title": "Advance Disbursed",
      "message": "Your cash advance of 350.0 USD has been disbursed to your account.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.385076",
      "read": false,
      "id": 26,
      "notificationType": "IN_APP",
      "title": "Advance Request Approved",
      "message": "Your cash advance request for 350.0 USD has been approved by test_mgr.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.231584",
      "read": false,
      "id": 23,
      "notificationType": "IN_APP",
      "title": "Advance Request Submitted",
      "message": "An advance request of 300.0 USD has been submitted.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.187084",
      "read": false,
      "id": 21,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:54.153874",
      "read": false,
      "id": 20,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:53.860182",
      "read": false,
      "id": 18,
      "notificationType": "IN_APP",
      "title": "Trip Request CANCELLED",
      "message": "Your trip request to New York is now cancelled.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:53.824284",
      "read": false,
      "id": 17,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:53.790699",
      "read": false,
      "id": 16,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:53.699798",
      "read": false,
      "id": 14,
      "notificationType": "IN_APP",
      "title": "Trip Request COMPLETED",
      "message": "Your trip request to New York is now completed.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.65027",
      "read": false,
      "id": 13,
      "notificationType": "IN_APP",
      "title": "Visa Requirement Updated",
      "message": "A visa requirement for United States has been updated on your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.603528",
      "read": false,
      "id": 12,
      "notificationType": "IN_APP",
      "title": "Visa Requirement Updated",
      "message": "A visa requirement for United States has been updated on your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.491741",
      "read": false,
      "id": 11,
      "notificationType": "IN_APP",
      "title": "Visa Requirement Added",
      "message": "A visa requirement for United States has been added to your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.445773",
      "read": false,
      "id": 10,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Deleted",
      "message": "An itinerary leg (London to New York) has been removed from your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.422757",
      "read": false,
      "id": 9,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Added",
      "message": "An itinerary leg (London to New York) has been added to your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.332433",
      "read": false,
      "id": 8,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Updated",
      "message": "An itinerary leg (London to New York) has been updated on your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.2206",
      "read": false,
      "id": 7,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Added",
      "message": "An itinerary leg (London to New York) has been added to your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:53.119558",
      "read": false,
      "id": 6,
      "notificationType": "IN_APP",
      "title": "Trip Request REJECTED",
      "message": "Your trip request to New York has been rejected by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:53.084421",
      "read": false,
      "id": 5,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:52.98546",
      "read": false,
      "id": 3,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:52.905556",
      "read": false,
      "id": 2,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    }
  ]
}
```
- **Result**: PASS

### GET /api/notifications/unread — Get unread notifications for employee (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "message": "Unread notifications retrieved",
  "count": 41,
  "notifications": [
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.955955",
      "read": false,
      "id": 77,
      "notificationType": "IN_APP",
      "title": "Expense Claim PAID",
      "message": "Your expense claim 'Trip 1 Expenses Claim' has been paid via BANK_TRANSFER. Status: paid",
      "category": "ExpenseClaim",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:55.892425",
      "read": false,
      "id": 76,
      "notificationType": "IN_APP",
      "title": "Expense Claim REJECTED",
      "message": "Your expense claim 'Claim Reject Test' has been rejected.",
      "category": "ExpenseClaim",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.730541",
      "read": false,
      "id": 74,
      "notificationType": "IN_APP",
      "title": "Trip Request COMPLETED",
      "message": "Your trip request to New York is now completed.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:55.695225",
      "read": false,
      "id": 73,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:55.660963",
      "read": false,
      "id": 72,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:55.55884",
      "read": false,
      "id": 70,
      "notificationType": "IN_APP",
      "title": "Expense Claim APPROVED",
      "message": "Your expense claim 'Trip 1 Expenses Claim' has been approved.",
      "category": "ExpenseClaim",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "System Automated Compliance"
      },
      "createdAt": "2026-07-12T22:51:55.476803",
      "read": false,
      "id": 68,
      "notificationType": "IN_APP",
      "title": "Compliance Policy Exception Flagged",
      "message": "A policy compliance exception has been flagged on your expense claim: Policy per diem limit of 300.0 USD exceeded by 100.00 USD. ",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "System Automated Compliance"
      },
      "createdAt": "2026-07-12T22:51:55.364969",
      "read": false,
      "id": 67,
      "notificationType": "IN_APP",
      "title": "Compliance Policy Exception Flagged",
      "message": "A policy compliance exception has been flagged on your expense claim: Policy per diem limit of 300.0 USD exceeded by 100.00 USD. ",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.065869",
      "read": false,
      "id": 64,
      "notificationType": "IN_APP",
      "title": "Advance Request Forfeited",
      "message": "Your cash advance request of 100.0 USD has been forfeited.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:55.023843",
      "read": false,
      "id": 61,
      "notificationType": "IN_APP",
      "title": "Advance Disbursed",
      "message": "Your cash advance of 100.0 USD has been disbursed to your account.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.984163",
      "read": false,
      "id": 58,
      "notificationType": "IN_APP",
      "title": "Advance Request Approved",
      "message": "Your cash advance request for 100.0 USD has been approved by test_mgr.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.947663",
      "read": false,
      "id": 55,
      "notificationType": "IN_APP",
      "title": "Advance Request Submitted",
      "message": "An advance request of 100.0 USD has been submitted.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.910828",
      "read": false,
      "id": 53,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:54.881961",
      "read": false,
      "id": 52,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.806712",
      "read": false,
      "id": 48,
      "notificationType": "IN_APP",
      "title": "Advance Request Settled",
      "message": "Your cash advance of 300.0 USD has been successfully settled.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.753935",
      "read": false,
      "id": 45,
      "notificationType": "IN_APP",
      "title": "Advance Disbursed",
      "message": "Your cash advance of 300.0 USD has been disbursed to your account.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.718052",
      "read": false,
      "id": 42,
      "notificationType": "IN_APP",
      "title": "Advance Request Approved",
      "message": "Your cash advance request for 300.0 USD has been approved by test_mgr.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.678469",
      "read": false,
      "id": 39,
      "notificationType": "IN_APP",
      "title": "Advance Request Submitted",
      "message": "An advance request of 300.0 USD has been submitted.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.636917",
      "read": false,
      "id": 37,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:54.607256",
      "read": false,
      "id": 36,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.512299",
      "read": false,
      "id": 32,
      "notificationType": "IN_APP",
      "title": "Advance Request Settled",
      "message": "Your cash advance of 350.0 USD has been successfully settled.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.447299",
      "read": false,
      "id": 29,
      "notificationType": "IN_APP",
      "title": "Advance Disbursed",
      "message": "Your cash advance of 350.0 USD has been disbursed to your account.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.385076",
      "read": false,
      "id": 26,
      "notificationType": "IN_APP",
      "title": "Advance Request Approved",
      "message": "Your cash advance request for 350.0 USD has been approved by test_mgr.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:54.231584",
      "read": false,
      "id": 23,
      "notificationType": "IN_APP",
      "title": "Advance Request Submitted",
      "message": "An advance request of 300.0 USD has been submitted.",
      "category": "Advance",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:54.187084",
      "read": false,
      "id": 21,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:54.153874",
      "read": false,
      "id": 20,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:53.860182",
      "read": false,
      "id": 18,
      "notificationType": "IN_APP",
      "title": "Trip Request CANCELLED",
      "message": "Your trip request to New York is now cancelled.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:53.824284",
      "read": false,
      "id": 17,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:53.790699",
      "read": false,
      "id": 16,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": null
      },
      "createdAt": "2026-07-12T22:51:53.699798",
      "read": false,
      "id": 14,
      "notificationType": "IN_APP",
      "title": "Trip Request COMPLETED",
      "message": "Your trip request to New York is now completed.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.65027",
      "read": false,
      "id": 13,
      "notificationType": "IN_APP",
      "title": "Visa Requirement Updated",
      "message": "A visa requirement for United States has been updated on your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.603528",
      "read": false,
      "id": 12,
      "notificationType": "IN_APP",
      "title": "Visa Requirement Updated",
      "message": "A visa requirement for United States has been updated on your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.491741",
      "read": false,
      "id": 11,
      "notificationType": "IN_APP",
      "title": "Visa Requirement Added",
      "message": "A visa requirement for United States has been added to your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.445773",
      "read": false,
      "id": 10,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Deleted",
      "message": "An itinerary leg (London to New York) has been removed from your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.422757",
      "read": false,
      "id": 9,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Added",
      "message": "An itinerary leg (London to New York) has been added to your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.332433",
      "read": false,
      "id": 8,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Updated",
      "message": "An itinerary leg (London to New York) has been updated on your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "id": null,
        "username": "Travel Desk"
      },
      "createdAt": "2026-07-12T22:51:53.2206",
      "read": false,
      "id": 7,
      "notificationType": "IN_APP",
      "title": "Itinerary Leg Added",
      "message": "An itinerary leg (London to New York) has been added to your trip.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:53.119558",
      "read": false,
      "id": 6,
      "notificationType": "IN_APP",
      "title": "Trip Request REJECTED",
      "message": "Your trip request to New York has been rejected by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:53.084421",
      "read": false,
      "id": 5,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:46.160358",
        "role": "APPROVING_MANAGER",
        "active": true,
        "id": 5,
        "department": "DEPT-GEN",
        "email": "test_mgr@example.com",
        "username": "test_mgr",
        "updatedAt": "2026-07-12T22:51:46.225426"
      },
      "createdAt": "2026-07-12T22:51:52.98546",
      "read": false,
      "id": 3,
      "notificationType": "IN_APP",
      "title": "Trip Request APPROVED",
      "message": "Your trip request to New York has been approved by test_mgr.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    },
    {
      "actor": {
        "createdAt": "2026-07-12T22:51:44.392502",
        "role": "EMPLOYEE",
        "active": true,
        "id": 3,
        "department": "DEPT-GEN",
        "email": "test_emp_updated@example.com",
        "username": "test_emp",
        "updatedAt": "2026-07-12T22:51:50.529679"
      },
      "createdAt": "2026-07-12T22:51:52.905556",
      "read": false,
      "id": 2,
      "notificationType": "IN_APP",
      "title": "Trip Request Submitted",
      "message": "Your trip request to New York has been successfully submitted.",
      "category": "TripRequest",
      "user": {
        "id": null,
        "username": null
      },
      "status": "Unread"
    }
  ]
}
```
- **Result**: PASS

### POST /api/notifications/{id}/read — Mark notification as read (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "notification": {
    "read": true,
    "id": 77,
    "title": "Expense Claim PAID",
    "message": "Your expense claim 'Trip 1 Expenses Claim' has been paid via BANK_TRANSFER. Status: paid",
    "category": "ExpenseClaim",
    "status": "Read"
  },
  "status": "Notification marked as read"
}
```
- **Result**: PASS

### POST /api/notifications/{id}/read — Mark other user's notification as read (edge)
- **Role/Token used**: COMPLIANCE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 403
- **Response Body (JSON)**:
```json
{
  "message": "You are not authorized to mark this notification as read",
  "status": "error"
}
```
- **Result**: PASS

### POST /api/notifications/{id}/dismiss — Dismiss notification (happy)
- **Role/Token used**: EMPLOYEE
- **Preconditions**: None
- **Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer eyJhbGciOi..."
}
```
- **Request Body (JSON)**:
  None
- **Response Status**: 200
- **Response Body (JSON)**:
```json
{
  "notification": {
    "read": false,
    "id": 77,
    "title": "Expense Claim PAID",
    "message": "Your expense claim 'Trip 1 Expenses Claim' has been paid via BANK_TRANSFER. Status: paid",
    "category": "ExpenseClaim",
    "status": "Dismissed"
  },
  "status": "Notification dismissed"
}
```
- **Result**: PASS

## Issues Found

Several critical API inconsistencies and server-side errors were uncovered during the end-to-end test execution:

### 1. JPA LazyInitializationException (no Session)
Multiple GET endpoints fail with an HTTP 500 Internal Server Error due to Hibernate trying to serialize lazily-loaded associations outside of an active JPA session or transaction context:
- **`GET /api/itinerary/{legId}`**: Throws `could not initialize proxy [com.journeyplus.trip.entity.TripRequest#1] - no Session` when attempting to serialize the `TripRequest` proxy associated with the itinerary leg.
- **`GET /api/visa/{visaId}`**: Throws `could not initialize proxy [com.journeyplus.trip.entity.TripRequest#1] - no Session` when attempting to serialize the `TripRequest` proxy associated with the visa requirement.
- **`GET /api/settlements/{id}`**: Throws `could not initialize proxy [com.journeyplus.advance.entity.AdvanceRequest#1] - no Session` when attempting to serialize the `AdvanceRequest` proxy associated with the settlement.
- **`GET /api/reports/top-travellers`**: Throws `could not initialize proxy [com.journeyplus.iam.entity.User#3] - no Session` when attempting to serialize the `User` object (employee) associated with the metrics report.

### 2. Jackson Serialization Error during JSON rendering
- **`GET /api/compliance/exceptions`** and **`GET /api/compliance/exceptions?status=PENDING`**: Return HTTP 500 with detail `Failed to write request` because Jackson serialization fails on lazy proxy instances inside the returned collection.

### 3. API Error Status Code Inconsistencies
The API uses `400 Bad Request` instead of `404 Not Found` for resource lookup failures across multiple controllers:
- **`GET /api/grades/{id}`** (non-existent ID): Returns `400 Bad Request` instead of `404 Not Found`.
- **`GET /api/city-tiers/{id}`** (non-existent ID): Returns `400 Bad Request` instead of `404 Not Found`.
- **`GET /api/travel-policies/{id}`** (non-existent ID): Returns `400 Bad Request` instead of `404 Not Found`.
- **`GET /api/itinerary/{legId}`** (non-existent ID): Returns `400 Bad Request` instead of `404 Not Found`.
- **`GET /api/visa/{visaId}`** (non-existent ID): Returns `400 Bad Request` instead of `404 Not Found`.
- **`PUT /api/advances/{id}`** (non-existent ID): Returns `400 Bad Request` instead of `404 Not Found`.
- **`POST /api/advances/{advanceId}/settlements`** (non-existent ID): Returns `400 Bad Request` instead of `404 Not Found`.

