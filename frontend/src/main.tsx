import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Activity, ArrowUpRight, Bot, CheckCircle2, CirclePlay, Clock3, GitBranch, ShieldCheck, Workflow, X, LoaderCircle } from 'lucide-react';
import { getRunTone } from './run-status';
import './style.css';

type RunEvent = { timestamp: string; type: string; message: string };
type PlanStep = { order: number; title: string; description: string; status: string };
type Run = {
  id: string;
  workflowName: string;
  task: string;
  status: string;
  summary: string;
  createdAt: string;
  plan: PlanStep[];
  events: RunEvent[];
  durationMs: number | null;
  failureReason: 'TIMEOUT' | 'PROVIDER_UNAVAILABLE' | 'OUTPUT_INVALID' | 'POLICY_DENIED' | 'UNKNOWN' | null;
};

const failureLabels: Record<NonNullable<Run['failureReason']>, string> = {
  TIMEOUT: 'Execution timed out',
  PROVIDER_UNAVAILABLE: 'Model provider unavailable',
  OUTPUT_INVALID: 'Output did not pass validation',
  POLICY_DENIED: 'Execution blocked by policy',
  UNKNOWN: 'Unclassified failure',
};

function App() {
  const [runs, setRuns] = useState<Run[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [workflowName, setWorkflowName] = useState('Research brief');
  const [task, setTask] = useState('');
  const [apiError, setApiError] = useState('');
  const [selectedRunId, setSelectedRunId] = useState('');

  const refreshRuns = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/runs');
      if (!response.ok) throw new Error('API returned ' + response.status);
      const data = await response.json() as Run[];
      setRuns(data);
      setApiError('');
    } catch {
      setApiError('Backend API unavailable. Start the Spring Boot API on port 8080, then refresh.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { void refreshRuns(); }, []);

  const handleCreateRun = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setApiError('');
    try {
      const response = await fetch('/api/runs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ workflowName, task }),
      });
      if (!response.ok) {
        if (response.status === 400) throw new Error('Please enter a workflow name (max 80 characters) and task (max 1,000 characters).');
        throw new Error('Run could not be created (HTTP ' + response.status + ').');
      }
      const created = await response.json() as Run;
      setRuns((current) => [created, ...current]);
      setSelectedRunId(created.id);
      setTask('');
      setShowCreateForm(false);
    } catch (error) {
      setApiError(error instanceof Error ? error.message : 'Unable to create this run.');
    } finally {
      setSubmitting(false);
    }
  };

  const selectedRun = runs.find((run) => run.id === selectedRunId);
  const successfulRuns = runs.filter((run) => run.status === 'Succeeded').length;
  const failedRuns = runs.filter((run) => run.status === 'Failed').length;
  const metrics = [
    { label: 'Workflow runs', value: String(runs.length), note: 'Persisted run history', icon: Activity },
    { label: 'Success rate', value: runs.length ? Math.round(successfulRuns / runs.length * 100) + '%' : '—', note: 'Based on loaded run records', icon: CheckCircle2 },
    { label: 'Failed runs', value: String(failedRuns), note: 'Safe failure codes only', icon: ShieldCheck },
  ];

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand"><div className="brand-mark"><Workflow size={21} /></div><span>agent<span className="brand-accent">forge</span></span></div>
        <div className="workspace-label">WORKSPACE</div>
        <button className="nav-item active"><Activity size={17} /> Overview</button>
        <button className="nav-item"><Workflow size={17} /> Workflows <span className="nav-count">1</span></button>
        <button className="nav-item"><Bot size={17} /> Agent registry</button>
        <button className="nav-item"><ShieldCheck size={17} /> Approvals <span className="nav-count alert">0</span></button>
        <button className="nav-item"><Clock3 size={17} /> Run history</button>
        <div className="sidebar-bottom"><div className="avatar">AF</div><div><strong>Personal workspace</strong><small>Local development · Mock mode</small></div><span className="online-dot" /></div>
      </aside>

      <section className="main-panel">
        <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> <strong>Overview</strong></div><div className="topbar-right"><span className="env-pill"><i /> Development</span><div className="user-avatar">K</div></div></header>
        <div className="page-content">
          <div className="welcome-row"><div><div className="eyebrow">YOUR AGENT OPERATIONS CENTER</div><h1>Good work starts with <span>visibility.</span></h1><p>Build, run, and govern AI workflows from one clear workspace.</p></div><button className="primary-button" onClick={() => setShowCreateForm((open) => !open)}><CirclePlay size={17} /> Create workflow <ArrowUpRight size={16} /></button></div>
          <div className="status-banner"><div className="status-icon"><ShieldCheck size={19} /></div><div><strong>Deterministic mock mode</strong><p>Safe simulated execution · No external model or tools are invoked · Run events are returned by the API</p></div><span className="banner-tag">MOCK</span></div>

          {showCreateForm && <section className="panel create-form-panel">
            <div className="panel-heading"><div><h2>Create a mock run</h2><p>Submit a task to the bounded deterministic runner</p></div><button className="icon-button" type="button" aria-label="Close create form" onClick={() => setShowCreateForm(false)}><X size={16} /></button></div>
            <form className="create-form" onSubmit={handleCreateRun}>
              <label htmlFor="workflow-name">Workflow name</label>
              <input id="workflow-name" maxLength={80} required value={workflowName} onChange={(event) => setWorkflowName(event.target.value)} />
              <label htmlFor="workflow-task">Task</label>
              <textarea id="workflow-task" maxLength={1000} minLength={1} required rows={3} value={task} onChange={(event) => setTask(event.target.value)} placeholder="Describe what the mock workflow should process..." />
              <div className="form-actions"><span>{task.length}/1000 characters</span><button className="primary-button" type="submit" disabled={submitting}>{submitting ? <LoaderCircle size={16} className="spin" /> : <CirclePlay size={16} />}{submitting ? 'Creating…' : 'Run mock workflow'}</button></div>
            </form>
          </section>}

          {apiError && <div className="api-error" role="alert"><strong>Could not complete request</strong><span>{apiError}</span><button className="quiet-button" onClick={() => void refreshRuns()}>Retry connection</button></div>}

          <div className="section-heading"><div><h2>Workspace health</h2><p>Persisted run outcomes and safe diagnostics</p></div><button className="quiet-button" onClick={() => void refreshRuns()} disabled={loading}>{loading ? 'Refreshing…' : 'Refresh runs'} <span>↻</span></button></div>
          <div className="metric-grid">{metrics.map(({ label, value, note, icon: Icon }) => <article className="metric-card" key={label}><div className="metric-top"><span>{label}</span><div className="metric-icon"><Icon size={17} /></div></div><div className="metric-value">{value}</div><div className="metric-note">{note}</div></article>)}</div>
          <div className="content-grid">
            <section className="panel runs-panel"><div className="panel-heading"><div><h2>Recent runs</h2><p>Inspect outcomes and execution events</p></div><button className="text-button" onClick={() => void refreshRuns()}>Refresh <ArrowUpRight size={14} /></button></div>
              <div className="run-list">
                {loading && <div className="empty-state"><LoaderCircle size={18} className="spin" /> Loading run history…</div>}
                {!loading && runs.length === 0 && <div className="empty-state">No runs yet. Select “Create workflow” to start a safe mock run.</div>}
                {!loading && runs.map((run) => <article className="run-row" key={run.id}>
                  <div className="run-symbol"><Workflow size={17} /></div>
                  <button className="run-info run-info-button" onClick={() => setSelectedRunId(run.id)}><strong>{run.workflowName}</strong><small>{run.id.slice(0, 8)} · {new Date(run.createdAt).toLocaleString()}</small></button>
                  <span className={'run-status ' + getRunTone(run.status)}><i />{run.status}</span>
                  <button className="more-button" aria-label={'Inspect ' + run.workflowName} onClick={() => setSelectedRunId(run.id)}>···</button>
                </article>)}
              </div><div className="panel-footer">Run records, plan snapshots, and timeline events persist in PostgreSQL.</div>
            </section>
            <section className="panel quick-panel"><div className="panel-heading"><div><h2>Start with a template</h2><p>Safe starting points for common work</p></div></div><button className="template-row" onClick={() => { setWorkflowName('Research & synthesize'); setTask('Summarize the main points of this task without calling external tools.'); setShowCreateForm(true); }}><div className="template-icon purple"><Bot size={18} /></div><div><strong>Research & synthesize</strong><small>Gather and summarize a supplied task</small></div><ArrowUpRight size={15} /></button><button className="template-row" onClick={() => { setWorkflowName('Review a change'); setTask('Review this supplied change description and list potential risks.'); setShowCreateForm(true); }}><div className="template-icon teal"><GitBranch size={18} /></div><div><strong>Review a change</strong><small>Inspect a description with bounded checks</small></div><ArrowUpRight size={15} /></button><button className="template-row" onClick={() => { setWorkflowName('Approval workflow'); setTask('Describe the sensitive action that would require human approval.'); setShowCreateForm(true); }}><div className="template-icon amber"><ShieldCheck size={18} /></div><div><strong>Approval workflow</strong><small>Preview a future approval-gated task</small></div><ArrowUpRight size={15} /></button><div className="safety-note"><ShieldCheck size={16} /><span>The mock runner does not call an LLM, execute tools, or perform external side effects.</span></div></section>
          </div>

          {selectedRun && <section className="panel run-detail-panel"><div className="panel-heading"><div><h2>{selectedRun.workflowName}</h2><p>Run {selectedRun.id}</p></div><button className="icon-button" aria-label="Close run details" onClick={() => setSelectedRunId('')}><X size={16} /></button></div><div className="run-detail-body"><span className={'run-status ' + getRunTone(selectedRun.status)}><i />{selectedRun.status}</span><p>{selectedRun.summary}</p><div className="diagnostics-grid"><div><span>Execution duration</span><strong>{selectedRun.durationMs == null ? 'Not measured (mock mode)' : selectedRun.durationMs + ' ms'}</strong></div><div><span>Failure classification</span><strong>{selectedRun.status === 'Failed' ? (selectedRun.failureReason ? failureLabels[selectedRun.failureReason] : 'Run failed; safe details unavailable.') : 'No failure recorded'}</strong></div><div><span>Token / cost usage</span><strong>Unavailable — no model provider invoked</strong></div></div><h3>Execution plan</h3><ol className="timeline">{selectedRun.plan.map((step) => <li key={step.order}><time>Step {step.order}</time><strong>{step.title}</strong><span>{step.description}</span><span className="plan-step-status">{step.status}</span></li>)}</ol><h3>Execution timeline</h3><ol className="timeline">{selectedRun.events.map((item, index) => <li key={item.type + index}><time>{new Date(item.timestamp).toLocaleTimeString()}</time><strong>{item.type}</strong><span>{item.message}</span></li>)}</ol></div></section>}

          <footer className="page-footer"><span>AGENTFORGE <span className="footer-separator">/</span> OBSERVABILITY FIRST</span><span>Session 8 · Run diagnostics <i /></span></footer>
        </div>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')!).render(<React.StrictMode><App /></React.StrictMode>);
