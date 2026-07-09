import { SummaryCard } from "@/components/common/SummaryCard";
import { IconTrendingUp, IconReceipt } from "@/components/common/Icon";
import { formatCurrency } from "@/utils/formatCurrency";

export function SalesSummary({ totalSales, orderCount, isLoading = false }) {
  return (
    <div className="kpi-grid cols-2">
      <SummaryCard label="총 매출" value={formatCurrency(totalSales)} icon={IconTrendingUp} accent alignValue="right" isLoading={isLoading} />
      <SummaryCard label="주문 건수" value={`${orderCount}건`} icon={IconReceipt} alignValue="right" isLoading={isLoading} />
    </div>
  );
}
