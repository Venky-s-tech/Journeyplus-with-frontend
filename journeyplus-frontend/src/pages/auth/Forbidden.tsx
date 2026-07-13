import React from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "../../components/ui/button";
import { ShieldAlert } from "lucide-react";

export const Forbidden: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-background text-foreground p-4 text-center">
      <div className="mx-auto h-16 w-16 text-destructive mb-4">
        <ShieldAlert className="h-full w-full animate-pulse" />
      </div>
      <h1 className="text-3xl font-bold tracking-tight mb-2">403 Access Denied</h1>
      <p className="text-muted-foreground text-sm max-w-md mb-6">
        You do not have permission to view this resource. Your role does not grant access to this page.
      </p>
      <Button onClick={() => navigate("/")}>
        Go to Dashboard
      </Button>
    </div>
  );
};

export default Forbidden;
