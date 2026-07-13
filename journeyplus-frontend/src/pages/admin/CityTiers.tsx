import React, { useState } from "react";
import { useCityTiers } from "../../hooks";
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
import { CityTier } from "../../types";
import { formatCurrency } from "../../lib/utils";

export const CityTiers: React.FC = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [isOpen, setIsOpen] = useState(false);

  // Form states
  const [cityName, setCityName] = useState("");
  const [country, setCountry] = useState("");
  const [tier, setTier] = useState<"TIER1" | "TIER2" | "TIER3">("TIER1");
  const [perDiemRate, setPerDiemRate] = useState(100);
  const [hotelCap, setHotelCap] = useState(150);

  const { data: cityTiers, isLoading } = useCityTiers();

  const createMutation = useMutation({
    mutationFn: adminApi.createCityTier,
    onSuccess: () => {
      toast("City tier configured successfully", "success", "Configured");
      setIsOpen(false);
      setCityName("");
      setCountry("");
      setPerDiemRate(100);
      setHotelCap(150);
      queryClient.invalidateQueries({ queryKey: ["admin", "city-tiers"] });
    },
    onError: (err: any) => {
      toast(err.response?.data?.message || "Failed to create city tier", "error");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: adminApi.deleteCityTier,
    onSuccess: () => {
      toast("City tier configuration removed", "success");
      queryClient.invalidateQueries({ queryKey: ["admin", "city-tiers"] });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({
      cityName,
      country,
      tier,
      perDiemRate: Number(perDiemRate),
      hotelCapPerNight: Number(hotelCap),
    });
  };

  const columns = [
    {
      header: "City ID",
      accessor: (ct: CityTier) => <span className="font-semibold">#{ct.id}</span>,
    },
    {
      header: "Location",
      accessor: (ct: CityTier) => <span>{ct.cityName}, {ct.country}</span>,
    },
    {
      header: "Tier",
      accessor: (ct: CityTier) => <span className="text-xs font-semibold">{ct.tier}</span>,
    },
    {
      header: "Per Diem Limit",
      accessor: (ct: CityTier) => <span>{formatCurrency(ct.perDiemRate)} / day</span>,
    },
    {
      header: "Hotel Night Cap",
      accessor: (ct: CityTier) => <span>{formatCurrency(ct.hotelCapPerNight)} / night</span>,
    },
    {
      header: "Action",
      accessor: (ct: CityTier) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="destructive" onClick={() => deleteMutation.mutate(ct.id)}>
            Delete
          </Button>
        </div>
      ),
      align: "right" as const,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Location City Tiers</h1>
          <p className="text-xs text-muted-foreground">
            Manage international and domestic location tiers, daily food per-diems, and hotel room cost limits.
          </p>
        </div>

        <Dialog open={isOpen} onOpenChange={setIsOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="h-4 w-4" /> Add City Tier
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Configure City Tier caps</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label>City Name</Label>
                  <Input required placeholder="Bangalore" value={cityName} onChange={(e) => setCityName(e.target.value)} />
                </div>
                <div className="space-y-1">
                  <Label>Country</Label>
                  <Input required placeholder="India" value={country} onChange={(e) => setCountry(e.target.value)} />
                </div>
              </div>

              <div className="space-y-1">
                <Label>Tier Classification</Label>
                <select
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                  value={tier}
                  onChange={(e: any) => setTier(e.target.value)}
                >
                  <option value="TIER1">Tier 1 - High Expense Metro</option>
                  <option value="TIER2">Tier 2 - Mid Expense Metro</option>
                  <option value="TIER3">Tier 3 - Regional/Suburban</option>
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label>Per Diem Rate (USD)</Label>
                  <Input type="number" required value={perDiemRate} onChange={(e) => setPerDiemRate(Number(e.target.value))} />
                </div>
                <div className="space-y-1">
                  <Label>Hotel Cap Per Night (USD)</Label>
                  <Input type="number" required value={hotelCap} onChange={(e) => setHotelCap(Number(e.target.value))} />
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit">Configure</Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <DataTable
        columns={columns}
        data={cityTiers}
        isLoading={isLoading}
        emptyMessage="No location city tiers configured."
      />
    </div>
  );
};

export default CityTiers;
