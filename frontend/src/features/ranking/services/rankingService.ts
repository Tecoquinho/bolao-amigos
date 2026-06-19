import { apiRequest } from "../../../shared/services/apiClient";
import type { RankingEntry } from "../../../shared/types/api";

export function getRanking() {
  return apiRequest<RankingEntry[]>("/api/ranking");
}
