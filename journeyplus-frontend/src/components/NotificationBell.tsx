import React, { useState } from "react";
import { Bell, Check, Trash } from "lucide-react";
import { Link } from "react-router-dom";
import { useNotifications, useMarkNotificationRead, useDismissNotification } from "../hooks";
import { cn } from "../lib/utils";

export const NotificationBell: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { data } = useNotifications();
  const markReadMutation = useMarkNotificationRead();
  const dismissMutation = useDismissNotification();

  const notifications = data?.notifications || [];
  const unreadCount = notifications.filter((n) => !n.read && n.status !== "Dismissed").length;

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 rounded-full text-muted-foreground hover:text-foreground hover:bg-muted focus:outline-none transition-colors"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground animate-pulse">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <>
          <div className="fixed inset-0 z-40" onClick={() => setIsOpen(false)} />
          <div className="absolute right-0 mt-2 w-80 rounded-md border bg-card text-card-foreground shadow-lg z-50 overflow-hidden animate-in fade-in-0 slide-in-from-top-2 duration-150 border-border">
            <div className="p-3 border-b flex items-center justify-between">
              <span className="font-semibold text-sm">Notifications</span>
              <Link
                to="/notifications"
                onClick={() => setIsOpen(false)}
                className="text-xs text-primary hover:underline"
              >
                View all
              </Link>
            </div>
            <div className="max-h-64 overflow-y-auto divide-y divide-border">
              {notifications.length === 0 ? (
                <div className="p-4 text-center text-xs text-muted-foreground">
                  No notifications
                </div>
              ) : (
                notifications.slice(0, 5).map((n) => (
                  <div
                    key={n.id}
                    className={cn(
                      "p-3 flex items-start gap-2 hover:bg-muted/30 transition-colors",
                      !n.read && "bg-muted/10"
                    )}
                  >
                    <div className="flex-1 flex flex-col gap-0.5">
                      <span className="font-medium text-xs text-foreground">{n.title}</span>
                      <span className="text-[11px] text-muted-foreground line-clamp-2">
                        {n.message}
                      </span>
                    </div>
                    <div className="flex flex-col gap-1 shrink-0">
                      {!n.read && (
                        <button
                          onClick={() => markReadMutation.mutate(n.id)}
                          title="Mark as read"
                          className="p-1 rounded hover:bg-muted text-primary"
                        >
                          <Check className="h-3.5 w-3.5" />
                        </button>
                      )}
                      <button
                        onClick={() => dismissMutation.mutate(n.id)}
                        title="Dismiss"
                        className="p-1 rounded hover:bg-muted text-muted-foreground hover:text-destructive"
                      >
                        <Trash className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default NotificationBell;
