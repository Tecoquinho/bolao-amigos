interface DateStepperProps {
  label: string;
  onPrevious: () => void;
  onNext: () => void;
  disablePrevious: boolean;
  disableNext: boolean;
}

function buttonClass(disabled: boolean) {
  return [
    "flex h-11 w-11 items-center justify-center rounded-2xl border-[2px] text-lg font-black transition",
    disabled
      ? "border-[#d8d2c4] bg-[#e8e1d3] text-ink/25"
      : "border-[#1a3a2f] bg-white text-[#1a3a2f] shadow-[2px_2px_0_#1a3a2f]",
  ].join(" ");
}

export function DateStepper({
  label,
  onPrevious,
  onNext,
  disablePrevious,
  disableNext,
}: DateStepperProps) {
  return (
    <div className="flex items-center justify-between gap-3 rounded-[26px] border-[2.5px] border-[#1a3a2f] bg-[#e8e1d3] p-2 shadow-[3px_4px_0_#1a3a2f]">
      <button
        type="button"
        onClick={onPrevious}
        disabled={disablePrevious}
        aria-label="Data anterior"
        className={buttonClass(disablePrevious)}
      >
        {"<"}
      </button>
      <div className="min-w-0 flex-1 text-center">
        <p className="text-[10px] font-black uppercase tracking-[0.12em] text-[#7a9e8e]">Navegar por data</p>
        <p className="truncate text-base font-black text-[#1a3a2f]">{label}</p>
      </div>
      <button
        type="button"
        onClick={onNext}
        disabled={disableNext}
        aria-label="Proxima data"
        className={buttonClass(disableNext)}
      >
        {">"}
      </button>
    </div>
  );
}
