import React from "react";
import { useTopTravellers } from "../../hooks";
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

  // Premium HSL-tailored colors for chart cells
  const COLORS = ["#3b82f6", "#8b5cf6", "#10b981", "#f59e0b", "#ef4444"];

  // Mock department spend distribution
  const deptData = [
    { name: "Engineering", amount: 62000 },
    { name: "Sales", amount: 98000 },
    { name: "Marketing", amount: 24000 },
    { name: "Finance", amount: 15000 },
    { name: "HR", amount: 8000 },
  ];

  // Category distribution
  const categoryData = [
    { name: "Meals", value: 34000 },
    { name: "Accommodation", value: 120000 },
    { name: "Transport", value: 45000 },
    { name: "Visa", value: 11000 },
    { name: "Misc", value: 6000 },
  ];

  // Monthly trends
  const trendData = [
    { month: "Jan", amount: 12000 },
    { month: "Feb", amount: 19000 },
    { month: "Mar", amount: 32000 },
    { month: "Apr", amount: 24000 },
    { month: "May", amount: 48000 },
    { month: "Jun", amount: 55000 },
  ];

  const columns = [
    {
      header: "Rank",
      accessor: (_: any, idx?: number) => <span className="font-semibold">#{idx !== undefined ? idx + 1 : 1}</span>,
    },
    {
      header: "Traveller Name",
      accessor: (t: any) => <span>{t.user?.name || t.user?.username || "Test Traveller"}</span>,
    },
    {
      header: "Total Cost Incurred",
      accessor: (t: any) => <span className="font-semibold text-primary">{formatCurrency(t.totalCost || 0)}</span>,
    },
    {
      header: "Grade",
      accessor: (t: any) => <span className="text-xs uppercase font-medium">{t.user?.gradeId || "G2"}</span>,
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
          <p className="text-2xl font-bold">78.2%</p>
          <div className="w-full bg-muted h-2 rounded-full mt-2 overflow-hidden">
            <div className="bg-blue-600 h-full w-[78%]" />
          </div>
        </div>

        <div className="p-4 bg-card border border-border rounded-lg shadow-sm">
          <span className="text-[10px] uppercase font-bold text-muted-foreground">Advance Settlement Rate</span>
          <p className="text-2xl font-bold">92.4%</p>
          <div className="w-full bg-muted h-2 rounded-full mt-2 overflow-hidden">
            <div className="bg-green-600 h-full w-[92%]" />
          </div>
        </div>

        <div className="p-4 bg-card border border-border rounded-lg shadow-sm">
          <span className="text-[10px] uppercase font-bold text-muted-foreground">Policy Compliance Exception Rate</span>
          <p className="text-2xl font-bold text-destructive">4.1%</p>
          <div className="w-full bg-muted h-2 rounded-full mt-2 overflow-hidden">
            <div className="bg-destructive h-full w-[4%]" />
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
