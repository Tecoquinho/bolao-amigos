import { apiRequest } from "../../../shared/services/apiClient";
import type { Match, MatchStatus, TournamentPhase } from "../../../shared/types/api";

export interface MatchFilters {
  status?: MatchStatus;
  phase?: TournamentPhase;
  date?: string;
}

export function getMatches(filters: MatchFilters = {}) {
  const params = new URLSearchParams();

  if (filters.status) {
    params.set("status", filters.status);
  }
  if (filters.phase) {
    params.set("phase", filters.phase);
  }
  if (filters.date) {
    params.set("date", filters.date);
  }

  const query = params.toString();
  return apiRequest<Match[]>(query ? `/api/matches?${query}` : "/api/matches");
}

export function getMatchById(matchId: number) {
  return apiRequest<Match>(`/api/matches/${matchId}`);
}
