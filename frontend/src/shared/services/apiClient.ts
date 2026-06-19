import type { ApiErrorPayload } from "../types/api";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export class ApiError extends Error {
  constructor(message: string, public readonly status: number) {
    super(message);
    this.name = "ApiError";
  }
}

async function parseErrorMessage(response: Response) {
  try {
    const payload = (await response.json()) as ApiErrorPayload;
    return payload.message ?? "Falha ao carregar dados do servidor.";
  } catch {
    return "Falha ao carregar dados do servidor.";
  }
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
    throw new ApiError(await parseErrorMessage(response), response.status);
  }

  return (await response.json()) as T;
}
