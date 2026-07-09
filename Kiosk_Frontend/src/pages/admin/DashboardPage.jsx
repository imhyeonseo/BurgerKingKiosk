import { useQuery } from "@tanstack/react-query"
import { getDashboard } from "@/api/dashboard"
import { PageHeader } from "@/components/common/PageHeader"
import { SummaryCard } from "@/components/common/SummaryCard"
import { DataTable } from "@/components/common/DataTable"
import { StatusBadge } from "@/components/common/StatusBadge"
import {
    IconTrendingUp,
    IconReceipt,
    IconAlert,
} from "@/components/common/Icon"
import { formatCurrency } from "@/utils/formatCurrency"
import { formatDateTime } from "@/utils/formatDate"

const COLUMNS = [
    { key: "no", label: "No.", align: "id", render: (_row, i) => i + 1 },
    { key: "orderNumber", label: "주문번호", align: "id" },
    {
        key: "totalPrice",
        label: "금액",
        align: "right",
        render: (row) => formatCurrency(row.totalPrice),
    },
    {
        key: "status",
        label: "상태",
        align: "center",
        render: (row) => (
            <StatusBadge
                status={row.status === "COMPLETED" ? "success" : "danger"}
                label={row.status}
            />
        ),
    },
    {
        key: "createdAt",
        label: "일시",
        align: "center",
        render: (row) => formatDateTime(row.createdAt),
    },
]

export function DashboardPage() {
    const { data, isLoading } = useQuery({
        queryKey: ["dashboard"],
        queryFn: getDashboard,
        refetchOnWindowFocus: true,
    })

    return (
        <>
            <PageHeader title="대시보드" subtitle="오늘 매장 현황" />

            <div className="kpi-grid">
                <SummaryCard
                    label="오늘 매출"
                    value={data ? formatCurrency(data.todaySales) : "-"}
                    icon={IconTrendingUp}
                    accent
                    alignValue="right"
                    isLoading={isLoading}
                />
                <SummaryCard
                    label="오늘 주문 건수"
                    value={data ? `${data.todayOrderCount}건` : "-"}
                    icon={IconReceipt}
                    alignValue="right"
                    isLoading={isLoading}
                />
                <SummaryCard
                    label="이번 달 매출"
                    value={data ? formatCurrency(data.monthSales) : "-"}
                    icon={IconTrendingUp}
                    alignValue="right"
                    isLoading={isLoading}
                />
                <SummaryCard
                    label="품절 메뉴 / 전체"
                    value={
                        data
                            ? `${data.soldOutMenuCount} / ${data.totalMenuCount}`
                            : "-"
                    }
                    icon={IconAlert}
                    alignValue="right"
                    isWarning={!!data && data.soldOutMenuCount > 0}
                    isLoading={isLoading}
                />
            </div>

            <div className="panel">
                <div className="panel-header">
                    <h2>최근 주문 5건</h2>
                </div>
                <div className="panel-body">
                    <DataTable
                        columns={COLUMNS}
                        rows={data?.recentOrders ?? []}
                        rowKey={(row) => row.orderNumber}
                        isLoading={isLoading}
                    />
                </div>
            </div>
        </>
    )
}
