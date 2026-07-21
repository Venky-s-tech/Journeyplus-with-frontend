import React, { useRef, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../../lib/auth-context";
import {
  useClaim,
  useClaimLines,
  useAddClaimLine,
  useUpdateClaimLine,
  useDeleteClaimLine,
  useUploadLineReceipt,
  useDeleteLineReceipt,
  useSubmitClaimLine,
  useSubmitClaim,
  useApproveClaim,
  useRejectClaim,
  useReimburseClaim,
  useTrip,
} from "../../hooks";
import { uploadReceipt, getLineReceiptUrl } from "../../api/expenses";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { StatusBadge } from "../../components/StatusBadge";
import { formatCurrency, formatDate } from "../../lib/utils";
import {
  Receipt,
  Plus,
  AlertTriangle,
  DollarSign,
  Paperclip,
  FileText,
  Image as ImageIcon,
  X,
  Briefcase,
  Pencil,
  Trash2,
  CheckCircle2,
  Eye,
  Download,
  RefreshCw,
} from "lucide-react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../../components/ui/dialog";

// Item 4: allowed document formats for expense line attachments.
const ALLOWED_FILE_TYPES = ["application/pdf", "image/jpeg", "image/png"];
const ALLOWED_FILE_EXTENSIONS = ".pdf,.jpg,.jpeg,.png";

export const ExpenseDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();
  const claimId = Number(id);

  const { data: claim, isLoading, error } = useClaim(claimId);

  // FIX ("lines adding but not showing"): the backend's ExpenseClaim entity
  // has no `expenseLines` collection field at all - GET /api/expenses/{id}
  // never includes lines, no matter how many exist or how it's refetched.
  // Lines only ever come back from the separate GET /api/expenses/{id}/lines
  // endpoint, which is what useClaimLines calls.
  const { data: lines, isLoading: linesLoading } = useClaimLines(claimId);

  // "Trip details need to show" on the Details view + resolving the
  // approver's username: the ExpenseClaim response has no usable approver
  // username field, so fetch the linked trip (which has approver.username).
  const { data: linkedTrip } = useTrip(claim?.tripRequestId || 0);

  // Mutations
  const addLineMutation = useAddClaimLine(claimId);
  const updateLineMutation = useUpdateClaimLine(claimId);
  const deleteLineMutation = useDeleteClaimLine(claimId);
  const uploadLineReceiptMutation = useUploadLineReceipt(claimId);
  const deleteLineReceiptMutation = useDeleteLineReceipt(claimId);
  const submitLineMutation = useSubmitClaimLine(claimId);
  const submitClaimMutation = useSubmitClaim();
  const approveClaimMutation = useApproveClaim();
  const rejectClaimMutation = useRejectClaim();
  const reimburseClaimMutation = useReimburseClaim();

  // Hidden file input refs per line item
  const lineFileInputRefs = useRef<{ [lineId: number]: HTMLInputElement | null }>({});

  const handleLineFileChange = (lineId: number, e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    uploadLineReceiptMutation.mutate(
      { lineId, file },
      {
        onSuccess: () => {
          toast("Receipt uploaded & compliance re-evaluated", "success", "Receipt Updated");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Failed to upload receipt", "error");
        },
      }
    );
  };

  const handleLineFileDelete = (lineId: number) => {
    if (confirm("Remove receipt document from this expense line?")) {
      deleteLineReceiptMutation.mutate(lineId, {
        onSuccess: () => {
          toast("Receipt removed from line item", "success", "Receipt Removed");
        },
      });
    }
  };

  // State
  const [isLineDialogOpen, setIsLineDialogOpen] = useState(false);
  const [editingLineId, setEditingLineId] = useState<number | null>(null);
  const [isReimburseDialogOpen, setIsReimburseDialogOpen] = useState(false);
  const [managerComment, setManagerComment] = useState("");

  // Line Form states
  const [expenseDate, setExpenseDate] = useState("");
  const [category, setCategory] = useState<"ACCOMMODATION" | "MEALS" | "TRANSPORT" | "VISA" | "MISC">("MEALS");
  const [amount, setAmount] = useState(0);
  const [merchant, setMerchant] = useState("");
  const [description, setDescription] = useState("");
  const [justification, setJustification] = useState("");
  const [receiptPath, setReceiptPath] = useState("");
  const [receiptRef, setReceiptRef] = useState("");
  const [receiptFileName, setReceiptFileName] = useState("");
  const [receiptFileType, setReceiptFileType] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Reimburse form states
  const [paymentMethod, setPaymentMethod] = useState("BANK_TRANSFER");
  const [txnRef, setTxnRef] = useState("");
  const [reimbAmount, setReimbAmount] = useState(0);

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
      </div>
    );
  }

  if (error || !claim) {
    return (
      <div className="p-6 text-center bg-card border rounded-lg border-border">
        <p className="text-sm text-destructive">Error loading expense claim details.</p>
        <Button onClick={() => navigate("/expenses")} className="mt-4">
          Back to List
        </Button>
      </div>
    );
  }

  const isEmployee = user?.role === "EMPLOYEE";
  const isApprover = user?.role === "APPROVING_MANAGER";
  const isFinance = user?.role === "FINANCE";
  const expenseLines = lines || [];

  const advanceAdjusted = claim.advanceAdjusted ?? 0;
  const netReimbursable = claim.netReimbursable ?? claim.totalAmount;
  const approverUsername = claim.approverUsername || linkedTrip?.approver?.username || "—";

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!ALLOWED_FILE_TYPES.includes(file.type)) {
      toast("Only PDF, JPEG, or PNG files are allowed", "error", "Invalid File Type");
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    setIsUploading(true);
    try {
      const data = await uploadReceipt(file);
      setReceiptPath(data.filePath);
      setReceiptRef(data.receiptRef);
      setReceiptFileName(file.name);
      setReceiptFileType(file.type);
      toast("Receipt uploaded successfully", "success", "Uploaded");
    } catch (err) {
      toast("Receipt upload failed", "error", "Error");
    } finally {
      setIsUploading(false);
    }
  };

  const handleRemoveAttachment = () => {
    setReceiptPath("");
    setReceiptRef("");
    setReceiptFileName("");
    setReceiptFileType("");
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleOpenAddLine = () => {
    setEditingLineId(null);
    setCategory("MEALS");
    setExpenseDate(new Date().toISOString().split("T")[0]);
    setAmount(0);
    setMerchant("");
    setDescription("");
    setJustification("");
    handleRemoveAttachment();
    setIsLineDialogOpen(true);
  };

  const handleOpenEditLine = (line: any) => {
    setEditingLineId(line.id);
    setCategory(line.category || "MEALS");
    setExpenseDate(line.expenseDate || "");
    setAmount(line.amount || 0);
    setMerchant(line.merchant || "");
    setDescription(line.description || "");
    setJustification(line.justification || "");
    setReceiptPath(line.receiptPath || "");
    setReceiptRef(line.receiptRef || line.receiptPath || "");
    if (line.receiptRef || line.receiptPath) {
      setReceiptFileName("Receipt Document");
    } else {
      setReceiptFileName("");
    }
    setIsLineDialogOpen(true);
  };

  const handleSaveLine = (e: React.FormEvent) => {
    e.preventDefault();
    const lineData = {
      expenseDate,
      category,
      amount: Number(amount),
      originalCurrency: "USD",
      merchant,
      description,
      justification,
      receiptRef,
      receiptPath,
    };

    if (editingLineId) {
      updateLineMutation.mutate(
        { lineId: editingLineId, data: lineData },
        {
          onSuccess: () => {
            toast("Expense line item updated", "success", "Updated");
            setIsLineDialogOpen(false);
            setEditingLineId(null);
          },
          onError: (err: any) => {
            toast(err.response?.data?.message || "Failed to update line item", "error");
          },
        }
      );
    } else {
      addLineMutation.mutate(lineData, {
        onSuccess: () => {
          toast("Expense line item added", "success", "Added");
          setIsLineDialogOpen(false);
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Failed to add line item", "error");
        },
      });
    }
  };

  const handleDeleteLine = (lineId: number) => {
    if (confirm("Are you sure you want to delete this expense line?")) {
      deleteLineMutation.mutate(lineId, {
        onSuccess: () => {
          toast("Expense line deleted", "success", "Deleted");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Failed to delete expense line", "error");
        },
      });
    }
  };

  const handleSubmitLine = (lineId: number) => {
    submitLineMutation.mutate(lineId, {
      onSuccess: () => {
        toast("Compliance check triggered for line", "success", "Checked");
      },
    });
  };

  const handleSubmitClaim = () => {
    submitClaimMutation.mutate(claimId, {
      onSuccess: () => {
        toast("Expense claim submitted to manager", "success", "Submitted");
      },
      onError: (err: any) => {
        toast(err.response?.data?.message || "Failed to submit claim", "error");
      },
    });
  };

  const handleManagerAction = (approve: boolean) => {
    const mutation = approve ? approveClaimMutation : rejectClaimMutation;
    mutation.mutate(
      { id: claimId, comments: managerComment },
      {
        onSuccess: () => {
          toast(approve ? "Claim approved" : "Claim rejected", "success", "Success");
          setManagerComment("");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Action failed", "error");
        },
      }
    );
  };

  const handleReimburseSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    reimburseClaimMutation.mutate(
      {
        claimId,
        paymentMethod,
        transactionReference: txnRef,
        amount: Number(reimbAmount),
      },
      {
        onSuccess: () => {
          toast("Reimbursement payment processed", "success", "Paid");
          setIsReimburseDialogOpen(false);
          setTxnRef("");
        },
        onError: (err: any) => {
          toast(err.response?.data?.message || "Payment failed", "error");
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      {/* Top Header Card */}
      <div className="p-6 bg-card border border-border rounded-lg shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold">{claim.claimTitle}</h1>
            <StatusBadge status={claim.status} />
          </div>
          <p className="text-xs text-muted-foreground">
            ID: #{claim.id} {claim.tripRequestId != null && <>| Trip: #{claim.tripRequestId}</>} | Submitted: {formatDate(claim.submittedDate)}
          </p>
        </div>

        {/* Action controls */}
        <div className="flex flex-wrap gap-2">
          {isEmployee && claim.status === "DRAFT" && (
            <>
              <Button onClick={handleSubmitClaim} disabled={expenseLines.length === 0}>
                Submit entire Claim
              </Button>
            </>
          )}

          {isApprover && claim.status === "SUBMITTED" && (
            <div className="flex flex-col gap-2 w-full sm:w-auto">
              <Input
                placeholder="Review comments..."
                value={managerComment}
                onChange={(e) => setManagerComment(e.target.value)}
                className="w-full sm:w-64 h-9"
              />
              <div className="flex gap-2">
                <Button onClick={() => handleManagerAction(true)} className="bg-green-600 hover:bg-green-700 flex-1 sm:flex-initial">
                  Approve Claim
                </Button>
                <Button onClick={() => handleManagerAction(false)} variant="destructive" className="flex-1 sm:flex-initial">
                  Reject Claim
                </Button>
              </div>
            </div>
          )}

          {isFinance && claim.status === "APPROVED" && (
            <Button
              onClick={() => {
                setReimbAmount(claim.totalAmount);
                setIsReimburseDialogOpen(true);
              }}
              className="bg-purple-600 hover:bg-purple-700"
            >
              Process Payment
            </Button>
          )}

          <Button variant="outline" onClick={() => navigate(isFinance ? "/finance/expenses" : "/expenses")}>
            Back
          </Button>
        </div>
      </div>

      {/* Main layout */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Left Side: Summary figures */}
        <div className="md:col-span-1 space-y-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <h2 className="text-sm font-semibold border-b pb-2 flex items-center gap-2">
              <DollarSign className="h-4 w-4 text-muted-foreground" /> Claim Calculation
            </h2>
            <div className="space-y-3 text-xs">
              {/* Item 3: Total Amount - gross total of all expense lines */}
              <div className="flex justify-between">
                <span className="text-muted-foreground">Total Amount:</span>
                <span className="font-medium">{formatCurrency(claim.totalAmount, claim.originalCurrency)}</span>
              </div>
              {/* Item 3: Advance Claimed - amount deducted from an associated advance */}
              <div className="flex justify-between">
                <span className="text-muted-foreground">Advance Claimed:</span>
                <span className="font-medium">{formatCurrency(advanceAdjusted, claim.originalCurrency)}</span>
              </div>
              {/* Item 3: Net Reimbursed - Total Amount minus Advance Claimed */}
              <div className="flex justify-between border-t pt-2 mt-2">
                <span className="text-muted-foreground">Net Reimbursed:</span>
                <span className="font-bold text-lg text-primary">{formatCurrency(netReimbursable, claim.originalCurrency)}</span>
              </div>
              {/* Item 3: Approver Username */}
              <div className="flex justify-between border-t pt-2 mt-2">
                <span className="text-muted-foreground">Approver Username:</span>
                <span className="font-medium">{approverUsername}</span>
              </div>
            </div>
          </div>

          {claim.reimbursement && (
            <div className="p-4 bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-900 rounded-lg shadow-sm space-y-2">
              <h2 className="text-xs font-bold text-emerald-700 dark:text-emerald-400">Payment Processed</h2>
              <div className="space-y-1 text-[11px] text-emerald-800 dark:text-emerald-300">
                <p>Method: {claim.reimbursement.paymentMethod}</p>
                <p>Reference: {claim.reimbursement.transactionReference}</p>
                <p>Paid Amount: {formatCurrency(claim.reimbursement.amount)}</p>
              </div>
            </div>
          )}

          {/* "Trip id need to show, trip details need to show" on Details view */}
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <h2 className="text-sm font-semibold border-b pb-2 flex items-center gap-2">
              <Briefcase className="h-4 w-4 text-muted-foreground" /> Trip Details
            </h2>
            {claim.tripRequestId == null ? (
              <p className="text-xs text-muted-foreground py-2 text-center">No trip linked to this claim.</p>
            ) : !linkedTrip ? (
              <div className="flex h-16 items-center justify-center">
                <div className="h-5 w-5 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
              </div>
            ) : (
              <div className="space-y-2 text-xs">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Trip ID:</span>
                  <span className="font-semibold">#{linkedTrip.id}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Destination:</span>
                  <span className="font-medium">{linkedTrip.destination}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Purpose:</span>
                  <span className="font-medium text-right max-w-[60%]">{linkedTrip.purpose}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Travel Dates:</span>
                  <span className="font-medium">{formatDate(linkedTrip.departureDate)} - {formatDate(linkedTrip.returnDate)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Travel Type:</span>
                  <span className="font-medium">{linkedTrip.travelType}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Trip Status:</span>
                  <StatusBadge status={linkedTrip.status} />
                </div>
                <div className="flex justify-between pt-2 border-t mt-2">
                  <span className="text-muted-foreground">Employee:</span>
                  <span className="font-medium">{linkedTrip.employee?.username || "—"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Approver:</span>
                  <span className="font-medium">{linkedTrip.approver?.username || "—"}</span>
                </div>
                <div className="pt-2">
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full h-7 text-xs"
                    onClick={() => navigate(`/trips/${linkedTrip.id}`)}
                  >
                    Open Full Trip Record
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Side: Expense Line items */}
        <div className="md:col-span-2 space-y-4">
          <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
            <div className="flex items-center justify-between border-b pb-2">
              <h2 className="text-sm font-semibold flex items-center gap-2">
                <Receipt className="h-4 w-4 text-muted-foreground" /> Receipts & Invoices
              </h2>
              {isEmployee && claim.status === "DRAFT" && (
                <div className="flex items-center gap-2">
                  <Button size="sm" onClick={handleOpenAddLine} className="gap-1 h-7 text-xs bg-purple-600 hover:bg-purple-700">
                    <Plus className="h-3.5 w-3.5" /> Add Expense Item
                  </Button>
                </div>
              )}

              <Dialog
                open={isLineDialogOpen}
                onOpenChange={(open) => {
                  setIsLineDialogOpen(open);
                  if (!open) {
                    setEditingLineId(null);
                    handleRemoveAttachment();
                  }
                }}
              >
                <DialogContent className="max-w-md">
                  <DialogHeader>
                    <DialogTitle>{editingLineId ? "Edit Expense Line" : "Add Expense Line"}</DialogTitle>
                  </DialogHeader>
                  <form onSubmit={handleSaveLine} className="space-y-3 text-xs">
                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-1">
                        <Label>Category</Label>
                        <select
                          className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                          value={category}
                          onChange={(e: any) => setCategory(e.target.value)}
                        >
                          <option value="ACCOMMODATION">Accommodation</option>
                          <option value="MEALS">Meals / Food</option>
                          <option value="TRANSPORT">Transport / Conveyance</option>
                          <option value="VISA">Visa Fees</option>
                          <option value="MISC">Miscellaneous</option>
                        </select>
                      </div>

                      <div className="space-y-1">
                        <Label>Expense Date</Label>
                        <Input type="date" required value={expenseDate} onChange={(e) => setExpenseDate(e.target.value)} />
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div className="space-y-1">
                        <Label>Amount (USD)</Label>
                        <Input type="number" required min={1} value={amount} onChange={(e) => setAmount(Number(e.target.value))} />
                      </div>

                      <div className="space-y-1">
                        <Label>Merchant / Vendor</Label>
                        <Input placeholder="e.g. Uber, Hilton, Delta" value={merchant} onChange={(e) => setMerchant(e.target.value)} />
                      </div>
                    </div>

                    <div className="space-y-1">
                      <Label>Description / Item Details</Label>
                      <Input placeholder="e.g. Taxi fare to client site" value={description} onChange={(e) => setDescription(e.target.value)} />
                    </div>

                    <div className="space-y-1">
                      <Label>Justification (Optional)</Label>
                      <Input placeholder="Reason if exceeding standard policy" value={justification} onChange={(e) => setJustification(e.target.value)} />
                    </div>

                    {/* Receipt Attachment control */}
                    <div className="space-y-1">
                      <Label>Receipt Attachment</Label>
                      <input
                        ref={fileInputRef}
                        type="file"
                        accept={ALLOWED_FILE_EXTENSIONS}
                        onChange={handleFileUpload}
                        disabled={isUploading}
                        className="hidden"
                      />

                      {!receiptFileName && !receiptRef && !receiptPath ? (
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className="gap-1.5 h-8 text-xs w-full justify-center"
                          disabled={isUploading}
                          onClick={() => fileInputRef.current?.click()}
                        >
                          <Paperclip className="h-3.5 w-3.5" />
                          {isUploading ? "Uploading file..." : "Upload Receipt Document"}
                        </Button>
                      ) : (
                        <div className="flex items-center justify-between gap-2 p-2 border rounded-md bg-muted/20">
                          <div className="flex items-center gap-2 min-w-0">
                            {receiptFileType === "application/pdf" ? (
                              <FileText className="h-4 w-4 text-red-500 shrink-0" />
                            ) : (
                              <ImageIcon className="h-4 w-4 text-blue-500 shrink-0" />
                            )}
                            <span className="text-[11px] text-foreground truncate">{receiptFileName || "Receipt File Linked"}</span>
                          </div>
                          <button
                            type="button"
                            onClick={handleRemoveAttachment}
                            className="text-muted-foreground hover:text-destructive shrink-0"
                          >
                            <X className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      )}
                      <p className="text-[10px] text-muted-foreground">Formats allowed: PDF, JPEG, PNG. Essential for policy compliance.</p>
                    </div>

                    <div className="flex justify-end gap-2 pt-2">
                      <Button type="button" variant="outline" onClick={() => setIsLineDialogOpen(false)}>
                        Cancel
                      </Button>
                      <Button type="submit" disabled={addLineMutation.isPending || updateLineMutation.isPending || isUploading}>
                        {editingLineId ? "Update Item" : "Save Item"}
                      </Button>
                    </div>
                  </form>
                </DialogContent>
              </Dialog>
            </div>

            {/* List of Expense Lines */}
            <div className="divide-y divide-border">
              {linesLoading ? (
                <div className="flex h-16 items-center justify-center">
                  <div className="h-5 w-5 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
                </div>
              ) : expenseLines.length === 0 ? (
                <p className="text-xs text-muted-foreground py-6 text-center">No expense lines added yet. Click 'Add Expense Item' to begin logging receipts.</p>
              ) : (
                expenseLines.map((line) => {
                  const hasReceipt = !!(line.receiptRef || line.receiptPath);
                  const isCompliant = line.status === "INCLUDED" && line.policyCompliant;
                  const receiptUrl = getLineReceiptUrl(claimId, line.id);

                  return (
                    <div key={line.id} className="py-3 flex flex-col sm:flex-row sm:items-center justify-between text-xs gap-3">
                      {/* Hidden File Input for Line Receipt Upload/Replace */}
                      <input
                        type="file"
                        ref={(el) => { lineFileInputRefs.current[line.id] = el; }}
                        className="hidden"
                        accept={ALLOWED_FILE_EXTENSIONS}
                        onChange={(e) => handleLineFileChange(line.id, e)}
                      />

                      <div className="space-y-1 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="font-bold text-foreground">{line.category}</span>
                          {line.merchant && <span className="text-muted-foreground">• {line.merchant}</span>}

                          {/* Policy Status Badge */}
                          {isCompliant ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-100 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-400">
                              <CheckCircle2 className="h-3 w-3" /> Policy Compliant
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-100 dark:bg-amber-950/40 text-amber-700 dark:text-amber-400">
                              <AlertTriangle className="h-3 w-3" /> Flagged Exception
                            </span>
                          )}

                          {/* Receipt Status Badge & Action Controls */}
                          {hasReceipt ? (
                            <div className="inline-flex items-center gap-1">
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-medium bg-blue-100 dark:bg-blue-950/40 text-blue-700 dark:text-blue-400">
                                <Paperclip className="h-3 w-3" /> Receipt Available
                              </span>
                              
                              <Button
                                size="sm"
                                variant="ghost"
                                className="h-6 text-[10px] px-1.5 gap-1 text-blue-600 hover:text-blue-700"
                                onClick={() => window.open(receiptUrl, "_blank")}
                                title="View Receipt Document"
                              >
                                <Eye className="h-3 w-3" /> View
                              </Button>

                              <a
                                href={receiptUrl}
                                download
                                className="inline-flex items-center gap-1 h-6 text-[10px] px-1.5 font-medium text-muted-foreground hover:text-foreground"
                                title="Download Receipt"
                              >
                                <Download className="h-3 w-3" /> Download
                              </a>

                              {isEmployee && claim.status === "DRAFT" && (
                                <>
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    className="h-6 text-[10px] px-1.5 gap-1 text-muted-foreground hover:text-foreground"
                                    onClick={() => lineFileInputRefs.current[line.id]?.click()}
                                    title="Replace Receipt File"
                                    disabled={uploadLineReceiptMutation.isPending}
                                  >
                                    <RefreshCw className="h-3 w-3" /> Replace
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    className="h-6 text-[10px] px-1 text-destructive hover:bg-destructive/10"
                                    onClick={() => handleLineFileDelete(line.id)}
                                    title="Remove Receipt File"
                                    disabled={deleteLineReceiptMutation.isPending}
                                  >
                                    <X className="h-3 w-3" /> Remove
                                  </Button>
                                </>
                              )}
                            </div>
                          ) : (
                            <div className="inline-flex items-center gap-1.5">
                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold bg-red-100 dark:bg-red-950/40 text-red-700 dark:text-red-400">
                                <AlertTriangle className="h-3 w-3" /> Missing Receipt
                              </span>
                              {isEmployee && claim.status === "DRAFT" && (
                                <Button
                                  size="sm"
                                  variant="outline"
                                  className="h-6 text-[10px] px-2 gap-1 border-primary text-primary hover:bg-primary/10"
                                  onClick={() => lineFileInputRefs.current[line.id]?.click()}
                                  disabled={uploadLineReceiptMutation.isPending}
                                >
                                  <Paperclip className="h-3 w-3" /> Upload Receipt
                                </Button>
                              )}
                            </div>
                          )}
                        </div>

                        <p className="text-[11px] text-muted-foreground">
                          Date: {formatDate(line.expenseDate)} {line.description && `— ${line.description}`}
                        </p>

                        {line.complianceRemarks && !isCompliant && (
                          <p className="text-[10px] text-amber-600 dark:text-amber-400 flex items-center gap-1">
                            <AlertTriangle className="h-3 w-3 shrink-0" /> {line.complianceRemarks}
                          </p>
                        )}
                      </div>

                      <div className="flex items-center justify-between sm:justify-end gap-3 pt-2 sm:pt-0 border-t sm:border-0 border-border">
                        <span className="font-bold text-sm text-primary">{formatCurrency(line.amount, line.originalCurrency)}</span>

                        {isEmployee && claim.status === "DRAFT" && (
                          <div className="flex items-center gap-1">
                            <Button
                              size="sm"
                              variant="ghost"
                              className="h-7 w-7 p-0 text-muted-foreground hover:text-foreground"
                              onClick={() => handleOpenEditLine(line)}
                              title="Edit Expense Line"
                            >
                              <Pencil className="h-3.5 w-3.5" />
                            </Button>
                            <Button
                              size="sm"
                              variant="ghost"
                              className="h-7 w-7 p-0 text-muted-foreground hover:text-destructive"
                              onClick={() => handleDeleteLine(line.id)}
                              title="Delete Expense Line"
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </Button>
                            {!isCompliant && (
                              <Button size="sm" variant="outline" className="h-7 text-[10px] px-2" onClick={() => handleSubmitLine(line.id)}>
                                Re-audit
                              </Button>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Reimbursement Payment Processing Dialog */}
      <Dialog open={isReimburseDialogOpen} onOpenChange={setIsReimburseDialogOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Process Reimbursement Payment</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleReimburseSubmit} className="space-y-3 text-xs">
            <div className="space-y-1">
              <Label>Payment Mode</Label>
              <select
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors text-foreground"
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
              >
                <option value="BANK_TRANSFER">Bank Wire / Transfer</option>
                <option value="CREDIT_CARD">Corporate Card reimbursement</option>
                <option value="CASH">Petty Cash</option>
              </select>
            </div>

            <div className="space-y-1">
              <Label>Transaction Reference ID</Label>
              <Input
                required
                placeholder="TXN123456789"
                value={txnRef}
                onChange={(e) => setTxnRef(e.target.value)}
              />
            </div>

            <div className="space-y-1">
              <Label>Payment Amount (USD)</Label>
              <Input
                type="number"
                required
                value={reimbAmount}
                onChange={(e) => setReimbAmount(Number(e.target.value))}
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" onClick={() => setIsReimburseDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit">Disburse Payment</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ExpenseDetails;
