interface StatCardProps {
  label: string;
  value: string;
  critical?: boolean;
}

export function StatCard({ label, value, critical }: StatCardProps) {
  return (
    <div className="stat-card">
      <div className="stat-label">{label}</div>
      <div className={`stat-value${critical ? " critical" : ""}`}>{value}</div>
    </div>
  );
}
