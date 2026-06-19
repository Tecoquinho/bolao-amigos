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
  const activeDayPoints = activeGroup?.items.reduce((total, prediction) => total + prediction.pointsAwarded, 0) ?? 0;
  const rankingText = state.rankingEntry
    ? `${state.rankingEntry.position}o lugar - ${state.rankingEntry.totalPoints} pts`
    : "Sem posicao no ranking";
  const exactHits = state.rankingEntry?.exactScoreHits ?? 0;
  const resultHits = state.rankingEntry?.resultHits ?? 0;

  return (
    <div className="space-y-5">
      <header className="space-y-3 px-1">
        <Link to="/" className="inline-flex items-center gap-1.5 text-xs font-semibold text-[#1a3a2f]" aria-label="Voltar para o ranking">
          <span aria-hidden="true" className="text-sm leading-none">
            {"<-"}
          </span>
          <span>Voltar</span>
        </Link>
        <div className="rounded-[26px] border-[2.5px] border-[#1a3a2f] bg-white px-4 py-3 shadow-[3px_4px_0_#1a3a2f]">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="truncate text-[18px] font-black leading-tight text-[#1a3a2f]">{state.participant?.name ?? "Participante"}</p>
              <p className="mt-0.5 text-[11px] font-bold uppercase tracking-[0.06em] text-[#7a9e8e]">
                {state.rankingEntry ? `${state.rankingEntry.position}o lugar` : "Participante"}
              </p>
            </div>
            <div className="shrink-0 text-right">
              <p className="text-[24px] font-black leading-none text-[#1a3a2f]">{state.rankingEntry?.totalPoints ?? 0}</p>
              <p className="text-[10px] font-extrabold uppercase tracking-[0.05em] text-black/35">pontos</p>
            </div>
          </div>
          <div className="mt-3 grid grid-cols-3 gap-2">
            <div className="rounded-2xl border-[2px] border-[#1a3a2f] bg-[#f7f3e8] px-2.5 py-2 text-center">
              <p className="text-[9px] font-black uppercase tracking-[0.08em] text-[#7a9e8e]">Posicao</p>
              <p className="mt-1 text-sm font-black text-[#1a3a2f]">{state.rankingEntry?.position ?? "-"}</p>
            </div>
            <div className="rounded-2xl border-[2px] border-[#1a3a2f] bg-[#f7f3e8] px-2.5 py-2 text-center">
              <p className="text-[9px] font-black uppercase tracking-[0.08em] text-[#7a9e8e]">Exatos</p>
              <p className="mt-1 text-sm font-black text-[#1a3a2f]">{exactHits}</p>
            </div>
            <div className="rounded-2xl border-[2px] border-[#1a3a2f] bg-[#f7f3e8] px-2.5 py-2 text-center">
              <p className="text-[9px] font-black uppercase tracking-[0.08em] text-[#7a9e8e]">Acertos</p>
              <p className="mt-1 text-sm font-black text-[#1a3a2f]">{resultHits}</p>
            </div>
          </div>
        </div>
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
          <div className="rounded-[22px] border-[2px] border-[#1a3a2f] bg-[#f7f3e8] px-4 py-3 text-center shadow-[2px_2px_0_#1a3a2f]">
            <p className="text-[10px] font-black uppercase tracking-[0.12em] text-[#7a9e8e]">Recorte do dia</p>
            <p className="mt-1 text-base font-black text-[#1a3a2f]">{activeDayPoints} pontos nesse dia</p>
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
