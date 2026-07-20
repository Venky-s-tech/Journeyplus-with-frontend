import React from "react";
import {
  useTopTravellers,
  useAnalyticsSummary,
  useSpendByDepartment,
  useSpendByCategory,
  useMonthlyTrends,
} from "../../hooks";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from "recharts";
import { DataTable } from "../../components/DataTable";
import { formatCurrency } from "../../lib/utils";
import { TrendingUp, Award, Coins } from "lucide-react";

export const Analytics: React.FC = () => {
  const { data: topTravellers, isLoading } = useTopTravellers();
  const { data: summaryData } = useAnalyticsSummary();
  const { data: deptDataRaw } = useSpendByDepartment();
  const { data: categoryDataRaw } = useSpendByCategory();
  const { data: trendDataRaw } = useMonthlyTrends();

  // Premium HSL-tailored colors for chart cells
  const COLORS = ["#3b82f6", "#8b5cf6", "#10b981", "#f59e0b", "#ef4444"];

  const deptData = deptDataRaw || [];
  const categoryData = categoryDataRaw || [];
  const trendData = trendDataRaw || [];

  const budgetPct = summaryData?.budgetUtilisationPct ?? 0;
  const settlementPct = summaryData?.advanceSettlementRatePct ?? 0;
  const exceptionPct = summaryData?.policyExceptionRatePct ?? 0;

  const columns = [
    {
      header: "Rank",
      accessor: (_: any, idx?: number) => <span className="font-semibold">#{idx !== undefined ? idx + 1 : 1}</span>,
    },
    {
      header: "Traveller Name",
      accessor: (t: any) => <span>{t.employeeName || t.name || t.user?.name || "Corporate Traveller"}</span>,
    },
    {
      header: "Department",
      accessor: (t: any) => <span className="text-xs uppercase font-medium">{t.departmentId || "GENERAL"}</span>,
    },
    {
      header: "Total Cost Incurred",
      accessor: (t: any) => <span className="font-semibold text-primary">{formatCurrency(t.totalSpend || t.totalCost || 0)}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Analytics & Reports</h1>
        <p className="text-xs text-muted-foreground">
          Visual budget utilisation charts, department expenditures, and top corporate travelers list.
        </p>
      </div>

      {/* Top row cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-4 bg-card border border-border rounded-lg shadow-sm">
          <span className="text-[10px] uppercase font-bold text-muted-foreground">Budget Utilisation Rate</span>
          <p className="text-2xl font-bold">{budgetPct}%</p>
          <div className="w-full bg-muted h-2 rounded-full mt-2 overflow-hidden">
            <div className="bg-blue-600 h-full" style={{ width: `${Math.min(100, budgetPct)}%` }} />
          </div>
        </div>

        <div className="p-4 bg-card border border-border rounded-lg shadow-sm">
          <span className="text-[10px] uppercase font-bold text-muted-foreground">Advance Settlement Rate</span>
          <p className="text-2xl font-bold">{settlementPct}%</p>
          <div className="w-full bg-muted h-2 rounded-full mt-2 overflow-hidden">
            <div className="bg-green-600 h-full" style={{ width: `${Math.min(100, settlementPct)}%` }} />
          </div>
        </div>

        <div className="p-4 bg-card border border-border rounded-lg shadow-sm">
          <span className="text-[10px] uppercase font-bold text-muted-foreground">Policy Exception Rate</span>
          <p className="text-2xl font-bold text-destructive">{exceptionPct}%</p>
          <div className="w-full bg-muted h-2 rounded-full mt-2 overflow-hidden">
            <div className="bg-destructive h-full" style={{ width: `${Math.min(100, exceptionPct)}%` }} />
          </div>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Spend by Department */}
        <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
          <h2 className="text-sm font-semibold flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-muted-foreground" /> Spend by Department (USD)
          </h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={deptData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip formatter={(value) => formatCurrency(Number(value))} />
                <Bar dataKey="amount" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Spend Category Distribution */}
        <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
          <h2 className="text-sm font-semibold">Spend Categories Distribution</h2>
          <div className="h-64 flex flex-col sm:flex-row items-center justify-between">
            <div className="w-full sm:w-1/2 h-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {categoryData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => formatCurrency(Number(value))} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="w-full sm:w-1/2 flex flex-col gap-2 text-xs">
              {categoryData.map((item, index) => (
                <div key={item.name} className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="h-3 w-3 rounded-full shrink-0" style={{ backgroundColor: COLORS[index % COLORS.length] }} />
                    <span>{item.name}</span>
                  </div>
                  <span className="font-semibold">{formatCurrency(item.value)}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Spending Monthly Trend */}
        <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4 lg:col-span-2">
          <h2 className="text-sm font-semibold">Travel Expenditures Monthly Trend</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip formatter={(value) => formatCurrency(Number(value))} />
                <Line type="monotone" dataKey="amount" stroke="#8b5cf6" strokeWidth={2} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Top Travellers Board */}
      <div className="p-4 bg-card border border-border rounded-lg shadow-sm space-y-4">
        <h2 className="text-sm font-semibold flex items-center gap-2">
          <Award className="h-4 w-4 text-muted-foreground" /> Top Traveller Metrics
        </h2>
        {/* We map a list with index manually to include ranking */}
        <DataTable
          columns={columns}
          data={topTravellers || []}
          isLoading={isLoading}
          emptyMessage="No top traveller metrics compiled by the server."
        />
      </div>
    </div>
  );
};

export default Analytics;
