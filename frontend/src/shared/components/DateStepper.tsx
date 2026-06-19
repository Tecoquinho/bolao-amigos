interface DateStepperProps {
  label: string;
  onPrevious: () => void;
  onNext: () => void;
  disablePrevious: boolean;
  disableNext: boolean;
}

function buttonClass(disabled: boolean) {
  return [
    "flex h-11 w-11 items-center justify-center rounded-2xl border text-lg font-semibold transition",
    disabled
      ? "border-mist bg-mist/70 text-ink/25"
      : "border-pine/10 bg-white text-pine shadow-sm hover:border-pine/20 hover:bg-pine hover:text-white",
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
    <div className="flex items-center justify-between gap-3 rounded-[26px] bg-mist/85 p-2">
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
        <p className="truncate text-base font-semibold text-ink">{label}</p>
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
