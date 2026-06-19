import type { MatchStatus } from "../types/api";
import { formatMatchStatus } from "../utils/format";

interface StatusBadgeProps {
  status: MatchStatus;
}

const statusClassMap: Record<MatchStatus, string> = {
  SCHEDULED: "border-[#1a3a2f] bg-[#f0ede4] text-[#205347]",
  LIVE: "border-[#9a1f1f] bg-[#ffe4e4] text-[#9a1f1f]",
  FINISHED: "border-[#1a3a2f] bg-[#d9eadf] text-[#1a3a2f]",
};

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={`inline-flex rounded-xl border-[2px] px-2 py-1 text-[10px] font-black ${statusClassMap[status]}`}>
      {formatMatchStatus(status)}
    </span>
  );
}
