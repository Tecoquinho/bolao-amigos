interface EmptyStateProps {
  title: string;
  description: string;
}

export function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <div className="rounded-[28px] bg-white p-6 text-center shadow-glow">
      <p className="text-base font-semibold text-ink">{title}</p>
      <p className="mt-2 text-sm text-ink/65">{description}</p>
    </div>
  );
}
