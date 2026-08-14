import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getRun, replayStep } from "../api/endpoints";
import { StatusChip } from "../components/StatusChip";
import type { RunDetail, StepStatusInfo } from "../api/types";
import { formatTimestamp } from "../format";

function layoutColumns(steps: StepStatusInfo[]): StepStatusInfo[][] {
  const byName = new Map(steps.map((s) => [s.stepName, s]));
  const layerByName = new Map<string, number>();

  function layerOf(name: string, seen: Set<string>): number {
    if (layerByName.has(name)) return layerByName.get(name)!;
    if (seen.has(name)) return 0; // guards against a cycle slipping through
    seen.add(name);

    const step = byName.get(name);
    if (!step || step.dependsOn.length === 0) {
      layerByName.set(name, 0);
      return 0;
    }

    const layer = 1 + Math.max(...step.dependsOn.map((dep) => layerOf(dep, seen)));
    layerByName.set(name, layer);
    return layer;
  }

  steps.forEach((s) => layerOf(s.stepName, new Set()));

  const maxLayer = Math.max(0, ...Array.from(layerByName.values()));
  const columns: StepStatusInfo[][] = Array.from({ length: maxLayer + 1 }, () => []);
  steps.forEach((s) => columns[layerByName.get(s.stepName)!].push(s));
  return columns;
}

export function RunDetailPage() {
  const { runId } = useParams<{ runId: string }>();
  const navigate = useNavigate();
  const [run, setRun] = useState<RunDetail | null>(null);
  const [selectedStepId, setSelectedStepId] = useState<string | null>(null);
  const [replaying, setReplaying] = useState(false);

  const load = useCallback(async () => {
    if (!runId) return;
    const result = await getRun(runId);
    setRun(result);
  }, [runId]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!run || (run.status !== "RUNNING" && run.status !== "PENDING")) return;
    const interval = setInterval(load, 1500);
    return () => clearInterval(interval);
  }, [run, load]);

  if (!run) {
    return <div className="loading-state">Loading run...</div>;
  }

  const columns = layoutColumns(run.steps);
  const selectedStep = run.steps.find((s) => s.stepId === selectedStepId) ?? null;

  async function handleReplay(stepId: string) {
    setReplaying(true);
    try {
      await replayStep(stepId);
      await load();
    } finally {
      setReplaying(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">
          {run.workflowName} <span className="mono" style={{ color: "var(--text-faint)", fontWeight: 500 }}>
            {run.runId.slice(0, 8)}
          </span>
        </h1>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          Back
        </button>
      </div>

      <div className="run-meta">
        <div className="run-meta-item">
          <div className="label">Status</div>
          <div className="value">
            <StatusChip status={run.status} />
          </div>
        </div>
        <div className="run-meta-item">
          <div className="label">Submitted by</div>
          <div className="value">{run.submittedBy ?? "-"}</div>
        </div>
        <div className="run-meta-item">
          <div className="label">Started</div>
          <div className="value">{formatTimestamp(run.startedAt)}</div>
        </div>
        <div className="run-meta-item">
          <div className="label">Completed</div>
          <div className="value">{formatTimestamp(run.completedAt)}</div>
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 300px", gap: 20 }}>
        <div className="data-table" style={{ padding: 8 }}>
          <div className="graph">
            {columns.map((column, i) => (
              <div className="graph-column" key={i}>
                {column.map((step) => (
                  <div
                    key={step.stepId}
                    className={`graph-node status-${step.status.toLowerCase()}${step.stepId === selectedStepId ? " selected" : ""}`}
                    onClick={() => setSelectedStepId(step.stepId)}
                  >
                    <div className="graph-node-name">{step.stepName}</div>
                    <StatusChip status={step.status} />
                    <div className="graph-node-meta" style={{ marginTop: 6 }}>
                      {step.dependsOn.length > 0 ? `waits on ${step.dependsOn.join(", ")}` : "no dependencies"}
                    </div>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>

        <div className="detail-panel">
          <h3>{selectedStep ? selectedStep.stepName : "Select a step"}</h3>
          {selectedStep ? (
            <>
              <div className="detail-row">
                <span className="label">Status</span>
                <StatusChip status={selectedStep.status} />
              </div>
              <div className="detail-row">
                <span className="label">Attempts</span>
                <span>{selectedStep.attemptCount}</span>
              </div>
              <div className="detail-row">
                <span className="label">Depends on</span>
                <span>{selectedStep.dependsOn.length ? selectedStep.dependsOn.join(", ") : "none"}</span>
              </div>
              {selectedStep.status === "DEAD_LETTER" && (
                <button
                  className="btn"
                  style={{ marginTop: 14 }}
                  disabled={replaying}
                  onClick={() => handleReplay(selectedStep.stepId)}
                >
                  {replaying ? "Replaying..." : "Replay step"}
                </button>
              )}
            </>
          ) : (
            <p style={{ color: "var(--text-faint)", fontSize: 13.5 }}>Click a step in the graph to see details.</p>
          )}
        </div>
      </div>
    </div>
  );
}
