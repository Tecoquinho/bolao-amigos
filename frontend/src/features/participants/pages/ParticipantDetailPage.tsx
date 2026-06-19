import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { LoadingState } from "../../../shared/components/LoadingState";
import { ErrorState } from "../../../shared/components/ErrorState";
import { EmptyState } from "../../../shared/components/EmptyState";
import { DateStepper } from "../../../shared/components/DateStepper";
import { groupItemsByDate, resolveInitialDateIndex } from "../../../shared/utils/dateNavigation";
import type { Participant, ParticipantPrediction, RankingEntry } from "../../../shared/types/api";
import { getParticipantById, getParticipantPredictions } from "../services/participantService";
import { PredictionCard } from "../components/PredictionCard";
import { getRanking } from "../../ranking/services/rankingService";

interface ParticipantState {
  participant: Participant | null;
  predictions: ParticipantPrediction[];
  rankingEntry: RankingEntry | null;
}

export function ParticipantDetailPage() {
  const params = useParams();
  const participantId = Number(params.participantId);
  const [state, setState] = useState<ParticipantState>({ participant: null, predictions: [], rankingEntry: null });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeDateIndex, setActiveDateIndex] = useState(0);

  async function loadParticipant() {
    if (!Number.isFinite(participantId)) {
      setError("Participante invalido.");
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const [participant, predictions, ranking] = await Promise.all([
        getParticipantById(participantId),
        getParticipantPredictions(participantId),
        getRanking(),
      ]);
      const rankingEntry = ranking.find((entry) => entry.participantId === participantId) ?? null;
      const groupedPredictions = groupItemsByDate(predictions);

      setState({ participant, predictions, rankingEntry });
      setActiveDateIndex(resolveInitialDateIndex(groupedPredictions.map((group) => group.key)));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Nao foi possivel carregar o participante.");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadParticipant();
  }, [participantId]);

  if (isLoading) {
    return <LoadingState message="Carregando os palpites do participante..." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={() => void loadParticipant()} />;
  }

  const groupedPredictions = groupItemsByDate(state.predictions);
  const activeGroup = groupedPredictions[activeDateIndex];
  const hasPredictions = groupedPredictions.length > 0;
  const activeDayPoints = activeGroup.items.reduce((total, prediction) => total + prediction.pointsAwarded, 0);
  const rankingText = state.rankingEntry
    ? `${state.rankingEntry.position}o lugar - ${state.rankingEntry.totalPoints} pts`
    : "Sem posicao no ranking";

  return (
    <div className="space-y-5">
      <header className="px-1">
        <Link
          to="/"
          className="inline-flex items-center gap-2 rounded-full border-[2px] border-[#1a3a2f] bg-white px-3 py-2 text-sm font-black text-[#1a3a2f] shadow-[2px_2px_0_#1a3a2f] transition-transform duration-150 active:translate-x-[1px] active:translate-y-[1px] active:shadow-[1px_1px_0_#1a3a2f]"
          aria-label="Voltar para o ranking"
        >
          <span
            aria-hidden="true"
            className="flex h-6 w-6 items-center justify-center rounded-full border-[2px] border-[#1a3a2f] bg-[#f0ede4] text-base leading-none"
          >
            ←
          </span>
          <span className="truncate">{state.participant?.name ?? "Participante"}</span>
        </Link>
        <p className="mt-3 text-sm font-medium text-ink/65">{rankingText}</p>
      </header>

      {!hasPredictions ? (
        <EmptyState title="Sem palpites" description="Nenhum palpite foi encontrado para este participante." />
      ) : (
        <>
          <DateStepper
            label={activeGroup.label}
            onPrevious={() => setActiveDateIndex((currentIndex) => currentIndex - 1)}
            onNext={() => setActiveDateIndex((currentIndex) => currentIndex + 1)}
            disablePrevious={activeDateIndex === 0}
            disableNext={activeDateIndex === groupedPredictions.length - 1}
          />
          <div className="rounded-[22px] border border-pine/10 bg-white px-4 py-3 text-center shadow-sm">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-spruce">Pontos do dia</p>
            <p className="mt-1 text-lg font-semibold text-ink">{activeDayPoints} pontos nesse dia</p>
          </div>
          <div className="space-y-3">
            {activeGroup.items.map((prediction) => (
              <PredictionCard key={prediction.predictionId} prediction={prediction} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
