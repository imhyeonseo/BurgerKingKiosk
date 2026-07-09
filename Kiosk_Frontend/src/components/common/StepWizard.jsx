import { IconCheck } from "@/components/common/Icon";

export function StepWizard({ steps, currentStep }) {
  return (
    <div className="step-wizard">
      {steps.map((label, index) => {
        const stepNumber = index + 1;
        const status = stepNumber < currentStep ? "done" : stepNumber === currentStep ? "active" : "";
        return (
          <div key={label} style={{ display: "flex", alignItems: "center", gap: "var(--sp-3)" }}>
            <div className={`step ${status}`}>
              <span className="dot">{status === "done" ? <IconCheck size={14} /> : stepNumber}</span>
              {label}
            </div>
            {index < steps.length - 1 && <div className="connector" />}
          </div>
        );
      })}
    </div>
  );
}
