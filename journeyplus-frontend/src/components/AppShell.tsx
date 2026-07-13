import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../lib/auth-context";
import { NotificationBell } from "./NotificationBell";
import {
  LayoutDashboard,
  Plane,
  Coins,
  Receipt,
  ShieldCheck,
  BarChart3,
  Settings,
  LogOut,
  Menu,
  ChevronLeft,
  ChevronRight,
  Sun,
  Moon,
  Users,
  Compass,
  FileText,
  History,
} from "lucide-react";
import { Button } from "./ui/button";

export const AppShell: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const [darkMode, setDarkMode] = useState(false);

  useEffect(() => {
    // Check if dark class exists on document
    const isDark = document.documentElement.classList.contains("dark");
    setDarkMode(isDark);
  }, []);

  const toggleDarkMode = () => {
    const nextDark = !darkMode;
    setDarkMode(nextDark);
    if (nextDark) {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const getNavItems = () => {
    if (!user) return [];

    const common = [{ name: "Dashboard", path: "/", icon: LayoutDashboard }];

    switch (user.role) {
      case "EMPLOYEE":
        return [
          ...common,
          { name: "My Trips", path: "/trips", icon: Plane },
          { name: "My Advances", path: "/advances", icon: Coins },
          { name: "Expense Claims", path: "/expenses", icon: Receipt },
          { name: "Notifications", path: "/notifications", icon: BellIcon },
        ];
      case "TRAVEL_DESK":
        return [
          ...common,
          { name: "Trips & Bookings", path: "/trips", icon: Plane },
        ];
      case "APPROVING_MANAGER":
        return [
          ...common,
          { name: "Approvals", path: "/approvals", icon: ShieldCheck },
        ];
      case "FINANCE":
        return [
          ...common,
          { name: "Advances Queue", path: "/finance/advances", icon: Coins },
          { name: "Expenses Queue", path: "/finance/expenses", icon: Receipt },
          { name: "Analytics", path: "/analytics", icon: BarChart3 },
        ];
      case "COMPLIANCE":
        return [
          ...common,
          { name: "Exceptions", path: "/compliance/exceptions", icon: ShieldCheck },
          { name: "Claims Auditing", path: "/compliance/auditing", icon: FileText },
        ];
      case "ADMIN":
        return [
          ...common,
          { name: "Users & Delegation", path: "/admin/users", icon: Users },
          { name: "Grade Grid", path: "/admin/grades", icon: Settings },
          { name: "City Tiers", path: "/admin/city-tiers", icon: Compass },
          { name: "Travel Policies", path: "/admin/policies", icon: FileText },
          { name: "Audit Trail", path: "/admin/audit", icon: History },
          { name: "System Analytics", path: "/analytics", icon: BarChart3 },
        ];
      default:
        return common;
    }
  };

  const navItems = getNavItems();

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-background text-foreground">
      {/* Sidebar */}
      <aside
        className={`flex flex-col border-r border-border bg-card transition-all duration-300 ${
          collapsed ? "w-16" : "w-64"
        }`}
      >
        {/* Header/Logo */}
        <div className="flex h-14 items-center justify-between px-4 border-b border-border">
          {!collapsed && (
            <span className="text-md font-bold tracking-tight bg-gradient-to-r from-primary to-indigo-500 bg-clip-text text-transparent">
              JourneyPlus
            </span>
          )}
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 ml-auto"
            onClick={() => setCollapsed(!collapsed)}
          >
            {collapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
          </Button>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 space-y-1 p-2 overflow-y-auto">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground hover:bg-muted hover:text-foreground"
                }`}
              >
                <Icon className="h-4 w-4 shrink-0" />
                {!collapsed && <span>{item.name}</span>}
              </Link>
            );
          })}
        </nav>

        {/* Footer info & theme toggle */}
        <div className="p-2 border-t border-border flex flex-col gap-1">
          <Button
            variant="ghost"
            className={`w-full justify-start gap-3 px-3 py-2 ${collapsed ? "justify-center" : ""}`}
            onClick={toggleDarkMode}
          >
            {darkMode ? <Sun className="h-4 w-4 shrink-0" /> : <Moon className="h-4 w-4 shrink-0" />}
            {!collapsed && <span>{darkMode ? "Light Mode" : "Dark Mode"}</span>}
          </Button>
          <Button
            variant="ghost"
            className={`w-full justify-start gap-3 px-3 py-2 text-destructive hover:bg-destructive/10 hover:text-destructive ${
              collapsed ? "justify-center" : ""
            }`}
            onClick={handleLogout}
          >
            <LogOut className="h-4 w-4 shrink-0" />
            {!collapsed && <span>Logout</span>}
          </Button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Header Bar */}
        <header className="h-14 border-b border-border bg-card flex items-center justify-between px-6 shrink-0 shadow-sm">
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-muted-foreground">
              Workspace / {user?.role.replace("_", " ")}
            </span>
          </div>

          <div className="flex items-center gap-4">
            <NotificationBell />
            <div className="h-6 w-px bg-border" />
            <div className="flex items-center gap-2">
              <div className="flex flex-col text-right hidden sm:flex">
                <span className="text-sm font-semibold">{user?.name}</span>
                <span className="text-[10px] text-muted-foreground">{user?.email}</span>
              </div>
              <div className="h-8 w-8 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-xs font-semibold text-primary">
                {user?.name.substring(0, 2).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Main Scrolling Body */}
        <main className="flex-1 overflow-y-auto bg-background/50 p-6">
          <div className="max-w-6xl mx-auto space-y-6">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};

// Dummy element for Notifications fallback icon in items mapping
const BellIcon = (props: any) => <NotificationBellIcon {...props} />;
const NotificationBellIcon = (props: any) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    width="24"
    height="24"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    {...props}
  >
    <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
    <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
  </svg>
);

export default AppShell;
