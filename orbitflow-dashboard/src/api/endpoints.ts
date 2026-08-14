import { api } from "./client";
import type {
  DashboardStats,
  DlqEntry,
  LoginResponse,
  RunDetail,
  RunSummary,
  StepStatusInfo,
  WorkflowSummary,
} from "./types";

export function login(username: string, password: string) {
  return api.post<LoginResponse>("/auth/login", { username, password });
}

export function getStats() {
  return api.get<DashboardStats>("/stats");
}

export function listWorkflows() {
  return api.get<WorkflowSummary[]>("/workflows");
}

export function listRuns(params: { workflow?: string; status?: string; limit?: number } = {}) {
  const query = new URLSearchParams();
  if (params.workflow) query.set("workflow", params.workflow);
  if (params.status) query.set("status", params.status);
  query.set("limit", String(params.limit ?? 20));
  return api.get<RunSummary[]>(`/runs?${query.toString()}`);
}

export function getRun(runId: string) {
  return api.get<RunDetail>(`/runs/${runId}`);
}

export function listDlq() {
  return api.get<DlqEntry[]>("/dlq");
}

export function replayStep(stepId: string) {
  return api.post<StepStatusInfo>(`/dlq/${stepId}/replay`);
}
