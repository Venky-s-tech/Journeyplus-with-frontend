import React from "react";

interface StatusBadgeProps {
  status: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const cleanStatus = status?.toUpperCase() || "";

  let classes = "bg-slate-100 text-slate-800 border-slate-200 dark:bg-slate-900/30 dark:text-slate-400 dark:border-slate-800";

  switch (cleanStatus) {
    // Trip Status
    case "DRAFT":
      classes = "bg-gray-100 text-gray-800 border-gray-200 dark:bg-gray-800/40 dark:text-gray-300 dark:border-gray-800";
      break;
    case "SUBMITTED":
    case "PENDING_APPROVAL":
    case "PENDING":
    case "UNREAD":
      classes = "bg-blue-50 text-blue-700 border-blue-150 dark:bg-blue-900/20 dark:text-blue-300 dark:border-blue-800";
      break;
    case "APPROVED":
    case "GRANTED":
    case "READ":
    case "ACTIVE":
      classes = "bg-green-50 text-green-700 border-green-150 dark:bg-green-900/20 dark:text-green-300 dark:border-green-800";
      break;
    case "REJECTED":
    case "FAILED":
      classes = "bg-red-50 text-red-700 border-red-150 dark:bg-red-900/20 dark:text-red-300 dark:border-red-800";
      break;
    case "COMPLETED":
    case "DISBURSED":
    case "PAID":
    case "RESOLVED":
      classes = "bg-purple-50 text-purple-700 border-purple-150 dark:bg-purple-900/20 dark:text-purple-300 dark:border-purple-800";
      break;
    case "CANCELLED":
    case "FORFEITED":
    case "DISMISSED":
      classes = "bg-slate-50 text-slate-700 border-slate-150 dark:bg-slate-900/20 dark:text-slate-300 dark:border-slate-800";
      break;
    case "PARTIALLY_PAID":
    case "SETTLED":
      classes = "bg-emerald-50 text-emerald-700 border-emerald-150 dark:bg-emerald-950/20 dark:text-emerald-300 dark:border-emerald-900";
      break;
    case "INCLUDED":
      classes = "bg-cyan-50 text-cyan-700 border-cyan-150 dark:bg-cyan-900/20 dark:text-cyan-300 dark:border-cyan-800";
      break;
    case "FLAGGED":
      classes = "bg-yellow-50 text-yellow-700 border-yellow-150 dark:bg-yellow-900/20 dark:text-yellow-300 dark:border-yellow-800";
      break;
  }

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border ${classes}`}>
      {status}
    </span>
  );
};

export default StatusBadge;
