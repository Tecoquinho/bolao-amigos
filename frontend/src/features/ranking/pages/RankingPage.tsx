import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { EmptyState } from "../../../shared/components/EmptyState";
import { ErrorState } from "../../../shared/components/ErrorState";
import { LoadingState } from "../../../shared/components/LoadingState";
import type { Match, RankingEntry } from "../../../shared/types/api";
import { getMatches } from "../../matches/services/matchService";
import { getRanking } from "../services/rankingService";

const DANGER_CUTOFF_POSITION = 17;

function formatPlace(position: number) {
  return `${position}º lugar`;
}

function formatFinishedMatchesText(matches: Match[]) {
  const finishedMatchesCount = matches.filter((match) => match.status === "FINISHED").length;
  return `${finishedMatchesCount} / ${matches.length} jogos finalizados`;
}

function rankingCardClassName(position: number) {
  if (position === 1) {
    return "group relative flex items-center gap-3 rounded-2xl border-[2.5px] border-[#b8880a] bg-gradient-to-br from-[#ffe97a] to-[#f5c842] px-3.5 py-3 shadow-[3px_4px_0_#b8880a] transition active:translate-y-px";
  }

  if (position === 2) {
    return "group relative flex items-center gap-3 rounded-2xl border-[2.5px] border-[#7a7a7a] bg-gradient-to-br from-[#f2f2f2] to-[#d8d8d8] px-3.5 py-3 shadow-[3px_4px_0_#7a7a7a] transition active:translate-y-px";
  }

  if (position === 3) {
    return "group relative flex items-center gap-3 rounded-2xl border-[2.5px] border-[#8a4e1e] bg-gradient-to-br from-[#f5c89a] to-[#e09060] px-3.5 py-3 shadow-[3px_4px_0_#8a4e1e] transition active:translate-y-px";
  }

  if (position >= DANGER_CUTOFF_POSITION) {
    return "group relative flex items-center gap-3 rounded-2xl border-[2.5px] border-[#9a1f1f] bg-gradient-to-br from-[#ffe4e4] to-[#ffcaca] px-3.5 py-3 shadow-[3px_4px_0_#9a1f1f] transition active:translate-y-px";
  }

  return "group relative flex items-center gap-3 rounded-2xl border-[2.5px] border-[#1a3a2f] bg-white px-3.5 py-3 shadow-[3px_4px_0_#1a3a2f] transition active:translate-y-px";
}

function rankingBadgeClassName(position: number) {
  if (position === 1) {
    return "flex h-[30px] w-[30px] shrink-0 items-center justify-center rounded-full border-[2.5px] border-[#7a5400] bg-[#b8880a] text-[13px] font-black text-[#fff8e0]";
  }

  if (position === 2) {
    return "flex h-[30px] w-[30px] shrink-0 items-center justify-center rounded-full border-[2.5px] border-[#4a4a4a] bg-[#7a7a7a] text-[13px] font-black text-[#f0f0f0]";
  }

  if (position === 3) {
    return "flex h-[30px] w-[30px] shrink-0 items-center justify-center rounded-full border-[2.5px] border-[#5a2e08] bg-[#8a4e1e] text-[13px] font-black text-[#fff0e0]";
  }

  if (position >= DANGER_CUTOFF_POSITION) {
    return "flex h-[30px] w-[30px] shrink-0 items-center justify-center rounded-full border-[2.5px] border-[#6a0f0f] bg-[#9a1f1f] text-[13px] font-black text-[#ffe4e4]";
  }

  return "flex h-[30px] w-[30px] shrink-0 items-center justify-center rounded-full border-[2.5px] border-[#1a3a2f] bg-[#205347] text-[13px] font-black text-[#c8e8dc]";
}

