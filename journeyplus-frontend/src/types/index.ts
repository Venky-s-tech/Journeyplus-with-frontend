export type UserRole =
  | "EMPLOYEE"
  | "TRAVEL_DESK"
  | "APPROVING_MANAGER"
  | "FINANCE"
  | "COMPLIANCE"
  | "ADMIN";

export interface User {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  name: string;
  phone: string;
  departmentId: string;
  gradeId: string;
  active: boolean;
  status?: string;
  approvalStatus?: string;
  permissions?: string[];
}

export interface UserRegistration {
  username: string;
  email: string;
  password?: string;
  role: UserRole;
  name: string;
  phone: string;
  departmentId: string;
}

export interface DelegationSetup {
  delegateApproverId: number;
  delegationStart: string;
  delegationEnd: string;
}

export interface Grade {
  id: string;
  gradeName: string;
  description: string;
  status: string;
}

export interface CityTier {
  id: number;
  cityName: string;
  country: string;
  tier: "TIER1" | "TIER2" | "TIER3";
  perDiemRate: number;
  hotelCapPerNight: number;
}

export type TravelType = "DOMESTIC" | "INTERNATIONAL";
export type FlightClass = "ECONOMY" | "BUSINESS" | "FIRST";
export type HotelCategory = "STANDARD" | "PREMIUM" | "LUXURY";

export interface TravelPolicy {
  id: number;
  policyName: string;
  description: string;
  gradeId: string;
  travelType: TravelType;
  flightClass: FlightClass;
  hotelCategory: HotelCategory;
  perDiemRate: number;
  localConveyanceLimit: number;
  status: string;
  maxAmountPerTrip: number;
  requiresVisaVerification: boolean;
}

export type TripStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "APPROVED"
  | "BOOKED"
  | "REJECTED"
  | "COMPLETED"
  | "CANCELLED";

export interface ItineraryLeg {
  id: number;
  origin: string;
  destination: string;
  legType: "FLIGHT" | "TRAIN" | "HOTEL" | "CAR_RENTAL";
  travelDate: string;
  departureDateTime: string;
  arrivalDateTime: string;
  carrierDetails: string;
  bookingRef: string;
  cost: number;
  originalCurrency: string;
  usdEquivalent: number;
  bookingStatus?: string;
  bookingReference?: string;
}

export interface VisaRequirement {
  id: number;
  country: string;
  visaType: string;
  requiresVisa: boolean;
  applicationDate?: string;
  submittedDate?: string;
  status: string;
  notes?: string;
}

// Matches the backend's SimpleUserDTO shape returned for `employee` and
// `approver` on TripRequest (see TripRequestController#toSimpleUser /
// TripResponse.java) - NOT the full User entity, so fields like `name` are
// not present here even though they exist on the full User type.
export interface SimpleUser {
  id: number;
  username: string;
  email: string;
  role: string;
}

export interface TripRequest {
  id: number;
  purpose: string;
  destination: string;
  departureDate: string;
  returnDate: string;
  travelType: TravelType;
  estimatedCost: number;
  comments: string;
  status: TripStatus;
  employee?: SimpleUser;
  approver?: SimpleUser;
  itineraryLegs?: ItineraryLeg[];
  visas?: VisaRequirement[];
}

// Matches AdvanceSettlementResponse.java exactly.
export interface Settlement {
  id: number;
  advanceRequestId: number;
  amountUtilised: number;
  amountReturned: number;
  settlementDate?: string;
  status: string;
  remarks?: string;
  createdDate?: string;
  updatedDate?: string;
}

// Matches the real backend enum (AdvanceStatus.java) - there is no
// PENDING_APPROVAL or REJECTED value; a new advance request starts as
// REQUESTED.
export type AdvanceStatus =
  | "REQUESTED"
  | "APPROVED"
  | "DISBURSED"
  | "SETTLED"
  | "FORFEITED";

// Matches AdvanceResponse.java exactly. Note there is NO nested tripRequest
// object, no approverUsername/approvedAmount/remarks/settlements field on
// this response - only approvedById (a raw user id, not a username). To
// display the approver's username, resolve it via the linked TripRequest
// (trip.approver.username) using tripRequestId, e.g. by matching against an
// already-fetched trips list or calling useTrip(tripRequestId).
export interface TravelAdvance {
  id: number;
  tripRequestId: number;
  employeeId?: number;
  requestedAmount: number;
  currency: string;
  purposeDetails: string;
  status: AdvanceStatus;
  usdEquivalent: number;
  approvedById?: number;
  createdDate?: string;
  updatedDate?: string;
  disbursementDate?: string;
}

