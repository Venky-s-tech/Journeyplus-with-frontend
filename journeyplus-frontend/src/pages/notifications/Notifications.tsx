import React, { useState } from "react";
import {
  useNotifications,
  useMarkNotificationRead,
  useDismissNotification,
} from "../../hooks";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { StatusBadge } from "../../components/StatusBadge";
import {
  Bell,
  Check,
  Trash2,
  Plane,
  Coins,
  Receipt,
  ShieldCheck,
  AlertOctagon,
} from "lucide-react";
import { cn } from "../../lib/utils";

export const Notifications: React.FC = () => {
  const { toast } = useToast();
  const [filter, setFilter] = useState<"ALL" | "UNREAD">("ALL");

  const { data, isLoading } = useNotifications();
  const markReadMutation = useMarkNotificationRead();
  const dismissMutation = useDismissNotification();

  const notifications = data?.notifications || [];

  const filteredNotifications = notifications.filter((n) => {
    if (n.status === "Dismissed") return false;
    if (filter === "UNREAD") return !n.read;
    return true;
  });

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case "TripRequest":
        return <Plane className="h-4 w-4 text-blue-500" />;
      case "Advance":
        return <Coins className="h-4 w-4 text-purple-500" />;
      case "ExpenseClaim":
        return <Receipt className="h-4 w-4 text-green-500" />;
      case "PolicyException":
      case "Compliance":
        return <ShieldCheck className="h-4 w-4 text-amber-500" />;
      default:
        return <Bell className="h-4 w-4 text-muted-foreground" />;
    }
  };

  const handleMarkAllRead = () => {
    const unread = notifications.filter((n) => !n.read);
    unread.forEach((n) => markReadMutation.mutate(n.id));
    toast("All notifications marked as read", "success");
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Notifications Workspace</h1>
          <p className="text-xs text-muted-foreground">
            Manage alerts and notifications sent from automated workflows and policy evaluations.
          </p>
        </div>

        <div className="flex gap-2">
          <Button size="sm" variant="outline" onClick={handleMarkAllRead}>
            Mark all read
          </Button>
        </div>
      </div>

      {/* Filter and Content */}
      <div className="space-y-4">
        <div className="flex gap-2 border-b border-border pb-2">
          <button
            onClick={() => setFilter("ALL")}
            className={cn(
              "px-3 py-1.5 text-xs font-semibold rounded-md transition-colors",
              filter === "ALL" ? "bg-muted text-foreground" : "text-muted-foreground hover:bg-muted/30"
            )}
          >
            All alerts
          </button>
          <button
            onClick={() => setFilter("UNREAD")}
            className={cn(
              "px-3 py-1.5 text-xs font-semibold rounded-md transition-colors",
              filter === "UNREAD" ? "bg-muted text-foreground" : "text-muted-foreground hover:bg-muted/30"
            )}
          >
            Unread
          </button>
        </div>

        {isLoading ? (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-16 border rounded bg-card animate-pulse" />
            ))}
          </div>
        ) : filteredNotifications.length === 0 ? (
          <div className="p-12 text-center border rounded-lg bg-card text-muted-foreground text-sm">
            No notifications to show.
          </div>
        ) : (
          <div className="space-y-3">
            {filteredNotifications.map((n) => (
              <div
                key={n.id}
                className={cn(
                  "p-4 border border-border rounded-lg bg-card flex items-start gap-3 transition-colors shadow-sm",
                  !n.read && "border-primary/20 bg-primary/5"
                )}
              >
                <div className="p-2 bg-muted/50 rounded-full shrink-0">
                  {getCategoryIcon(n.category)}
                </div>

                <div className="flex-1 space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-xs text-foreground">{n.title}</span>
                    <span className="text-[10px] text-muted-foreground">({n.category})</span>
                  </div>
                  <p className="text-xs text-muted-foreground">{n.message}</p>
                </div>

                <div className="flex gap-1 shrink-0">
                  {!n.read && (
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => markReadMutation.mutate(n.id)}
                      className="p-1.5 h-8 text-primary"
                    >
                      <Check className="h-4 w-4" />
                    </Button>
                  )}
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => dismissMutation.mutate(n.id)}
                    className="p-1.5 h-8 text-muted-foreground hover:text-destructive"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Notifications;
