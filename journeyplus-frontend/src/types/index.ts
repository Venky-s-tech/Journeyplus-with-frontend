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
}

export interface UserRegistration {
  username: string;
  email: string;
  password?: string;
  role: UserRole;
  name: string;
  phone: string;
  gradeId: string;
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
  approverUsername: string;
  employee?: User;
  approver?: User;
  itineraryLegs?: ItineraryLeg[];
  visas?: VisaRequirement[];
}

export interface Settlement {
  id: number;
  amountUtilised: number;
  amountReturned: number;
  remarks: string;
  settledDate?: string;
}

export type AdvanceStatus =
  | "PENDING_APPROVAL"
  | "APPROVED"
  | "REJECTED"
  | "DISBURSED"
  | "SETTLED"
  | "FORFEITED";

export interface TravelAdvance {
  id: number;
  tripRequestId: number;
  requestedAmount: number;
  currency: string;
  purposeDetails: string;
  status: AdvanceStatus;
  usdEquivalent: number;
  approvedAmount?: number;
  disbursedDate?: string;
  remarks?: string;
  settlements?: Settlement[];
}

export type ExpenseCategory =
  | "ACCOMMODATION"
  | "MEALS"
  | "TRANSPORT"
  | "VISA"
  | "MISC";

export type ComplianceStatus = "INCLUDED" | "FLAGGED" | "REJECTED" | "PENDING";

export interface ExpenseLine {
  id: number;
  expenseDate: string;
  category: ExpenseCategory;
  amount: number;
  originalCurrency: string;
  receiptPath?: string;
  complianceStatus?: ComplianceStatus;
  complianceRemarks?: string;
}

export type ClaimStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "APPROVED"
  | "REJECTED"
  | "PAID"
  | "PARTIALLY_PAID";

export interface ExpenseClaim {
  id: number;
  claimTitle: string;
  submittedDate: string;
  totalAmount: number;
  originalCurrency: string;
  status: ClaimStatus;
  expenseLines: ExpenseLine[];
  reimbursement?: {
    paymentMethod: string;
    transactionReference: string;
    amount: number;
  };
}

export interface ComplianceException {
  id: number;
  violationType: string;
  flaggedAmount: number;
  status: "PENDING" | "RESOLVED" | "REJECTED";
  justification?: string;
  resolvedDate?: string;
  expenseLine?: ExpenseLine;
  expenseClaimId?: number;
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
