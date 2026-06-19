interface ErrorStateProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <div className="rounded-[28px] bg-white p-6 text-center shadow-glow">
      <p className="text-sm font-semibold text-coral">Nao deu para carregar.</p>
      <p className="mt-2 text-sm text-ink/75">{message}</p>
      {onRetry ? (
        <button
          type="button"
          onClick={onRetry}
          className="mt-5 rounded-full bg-pine px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-spruce"
        >
          Tentar novamente
        </button>
      ) : null}
    </div>
  );
}
