import type { MatchStatus, TournamentPhase } from "../types/api";

const phaseLabels: Record<TournamentPhase, string> = {
  GROUP_STAGE: "Fase de grupos",
  ROUND_OF_32: "32 avos",
  ROUND_OF_16: "Oitavas",
  QUARTER_FINAL: "Quartas",
  SEMI_FINAL: "Semifinal",
  THIRD_PLACE: "3o lugar",
  FINAL: "Final",
};

const statusLabels: Record<MatchStatus, string> = {
  SCHEDULED: "Agendado",
  LIVE: "Ao vivo",
  FINISHED: "Finalizado",
};

export function formatPhase(phase: TournamentPhase) {
  return phaseLabels[phase];
}

export function formatMatchStatus(status: MatchStatus) {
  return statusLabels[status];
}

export function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "America/Sao_Paulo",
  }).format(new Date(value));
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "medium",
    timeZone: "America/Sao_Paulo",
  }).format(new Date(value));
}

export function formatCompactDateTime(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    timeZone: "America/Sao_Paulo",
  }).format(new Date(value));
}

export function formatTime(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
    timeZone: "America/Sao_Paulo",
  }).format(new Date(value));
}

export function toDateInputValue(value: Date) {
  const year = value.getFullYear();
  const month = `${value.getMonth() + 1}`.padStart(2, "0");
  const day = `${value.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function formatScore(homeScore: number | null, awayScore: number | null) {
  if (homeScore === null || awayScore === null) {
    return "Aguardando resultado";
  }
  return `${homeScore} x ${awayScore}`;
}
