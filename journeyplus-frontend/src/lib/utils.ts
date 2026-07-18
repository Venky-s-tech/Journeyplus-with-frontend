import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// The backend's GlobalExceptionHandler returns Spring's ProblemDetail
// (RFC 7807) format for IllegalArgumentException/IllegalStateException/
// AccessDeniedException/BadCredentialsException/generic Exception - all of
// which put the human-readable reason in a `detail` field, not `message`.
// Only HttpMessageNotReadableException uses a plain `message` field. Every
// error handler in this app was checking `.message` first, which is
// undefined for the vast majority of backend errors (400/401/403/409/500),
// so users only ever saw the generic fallback text and never the real
// reason the backend rejected the request. Use this everywhere instead of
// reaching into err.response?.data?.message directly.
export function getErrorMessage(err: any, fallback: string): string {
  return (
    err?.response?.data?.detail ||
    err?.response?.data?.message ||
    fallback
  );
}

export function formatCurrency(amount: number, currency: string = "USD") {
  // Only a well-formed 3-letter alpha code is a valid ISO 4217 currency;
  // anything else (missing, malformed, or garbage values from bad/mock
  // data) falls back to USD instead of throwing and crashing the tree.
  const isValidCode = typeof currency === "string" && /^[A-Za-z]{3}$/.test(currency);
  const code = isValidCode ? currency.toUpperCase() : "USD";
  const safeAmount = typeof amount === "number" && !isNaN(amount) ? amount : 0;

  try {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: code,
    }).format(safeAmount);
  } catch {
    // Final safety net in case Intl still rejects the code for some
    // locale-specific reason.
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(safeAmount);
  }
}

export function formatDate(dateStr: string) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  // Ensure invalid dates do not break the UI
  if (isNaN(date.getTime())) return dateStr;
  return date.toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}
