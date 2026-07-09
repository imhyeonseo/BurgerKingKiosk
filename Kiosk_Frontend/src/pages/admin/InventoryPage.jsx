import { useState } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { getInventory, updateInventory } from "@/api/inventory"
import { useUiStore } from "@/stores/uiStore"
import { PageHeader } from "@/components/common/PageHeader"
import { DataTable } from "@/components/common/DataTable"
import { StatusBadge } from "@/components/common/StatusBadge"

const PAGE_SIZE = 20

function InventoryActionsCell({ row, isEditing, onEdit, onCancel }) {
    const queryClient = useQueryClient()
    const showToast = useUiStore((s) => s.showToast)
    const [quantity, setQuantity] = useState(row.quantity)

    const mutation = useMutation({
        mutationFn: () => updateInventory(row.menuId, quantity),
        onSuccess: () => {
            showToast("success", "저장되었습니다.")
            queryClient.invalidateQueries({ queryKey: ["admin-inventory"] })
            onCancel()
        },
    })

    if (!isEditing) {
        return (
            <div className="actions-right">
                <button className="btn-ghost btn-sm" onClick={onEdit}>
                    수정
                </button>
            </div>
        )
    }

    return (
        <div className="actions-right">
            <input
                className="field-input"
                type="number"
                min={0}
                style={{ width: 80 }}
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
            />
            <button className="btn-ghost btn-sm" onClick={onCancel}>
                취소
            </button>
            <button
                className="btn-primary btn-sm"
                disabled={mutation.isPending}
                onClick={() => mutation.mutate()}
            >
                저장
            </button>
        </div>
    )
}

export function InventoryPage() {
    const [isSoldOut, setIsSoldOut] = useState("")
    const [page, setPage] = useState(0)
    const [editingMenuId, setEditingMenuId] = useState(null)

    const filters = {
        isSoldOut: isSoldOut === "" ? undefined : isSoldOut === "true",
        page,
        size: PAGE_SIZE,
    }
    const { data, isLoading } = useQuery({
        queryKey: ["admin-inventory", filters],
        queryFn: () => getInventory(filters),
    })

    const columns = [
        {
            key: "no",
            label: "No.",
            align: "id",
            render: (_r, i) => page * PAGE_SIZE + i + 1,
        },
        { key: "menuName", label: "메뉴명", align: "center" },
        { key: "categoryName", label: "카테고리", align: "center" },
        { key: "quantity", label: "현재 재고", align: "right" },
        {
            key: "isSoldOut",
            label: "상태",
            align: "center",
            render: (row) => (
                <StatusBadge
                    status={row.isSoldOut ? "warning" : "success"}
                    label={row.isSoldOut ? "품절" : "판매 가능"}
                />
            ),
        },
        {
            key: "actions",
            align: "right",
            render: (row) => (
                <InventoryActionsCell
                    row={row}
                    isEditing={editingMenuId === row.menuId}
                    onEdit={() => setEditingMenuId(row.menuId)}
                    onCancel={() => setEditingMenuId(null)}
                />
            ),
        },
    ]

    const totalPages = data?.totalPages ?? 0

    return (
        <>
            <PageHeader
                title="재고 관리"
                subtitle="재고 수량은 이 화면에서만 변경 (메뉴 수정 API와 분리)"
            />

            <div className="panel">
                <div className="panel-body">
                    <div className="toolbar">
                        <select
                            className="field-input"
                            value={isSoldOut}
                            onChange={(e) => {
                                setIsSoldOut(e.target.value)
                                setPage(0)
                            }}
                        >
                            <option value="">전체</option>
                            <option value="true">품절만</option>
                            <option value="false">판매 가능만</option>
                        </select>
                    </div>

                    <DataTable
                        columns={columns}
                        rows={data?.content ?? []}
                        rowKey={(row) => row.menuId}
                        isLoading={isLoading}
                    />

                    {totalPages > 1 && (
                        <div className="pagination">
                            {Array.from({ length: totalPages }).map((_, i) => (
                                <button
                                    key={i}
                                    className={i === page ? "active" : ""}
                                    onClick={() => setPage(i)}
                                >
                                    {i + 1}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </>
    )
}
