export type RunStatus = "PENDING" | "RUNNING" | "COMPLETED" | "FAILED";

export type StepStatus =
  | "PENDING"
  | "READY"
  | "RUNNING"
  | "COMPLETED"
  | "FAILED"
  | "DEAD_LETTER";

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface RunSummary {
  runId: string;
  workflowName: string;
  status: RunStatus;
  submittedBy: string | null;
  startedAt: string;
  completedAt: string | null;
}

export interface WorkflowSummary {
  id: number;
  name: string;
  createdAt: string;
  stepNames: string[];
  lastRun: RunSummary | null;
}

export interface StepStatusInfo {
  stepId: string;
  stepName: string;
  status: StepStatus;
  dependsOn: string[];
  attemptCount: number;
}

export interface RunDetail {
  runId: string;
  workflowName: string;
  status: RunStatus;
  submittedBy: string | null;
  startedAt: string;
  completedAt: string | null;
  steps: StepStatusInfo[];
}

export interface DlqEntry {
  stepId: string;
  stepName: string;
  runId: string;
  workflowName: string;
  failureReason: string | null;
  failedAt: string;
  attempts: number;
}

export interface DashboardStats {
  activeRuns: number;
  completedToday: number;
  inDeadLetterQueue: number;
  avgStepDurationMs: number | null;
}
