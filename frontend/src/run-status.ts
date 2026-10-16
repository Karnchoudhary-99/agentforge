export type RunStatus = 'Succeeded' | 'Awaiting approval' | 'Failed' | 'Running' | 'Queued';
export type RunTone = 'success' | 'pending' | 'failed' | 'active' | 'neutral';

/** Map an API run status to a finite UI tone; unknown values never become CSS classes. */
export function getRunTone(status: string): RunTone {
  switch (status) {
    case 'Succeeded': return 'success';
    case 'Awaiting approval': return 'pending';
    case 'Failed': return 'failed';
    case 'Running': return 'active';
    case 'Queued': return 'neutral';
    default: return 'neutral';
  }
}
