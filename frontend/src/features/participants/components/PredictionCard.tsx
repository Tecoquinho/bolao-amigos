import type { ParticipantPrediction } from "../../../shared/types/api";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { TeamLabel } from "../../../shared/components/TeamLabel";
import { formatScore, formatTime } from "../../../shared/utils/format";

interface PredictionCardProps {
  prediction: ParticipantPrediction;
}

export function PredictionCard({ prediction }: PredictionCardProps) {
  return (
    <article className="rounded-2xl border-[2.5px] border-[#1a3a2f] bg-white px-4 py-3 shadow-[3px_4px_0_#1a3a2f]">
      <div className="space-y-3">
        <div className="min-w-0 text-center">
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
          <p className="mt-1 text-center text-xs font-bold text-ink/60">{formatTime(prediction.startsAt)}</p>
        </div>
        <div className="flex items-start justify-between gap-3">
          <div className="space-y-1.5 text-sm">
            <p className="text-ink/85">
              <span className="font-semibold">Palpite:</span> {prediction.predictedHomeScore} x {prediction.predictedAwayScore}
            </p>
            <p className="text-ink/65">
              <span className="font-semibold">Resultado:</span> {formatScore(prediction.officialHomeScore, prediction.officialAwayScore)}
            </p>
            {prediction.matchStatus === "FINISHED" ? (
              <p className="font-semibold text-pine">+{prediction.pointsAwarded} pts</p>
            ) : null}
          </div>
          <StatusBadge status={prediction.matchStatus} />
        </div>
      </div>
    </article>
  );
}
