import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { registerUser } from "../../api/auth";
import { useToast } from "../../components/ui/toast";
import { Button } from "../../components/ui/button";
import { Input } from "../../components/ui/input";
import { Label } from "../../components/ui/label";
import { Shield } from "lucide-react";

const registerSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
  email: z.string().email("Invalid email address"),
  password: z.string().min(6, "Password must be at least 6 characters"),
  name: z.string().min(1, "Full Name is required"),
  phone: z.string().min(10, "Phone number must be at least 10 digits"),
  role: z.enum(["EMPLOYEE", "TRAVEL_DESK", "APPROVING_MANAGER", "FINANCE", "COMPLIANCE", "ADMIN"]),
  gradeId: z.string().min(1, "Grade is required"),
  departmentId: z.string().min(1, "Department is required"),
});

type RegisterFields = z.infer<typeof registerSchema>;

export const Register: React.FC = () => {
  const { toast } = useToast();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successInfo, setSuccessInfo] = useState<{ submitted: boolean; autoActive: boolean } | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFields>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      role: "EMPLOYEE",
      gradeId: "G2",
      departmentId: "DEPT-GEN",
    },
  });

  const onSubmit = async (data: RegisterFields) => {
    setIsSubmitting(true);
    try {
      await registerUser(data);
      const isEmployee = data.role === "EMPLOYEE";
      setSuccessInfo({ submitted: true, autoActive: isEmployee });
      toast(
        isEmployee
          ? "Account registered successfully!"
          : "Registration submitted for admin approval.",
        "success",
        "Registration Complete"
      );
    } catch (e: any) {
      console.error(e);
      const errMsg = e.response?.data?.message || "Registration failed. Try again.";
      toast(errMsg, "error", "Error");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (successInfo?.submitted) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-muted/30 p-4">
        <div className="w-full max-w-md rounded-lg border border-border bg-card p-8 shadow-lg text-center space-y-4">
          <div className="mx-auto h-12 w-12 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center text-green-600 dark:text-green-400">
            ✓
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Registration Complete</h1>
          {successInfo.autoActive ? (
            <p className="text-sm text-muted-foreground">
              Your account is now active! You can sign in immediately using your username and password.
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">
              Your registration has been submitted. Since you requested an administrative/privileged role, your account will remain pending until an administrator reviews and approves it.
            </p>
          )}
          <Button onClick={() => navigate("/login")} className="w-full">
            Proceed to Login
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/30 p-4">
      <div className="w-full max-w-lg rounded-lg border border-border bg-card p-8 shadow-lg">
        {/* Header */}
        <div className="flex flex-col items-center gap-2 text-center mb-6">
          <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
            <Shield className="h-5 w-5" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Create your account</h1>
          <p className="text-xs text-muted-foreground">
            Sign up for the JourneyPlus Corporate Expense System
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label htmlFor="username">Username</Label>
              <Input id="username" placeholder="e.g. jdoe" {...register("username")} />
              {errors.username && <p className="text-xs text-destructive">{errors.username.message}</p>}
            </div>

            <div className="space-y-1">
              <Label htmlFor="name">Full Name</Label>
              <Input id="name" placeholder="John Doe" {...register("name")} />
              {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" placeholder="john@example.com" {...register("email")} />
              {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
            </div>

            <div className="space-y-1">
              <Label htmlFor="phone">Phone</Label>
              <Input id="phone" placeholder="+1234567890" {...register("phone")} />
              {errors.phone && <p className="text-xs text-destructive">{errors.phone.message}</p>}
            </div>
          </div>

          <div className="space-y-1">
            <Label htmlFor="password">Password</Label>
            <Input id="password" type="password" placeholder="••••••••" {...register("password")} />
            {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="space-y-1">
              <Label htmlFor="role">Role</Label>
              <select
                id="role"
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring text-foreground"
                {...register("role")}
              >
                <option value="EMPLOYEE">Employee</option>
                <option value="TRAVEL_DESK">Travel Desk</option>
                <option value="APPROVING_MANAGER">Manager</option>
                <option value="FINANCE">Finance</option>
                <option value="COMPLIANCE">Compliance</option>
                <option value="ADMIN">Admin</option>
              </select>
              {errors.role && <p className="text-xs text-destructive">{errors.role.message}</p>}
            </div>

            <div className="space-y-1">
              <Label htmlFor="gradeId">Grade</Label>
              <select
                id="gradeId"
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring text-foreground"
                {...register("gradeId")}
              >
                <option value="G1">G1 - Associate</option>
                <option value="G2">G2 - Professional</option>
                <option value="G3">G3 - Manager</option>
                <option value="G4">G4 - Executive</option>
              </select>
              {errors.gradeId && <p className="text-xs text-destructive">{errors.gradeId.message}</p>}
            </div>

            <div className="space-y-1">
              <Label htmlFor="departmentId">Department</Label>
              <select
                id="departmentId"
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring text-foreground"
                {...register("departmentId")}
              >
                <option value="DEPT-GEN">General</option>
                <option value="DEPT-IT">IT Services</option>
                <option value="DEPT-HR">Human Resources</option>
                <option value="DEPT-FIN">Finance & Accounting</option>
              </select>
              {errors.departmentId && <p className="text-xs text-destructive">{errors.departmentId.message}</p>}
            </div>
          </div>

          <Button type="submit" className="w-full mt-4" disabled={isSubmitting}>
            {isSubmitting ? "Creating account..." : "Register"}
          </Button>
        </form>

        {/* Footer */}
        <div className="mt-6 text-center text-xs text-muted-foreground">
          Already have an account?{" "}
          <Link to="/login" className="font-semibold text-primary hover:underline">
            Sign in here
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
