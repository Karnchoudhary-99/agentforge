import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, createRun, getApiErrorMessage, getRun, listRuns } from './run-api';
afterEach(() => vi.unstubAllGlobals());
const sampleRun = {
  id:'run-123', workflowName:'Research brief', task:'Summarize notes', status:'Succeeded',
  summary:'Completed in mock mode', createdAt:'2026-10-22T08:00:00Z', plan:[], events:[],
  durationMs:null, failureReason:null,
};
function response(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers:{'Content-Type':'application/json'} });
}
describe('run API client', () => {
  it('loads persisted run history', async () => {
    const fetchMock=vi.fn().mockResolvedValue(response([sampleRun])); vi.stubGlobal('fetch',fetchMock);
    await expect(listRuns()).resolves.toEqual([sampleRun]);
    expect(fetchMock).toHaveBeenCalledWith('/api/runs',{signal:undefined});
  });
  it('encodes a run identifier when refreshing its timeline', async () => {
    const fetchMock=vi.fn().mockResolvedValue(response(sampleRun)); vi.stubGlobal('fetch',fetchMock);
    await expect(getRun('run/with spaces')).resolves.toEqual(sampleRun);
    expect(fetchMock).toHaveBeenCalledWith('/api/runs/run%2Fwith%20spaces',{signal:undefined});
  });
  it('submits only bounded form fields as JSON', async () => {
    const fetchMock=vi.fn().mockResolvedValue(response(sampleRun,201)); vi.stubGlobal('fetch',fetchMock);
    const input={workflowName:'Research brief',task:'Summarize notes'};
    await expect(createRun(input)).resolves.toEqual(sampleRun);
    expect(fetchMock).toHaveBeenCalledWith('/api/runs',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(input),signal:undefined});
  });
  it('maps validation and authorization failures to safe messages', async () => {
    vi.stubGlobal('fetch',vi.fn().mockResolvedValue(response({message:'internal detail'},400)));
    await expect(createRun({workflowName:'',task:''})).rejects.toMatchObject({status:400,message:'Please enter a workflow name (max 80 characters) and task (max 1,000 characters).'});
    vi.stubGlobal('fetch',vi.fn().mockResolvedValue(response({},401)));
    await expect(listRuns()).rejects.toBeInstanceOf(ApiError);
    await expect(listRuns()).rejects.toMatchObject({status:401});
    expect(getApiErrorMessage(new ApiError(401,'safe auth message'),'history')).toBe('safe auth message');
  });
  it('does not expose raw network exceptions', async () => {
    vi.stubGlobal('fetch',vi.fn().mockRejectedValue(new Error('network internals')));
    await expect(listRuns()).rejects.toMatchObject({status:null,message:'Backend API unavailable. Start the Spring Boot API on port 8080, then retry.'});
    expect(getApiErrorMessage(new Error('network down'),'detail')).toBe('Unable to refresh this run timeline. The last loaded details are still shown.');
  });
});
