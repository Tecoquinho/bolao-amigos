export type MatchStatus = "SCHEDULED" | "LIVE" | "FINISHED";

export type TournamentPhase =
  | "GROUP_STAGE"
  | "ROUND_OF_32"
  | "ROUND_OF_16"
  | "QUARTER_FINAL"
  | "SEMI_FINAL"
  | "THIRD_PLACE"
  | "FINAL";

export interface RankingEntry {
  position: number;
  participantId: number;
  participantName: string;
  totalPoints: number;
  exactScoreHits: number;
  resultHits: number;
}

export interface Participant {
  id: number;
  name: string;
}

export interface ParticipantPrediction {
  predictionId: number;
  matchId: number;
  phase: TournamentPhase;
  matchNumber: number;
  startsAt: string;
  matchStatus: MatchStatus;
  homeTeamName: string;
  homeTeamFifaCode: string | null;
  homeTeamFlagUrl: string | null;
  awayTeamName: string;
  awayTeamFifaCode: string | null;
  awayTeamFlagUrl: string | null;
  predictedHomeScore: number;
  predictedAwayScore: number;
  officialHomeScore: number | null;
  officialAwayScore: number | null;
  pointsAwarded: number;
}

export interface Match {
  id: number;
  phase: TournamentPhase;
  matchNumber: number;
  homeTeamId: number;
  homeTeamName: string;
  homeTeamFifaCode: string | null;
  homeTeamFlagUrl: string | null;
  awayTeamId: number;
  awayTeamName: string;
  awayTeamFifaCode: string | null;
  awayTeamFlagUrl: string | null;
  startsAt: string;
  status: MatchStatus;
  venue: string | null;
  homeScore: number | null;
  awayScore: number | null;
  officialResultAt: string | null;
}

export interface ApiErrorPayload {
  message?: string;
}
