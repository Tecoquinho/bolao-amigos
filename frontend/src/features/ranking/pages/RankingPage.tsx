import { useEffect, useState } from "react";
import { LoadingState } from "../../../shared/components/LoadingState";
import { ErrorState } from "../../../shared/components/ErrorState";
import { EmptyState } from "../../../shared/components/EmptyState";
import { Link } from "react-router-dom";
import type { RankingEntry } from "../../../shared/types/api";
import { getRanking } from "../services/rankingService";
import { getMatches } from "../../matches/services/matchService";

export function RankingPage() {
  const [ranking, setRanking] = useState<RankingEntry[]>([]);
  const [finishedMatchesCount, setFinishedMatchesCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadRanking() {
    setIsLoading(true);
    setError(null);

    try {
      const [nextRanking, finishedMatches] = await Promise.all([
        getRanking(),
        getMatches({ status: "FINISHED" }),
      ]);
      setRanking(nextRanking);
      setFinishedMatchesCount(finishedMatches.length);
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
    <div className="space-y-5">
      <header className="px-1 pt-1">
        <h1 className="font-display text-3xl text-ink">Bolao dos Amigos</h1>
        <p className="mt-1 text-sm text-ink/65">Ranking geral</p>
        {!isLoading && !error ? <p className="mt-1 text-sm font-medium text-ink/55">{finishedMatchesCount} jogos finalizados</p> : null}
      </header>

      {isLoading ? <LoadingState message="Carregando ranking..." /> : null}
      {!isLoading && error ? <ErrorState message={error} onRetry={() => void loadRanking()} /> : null}
      {!isLoading && !error && ranking.length === 0 ? (
        <EmptyState title="Ranking indisponivel" description="Nenhuma entrada foi retornada pelo backend." />
      ) : null}
      {!isLoading && !error && ranking.length > 0 ? (
        <div className="space-y-3">
          {ranking.map((entry) => (
            <Link
              key={entry.participantId}
              to={`/participants/${entry.participantId}`}
              className="block rounded-[28px] bg-white px-5 py-4 shadow-glow transition hover:-translate-y-px"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="text-xs font-semibold uppercase tracking-[0.18em] text-spruce">{entry.position}o lugar</p>
                  <p className="mt-2 truncate text-lg font-semibold text-ink">{entry.participantName}</p>
                  <p className="mt-1 text-sm text-ink/55">
                    {entry.exactScoreHits} exatos - {entry.resultHits} acertos
                  </p>
                </div>
                <div className="shrink-0 text-right">
                  <p className="text-2xl font-semibold text-pine">{entry.totalPoints}</p>
                  <p className="text-xs text-ink/55">pts</p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      ) : null}
    </div>
  );
}
