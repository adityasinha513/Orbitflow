import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getStats, listWorkflows } from "../api/endpoints";
import { StatCard } from "../components/StatCard";
import { StatusChip } from "../components/StatusChip";
import type { DashboardStats, WorkflowSummary } from "../api/types";
import { formatDuration, formatRelativeTime } from "../format";

export function WorkflowsPage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [workflows, setWorkflows] = useState<WorkflowSummary[] | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      const [statsResult, workflowsResult] = await Promise.all([getStats(), listWorkflows()]);
      if (!cancelled) {
        setStats(statsResult);
        setWorkflows(workflowsResult);
      }
    }

    load();
    const interval = setInterval(load, 5000);
    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, []);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Workflows</h1>
      </div>

      <div className="stat-grid">
        <StatCard label="Active runs" value={stats ? String(stats.activeRuns) : "-"} />
        <StatCard label="Completed today" value={stats ? String(stats.completedToday) : "-"} />
        <StatCard
          label="In dead-letter queue"
          value={stats ? String(stats.inDeadLetterQueue) : "-"}
          critical={!!stats && stats.inDeadLetterQueue > 0}
        />
        <StatCard label="Avg step duration" value={stats ? formatDuration(stats.avgStepDurationMs) : "-"} />
      </div>

      <div className="data-table">
        {workflows === null ? (
          <div className="loading-state">Loading workflows...</div>
        ) : workflows.length === 0 ? (
          <div className="empty-state">No workflows defined yet.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Workflow</th>
                <th>Steps</th>
                <th>Last run</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {workflows.map((workflow) => (
                <tr
                  key={workflow.id}
                  className="clickable"
                  onClick={() => workflow.lastRun && navigate(`/runs/${workflow.lastRun.runId}`)}
                >
                  <td>
                    <div className="cell-title">{workflow.name}</div>
                    <div className="cell-sub">{workflow.stepNames.join(" · ")}</div>
                  </td>
                  <td>{workflow.stepNames.length}</td>
                  <td>{workflow.lastRun ? formatRelativeTime(workflow.lastRun.startedAt) : "never run"}</td>
                  <td>{workflow.lastRun ? <StatusChip status={workflow.lastRun.status} /> : "-"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
