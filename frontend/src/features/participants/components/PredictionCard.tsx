import type { ParticipantPrediction } from "../../../shared/types/api";
import { StatusBadge } from "../../../shared/components/StatusBadge";
import { formatScore, formatTime } from "../../../shared/utils/format";

interface PredictionCardProps {
  prediction: ParticipantPrediction;
}

export function PredictionCard({ prediction }: PredictionCardProps) {
  return (
    <article className="rounded-[28px] bg-white px-5 py-4 shadow-glow">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-lg font-semibold text-ink">
            {prediction.homeTeamName} x {prediction.awayTeamName}
          </p>
          <p className="mt-1 text-sm text-ink/60">{formatTime(prediction.startsAt)}</p>
        </div>
        <StatusBadge status={prediction.matchStatus} />
      </div>
      <div className="mt-4 space-y-2 text-sm">
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
    </article>
  );
}
