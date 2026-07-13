import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../lib/auth-context";
import {
  useTrips,
  useAdvances,
  useClaims,
  usePendingUsers,
  useExceptions,
} from "../../hooks";
import {
  Plane,
  Coins,
  Receipt,
  ShieldCheck,
  UserCheck,
  Plus,
  ArrowRight,
  TrendingUp,
  AlertTriangle,
  FolderLock,
} from "lucide-react";
import { Button } from "../../components/ui/button";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Queries (enabled based on roles to prevent unnecessary 403s)
  const isEmployee = user?.role === "EMPLOYEE";
  const isManager = user?.role === "APPROVING_MANAGER";
  const isFinance = user?.role === "FINANCE";
  const isCompliance = user?.role === "COMPLIANCE";
  const isAdmin = user?.role === "ADMIN";

  const { data: trips, isLoading: tripsLoading } = useTrips(user?.role || "EMPLOYEE");
  const { data: advances, isLoading: advLoading } = useAdvances(user?.role || "EMPLOYEE");
  const { data: claims, isLoading: claimsLoading } = useClaims(user?.role || "EMPLOYEE");

  const { data: pendingUsers, isLoading: pUsersLoading } = usePendingUsers();
  const { data: exceptions, isLoading: exceptionsLoading } = useExceptions("PENDING");

  if (!user) return null;

  // Render Employee Dashboard
  const renderEmployee = () => {
    const recentTrips = trips?.slice(0, 3) || [];
    const recentClaims = claims?.slice(0, 3) || [];
    const activeAdvanceAmount = advances
      ?.filter((a) => a.status === "DISBURSED")
      ?.reduce((acc, curr) => acc + curr.requestedAmount, 0) || 0;

    return (
      <div className="space-y-6 animate-in fade-in-50 duration-200">
        {/* Top Banner */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between p-6 bg-card border border-border rounded-lg gap-4 shadow-sm">
          <div className="space-y-1">
            <h1 className="text-xl font-bold">Welcome back, {user.name}</h1>
            <p className="text-xs text-muted-foreground">
              Request new trips, upload receipts, and check your active travel advances.
            </p>
          </div>
          <Button onClick={() => navigate("/trips")} className="gap-2 shrink-0">
            <Plus className="h-4 w-4" /> New Trip Request
          </Button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">My Total Trips</span>
              <p className="text-2xl font-bold">{trips?.length || 0}</p>
            </div>
            <div className="p-2 bg-primary/10 rounded-full text-primary">
              <Plane className="h-5 w-5" />
            </div>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">Active Cash Advance</span>
              <p className="text-2xl font-bold">{formatCurrency(activeAdvanceAmount)}</p>
            </div>
            <div className="p-2 bg-purple-50 dark:bg-purple-950/20 rounded-full text-purple-600">
              <Coins className="h-5 w-5" />
            </div>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">Pending Claims</span>
              <p className="text-2xl font-bold">
                {claims?.filter((c) => c.status === "SUBMITTED").length || 0}
              </p>
            </div>
            <div className="p-2 bg-yellow-50 dark:bg-yellow-950/20 rounded-full text-yellow-600">
              <Receipt className="h-5 w-5" />
            </div>
          </div>
        </div>

        {/* Recent Activity lists */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Trips list */}
          <div className="p-4 bg-card border border-border rounded-lg space-y-4 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Recent Trip Requests</h2>
              <Link to="/trips" className="text-xs text-primary hover:underline flex items-center gap-1">
                View all <ArrowRight className="h-3 w-3" />
              </Link>
            </div>
            <div className="divide-y divide-border">
              {recentTrips.length === 0 ? (
                <p className="text-xs text-muted-foreground py-4 text-center">No trips created yet.</p>
              ) : (
                recentTrips.map((t) => (
                  <div
                    key={t.id}
                    className="py-3 flex items-center justify-between hover:bg-muted/10 cursor-pointer rounded px-1"
                    onClick={() => navigate(`/trips/${t.id}`)}
                  >
                    <div className="flex flex-col gap-0.5">
                      <span className="font-medium text-xs">{t.destination}</span>
                      <span className="text-[10px] text-muted-foreground">{formatDate(t.departureDate)}</span>
                    </div>
                    <StatusBadge status={t.status} />
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Expenses list */}
          <div className="p-4 bg-card border border-border rounded-lg space-y-4 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold">Recent Expense Claims</h2>
              <Link to="/expenses" className="text-xs text-primary hover:underline flex items-center gap-1">
                View all <ArrowRight className="h-3 w-3" />
              </Link>
            </div>
            <div className="divide-y divide-border">
              {recentClaims.length === 0 ? (
                <p className="text-xs text-muted-foreground py-4 text-center">No expense claims filed yet.</p>
              ) : (
                recentClaims.map((c) => (
                  <div
                    key={c.id}
                    className="py-3 flex items-center justify-between hover:bg-muted/10 cursor-pointer rounded px-1"
                    onClick={() => navigate(`/expenses/${c.id}`)}
                  >
                    <div className="flex flex-col gap-0.5">
                      <span className="font-medium text-xs">{c.claimTitle}</span>
                      <span className="text-[10px] text-muted-foreground">{formatCurrency(c.totalAmount, c.originalCurrency)}</span>
                    </div>
                    <StatusBadge status={c.status} />
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  // Render Manager Dashboard
  const renderManager = () => {
    const pendingTrips = trips?.filter((t) => t.status === "SUBMITTED") || [];
    const pendingAdvances = advances?.filter((a) => a.status === "PENDING_APPROVAL") || [];
    // Claims wait list (for this demo we fetch claims and filter by status)
    const pendingClaims = claims?.filter((c) => c.status === "SUBMITTED") || [];

    return (
      <div className="space-y-6 animate-in fade-in-50 duration-200">
        <h1 className="text-2xl font-bold">Approver Dashboard</h1>

        {/* Counts */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">Pending Trips</span>
              <p className="text-2xl font-bold">{pendingTrips.length}</p>
            </div>
            <div className="p-2 bg-blue-50 dark:bg-blue-950/20 rounded-full text-blue-600">
              <Plane className="h-5 w-5" />
            </div>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">Pending Advances</span>
              <p className="text-2xl font-bold">{pendingAdvances.length}</p>
            </div>
            <div className="p-2 bg-purple-50 dark:bg-purple-950/20 rounded-full text-purple-600">
              <Coins className="h-5 w-5" />
            </div>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">Pending Claims</span>
              <p className="text-2xl font-bold">{pendingClaims.length}</p>
            </div>
            <div className="p-2 bg-yellow-50 dark:bg-yellow-950/20 rounded-full text-yellow-600">
              <Receipt className="h-5 w-5" />
            </div>
          </div>
        </div>

        <Button onClick={() => navigate("/approvals")} className="w-full sm:w-auto gap-2">
          View Approvals Queue <ArrowRight className="h-4 w-4" />
        </Button>
      </div>
    );
  };

  // Render Finance Dashboard
  const renderFinance = () => {
    return (
      <div className="space-y-6 animate-in fade-in-50 duration-200">
        <h1 className="text-2xl font-bold">Finance Dashboard</h1>

        {/* budget snapshot */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Total Budget Allocated</span>
            <p className="text-2xl font-bold">$250,000.00</p>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Total Disbursed Advances</span>
            <p className="text-2xl font-bold">$42,300.00</p>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Pending Disbursements</span>
            <p className="text-2xl font-bold text-blue-600">{advances?.length || 0}</p>
          </div>
        </div>

        <div className="flex gap-4">
          <Button onClick={() => navigate("/finance/advances")} className="gap-2">
            Disburse Advances <ArrowRight className="h-4 w-4" />
          </Button>
          <Button onClick={() => navigate("/finance/expenses")} variant="outline" className="gap-2">
            Reimburse Expenses <ArrowRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    );
  };

  // Render Compliance Dashboard
  const renderCompliance = () => {
    return (
      <div className="space-y-6 animate-in fade-in-50 duration-200">
        <h1 className="text-2xl font-bold">Compliance Dashboard</h1>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">Open Policy Exceptions</span>
              <p className="text-2xl font-bold text-destructive">{exceptions?.length || 0}</p>
            </div>
            <div className="p-2 bg-destructive/10 rounded-full text-destructive">
              <AlertTriangle className="h-5 w-5" />
            </div>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm flex items-center justify-between">
            <div className="space-y-1">
              <span className="text-[10px] uppercase font-bold text-muted-foreground">Recent Claim Audits</span>
              <p className="text-2xl font-bold text-green-600">8</p>
            </div>
            <div className="p-2 bg-green-50 dark:bg-green-950/20 rounded-full text-green-600">
              <FolderLock className="h-5 w-5" />
            </div>
          </div>
        </div>

        <Button onClick={() => navigate("/compliance/exceptions")} className="gap-2">
          Resolve Exceptions Queue <ArrowRight className="h-4 w-4" />
        </Button>
      </div>
    );
  };

  // Render Admin Dashboard
  const renderAdmin = () => {
    return (
      <div className="space-y-6 animate-in fade-in-50 duration-200">
        {/* KPIs */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Pending Approvals</span>
            <p className="text-2xl font-bold text-primary">{pendingUsers?.length || 0}</p>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Active Grades</span>
            <p className="text-2xl font-bold">4</p>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Active Travel Policies</span>
            <p className="text-2xl font-bold">2</p>
          </div>

          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-1">
            <span className="text-[10px] uppercase font-bold text-muted-foreground">Audit Logs Generated</span>
            <p className="text-2xl font-bold">128</p>
          </div>
        </div>

        <div className="flex flex-wrap gap-4">
          <Button onClick={() => navigate("/admin/users")} className="gap-2">
            <UserCheck className="h-4 w-4" /> Manage Users ({pendingUsers?.length || 0} pending)
          </Button>
          <Button onClick={() => navigate("/admin/policies")} variant="outline">
            Configure Entitlements
          </Button>
        </div>
      </div>
    );
  };

  // Display based on roles
  if (isEmployee) return renderEmployee();
  if (isManager) return renderManager();
  if (isFinance) return renderFinance();
  if (isCompliance) return renderCompliance();
  if (isAdmin) return renderAdmin();

  // Fallback
  return (
    <div className="p-6 text-center space-y-2 bg-card border rounded-lg border-border">
      <h1 className="text-xl font-bold">Welcome, {user.name}</h1>
      <p className="text-xs text-muted-foreground">Logged in as {user.role}</p>
    </div>
  );
};

export default Dashboard;
