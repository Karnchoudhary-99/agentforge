export type FailureReason =
  | 'TIMEOUT'
  | 'PROVIDER_UNAVAILABLE'
  | 'OUTPUT_INVALID'
  | 'POLICY_DENIED'
  | 'UNKNOWN';

const failureLabels: Record<FailureReason, string> = {
  TIMEOUT: 'Execution timed out',
  PROVIDER_UNAVAILABLE: 'Model provider unavailable',
  OUTPUT_INVALID: 'Output did not pass validation',
  POLICY_DENIED: 'Execution blocked by policy',
  UNKNOWN: 'Unclassified failure',
};

export function formatDuration(durationMs: number | null): string {
  if (durationMs == null) return 'Not measured (mock mode)';
  if (!Number.isFinite(durationMs) || durationMs < 0) return 'Unavailable';
  if (durationMs < 1000) return durationMs + ' ms';
  return (durationMs / 1000).toFixed(2) + ' s';
}

export function formatFailureReason(status: string, reason: FailureReason | null): string {
  if (status !== 'Failed') return 'No failure recorded';
  return reason ? failureLabels[reason] : 'Run failed; safe details unavailable.';
}
