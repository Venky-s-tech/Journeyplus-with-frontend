import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "./lib/auth-context";
import { ToastProvider } from "./components/ui/toast";
import { AppShell } from "./components/AppShell";
import { RoleGuard } from "./components/RoleGuard";

// Auth Pages
import { Login } from "./pages/auth/Login";
import { Register } from "./pages/auth/Register";
import { Forbidden } from "./pages/auth/Forbidden";

// Common Pages
import { Dashboard } from "./pages/dashboard/Dashboard";
import { Approvals } from "./pages/dashboard/Approvals";
import { Notifications } from "./pages/notifications/Notifications";
import { Analytics } from "./pages/analytics/Analytics";

// Trips Pages
import { TripsList } from "./pages/trips/TripsList";
import { TripDetails } from "./pages/trips/TripDetails";

// Advances Pages
import { Advances } from "./pages/advances/Advances";

// Expenses Pages
import { ExpensesList } from "./pages/expenses/ExpensesList";
import { ExpenseDetails } from "./pages/expenses/ExpenseDetails";

// Finance Pages
import { AdvancesQueue } from "./pages/finance/AdvancesQueue";
import { ExpensesQueue } from "./pages/finance/ExpensesQueue";

// Compliance Pages
import { Exceptions } from "./pages/compliance/Exceptions";
import { Audits } from "./pages/compliance/Audits";

// Admin Pages
import { Users } from "./pages/admin/Users";
import { Grades } from "./pages/admin/Grades";
import { CityTiers } from "./pages/admin/CityTiers";
import { Policies } from "./pages/admin/Policies";
import { AuditTrail } from "./pages/admin/AuditTrail";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

export const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Unauthenticated routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/403" element={<Forbidden />} />

              {/* Shell wrapped routes (guarded by authentication) */}
              <Route
                path="/"
                element={
                  <RoleGuard>
                    <AppShell>
                      <Dashboard />
                    </AppShell>
                  </RoleGuard>
                }
              />

              <Route
                path="/notifications"
                element={
                  <RoleGuard>
                    <AppShell>
                      <Notifications />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Trip request paths */}
              <Route
                path="/trips"
                element={
                  <RoleGuard allowedRoles={["EMPLOYEE", "TRAVEL_DESK", "ADMIN", "APPROVING_MANAGER"]}>
                    <AppShell>
                      <TripsList />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/trips/:id"
                element={
                  <RoleGuard allowedRoles={["EMPLOYEE", "TRAVEL_DESK", "ADMIN", "APPROVING_MANAGER"]}>
                    <AppShell>
                      <TripDetails />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Advance request paths */}
              <Route
                path="/advances"
                element={
                  <RoleGuard allowedRoles={["EMPLOYEE", "ADMIN"]}>
                    <AppShell>
                      <Advances />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Expense claim paths */}
              <Route
                path="/expenses"
                element={
                  <RoleGuard allowedRoles={["EMPLOYEE", "ADMIN", "APPROVING_MANAGER"]}>
                    <AppShell>
                      <ExpensesList />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/expenses/:id"
                element={
                  <RoleGuard allowedRoles={["EMPLOYEE", "ADMIN", "APPROVING_MANAGER", "FINANCE", "COMPLIANCE"]}>
                    <AppShell>
                      <ExpenseDetails />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Approver paths */}
              <Route
                path="/approvals"
                element={
                  <RoleGuard allowedRoles={["APPROVING_MANAGER", "ADMIN"]}>
                    <AppShell>
                      <Approvals />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Finance paths */}
              <Route
                path="/finance/advances"
                element={
                  <RoleGuard allowedRoles={["FINANCE", "ADMIN"]}>
                    <AppShell>
                      <AdvancesQueue />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/finance/expenses"
                element={
                  <RoleGuard allowedRoles={["FINANCE", "ADMIN"]}>
                    <AppShell>
                      <ExpensesQueue />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Compliance paths */}
              <Route
                path="/compliance/exceptions"
                element={
                  <RoleGuard allowedRoles={["COMPLIANCE", "ADMIN"]}>
                    <AppShell>
                      <Exceptions />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/compliance/auditing"
                element={
                  <RoleGuard allowedRoles={["COMPLIANCE", "ADMIN"]}>
                    <AppShell>
                      <Audits />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Analytics report path */}
              <Route
                path="/analytics"
                element={
                  <RoleGuard allowedRoles={["ADMIN", "FINANCE", "COMPLIANCE"]}>
                    <AppShell>
                      <Analytics />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Admin configuration paths */}
              <Route
                path="/admin/users"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <AppShell>
                      <Users />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/admin/grades"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <AppShell>
                      <Grades />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/admin/city-tiers"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <AppShell>
                      <CityTiers />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/admin/policies"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <AppShell>
                      <Policies />
                    </AppShell>
                  </RoleGuard>
                }
              />
              <Route
                path="/admin/audit"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <AppShell>
                      <AuditTrail />
                    </AppShell>
                  </RoleGuard>
                }
              />

              {/* Wildcard Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </ToastProvider>
    </QueryClientProvider>
  );
};

export default App;
