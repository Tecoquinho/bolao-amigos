import type { MatchStatus } from "../types/api";
import { formatMatchStatus } from "../utils/format";

interface StatusBadgeProps {
  status: MatchStatus;
}

const statusClassMap: Record<MatchStatus, string> = {
  SCHEDULED: "bg-spruce/10 text-spruce",
  LIVE: "bg-coral/12 text-coral",
  FINISHED: "bg-moss/20 text-pine",
};

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-semibold ${statusClassMap[status]}`}>
      {formatMatchStatus(status)}
    </span>
  );
}
