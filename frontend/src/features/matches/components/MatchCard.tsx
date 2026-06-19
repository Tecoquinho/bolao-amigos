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
    <article className="rounded-2xl border-[2.5px] border-[#1a3a2f] bg-white px-4 py-3 shadow-[3px_4px_0_#1a3a2f]">
      <div className="space-y-3">
        <div className="min-w-0 text-center">
          {isFinished ? (
            <div className="space-y-1">
              <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-2 overflow-hidden whitespace-nowrap text-[15px] font-black leading-tight text-ink">
                <TeamLabel
                  name={match.homeTeamName}
                  fifaCode={match.homeTeamFifaCode}
                  flagUrl={match.homeTeamFlagUrl}
                  flagPlacement="end"
                  justify="end"
                />
                <div className="flex items-center justify-center whitespace-nowrap text-[15px] font-black">
                  <span className="px-1.5 text-ink">{match.homeScore}</span>
                  <span className="text-ink/55">x</span>
                  <span className="px-1.5 text-ink">{match.awayScore}</span>
                </div>
                <TeamLabel
                  name={match.awayTeamName}
                  fifaCode={match.awayTeamFifaCode}
                  flagUrl={match.awayTeamFlagUrl}
                  flagPlacement="start"
                  justify="start"
                />
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-2 overflow-hidden whitespace-nowrap text-[15px] font-black text-ink">
              <TeamLabel
                name={match.homeTeamName}
                fifaCode={match.homeTeamFifaCode}
                flagUrl={match.homeTeamFlagUrl}
                flagPlacement="end"
                justify="end"
              />
              <span className="text-center text-ink/45">x</span>
              <TeamLabel
                name={match.awayTeamName}
                fifaCode={match.awayTeamFifaCode}
                flagUrl={match.awayTeamFlagUrl}
                flagPlacement="start"
                justify="start"
              />
            </div>
          )}
          <p className="mt-1 text-center text-xs font-bold text-ink/60">
            {formatTime(match.startsAt)} - {formatPhase(match.phase)}
          </p>
        </div>
        <div className="flex items-center justify-between gap-3 text-sm text-ink/70">
          <div className="min-w-0">
            {!isFinished ? <p>{formatScore(match.homeScore, match.awayScore)}</p> : null}
            {match.venue ? <p>{match.venue}</p> : null}
          </div>
          <StatusBadge status={match.status} />
        </div>
      </div>
    </article>
  );
}
