import React, { useCallback, useMemo, useRef, useState } from 'react';
import { Bot, CheckCircle2, CircleAlert, GitBranch, ShieldCheck, Trash2, Wrench } from 'lucide-react';
import ReactFlow, { addEdge, Background, Controls, MiniMap, Panel, useEdgesState, useNodesState, type Connection, type Edge, type Node } from 'reactflow';
import 'reactflow/dist/style.css';
import { validateWorkflowGraph, type WorkflowNodeData, type WorkflowNodeKind } from './workflow-graph';

const startNodes: Node<WorkflowNodeData>[] = [
  { id:'trigger-1', type:'default', position:{x:60,y:160}, data:{kind:'trigger',label:'Start request',description:'Entry point for this draft.'} },
  { id:'agent-1', type:'default', position:{x:320,y:160}, data:{kind:'agent',label:'Plan task',description:'Describe the bounded reasoning step.'} },
  { id:'end-1', type:'default', position:{x:580,y:160}, data:{kind:'end',label:'Finish',description:'Return a validated result.'} },
];
const startEdges: Edge[] = [
  { id:'edge-start-agent', source:'trigger-1', target:'agent-1', type:'smoothstep' },
  { id:'edge-agent-end', source:'agent-1', target:'end-1', type:'smoothstep' },
];
const palette: {kind:WorkflowNodeKind;label:string;icon:typeof Bot}[] = [
  {kind:'agent',label:'Agent step',icon:Bot},
  {kind:'tool',label:'Tool step',icon:Wrench},
  {kind:'approval',label:'Approval gate',icon:ShieldCheck},
];
function defaults(kind:WorkflowNodeKind,n:number):WorkflowNodeData {
  const map:Record<WorkflowNodeKind,Omit<WorkflowNodeData,'kind'>> = {
    trigger:{label:'Start request',description:'Entry point for this draft.'},
    agent:{label:'Agent step '+n,description:'Describe the bounded reasoning step.'},
    tool:{label:'Tool step '+n,description:'Request a registered tool through server policy.',toolName:'text.word-count'},
    approval:{label:'Approval gate '+n,description:'Pause for an explicit human decision.'},
    end:{label:'Finish',description:'Return a validated result.'},
  };
  return {kind,...map[kind]};
}
export function WorkflowEditor() {
  const [nodes,setNodes,onNodesChange]=useNodesState<WorkflowNodeData>(startNodes);
  const [edges,setEdges,onEdgesChange]=useEdgesState(startEdges);
  const [selectedId,setSelectedId]=useState<string|null>('agent-1');
  const nextId=useRef(2);
  const selected=nodes.find((n)=>n.id===selectedId);
  const validation=useMemo(()=>validateWorkflowGraph(nodes,edges),[nodes,edges]);
  const onConnect=useCallback((connection:Connection)=>setEdges((current)=>addEdge({...connection,type:'smoothstep'},current)),[setEdges]);
  const addNode=(kind:WorkflowNodeKind)=>{
    const n=nextId.current++;
    const id=kind+'-'+n;
    setNodes((current)=>[...current,{id,type:'default',position:{x:140+(n%3)*90,y:300+Math.floor(n/3)*80},data:defaults(kind,n)}]);
    setSelectedId(id);
  };
  const patchSelected=(patch:Partial<WorkflowNodeData>)=>{
    if(!selectedId)return;
    setNodes((current)=>current.map((n)=>n.id===selectedId?{...n,data:{...n.data,...patch}}:n));
  };
  const deleteSelected=()=>{
    if(!selected||selected.data.kind==='trigger'||selected.data.kind==='end')return;
    setNodes((current)=>current.filter((n)=>n.id!==selected.id));
    setEdges((current)=>current.filter((e)=>e.source!==selected.id&&e.target!==selected.id));
    setSelectedId(null);
  };
  return <section className="workflow-editor-shell" aria-labelledby="workflow-editor-title">
    <div className="workflow-editor-heading"><div><div className="eyebrow">VISUAL WORKFLOW BUILDER</div><h2 id="workflow-editor-title">Shape the workflow before it runs.</h2><p>Connect steps, edit details, and validate the draft graph.</p></div><div className="workflow-draft-badge"><GitBranch size={14}/> Draft only</div></div>
    <div className="workflow-safety-note" role="note"><ShieldCheck size={18}/><p><strong>Server-owned safety boundary.</strong> This editor does not execute or submit the graph. The backend remains authoritative for registered tools, permissions, approvals, and all execution decisions.</p></div>
    <div className="workflow-toolbar" aria-label="Add workflow step"><span>Add a step</span>{palette.map(({kind,label,icon:Icon})=><button key={kind} type="button" className="workflow-tool-button" onClick={()=>addNode(kind)}><Icon size={15}/> {label}</button>)}<span className="workflow-count">{nodes.length}/12 nodes · {edges.length}/20 connections</span></div>
    <div className="workflow-editor-layout">
      <div className="workflow-canvas" aria-label="Workflow graph canvas"><ReactFlow nodes={nodes} edges={edges} onNodesChange={onNodesChange} onEdgesChange={onEdgesChange} onConnect={onConnect} onNodeClick={(_event,node)=>setSelectedId(node.id)} onPaneClick={()=>setSelectedId(null)} fitView fitViewOptions={{padding:0.2}} minZoom={0.35} maxZoom={1.5} nodesDraggable nodesConnectable deleteKeyCode={['Backspace','Delete']} ariaLabel="Workflow editor"><Background color="#34364a" gap={20}/><Controls/><MiniMap pannable zoomable/><Panel position="top-left"><span className="workflow-canvas-hint">Drag to arrange · Connect handles to link steps</span></Panel></ReactFlow></div>
      <aside className="workflow-inspector" aria-labelledby="node-inspector-title"><div className="workflow-inspector-heading"><div><span>STEP CONFIGURATION</span><h3 id="node-inspector-title">Selected node</h3></div>{selected&&selected.data.kind!=='trigger'&&selected.data.kind!=='end'&&<button type="button" className="workflow-delete-button" onClick={deleteSelected} aria-label="Delete selected node"><Trash2 size={15}/></button>}</div>
        {!selected&&<p className="workflow-inspector-empty">Select a node on the canvas to edit its details.</p>}
        {selected&&<form className="workflow-node-form" onSubmit={(event)=>event.preventDefault()}><div className="workflow-kind-label"><span>Node type</span><strong>{selected.data.kind}</strong></div><label htmlFor="workflow-node-label">Step name</label><input id="workflow-node-label" maxLength={80} value={selected.data.label} onChange={(event)=>patchSelected({label:event.target.value})}/><label htmlFor="workflow-node-description">Description</label><textarea id="workflow-node-description" maxLength={240} rows={4} value={selected.data.description} onChange={(event)=>patchSelected({description:event.target.value})}/>
          {selected.data.kind==='tool'&&<div className="workflow-tool-setting"><label htmlFor="workflow-tool-name">Requested tool</label><select id="workflow-tool-name" value={selected.data.toolName??'text.word-count'} onChange={(event)=>patchSelected({toolName:event.target.value})}><option value="text.word-count">text.word-count · catalog entry</option></select><small>Catalog visibility is not permission. The server checks its own allowlist on execution.</small></div>}
          <p className="workflow-form-hint">Changes stay in this page only; draft persistence is not wired yet.</p></form>}
      </aside>
    </div>
    <div className={validation.valid?'workflow-validation valid':'workflow-validation invalid'} role="status" aria-live="polite">{validation.valid?<CheckCircle2 size={18}/>:<CircleAlert size={18}/>}<div><strong>{validation.valid?'Graph shape is valid':validation.errors.length+' validation issue'+(validation.errors.length===1?'':'s')}</strong>{validation.valid?<p>The draft is connected, acyclic, and within size limits. Save and execution are not connected.</p>:<ul>{validation.errors.map((error)=><li key={error}>{error}</li>)}</ul>}</div></div>
  </section>;
}
