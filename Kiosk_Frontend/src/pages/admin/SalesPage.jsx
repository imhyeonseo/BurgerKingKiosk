import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { getDailySales, getMonthlySales, getYearlySales } from "@/api/sales";
import { PageHeader } from "@/components/common/PageHeader";
import { SegmentedControl } from "@/components/common/SegmentedControl";
import { SalesSummary } from "@/components/common/SalesSummary";
import { DataTable } from "@/components/common/DataTable";
import { formatCurrency } from "@/utils/formatCurrency";
import { today, formatTime } from "@/utils/formatDate";

const PERIOD_OPTIONS = [
  { value: "daily", label: "일별" },
  { value: "monthly", label: "월별" },
  { value: "yearly", label: "연도별" },
];

const now = new Date();

export function SalesPage() {
  const [period, setPeriod] = useState("daily");
  const [date, setDate] = useState(today());
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [submitted, setSubmitted] = useState({ period: "daily", date: today() });

  const { data, isFetching } = useQuery({
    queryKey: ["admin-sales", submitted],
    queryFn: () => {
      if (submitted.period === "daily") return getDailySales(submitted.date);
      if (submitted.period === "monthly") return getMonthlySales(submitted.year, submitted.month);
      return getYearlySales(submitted.year);
    },
  });

  function handleSearch() {
    if (period === "daily") setSubmitted({ period, date });
    else if (period === "monthly") setSubmitted({ period, year, month });
    else setSubmitted({ period, year });
  }

  return (
    <>
      <PageHeader title="매출 조회" subtitle="status = COMPLETED인 주문만 집계" />

      <div className="toolbar">
        <SegmentedControl options={PERIOD_OPTIONS} value={period} onChange={setPeriod} />
        <div className="spacer" />
        {period === "daily" && (
          <input className="field-input" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        )}
        {period === "monthly" && (
          <>
            <input className="field-input" type="number" style={{ width: 90 }} value={year} onChange={(e) => setYear(Number(e.target.value))} />
            <input className="field-input" type="number" min={1} max={12} style={{ width: 70 }} value={month} onChange={(e) => setMonth(Number(e.target.value))} />
          </>
        )}
        {period === "yearly" && (
          <input className="field-input" type="number" style={{ width: 90 }} value={year} onChange={(e) => setYear(Number(e.target.value))} />
        )}
        <button className="btn-primary btn-sm" onClick={handleSearch}>조회</button>
      </div>

      {data && (
        <>
          <SalesSummary totalSales={data.totalSales} orderCount={data.orderCount} isLoading={isFetching} />

          {submitted.period === "daily" && (
            <div className="panel">
              <div className="panel-header"><h2>일별 매출 상세 - {data.date}</h2></div>
              <div className="panel-body">
                <DataTable
                  columns={[
                    { key: "orderNumber", label: "주문번호", align: "id" },
                    { key: "totalPrice", label: "결제금액", align: "right", render: (r) => formatCurrency(r.totalPrice) },
                    { key: "createdAt", label: "주문시각", align: "center", render: (r) => formatTime(r.createdAt) },
                  ]}
                  rows={data.orders}
                  rowKey={(r) => r.orderNumber}
                />
              </div>
            </div>
          )}

          {submitted.period === "monthly" && (
            <div className="panel">
              <div className="panel-header"><h2>월별 매출 추이 - {data.year}년 {data.month}월</h2></div>
              <div className="panel-body">
                <DataTable
                  columns={[
                    { key: "date", label: "일자", align: "id" },
                    { key: "dailySales", label: "일 매출", align: "right", render: (r) => formatCurrency(r.dailySales) },
                    { key: "dailyOrderCount", label: "일 주문건수", align: "right", render: (r) => `${r.dailyOrderCount}건` },
                  ]}
                  rows={data.dailyBreakdown}
                  rowKey={(r) => r.date}
                  footerRow={{ date: "합계", dailySales: formatCurrency(data.totalSales), dailyOrderCount: `${data.orderCount}건` }}
                />
              </div>
            </div>
          )}

          {submitted.period === "yearly" && (
            <div className="panel">
              <div className="panel-header"><h2>연도별 매출 - {data.year}년</h2></div>
              <div className="panel-body">
                <DataTable
                  columns={[
                    { key: "month", label: "월", align: "id", render: (r) => `${r.month}월` },
                    { key: "monthlySales", label: "월 매출", align: "right", render: (r) => formatCurrency(r.monthlySales) },
                    { key: "monthlyOrderCount", label: "월 주문건수", align: "right", render: (r) => `${r.monthlyOrderCount}건` },
                  ]}
                  rows={data.monthlyBreakdown}
                  rowKey={(r) => r.month}
                  footerRow={{ month: "합계", monthlySales: formatCurrency(data.totalSales), monthlyOrderCount: `${data.orderCount}건` }}
                />
              </div>
            </div>
          )}
        </>
      )}
    </>
  );
}
