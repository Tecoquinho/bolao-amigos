import { apiRequest } from "../../../shared/services/apiClient";
import type { Participant, ParticipantPrediction } from "../../../shared/types/api";

export function getParticipants() {
  return apiRequest<Participant[]>("/api/participants");
}

export function getParticipantById(participantId: number) {
  return apiRequest<Participant>(`/api/participants/${participantId}`);
}

export function getParticipantPredictions(participantId: number) {
  return apiRequest<ParticipantPrediction[]>(`/api/participants/${participantId}/predictions`);
}
