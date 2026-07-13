import React, { useState } from "react";
import {
  usePendingUsers,
  useApproveUser,
  useRejectUser,
} from "../../hooks";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as adminApi from "../../api/admin";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "../../components/ui/tabs";
import { Check, X, ShieldAlert, Award, UserCheck, Calendar } from "lucide-react";
import { User } from "../../types";

export const Users: React.FC = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  // Delegation state
  const [delegatorId, setDelegatorId] = useState("");
  const [delegateeId, setDelegateeId] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const { data: pending, isLoading: pendingLoading } = usePendingUsers();
  const approveMutation = useApproveUser();
  const rejectMutation = useRejectUser();

  // Fetch all users
  const { data: users, isLoading: usersLoading } = useQuery<User[]>({
    queryKey: ["admin", "users"],
    queryFn: () => adminApi.getUsers(),
  });

  // Delegation mutation
  const delegateMutation = useMutation({
    mutationFn: adminApi.setupDelegation,
    onSuccess: () => {
      toast("Delegation configured successfully", "success", "Delegation Active");
      setDelegatorId("");
      setDelegateeId("");
      setStartDate("");
      setEndDate("");
    },
    onError: (err: any) => {
      toast(err.response?.data?.message || "Failed to setup delegation", "error");
    },
  });

  const handleDelegationSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!delegateeId) {
      toast("Please select a delegate approver", "error");
      return;
    }
    delegateMutation.mutate({
      delegateApproverId: Number(delegateeId),
      delegationStart: `${startDate}T00:00:00.000Z`,
      delegationEnd: `${endDate}T23:59:59.000Z`,
    });
  };

  const pendingColumns = [
    {
      header: "Username",
      accessor: (u: User) => <span className="font-semibold">{u.username}</span>,
    },
    {
      header: "Name",
      accessor: (u: User) => <span>{u.name}</span>,
    },
    {
      header: "Requested Role",
      accessor: (u: User) => <span className="text-xs uppercase font-medium">{u.role}</span>,
    },
    {
      header: "Department",
      accessor: (u: User) => <span className="text-xs">{u.departmentId}</span>,
    },
    {
      header: "Decision Actions",
      accessor: (u: User) => (
        <div className="flex gap-2">
          <Button
            size="sm"
            onClick={() => approveMutation.mutate(u.id, {
              onSuccess: () => toast(`Approved registration for ${u.username}`, "success"),
            })}
            className="bg-green-600 hover:bg-green-700 h-8"
          >
            Approve
          </Button>
          <Button
            size="sm"
            variant="destructive"
            onClick={() => rejectMutation.mutate(u.id, {
              onSuccess: () => toast(`Rejected registration for ${u.username}`, "success"),
            })}
            className="h-8"
          >
            Reject
          </Button>
        </div>
      ),
    },
  ];

  const userColumns = [
    {
      header: "ID",
      accessor: (u: User) => <span className="font-semibold">#{u.id}</span>,
    },
    {
      header: "Name",
      accessor: (u: User) => <span>{u.name}</span>,
    },
    {
      header: "Email",
      accessor: (u: User) => <span className="text-xs">{u.email}</span>,
    },
    {
      header: "Role",
      accessor: (u: User) => <span className="text-xs uppercase font-medium">{u.role}</span>,
    },
    {
      header: "Grade",
      accessor: (u: User) => <span className="text-xs">{u.gradeId}</span>,
    },
    {
      header: "Department",
      accessor: (u: User) => <span className="text-xs">{u.departmentId}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Identity & Approvals Workspace</h1>
        <p className="text-xs text-muted-foreground">
          Approve pending registration requests, configure user roles, and define temporary delegation schedules.
        </p>
      </div>

      <Tabs defaultValue="pending">
        <TabsList className="mb-4">
          <TabsTrigger value="pending">Pending Approvals ({pending?.length || 0})</TabsTrigger>
          <TabsTrigger value="all">Active Directories</TabsTrigger>
          <TabsTrigger value="delegation">Delegation Settings</TabsTrigger>
        </TabsList>

        <TabsContent value="pending">
          <DataTable
            columns={pendingColumns}
            data={pending}
            isLoading={pendingLoading}
            emptyMessage="No pending registration requests awaiting approval."
          />
        </TabsContent>

        <TabsContent value="all">
          <DataTable
            columns={userColumns}
            data={users}
            isLoading={usersLoading}
            emptyMessage="No active directory users found."
          />
        </TabsContent>

        <TabsContent value="delegation">
          <div className="p-6 border border-border bg-card rounded-lg max-w-md shadow-sm">
            <h2 className="text-sm font-semibold border-b pb-2 mb-4 flex items-center gap-2">
              <Calendar className="h-4 w-4 text-muted-foreground" /> Delegate Approval Authority
            </h2>
            <form onSubmit={handleDelegationSubmit} className="space-y-3 text-xs">
              <p className="text-muted-foreground mb-4">
                Configure temporary delegation rules to forward pending trip/expense approvals to another active manager during absences.
              </p>

              <div className="space-y-1">
                <Label htmlFor="delegatee">Select Delegate Manager</Label>
                <select
                  id="delegatee"
                  required
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                  value={delegateeId}
                  onChange={(e) => setDelegateeId(e.target.value)}
                >
                  <option value="">Select a manager</option>
                  {users
                    ?.filter((u) => u.role === "APPROVING_MANAGER")
                    ?.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.name} ({u.username})
                      </option>
                    ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label>Start Date</Label>
                  <Input type="date" required value={startDate} onChange={(e) => setStartDate(e.target.value)} />
                </div>
                <div className="space-y-1">
                  <Label>End Date</Label>
                  <Input type="date" required value={endDate} onChange={(e) => setEndDate(e.target.value)} />
                </div>
              </div>

              <Button type="submit" className="w-full mt-4" disabled={delegateMutation.isPending}>
                {delegateMutation.isPending ? "Configuring..." : "Configure Delegation"}
              </Button>
            </form>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default Users;
