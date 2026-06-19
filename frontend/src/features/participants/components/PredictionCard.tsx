import type { ParticipantPrediction } from "../../../shared/types/api";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { TeamLabel } from "../../../shared/components/TeamLabel";
import { formatScore, formatTime } from "../../../shared/utils/format";

interface PredictionCardProps {
  prediction: ParticipantPrediction;
}

export function PredictionCard({ prediction }: PredictionCardProps) {
  const isFinished = prediction.matchStatus === "FINISHED";

  return (
    <article
      className={`rounded-2xl border-[2.5px] px-4 py-3 shadow-[3px_4px_0_#1a3a2f] ${
        isFinished ? "border-[#1a3a2f] bg-[#f7f3e8]" : "border-[#1a3a2f] bg-white"
      }`}
    >
      <div className="space-y-2.5">
        <div className="min-w-0 text-center">
          {isFinished ? (
            <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-2 overflow-hidden whitespace-nowrap text-[15px] font-black text-ink">
              <TeamLabel
                name={prediction.homeTeamName}
                fifaCode={prediction.homeTeamFifaCode}
                flagUrl={prediction.homeTeamFlagUrl}
                flagPlacement="end"
                justify="end"
              />
              <div className="flex items-center justify-center whitespace-nowrap text-[17px] font-black leading-none text-ink">
                <span className="inline-flex w-5 justify-center text-ink">{prediction.officialHomeScore}</span>
                <span className="inline-flex w-4 justify-center text-ink/55">x</span>
                <span className="inline-flex w-5 justify-center text-ink">{prediction.officialAwayScore}</span>
              </div>
              <TeamLabel
                name={prediction.awayTeamName}
                fifaCode={prediction.awayTeamFifaCode}
                flagUrl={prediction.awayTeamFlagUrl}
                flagPlacement="start"
                justify="start"
              />
            </div>
          ) : (
            <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-2 overflow-hidden whitespace-nowrap text-[15px] font-black text-ink">
              <TeamLabel
                name={prediction.homeTeamName}
                fifaCode={prediction.homeTeamFifaCode}
                flagUrl={prediction.homeTeamFlagUrl}
                flagPlacement="end"
                justify="end"
              />
              <span className="text-center text-ink/45">x</span>
              <TeamLabel
                name={prediction.awayTeamName}
                fifaCode={prediction.awayTeamFifaCode}
                flagUrl={prediction.awayTeamFlagUrl}
                flagPlacement="start"
                justify="start"
              />
            </div>
          )}
          <p className="mt-0.5 text-center text-xs font-bold text-ink/60">{formatTime(prediction.startsAt)}</p>
        </div>
        <div className="space-y-1 text-center text-sm">
          <div>
            <p className="text-[10px] font-black uppercase tracking-[0.08em] text-ink/55">Palpite</p>
            <div className={`mt-0.5 inline-flex items-center justify-center font-black text-ink ${isFinished ? "text-[17px] leading-none" : "text-[15px]"}`}>
              <span className="inline-flex w-5 justify-center">{prediction.predictedHomeScore}</span>
              <span className="inline-flex w-4 justify-center text-ink/55">x</span>
              <span className="inline-flex w-5 justify-center">{prediction.predictedAwayScore}</span>
            </div>
          </div>
          <div className="flex items-start justify-between gap-3">
            <div className="space-y-2 text-left text-sm">
              {!isFinished ? (
                <p className="text-ink/65">
                  <span className="font-semibold">Resultado:</span> {formatScore(prediction.officialHomeScore, prediction.officialAwayScore)}
                </p>
              ) : null}
              {isFinished ? (
                <p className="inline-flex rounded-full border-2 border-[#2d6a4f] bg-[#dcefdc] px-2.5 py-1 text-sm font-black text-[#1b4332]">
                  +{prediction.pointsAwarded} pts
                </p>
              ) : null}
            </div>
            <div className={isFinished ? "self-start" : "self-end pb-0.5"}>
              <StatusBadge status={prediction.matchStatus} />
            </div>
          </div>
        </div>
      </div>
    </article>
  );
}
