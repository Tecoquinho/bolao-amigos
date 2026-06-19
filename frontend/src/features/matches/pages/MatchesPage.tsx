import { useEffect, useState } from "react";
import { LoadingState } from "../../../shared/components/LoadingState";
import { ErrorState } from "../../../shared/components/ErrorState";
import { EmptyState } from "../../../shared/components/EmptyState";
import { DateStepper } from "../../../shared/components/DateStepper";
import { groupItemsByDate, resolveInitialDateIndex } from "../../../shared/utils/dateNavigation";
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

  return (
    <div className="space-y-5">
      <header className="px-1 pt-1">
        <h1 className="font-display text-3xl text-ink">Jogos</h1>
        <p className="mt-1 text-sm text-ink/65">Acompanhe a Copa por data</p>
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
