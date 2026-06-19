import { useEffect, useState } from "react";
import { LoadingState } from "../../../shared/components/LoadingState";
import { ErrorState } from "../../../shared/components/ErrorState";
import { EmptyState } from "../../../shared/components/EmptyState";
import { DateStepper } from "../../../shared/components/DateStepper";
import { formatLongDateLabel, getDateKey, groupItemsByDate, resolveInitialDateIndex } from "../../../shared/utils/dateNavigation";
import type { Match } from "../../../shared/types/api";
import { getMatches } from "../services/matchService";
import { MatchCard } from "../components/MatchCard";

export function MatchesPage() {
  const [matches, setMatches] = useState<Match[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeDateIndex, setActiveDateIndex] = useState(0);

  async function loadMatches() {
    setIsLoading(true);
    setError(null);

    try {
      const nextMatches = await getMatches();
      const groupedMatches = groupItemsByDate(nextMatches);
      setMatches(nextMatches);
      setActiveDateIndex(resolveInitialDateIndex(groupedMatches.map((group) => group.key)));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Nao foi possivel carregar os jogos.");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadMatches();
  }, []);

  const groupedMatches = groupItemsByDate(matches);
  const activeGroup = groupedMatches[activeDateIndex];
  const activeFinishedCount = activeGroup?.items.filter((match) => match.status === "FINISHED").length ?? 0;
  const activeScheduledCount = activeGroup?.items.filter((match) => match.status === "SCHEDULED").length ?? 0;
  const activeLiveCount = activeGroup?.items.filter((match) => match.status === "LIVE").length ?? 0;
  const isActiveDayToday = activeGroup ? activeGroup.key === getDateKey(new Date()) : false;
  const activeDaySummary = activeGroup
    ? [
        `${activeGroup.items.length} ${activeGroup.items.length === 1 ? "jogo" : "jogos"}`,
        `${activeFinishedCount} finalizados`,
        activeLiveCount > 0 ? `${activeLiveCount} ao vivo` : `${activeScheduledCount} agendados`,
      ].join("  •  ")
    : "";

  return (
    <div className="space-y-5">
      <header className="space-y-3 px-1 pt-1">
        <div className="min-w-0">
          <h1 className="whitespace-nowrap text-[22px] font-black tracking-[-0.4px] text-[#1a3a2f]">Jogos da Copa</h1>
          <p className="text-[12px] font-extrabold uppercase tracking-[0.06em] text-[#7a9e8e]">Acompanhe por data</p>
        </div>
        {!isLoading && !error && activeGroup ? (
          <div className="rounded-[26px] border-[2.5px] border-[#1a3a2f] bg-white px-4 py-3 shadow-[3px_4px_0_#1a3a2f]">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="truncate text-[18px] font-black leading-tight text-[#1a3a2f]">
                  {formatLongDateLabel(activeGroup.key)}
                </p>
                <p className="mt-1 text-[11px] font-bold text-black/50">{activeDaySummary}</p>
              </div>
              <div className="shrink-0 rounded-lg border-[2px] border-[#1a3a2f] bg-[#205347] px-2 py-1 text-[9px] font-black uppercase tracking-[0.06em] text-[#c8e8dc]">
                {isActiveDayToday ? "Hoje" : activeGroup.label}
              </div>
            </div>
          </div>
        ) : null}
      </header>

      {isLoading ? <LoadingState message="Carregando jogos..." /> : null}
      {!isLoading && error ? <ErrorState message={error} onRetry={() => void loadMatches()} /> : null}
      {!isLoading && !error && matches.length === 0 ? (
        <EmptyState title="Nenhum jogo encontrado" description="Nenhuma partida foi retornada pelo backend." />
      ) : null}
      {!isLoading && !error && matches.length > 0 ? (
        <>
          <DateStepper
            label={activeGroup.label}
            onPrevious={() => setActiveDateIndex((currentIndex) => currentIndex - 1)}
            onNext={() => setActiveDateIndex((currentIndex) => currentIndex + 1)}
            disablePrevious={activeDateIndex === 0}
            disableNext={activeDateIndex === groupedMatches.length - 1}
          />
          <div className="space-y-3">
            {activeGroup.items.map((match) => (
              <MatchCard key={match.id} match={match} />
            ))}
          </div>
        </>
      ) : null}
    </div>
  );
}
