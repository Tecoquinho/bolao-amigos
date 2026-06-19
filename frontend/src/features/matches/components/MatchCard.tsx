import type { Match } from "../../../shared/types/api";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { formatPhase, formatScore, formatTime } from "../../../shared/utils/format";

interface MatchCardProps {
  match: Match;
}

export function MatchCard({ match }: MatchCardProps) {
  return (
    <article className="rounded-[28px] bg-white px-5 py-4 shadow-glow">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-lg font-semibold text-ink">
            {match.homeTeamName} x {match.awayTeamName}
          </p>
          <p className="mt-1 text-sm text-ink/60">
            {formatTime(match.startsAt)} - {formatPhase(match.phase)}
          </p>
        </div>
        <StatusBadge status={match.status} />
      </div>
      <div className="mt-4 space-y-2 text-sm text-ink/70">
        <p>{formatScore(match.homeScore, match.awayScore)}</p>
        {match.venue ? <p>{match.venue}</p> : null}
      </div>
    </article>
  );
}