function rankingPlaceClassName(position: number) {
  if (position === 1) {
    return "mb-0.5 text-[9px] font-black uppercase tracking-[0.12em] text-[#7a5400]";
  }

  if (position === 2) {
    return "mb-0.5 text-[9px] font-black uppercase tracking-[0.12em] text-[#4a4a4a]";
  }

  if (position === 3) {
    return "mb-0.5 text-[9px] font-black uppercase tracking-[0.12em] text-[#5a2e08]";
  }

  if (position >= DANGER_CUTOFF_POSITION) {
    return "mb-0.5 text-[9px] font-black uppercase tracking-[0.12em] text-[#7a1010]";
  }

  return "mb-0.5 text-[9px] font-black uppercase tracking-[0.12em] text-[#205347]";
}

function rankingPointsClassName(position: number) {
  if (position === 1) {
    return "text-[21px] font-black leading-none text-[#7a5400]";
  }

  if (position === 2) {
    return "text-[21px] font-black leading-none text-[#4a4a4a]";
  }

  if (position === 3) {
    return "text-[21px] font-black leading-none text-[#5a2e08]";
  }

  if (position >= DANGER_CUTOFF_POSITION) {
    return "text-[21px] font-black leading-none text-[#9a1f1f]";
  }

  return "text-[21px] font-black leading-none text-[#1a3a2f]";
}

interface RankingCardProps {
  entry: RankingEntry;
}

function RankingCard({ entry }: RankingCardProps) {
  return (
    <Link to={`/participants/${entry.participantId}`} className={rankingCardClassName(entry.position)}>
      <div className={rankingBadgeClassName(entry.position)}>{entry.position}</div>

      <div className="min-w-0 flex-1">
        <div className={rankingPlaceClassName(entry.position)}>{formatPlace(entry.position)}</div>
        <div className="truncate text-[14px] font-black leading-[1.2] text-[#1a1a1a]">{entry.participantName}</div>
        <div className="mt-0.5 text-[11px] font-bold text-black/40">{entry.exactScoreHits} exatos, {entry.resultHits} corretos</div>
      </div>

      <div className="shrink-0 text-right">
        <div className={rankingPointsClassName(entry.position)}>{entry.totalPoints}</div>
        <div className="text-[10px] font-extrabold uppercase tracking-[0.05em] text-black/30">pts</div>
      </div>
    </Link>
  );
}

export function RankingPage() {
  const [ranking, setRanking] = useState<RankingEntry[]>([]);
  const [finishedMatchesText, setFinishedMatchesText] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadRanking() {
    setIsLoading(true);
    setError(null);

    try {
      const [nextRanking, matches] = await Promise.all([getRanking(), getMatches()]);
      setRanking(nextRanking);
      setFinishedMatchesText(formatFinishedMatchesText(matches));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Nao foi possivel carregar o ranking.");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadRanking();
  }, []);

  return (
    <div className="space-y-3">
      <header className="space-y-2 px-1 pt-1">
        <div className="min-w-0">
          <h1 className="whitespace-nowrap text-[22px] font-black tracking-[-0.4px] text-[#1a3a2f]">
            {"Bol\u00E3o dos Amigos da Carlos"}
          </h1>
          <p className="text-[12px] font-extrabold uppercase tracking-[0.06em] text-[#7a9e8e]">Copa do Mundo 2026</p>
        </div>
        {!isLoading && !error ? (
          <div className="inline-flex whitespace-nowrap rounded-full border-[2px] border-[#1a3a2f] bg-[#205347] px-2 py-1 text-[10px] font-black text-[#c8e8dc]">
            {finishedMatchesText}
          </div>
        ) : null}
      </header>

      {isLoading ? <LoadingState message="Carregando ranking..." /> : null}
      {!isLoading && error ? <ErrorState message={error} onRetry={() => void loadRanking()} /> : null}
      {!isLoading && !error && ranking.length === 0 ? (
        <EmptyState title="Ranking indisponivel" description="Nenhuma entrada foi retornada pelo backend." />
      ) : null}
      {!isLoading && !error && ranking.length > 0 ? (
        <div className="space-y-[7px] px-[10px]">
          {ranking.map((entry) => (
            <RankingCard key={entry.participantId} entry={entry} />
          ))}
        </div>
      ) : null}
    </div>
  );
}
