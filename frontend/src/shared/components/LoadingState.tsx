interface LoadingStateProps {
  message?: string;
}

export function LoadingState({ message = "Carregando dados..." }: LoadingStateProps) {
  return (
    <div className="rounded-[28px] bg-white px-5 py-10 text-center shadow-glow">
      <div className="mx-auto h-9 w-9 animate-spin rounded-full border-4 border-moss/20 border-t-spruce" />
      <p className="mt-4 text-sm font-medium text-ink/70">{message}</p>
    </div>
  );
}
