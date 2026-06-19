import type { Match } from "../../../shared/types/api";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { TeamLabel } from "../../../shared/components/TeamLabel";
import { formatPhase, formatScore, formatTime } from "../../../shared/utils/format";

interface MatchCardProps {
  match: Match;
}

export function MatchCard({ match }: MatchCardProps) {
  const isFinished = match.status === "FINISHED" && match.homeScore !== null && match.awayScore !== null;

  return (
    <article className="overflow-hidden rounded-2xl border-[2.5px] border-[#1a3a2f] bg-white px-4 py-3 shadow-[3px_4px_0_#1a3a2f]">
      <div className="space-y-2.5">
        <div className="min-w-0 text-center">
          {isFinished ? (
            <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-2 overflow-hidden whitespace-nowrap text-[13px] font-black leading-tight text-ink">
              <TeamLabel
                name={match.homeTeamName}
                fifaCode={match.homeTeamFifaCode}
                flagUrl={match.homeTeamFlagUrl}
                flagPlacement="end"
                justify="end"
              />
              <div className="flex items-center justify-center whitespace-nowrap text-[19px] font-black leading-none text-[#1a3a2f]">
                <span className="inline-flex w-5 justify-center">{match.homeScore}</span>
                <span className="inline-flex w-4 justify-center text-ink/55">x</span>
                <span className="inline-flex w-5 justify-center">{match.awayScore}</span>
              </div>
              <TeamLabel
                name={match.awayTeamName}
                fifaCode={match.awayTeamFifaCode}
                flagUrl={match.awayTeamFlagUrl}
                flagPlacement="start"
                justify="start"
              />
            </div>
          ) : (
            <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-2 overflow-hidden whitespace-nowrap text-[13px] font-black text-ink">
              <TeamLabel
                name={match.homeTeamName}
                fifaCode={match.homeTeamFifaCode}
                flagUrl={match.homeTeamFlagUrl}
                flagPlacement="end"
                justify="end"
              />
              <span className="inline-flex w-4 justify-center text-center text-ink/45">x</span>
              <TeamLabel
                name={match.awayTeamName}
                fifaCode={match.awayTeamFifaCode}
                flagUrl={match.awayTeamFlagUrl}
                flagPlacement="start"
                justify="start"
              />
            </div>
          )}
          <p className="mt-0.5 text-center text-[11px] font-bold text-ink/60">
            {formatTime(match.startsAt)}  •  {formatPhase(match.phase)}
          </p>
        </div>
        <div className="flex items-end justify-between gap-3 text-sm text-ink/70">
          <div className="min-w-0 space-y-1">
            {!isFinished ? <p className="text-[12px] font-semibold text-ink/65">{formatScore(match.homeScore, match.awayScore)}</p> : null}
            {match.venue ? <p className="truncate text-[12px] text-ink/55">{match.venue}</p> : null}
          </div>
          <div className="shrink-0 self-end pb-0.5">
            <StatusBadge status={match.status} />
          </div>
        </div>
      </div>
    </article>
  );
}
