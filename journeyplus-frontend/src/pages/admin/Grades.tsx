import React, { useState } from "react";
import { useGrades } from "../../hooks";
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
import { Grade } from "../../types";
import { getErrorMessage } from "../../lib/utils";

export const Grades: React.FC = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [isOpen, setIsOpen] = useState(false);

  // Form states
  const [gradeId, setGradeId] = useState("");
  const [gradeName, setGradeName] = useState("");
  const [description, setDescription] = useState("");

  const { data: grades, isLoading } = useGrades();

  const createMutation = useMutation({
    mutationFn: adminApi.createGrade,
    onSuccess: () => {
      toast("Grade created successfully", "success", "Created");
      setIsOpen(false);
      setGradeId("");
      setGradeName("");
      setDescription("");
      queryClient.invalidateQueries({ queryKey: ["admin", "grades"] });
    },
    onError: (err: any) => {
      toast(getErrorMessage(err, "Failed to create grade"), "error");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: adminApi.deleteGrade,
    onSuccess: () => {
      toast("Grade deactivated successfully", "success", "Deactivated");
      queryClient.invalidateQueries({ queryKey: ["admin", "grades"] });
    },
  });

  // Bug #5: reactivate an inactive grade by setting its status back to Active.
  const activateMutation = useMutation({
    mutationFn: (g: Grade) =>
      adminApi.updateGrade(g.id, {
        gradeName: g.gradeName,
        description: g.description,
        status: "Active",
      }),
    onSuccess: () => {
      toast("Grade activated successfully", "success", "Activated");
      queryClient.invalidateQueries({ queryKey: ["admin", "grades"] });
    },
    onError: (err: any) => {
      toast(getErrorMessage(err, "Failed to activate grade"), "error");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({
      id: gradeId,
      gradeName,
      description,
      status: "Active",
    });
  };

  const columns = [
    {
      header: "Grade ID",
      accessor: (g: Grade) => <span className="font-semibold">{g.id}</span>,
    },
    {
      header: "Grade Title",
      accessor: (g: Grade) => <span>{g.gradeName}</span>,
    },
    {
      header: "Description",
      accessor: (g: Grade) => <span className="text-xs text-muted-foreground">{g.description}</span>,
    },
    {
      header: "Status",
      accessor: (g: Grade) => <StatusBadge status={g.status} />,
    },
    {
      header: "Action",
      accessor: (g: Grade) => (
        <div className="flex gap-2 justify-end">
          {g.status === "Active" ? (
            <Button size="sm" variant="destructive" onClick={() => deleteMutation.mutate(g.id)}>
              Deactivate
            </Button>
          ) : (
            <Button size="sm" onClick={() => activateMutation.mutate(g)}>
              Activate
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
          <h1 className="text-2xl font-bold tracking-tight">Grade Matrix Settings</h1>
          <p className="text-xs text-muted-foreground">
            Manage corporate employee bands, hierarchy levels, and active grades.
          </p>
        </div>

        <Dialog open={isOpen} onOpenChange={setIsOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="h-4 w-4" /> Create Grade
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-sm">
            <DialogHeader>
              <DialogTitle>New Grade Band</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div className="space-y-1">
                <Label>Grade ID Code</Label>
                <Input placeholder="e.g. G7" required value={gradeId} onChange={(e) => setGradeId(e.target.value)} />
              </div>
              <div className="space-y-1">
                <Label>Grade Name Title</Label>
                <Input placeholder="e.g. Senior Director" required value={gradeName} onChange={(e) => setGradeName(e.target.value)} />
              </div>
              <div className="space-y-1">
                <Label>Description</Label>
                <Input placeholder="Director band policies" value={description} onChange={(e) => setDescription(e.target.value)} />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={() => setIsOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit">Create Grade</Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <DataTable
        columns={columns}
        data={grades}
        isLoading={isLoading}
        emptyMessage="No corporate grades configured."
      />
    </div>
  );
};

export default Grades;
