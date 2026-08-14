interface StatusChipProps {
  status: string;
}

export function StatusChip({ status }: StatusChipProps) {
  const className = `chip status-${status.toLowerCase()}`;
  return (
    <span className={className}>
      <span className="chip-dot" />
      {status.replace("_", " ")}
    </span>
  );
}