// Matches AdvanceSummaryResponse.java exactly - returned by
// GET /api/advances/{id}/summary, which is the endpoint that actually
// includes settlement history and running totals (the plain
// GET /api/advances/{id} response does not).
export interface AdvanceSummary {
  advanceDetails: TravelAdvance;
  settlementDetails: Settlement[];
  totalUtilisedAmount: number;
  totalReturnedAmount: number;
  outstandingAmount: number;
  currentStatus: string;
}

export type ExpenseCategory =
  | "ACCOMMODATION"
  | "MEALS"
  | "TRANSPORT"
  | "VISA"
  | "MISC";

export type ExpenseLineStatus = "INCLUDED" | "FLAGGED" | "REJECTED";

// Matches the ExpenseLine entity's actual JSON field names. There is no
// `complianceStatus` field on the backend - the compliance outcome is
// `status` (an ExpenseLineStatus enum with no PENDING value) plus a
// separate `policyComplianceStatus` string ("COMPLIANT"/"NON_COMPLIANT").
export interface ExpenseLine {
  id: number;
  expenseDate: string;
  category: ExpenseCategory;
  amount: number;
  originalCurrency: string;
  usdEquivalent?: number;
  receiptPath?: string;
  receiptRef?: string;
  merchant?: string;
  description?: string;
  justification?: string;
  status?: ExpenseLineStatus;
  policyComplianceStatus?: string;
  complianceRemarks?: string;
  policyCompliant?: boolean;
}

export type ClaimStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "APPROVED"
  | "REJECTED"
  | "PAID"
  | "PARTIALLY_PAID";

// Matches the ExpenseClaim entity's real JSON shape.
// IMPORTANT: the backend entity has NO `expenseLines` collection field at
// all (ExpenseLine only has a @ManyToOne back to the claim, no inverse
// @OneToMany was ever added) - so GET /api/expenses/{id} never returns
// lines. They must be fetched separately via GET /api/expenses/{id}/lines
// (see useClaimLines). `tripRequest` and `employee` are @JsonIgnore on the
// backend and will never appear here either - use `tripRequestId` (which IS
// exposed via a @JsonProperty getter) with useTrip() to get trip details/
// approver username. The real computed field names are `advanceAdjusted`
// and `netReimbursable`, not advanceClaimed/netReimbursed.
export interface ExpenseClaim {
  id: number;
  claimTitle: string;
  submittedDate: string;
  totalAmount: number;
  originalCurrency: string;
  usdEquivalent?: number;
  status: ClaimStatus;
  advanceAdjusted?: number;
  netReimbursable?: number;
  tripRequestId?: number;
  employeeId?: number;
  approverUsername?: string;
  // Populated client-side after a separate GET .../lines call, NOT part of
  // the raw backend response for this entity - see useClaimLines.
  expenseLines?: ExpenseLine[];
  reimbursement?: {
    paymentMethod: string;
    transactionReference: string;
    amount: number;
  };
}

// Lightweight view of the linked TravelPolicy, exposed on PolicyException so
// compliance officers can see what limit was actually breached.
export interface PolicyDetails {
  id: number;
  policyName: string;
  travelType: TravelType;
  flightClass: FlightClass;
  hotelCategory: HotelCategory;
  perDiemRate: number;
  localConveyanceLimit: number;
  maxAmountPerTrip: number;
  status: string;
}

// Matches the real PolicyException entity's JSON field names exactly -
// there is no `flaggedAmount`, `status`, `expenseClaimId`, or `resolvedDate`
// on the backend; the real fields are `amountExceeded`, `approvalStatus`,
// and `claimId` (via a dedicated getter), with no resolvedDate field at all.
export interface ComplianceException {
  id: number;
  violationType: string;
  amountExceeded: number;
  approvalStatus: "PENDING" | "APPROVED" | "REJECTED";
  justification?: string;
  claimId?: number;
  expenseLine?: ExpenseLine;
  policy?: PolicyDetails;
}

export interface AuditLog {
  id: number;
  username: string;
  action: string;
  entityName: string;
  entityId: string;
  timestamp: string;
  details: string;
}

export interface Notification {
  id: number;
  title: string;
  message: string;
  category: "TripRequest" | "Advance" | "ExpenseClaim" | "PolicyException" | "Compliance";
  status: "Read" | "Unread" | "Dismissed";
  read: boolean;
}
