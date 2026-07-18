import React from "react";
import { useQuery } from "@tanstack/react-query";
import * as adminApi from "../../api/admin";
import { DataTable } from "../../components/DataTable";
import { formatDate } from "../../lib/utils";
import { AuditLog } from "../../types";

export const AuditTrail: React.FC = () => {
  const { data: logs, isLoading } = useQuery<AuditLog[]>({
    queryKey: ["admin", "audit-logs"],
    queryFn: adminApi.getAuditLogs,
    // Bug #8: keep the audit trail current with newly generated records.
    refetchInterval: 10000,
    refetchOnWindowFocus: true,
    staleTime: 0,
  });

  const columns = [
    {
      header: "Log ID",
      accessor: (l: AuditLog) => <span className="font-semibold">#{l.id}</span>,
    },
    {
      header: "User",
      accessor: (l: AuditLog) => <span className="font-medium">{l.username}</span>,
    },
    {
      header: "Action Logged",
      accessor: (l: AuditLog) => <span className="text-xs uppercase font-semibold text-primary">{l.action}</span>,
    },
    {
      header: "Target Entity",
      accessor: (l: AuditLog) => (
        <span className="text-xs">
          {l.entityName} (ID: {l.entityId})
        </span>
      ),
    },
    {
      header: "Change details",
      accessor: (l: AuditLog) => <span className="text-xs text-muted-foreground">{l.details}</span>,
    },
    {
      header: "Timestamp",
      accessor: (l: AuditLog) => <span className="text-xs text-muted-foreground">{formatDate(l.timestamp)}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Security Audit Trail</h1>
        <p className="text-xs text-muted-foreground">
          Query automated activity logs, role change histories, and state modifications.
        </p>
      </div>

      <DataTable
        columns={columns}
        data={logs}
        isLoading={isLoading}
        emptyMessage="No system audit logs recorded."
      />
    </div>
  );
};

export default AuditTrail;
