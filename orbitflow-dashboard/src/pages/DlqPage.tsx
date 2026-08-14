import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { listDlq, replayStep } from "../api/endpoints";
import type { DlqEntry } from "../api/types";
import { formatRelativeTime } from "../format";

export function DlqPage() {
  const navigate = useNavigate();
  const [entries, setEntries] = useState<DlqEntry[] | null>(null);
  const [replayingStepId, setReplayingStepId] = useState<string | null>(null);

  const load = useCallback(async () => {
    const result = await listDlq();
    setEntries(result);
  }, []);

  useEffect(() => {
    load();
    const interval = setInterval(load, 8000);
    return () => clearInterval(interval);
  }, [load]);

  async function handleReplay(stepId: string) {
    setReplayingStepId(stepId);
    try {
      await replayStep(stepId);
      await load();
    } finally {
      setReplayingStepId(null);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Dead-letter queue</h1>
      </div>

      <p style={{ color: "var(--text-muted)", fontSize: 13.5, marginTop: -12, marginBottom: 20 }}>
        Steps land here after exhausting their retry budget. Nothing here is retried automatically
        — inspect the failure, fix the underlying issue, then replay.
      </p>

      <div className="data-table">
        {entries === null ? (
          <div className="loading-state">Loading...</div>
        ) : entries.length === 0 ? (
          <div className="empty-state">Nothing dead-lettered right now.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Step</th>
                <th>Run</th>
                <th>Failure reason</th>
                <th>Failed</th>
                <th>Attempts</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {entries.map((entry) => (
                <tr key={entry.stepId}>
                  <td>
                    <div className="cell-title">{entry.stepName}</div>
                    <div className="cell-sub">{entry.workflowName}</div>
                  </td>
                  <td className="mono" style={{ cursor: "pointer" }} onClick={() => navigate(`/runs/${entry.runId}`)}>
                    {entry.runId.slice(0, 8)}
                  </td>
                  <td style={{ maxWidth: 320, color: "var(--critical)" }}>{entry.failureReason ?? "-"}</td>
                  <td>{formatRelativeTime(entry.failedAt)}</td>
                  <td>{entry.attempts}</td>
                  <td>
                    <button
                      className="btn btn-secondary"
                      disabled={replayingStepId === entry.stepId}
                      onClick={() => handleReplay(entry.stepId)}
                    >
                      {replayingStepId === entry.stepId ? "Replaying..." : "Replay"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
