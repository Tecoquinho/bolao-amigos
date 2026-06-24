import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { LoadingState } from "../../../shared/components/LoadingState";
import { ErrorState } from "../../../shared/components/ErrorState";
import { EmptyState } from "../../../shared/components/EmptyState";
import { DateStepper } from "../../../shared/components/DateStepper";
import { groupItemsByDate, resolveInitialDateIndex } from "../../../shared/utils/dateNavigation";
import { formatDate, formatScore } from "../../../shared/utils/format";
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
  const [isAuditOpen, setIsAuditOpen] = useState(false);

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
          <button
            type="button"
            onClick={() => setIsAuditOpen(true)}
            className="w-full rounded-[22px] border-[2px] border-[#1a3a2f] bg-[#f7f3e8] px-4 py-3 text-center shadow-[2px_2px_0_#1a3a2f] transition-transform active:translate-y-[1px]"
          >
            <p className="text-[10px] font-black uppercase tracking-[0.12em] text-[#7a9e8e]">Auditoria</p>
            <p className="mt-1 text-base font-black text-[#1a3a2f]">Auditoria de Pontos</p>
          </button>
          <div className="space-y-3">
            {activeGroup.items.map((prediction) => (
              <PredictionCard key={prediction.predictionId} prediction={prediction} />
            ))}
          </div>
        </>
      )}

      {isAuditOpen ? (
        <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/45 px-3 py-4 sm:items-center">
          <div className="max-h-[90vh] w-full max-w-4xl overflow-hidden rounded-[28px] border-[2.5px] border-[#1a3a2f] bg-[#f7f3e8] shadow-[4px_5px_0_#1a3a2f]">
            <div className="flex items-start justify-between gap-4 border-b-[2px] border-[#1a3a2f] px-4 py-4">
              <div className="min-w-0">
                <p className="text-[11px] font-black uppercase tracking-[0.12em] text-[#7a9e8e]">Auditoria de Pontos</p>
                <p className="mt-1 truncate text-lg font-black text-[#1a3a2f]">{state.participant?.name ?? "Participante"}</p>
                <p className="text-xs font-semibold text-[#1a3a2f]/70">{rankingText}</p>
              </div>
              <button
                type="button"
                onClick={() => setIsAuditOpen(false)}
                className="shrink-0 rounded-full border-[2px] border-[#1a3a2f] bg-white px-3 py-1 text-xs font-black text-[#1a3a2f]"
              >
                Fechar
              </button>
            </div>
            <div className="max-h-[calc(90vh-88px)] overflow-auto px-3 py-3">
              <div className="min-w-[720px] overflow-hidden rounded-[22px] border-[2px] border-[#1a3a2f] bg-white">
                <table className="w-full table-fixed border-collapse">
                  <thead className="bg-[#efe7d3]">
                    <tr className="text-left text-[11px] font-black uppercase tracking-[0.08em] text-[#1a3a2f]">
                      <th className="border-b-[2px] border-[#1a3a2f] px-3 py-3">Jogo</th>
                      <th className="border-b-[2px] border-[#1a3a2f] px-3 py-3">Data</th>
                      <th className="border-b-[2px] border-[#1a3a2f] px-3 py-3">Palpite</th>
                      <th className="border-b-[2px] border-[#1a3a2f] px-3 py-3">Resultado final</th>
                      <th className="border-b-[2px] border-[#1a3a2f] px-3 py-3">Pontos</th>
                    </tr>
                  </thead>
                  <tbody>
                    {state.predictions.map((prediction) => (
                      <tr key={prediction.predictionId} className="align-top text-sm text-[#1a3a2f] odd:bg-[#fffaf0]">
                        <td className="border-b border-black/10 px-3 py-3 font-semibold">
                          <div>{prediction.homeTeamName} x {prediction.awayTeamName}</div>
                          <div className="text-xs font-bold uppercase tracking-[0.05em] text-[#7a9e8e]">
                            Jogo {prediction.matchNumber}
                          </div>
                        </td>
                        <td className="border-b border-black/10 px-3 py-3 text-xs font-semibold">
                          {formatDate(prediction.startsAt)}
                        </td>
                        <td className="border-b border-black/10 px-3 py-3 font-black">
                          {prediction.predictedHomeScore} x {prediction.predictedAwayScore}
                        </td>
                        <td className="border-b border-black/10 px-3 py-3 font-semibold">
                          {formatScore(prediction.officialHomeScore, prediction.officialAwayScore)}
                        </td>
                        <td className="border-b border-black/10 px-3 py-3">
                          <span className="inline-flex rounded-full border-2 border-[#1a3a2f] bg-[#f7f3e8] px-2.5 py-1 text-xs font-black">
                            {prediction.pointsAwarded} pts
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
