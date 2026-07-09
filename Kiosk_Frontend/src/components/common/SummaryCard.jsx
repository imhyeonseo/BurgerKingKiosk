export function SummaryCard({ label, value, icon: Icon, accent = false, alignValue = "left", isWarning = false, isLoading = false }) {
  return (
    <div className={`kpi-card ${isWarning ? "is-warning" : ""}`}>
      {Icon && (
        <div className="icon-badge">
          <Icon size={18} />
        </div>
      )}
      <div className="label">{label}</div>
      {isLoading ? (
        <div className="skeleton" style={{ height: 22, width: "60%" }} />
      ) : (
        <div className={`value ${accent ? "accent" : ""} ${alignValue === "right" ? "text-right" : alignValue === "center" ? "text-center" : ""}`}>{value}</div>
      )}
    </div>
  );
}
