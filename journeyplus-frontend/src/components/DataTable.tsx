import React from "react";

export interface Column<T> {
  header: string;
  accessor: (item: T) => React.ReactNode;
  align?: "left" | "center" | "right";
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[] | undefined;
  isLoading: boolean;
  emptyMessage?: string;
}

export function DataTable<T>({
  columns,
  data,
  isLoading,
  emptyMessage = "No records found.",
}: DataTableProps<T>) {
  if (isLoading) {
    return (
      <div className="w-full border border-border rounded-lg bg-card overflow-hidden">
        <table className="w-full border-collapse">
          <thead>
            <tr className="border-b border-border bg-muted/50">
              {columns.map((col, idx) => (
                <th key={idx} className="p-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {[1, 2, 3, 4, 5].map((rowIdx) => (
              <tr key={rowIdx} className="border-b border-border">
                {columns.map((_, colIdx) => (
                  <td key={colIdx} className="p-4">
                    <div className="h-4 bg-muted animate-pulse rounded w-3/4"></div>
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  }

  if (!data || data.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-12 border border-border rounded-lg bg-card text-center">
        <span className="text-sm text-muted-foreground">{emptyMessage}</span>
      </div>
    );
  }

  return (
    <div className="w-full border border-border rounded-lg bg-card overflow-x-auto shadow-sm">
      <table className="w-full border-collapse min-w-[600px]">
        <thead>
          <tr className="border-b border-border bg-muted/40">
            {columns.map((col, idx) => (
              <th
                key={idx}
                className={`p-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground text-${
                  col.align || "left"
                }`}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {data.map((item, rowIdx) => (
            <tr key={rowIdx} className="hover:bg-muted/30 transition-colors">
              {columns.map((col, colIdx) => (
                <td
                  key={colIdx}
                  className={`p-4 text-sm text-foreground text-${col.align || "left"}`}
                >
                  {col.accessor(item)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
export default DataTable;
