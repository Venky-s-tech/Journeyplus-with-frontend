import React, { useState } from "react";
import { usePolicies, useGrades } from "../../hooks";
import * as adminApi from "../../api/admin";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { DataTable } from "../../components/DataTable";
import { StatusBadge } from "../../components/StatusBadge";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";
import { Plus } from "lucide-react";
import { TravelPolicy } from "../../types";
import { formatCurrency } from "../../lib/utils";

export const Policies: React.FC = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [isOpen, setIsOpen] = useState(false);

  // Form states
  const [policyName, setPolicyName] = useState("");
  const [description, setDescription] = useState("");
  const [gradeId, setGradeId] = useState("G2");
  const [travelType, setTravelType] = useState<"DOMESTIC" | "INTERNATIONAL">("DOMESTIC");
  const [flightClass, setFlightClass] = useState("ECONOMY");
  const [hotelCategory, setHotelCategory] = useState("STANDARD");
  const [perDiemRate, setPerDiemRate] = useState(100);
  const [conveyanceLimit, setConveyanceLimit] = useState(50);
  const [maxAmount, setMaxAmount] = useState(1000);
  const [visaReq, setVisaReq] = useState(false);

  const { data: policies, isLoading } = usePolicies();
  const { data: grades } = useGrades();

  const createMutation = useMutation({
    mutationFn: adminApi.createTravelPolicy,
    onSuccess: () => {
      toast("Travel policy created successfully", "success", "Created");
      setIsOpen(false);
      setPolicyName("");
      setDescription("");
      setPerDiemRate(100);
      setConveyanceLimit(50);
      setMaxAmount(1000);
      setVisaReq(false);
      queryClient.invalidateQueries({ queryKey: ["admin", "policies"] });
    },
    onError: (err: any) => {
      toast(err.response?.data?.message || "Failed to create policy", "error");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: adminApi.deleteTravelPolicy,
    onSuccess: () => {
      toast("Policy deactivated successfully", "success");
      queryClient.invalidateQueries({ queryKey: ["admin", "policies"] });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({
      policyName,
      description,
      gradeId,
      travelType,
      flightClass: flightClass as any,
      hotelCategory: hotelCategory as any,
      perDiemRate: Number(perDiemRate),
      localConveyanceLimit: Number(conveyanceLimit),
      status: "ACTIVE",
      maxAmountPerTrip: Number(maxAmount),
      requiresVisaVerification: visaReq,
    });
  };

  const columns = [
    {
      header: "ID",
      accessor: (p: TravelPolicy) => <span className="font-semibold">#{p.id}</span>,
    },
    {
      header: "Policy Name",
      accessor: (p: TravelPolicy) => <span>{p.policyName}</span>,
    },
    {
      header: "Grade Scope",
      accessor: (p: TravelPolicy) => <span className="text-xs font-semibold">{p.gradeId}</span>,
    },
    {
      header: "Travel Type",
      accessor: (p: TravelPolicy) => <span className="text-xs">{p.travelType}</span>,
    },
    {
      header: "Flight Class / Hotel Class",
      accessor: (p: TravelPolicy) => (
        <span className="text-xs text-muted-foreground">
          {p.flightClass} / {p.hotelCategory}
        </span>
      ),
    },
    {
      header: "Max Trip Cap",
      accessor: (p: TravelPolicy) => <span className="font-semibold">{formatCurrency(p.maxAmountPerTrip)}</span>,
    },
    {
      header: "Visa Verified",
      accessor: (p: TravelPolicy) => (
        <span>{p.requiresVisaVerification ? "Yes" : "No"}</span>
      ),
    },
    {
      header: "Status",
      accessor: (p: TravelPolicy) => <StatusBadge status={p.status} />,
    },
    {
      header: "Action",
      accessor: (p: TravelPolicy) => (
        <div className="flex gap-2 justify-end">
          {p.status === "ACTIVE" && (
            <Button size="sm" variant="destructive" onClick={() => deleteMutation.mutate(p.id)}>
              Deactivate
            </Button>
          )}
        </div>
      ),
      align: "right" as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Travel Policies & Entitlements</h1>
          <p className="text-xs text-muted-foreground">
            Configure travel budgets, booking grade privileges, conveyance allowances, and compliance thresholds.
          </p>
        </div>

        <Dialog open={isOpen} onOpenChange={setIsOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="h-4 w-4" /> Add Policy
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Configure Travel Policy</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div className="space-y-1">
                <Label>Policy Name</Label>
                <Input required placeholder="Standard Professional Policy" value={policyName} onChange={(e) => setPolicyName(e.target.value)} />
              </div>

              <div className="space-y-1">
                <Label>Description</Label>
                <Input placeholder="Travel allowance details" value={description} onChange={(e) => setDescription(e.target.value)} />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label>Grade Band</Label>
                  <select
                    className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                    value={gradeId}
                    onChange={(e) => setGradeId(e.target.value)}
                  >
                    {grades?.map((g) => (
                      <option key={g.id} value={g.id}>
                        {g.gradeName} ({g.id})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-1">
                  <Label>Travel Type</Label>
                  <select
                    className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                    value={travelType}
                    onChange={(e: any) => setTravelType(e.target.value)}
                  >
                    <option value="DOMESTIC">Domestic</option>
                    <option value="INTERNATIONAL">International</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label>Flight Seat Privilege</Label>
                  <select
                    className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                    value={flightClass}
                    onChange={(e) => setFlightClass(e.target.value)}
                  >
                    <option value="ECONOMY">Economy</option>
                    <option value="BUSINESS">Business</option>
                    <option value="FIRST">First Class</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <Label>Hotel Standard Privilege</Label>
                  <select
                    className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                    value={hotelCategory}
                    onChange={(e) => setHotelCategory(e.target.value)}
                  >
                    <option value="STANDARD">Standard</option>
                    <option value="PREMIUM">Premium</option>
                    <option value="LUXURY">Luxury</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div className="space-y-1">
                  <Label>Per Diem Rate</Label>
                  <Input type="number" required value={perDiemRate} onChange={(e) => setPerDiemRate(Number(e.target.value))} />
                </div>
                <div className="space-y-1">
                  <Label>Local Cab Cap</Label>
                  <Input type="number" required value={conveyanceLimit} onChange={(e) => setConveyanceLimit(Number(e.target.value))} />
                </div>
                <div className="space-y-1">
                  <Label>Max Trip Cost</Label>
                  <Input type="number" required value={maxAmount} onChange={(e) => setMaxAmount(Number(e.target.value))} />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <input
                  type="checkbox"
                  id="visa"
                  checked={visaReq}
                  onChange={(e) => setVisaReq(e.target.checked)}
                  className="rounded border-input text-primary focus:ring-1 focus:ring-ring"
                />
                <Label htmlFor="visa" className="cursor-pointer">Requires Visa Verification</Label>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit">Create Policy</Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <DataTable
        columns={columns}
        data={policies}
        isLoading={isLoading}
        emptyMessage="No corporate policies configured."
      />
    </div>
  );
};

export default Policies;
