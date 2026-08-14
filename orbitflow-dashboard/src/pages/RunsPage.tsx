import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { listRuns } from "../api/endpoints";
import { StatusChip } from "../components/StatusChip";
import type { RunStatus, RunSummary } from "../api/types";
import { formatRelativeTime } from "../format";

const STATUS_OPTIONS: RunStatus[] = ["PENDING", "RUNNING", "COMPLETED", "FAILED"];

export function RunsPage() {
  const navigate = useNavigate();
  const [runs, setRuns] = useState<RunSummary[] | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>("");

  useEffect(() => {
    let cancelled = false;

    async function load() {
      const result = await listRuns({ status: statusFilter || undefined, limit: 50 });
      if (!cancelled) setRuns(result);
    }

    load();
    const interval = setInterval(load, 5000);
    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, [statusFilter]);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Runs</h1>
      </div>

      <div className="filter-bar">
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">All statuses</option>
          {STATUS_OPTIONS.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
      </div>

      <div className="data-table">
        {runs === null ? (
          <div className="loading-state">Loading runs...</div>
        ) : runs.length === 0 ? (
          <div className="empty-state">No runs match this filter.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Run</th>
                <th>Workflow</th>
                <th>Submitted by</th>
                <th>Started</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {runs.map((run) => (
                <tr key={run.runId} className="clickable" onClick={() => navigate(`/runs/${run.runId}`)}>
                  <td className="mono">{run.runId.slice(0, 8)}</td>
                  <td>{run.workflowName}</td>
                  <td>{run.submittedBy ?? "-"}</td>
                  <td>{formatRelativeTime(run.startedAt)}</td>
                  <td>
                    <StatusChip status={run.status} />
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
