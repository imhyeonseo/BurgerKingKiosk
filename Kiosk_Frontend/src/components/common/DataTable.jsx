/**
 * 정렬 규칙(Frontend.md 5장)이 강제되는 공통 테이블.
 * columns: [{ key, label, align: "left"|"center"|"right"|"id", render?: (row, index) => node }]
 * - th(헤더)는 항상 가운데 정렬 (col-status)
 * - td/tfoot은 align 값에 따라 정렬 규칙 적용
 */
export function DataTable({ columns, rows, rowKey, footerRow, emptyMessage = "데이터가 없습니다.", isLoading = false, onRowClick }) {
  const alignClass = (align) => {
    if (align === "right")  return "col-amount";  // 금액·수치: 우측 + tabular-nums
    if (align === "id")     return "col-id";       // 순번·ID·코드: 가운데 + tabular-nums
    if (align === "center") return "col-status";   // 단순 텍스트: 가운데
    return "col-text";                             // 서술 텍스트: 좌측
  };

  return (
    <table>
      <thead>
        <tr>
          {columns.map((col) => (
            <th key={col.key} className="col-status">
              {col.label}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {isLoading ? (
          Array.from({ length: 5 }).map((_, i) => (
            <tr key={`skeleton-${i}`}>
              {columns.map((col) => (
                <td key={col.key}>
                  <div className="skeleton" style={{ height: 16, width: "80%" }} />
                </td>
              ))}
            </tr>
          ))
        ) : rows.length === 0 ? (
          <tr>
            <td colSpan={columns.length} className="table-empty">
              {emptyMessage}
            </td>
          </tr>
        ) : (
          rows.map((row, index) => (
            <tr
              key={rowKey(row, index)}
              className={onRowClick ? "clickable" : undefined}
              onClick={onRowClick ? () => onRowClick(row) : undefined}
            >
              {columns.map((col) => (
                <td key={col.key} className={alignClass(col.align)}>
                  {col.render ? col.render(row, index) : row[col.key]}
                </td>
              ))}
            </tr>
          ))
        )}
      </tbody>
      {footerRow && !isLoading && rows.length > 0 && (
        <tfoot>
          <tr>
            {columns.map((col) => (
              <td key={col.key} className={alignClass(col.align)}>
                {footerRow[col.key] ?? ""}
              </td>
            ))}
          </tr>
        </tfoot>
      )}
    </table>
  );
}
