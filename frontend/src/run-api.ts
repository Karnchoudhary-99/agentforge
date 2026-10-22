export type RunEvent = { timestamp: string; type: string; message: string };
export type PlanStep = { order: number; title: string; description: string; status: string };
export type FailureReason = 'TIMEOUT' | 'PROVIDER_UNAVAILABLE' | 'OUTPUT_INVALID' | 'POLICY_DENIED' | 'UNKNOWN';
export type Run = {
  id: string; workflowName: string; task: string; status: string; summary: string; createdAt: string;
  plan: PlanStep[]; events: RunEvent[]; durationMs: number | null; failureReason: FailureReason | null;
};
export type CreateRunInput = { workflowName: string; task: string };

export class ApiError extends Error {
  constructor(readonly status: number | null, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}
function messageForStatus(status: number): string {
  if (status === 400) return 'Please enter a workflow name (max 80 characters) and task (max 1,000 characters).';
  if (status === 401 || status === 403) return 'The API rejected this request. Check the local API token and server configuration.';
  if (status === 404) return 'This run or API route was not found. Refresh run history and try again.';
  if (status === 503) return 'The backend is temporarily unavailable or its required configuration is missing.';
  if (status >= 500) return 'The backend could not complete this request (HTTP ' + status + '). Try again after checking the API logs.';
  return 'The request failed (HTTP ' + status + ').';
}
async function requestJson<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(path, init);
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') throw error;
    throw new ApiError(null, 'Backend API unavailable. Start the Spring Boot API on port 8080, then retry.');
  }
  if (!response.ok) throw new ApiError(response.status, messageForStatus(response.status));
  try { return await response.json() as T; }
  catch { throw new ApiError(response.status, 'The backend returned an unreadable response. Refresh and try again.'); }
}
export function listRuns(signal?: AbortSignal): Promise<Run[]> {
  return requestJson<Run[]>('/api/runs', { signal });
}
export function getRun(id: string, signal?: AbortSignal): Promise<Run> {
  return requestJson<Run>('/api/runs/' + encodeURIComponent(id), { signal });
}
export function createRun(input: CreateRunInput, signal?: AbortSignal): Promise<Run> {
  return requestJson<Run>('/api/runs', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(input), signal,
  });
}
export function getApiErrorMessage(error: unknown, action: 'history' | 'create' | 'detail'): string {
  if (error instanceof ApiError) return error.message;
  if (error instanceof Error && error.name === 'AbortError') return '';
  if (action === 'create') return 'Unable to create this run. Check the backend connection and retry.';
  if (action === 'detail') return 'Unable to refresh this run timeline. The last loaded details are still shown.';
  return 'Unable to refresh run history. Check the backend connection and retry.';
}
