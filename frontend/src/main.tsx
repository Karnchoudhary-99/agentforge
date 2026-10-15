import React from 'react';
import { createRoot } from 'react-dom/client';
import { Activity, ArrowUpRight, Bot, CheckCircle2, CirclePlay, Clock3, GitBranch, ShieldCheck, Workflow } from 'lucide-react';
import './style.css';

const metrics = [
  { label: 'Workflow runs', value: '128', note: '+12 this week', icon: Activity },
  { label: 'Success rate', value: '96.8%', note: 'Last 7 days', icon: CheckCircle2 },
  { label: 'Approval queue', value: '03', note: 'Needs your review', icon: ShieldCheck },
];

const runs = [
  { name: 'Research brief generator', id: 'run_01J9K2', status: 'Succeeded', tone: 'success', time: '2 min ago', duration: '18.4s' },
  { name: 'Release notes assistant', id: 'run_01J9K1', status: 'Awaiting approval', tone: 'pending', time: '14 min ago', duration: '—' },
  { name: 'Support ticket triage', id: 'run_01J9JZ', status: 'Failed', tone: 'failed', time: '31 min ago', duration: '4.2s' },
];

function App() {
  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand"><div className="brand-mark"><Workflow size={21} /></div><span>agent<span className="brand-accent">forge</span></span></div>
        <div className="workspace-label">WORKSPACE</div>
        <button className="nav-item active"><Activity size={17} /> Overview</button>
        <button className="nav-item"><Workflow size={17} /> Workflows <span className="nav-count">4</span></button>
        <button className="nav-item"><Bot size={17} /> Agent registry</button>
        <button className="nav-item"><ShieldCheck size={17} /> Approvals <span className="nav-count alert">3</span></button>
        <button className="nav-item"><Clock3 size={17} /> Run history</button>
        <div className="sidebar-bottom"><div className="avatar">AF</div><div><strong>Personal workspace</strong><small>Local development</small></div><span className="online-dot" /></div>
      </aside>

      <section className="main-panel">
        <header className="topbar"><div className="breadcrumb">Workspace <span>/</span> <strong>Overview</strong></div><div className="topbar-right"><span className="env-pill"><i /> Development</span><div className="user-avatar">K</div></div></header>
        <div className="page-content">
          <div className="welcome-row"><div><div className="eyebrow">YOUR AGENT OPERATIONS CENTER</div><h1>Good work starts with <span>visibility.</span></h1><p>Build, run, and govern AI workflows from one clear workspace.</p></div><button className="primary-button"><CirclePlay size={17} /> Create workflow <ArrowUpRight size={16} /></button></div>
          <div className="status-banner"><div className="status-icon"><ShieldCheck size={19} /></div><div><strong>Policy controls are enabled</strong><p>Registered tools only · Human approval for sensitive actions · Execution history on</p></div><span className="banner-tag">GUARDED</span></div>
          <div className="section-heading"><div><h2>Workspace health</h2><p>A quick read on your agent operations</p></div><button className="quiet-button">Last 7 days <span>⌄</span></button></div>
          <div className="metric-grid">{metrics.map(({ label, value, note, icon: Icon }) => <article className="metric-card" key={label}><div className="metric-top"><span>{label}</span><div className="metric-icon"><Icon size={17} /></div></div><div className="metric-value">{value}</div><div className="metric-note">{note}</div></article>)}</div>
          <div className="content-grid">
            <section className="panel runs-panel"><div className="panel-heading"><div><h2>Recent runs</h2><p>Inspect outcomes, evidence, and approvals</p></div><button className="text-button">View all <ArrowUpRight size={14} /></button></div><div className="run-list">{runs.map(run => <article className="run-row" key={run.id}><div className="run-symbol"><Workflow size={17} /></div><div className="run-info"><strong>{run.name}</strong><small>{run.id} · {run.time}</small></div><span className={'run-status ' + run.tone}><i />{run.status}</span><span className="run-duration">{run.duration}</span><button className="more-button" aria-label={'Open ' + run.name}>···</button></article>)}</div><div className="panel-footer">Showing sample interface data until live run history is connected.</div></section>
            <section className="panel quick-panel"><div className="panel-heading"><div><h2>Start with a template</h2><p>Safe starting points for common work</p></div></div><button className="template-row"><div className="template-icon purple"><Bot size={18} /></div><div><strong>Research & synthesize</strong><small>Gather sources, summarize findings</small></div><ArrowUpRight size={15} /></button><button className="template-row"><div className="template-icon teal"><GitBranch size={18} /></div><div><strong>Review a change</strong><small>Inspect a diff with bounded checks</small></div><ArrowUpRight size={15} /></button><button className="template-row"><div className="template-icon amber"><ShieldCheck size={18} /></div><div><strong>Approval workflow</strong><small>Pause before sensitive actions</small></div><ArrowUpRight size={15} /></button><div className="safety-note"><ShieldCheck size={16} /><span>AgentForge is designed so the model suggests actions, while application policy decides what can run.</span></div></section>
          </div>
          <footer className="page-footer"><span>AGENTFORGE <span className="footer-separator">/</span> OBSERVABILITY FIRST</span><span>Session 1 · Foundation <i /></span></footer>
        </div>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')!).render(<React.StrictMode><App /></React.StrictMode>);
