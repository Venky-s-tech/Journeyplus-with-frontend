# JourneyPlus API Flow Reference

## Module A: IAM & Admin

### POST /api/auth/register
**Input:**
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
**Response:**
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

### POST /api/auth/login
**Input:**
```json
{
  "username": "test_emp",
  "password": "Password@123"
}
```
**Response:**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9.eyJncmFkZUlkIjoiRzIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJ1c2VySWQiOjMsInN1YiI6InRlc3RfZW1wIiwiaWF0IjoxNzgzODc2OTA5LCJleHAiOjE3ODM4Nzc4MDl9.gqETlkz-9XvW2-wqJeWI0RGq11139tqM7jYlpPCk8N06ErP-LFufJYqF1X-JfZwN2yS9YIpL8c6fYvKtsbffI6OGsQhBJ0IoDR8Wk7ASU6P7_u5oiCalB6BvOjLjHZYTSJ68mvxG2Ta7JMcCzz1Zz-W_L2CEa0z6DR51qV7Nv0rp4JH7dafF33i4Gr2u5od3fs37_inafYkgRdt2kpWfjFhWvPKWgSze84ocObdhHf1B29VJpdQ7zZPfdoyplYc-NGZ1BcUzXg9UWavAMw7aSq_Gy2mz1pCRvTn2lC3hjERJp9HKYs8Sl971GtPI04RI_zYpxyY5p-GoW1YKPPFryw",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9.eyJncmFkZUlkIjoiRzIiLCJyb2xlIjoiRU1QTE9ZRUUiLCJ1c2VySWQiOjMsInN1YiI6InRlc3RfZW1wIiwiaWF0IjoxNzgzODc2OTA5LCJleHAiOjE3ODQ0ODE3MDl9.GgB0wB7In5FoJBO5mjGlrzaoZ4WCSPW6LAQ4A1UZKgq4UHBAa7_NeBVAqBZ18Txosdb76DImcQunP89xuUlwWap3BukR13TznKQ_2MsD8bIzdQulKK140NaikbHlyf6dWIBxyUlfT6RXwM3YXdC5xN7Aa9zN7vCFRpgAP0RA0VmtHVT1ZLUei0jYDvTEXfMh04xisbmaIC4IkVNJeFUdv6-dd4AksqF4pbMHE4Jyi55ZNA_d26rQfFh3OnABXGXZ_I0zhjl8maJLQ6YGPAmSqgbRw5o5Dl_0zhSPARWEEup7zCn-B6R9LymDwWKWx7XvGjAKuNV_UwDtlWewpIe2pA",
  "username": "test_emp",
  "role": "EMPLOYEE"
}
```

### GET /api/users/me
**Response:**
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

### GET /api/grades
**Response:**
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

## Module B: Audit

### GET /api/audit
**Response:**
```json
{}
```

## Module C: Policy & Entitlement

### POST /api/travel-policies
**Input:**
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
**Response:**
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

### POST /api/city-tiers
**Input:**
```json
{
  "cityName": "Bangalore",
  "country": "India",
  "tier": "TIER1",
  "perDiemRate": 100.0,
  "hotelCapPerNight": 200.0
}
```
**Response:**
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

### GET /api/travel-policies/search
**Response:**
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

### GET /api/travel-policies/calculate-allowance
**Response:**
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

## Module D: Trip Request & Itinerary

### POST /api/trips
**Input:**
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
**Response:**
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

### POST /api/trips/{id}/submit
**Response:**
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

### POST /api/trips/{id}/approve
**Response:**
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

### POST /api/trips/{tripId}/itinerary
**Input:**
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
**Response:**
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

### POST /api/trips/{tripId}/visa
**Input:**
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
**Response:**
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

### POST /api/trips/{id}/complete
**Response:**
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

### GET /api/trips/{id}/summary
**Response:**
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

## Module E: Documents

### POST /api/documents/upload
**Response:**
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

### GET /api/documents/{id}
**Response:**
```json
Dummy Receipt Content
```

## Module F: Travel Advance

### POST /api/advances
**Input:**
```json
{
  "tripRequestId": 5,
  "requestedAmount": 300.0,
  "currency": "USD",
  "purposeDetails": "Hotel and food pocket allowance",
  "usdEquivalent": 300.0
}
```
**Response:**
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

### POST /api/advances/{id}/approve
**Response:**
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

### POST /api/advances/{id}/disburse
**Response:**
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

### POST /api/advances/{id}/settle
**Input:**
```json
{
  "amountUtilised": 250.0,
  "amountReturned": 50.0,
  "remarks": "Utilised 250, returning 50"
}
```
**Response:**
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

### GET /api/advances/{id}/summary
**Response:**
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

## Module G: Expense Claim & Reimbursement

### POST /api/expenses
**Input:**
```json
{
  "claimTitle": "Trip 1 Expenses Claim",
  "submittedDate": "2026-08-11",
  "totalAmount": 0.0,
  "originalCurrency": "USD",
  "expenseLines": []
}
```
**Response:**
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

### POST /api/expenses/{claimId}/lines
**Input:**
```json
{
  "expenseDate": "2026-08-02",
  "category": "MEALS",
  "amount": 80.0,
  "originalCurrency": "USD",
  "receiptPath": "/receipts/meal1.jpg"
}
```
**Response:**
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

### POST /api/expenses/{claimId}/lines/{lineId}/submit
**Response:**
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

### POST /api/expenses/{claimId}/submit
**Response:**
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

### POST /api/expenses/{claimId}/approve
**Response:**
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

### POST /api/expenses/{claimId}/reimburse
**Input:**
```json
{
  "paymentMethod": "BANK_TRANSFER",
  "transactionReference": "TXN987654",
  "amount": 80.0
}
```
**Response:**
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

## Module H: Policy Compliance & Exception

### GET /api/compliance/exceptions
**Response:**
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Failed to write request",
  "instance": "/api/compliance/exceptions"
}
```

### POST /api/compliance/exceptions/{id}/resolve
**Response:**
```json
{}
```

### POST /api/compliance/claims/{claimId}/audit
**Response:**
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

## Module I: Analytics & Reporting

### POST /api/reports/metrics
**Response:**
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

### GET /api/reports/top-travellers
**Response:**
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

## Module J: Notifications

### GET /api/notifications
**Response:**
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

### POST /api/notifications/{id}/read
**Response:**
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
