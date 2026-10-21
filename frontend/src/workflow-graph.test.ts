import { describe, expect, it } from 'vitest';
import type { Edge, Node } from 'reactflow';
import { MAX_WORKFLOW_NODES, validateWorkflowGraph, type WorkflowNodeData } from './workflow-graph';

const node = (id: string, kind: WorkflowNodeData['kind'], label: string = kind, toolName?: string): Node<WorkflowNodeData> => ({
  id, type: 'default', position: { x: 0, y: 0 },
  data: { kind, label, description: 'Draft step', ...(toolName ? { toolName } : {}) },
});
const edge = (id: string, source: string, target: string): Edge => ({ id, source, target });
function validGraph() {
  return {
    nodes: [node('start', 'trigger', 'Start'), node('agent', 'agent', 'Summarize'), node('end', 'end', 'Finish')],
    edges: [edge('e1', 'start', 'agent'), edge('e2', 'agent', 'end')],
  };
}
describe('validateWorkflowGraph', () => {
  it('accepts a connected acyclic trigger-to-end draft', () => {
    const g = validGraph();
    expect(validateWorkflowGraph(g.nodes, g.edges)).toEqual({ valid: true, errors: [] });
  });
  it('rejects nodes disconnected from the trigger', () => {
    const g = validGraph();
    const r = validateWorkflowGraph([...g.nodes, node('orphan', 'agent')], g.edges);
    expect(r.valid).toBe(false);
    expect(r.errors.join(' ')).toContain('Every node must be reachable from the trigger');
  });
  it('rejects cycles and illegal end direction', () => {
    const g = validGraph();
    const r = validateWorkflowGraph(g.nodes, [...g.edges, edge('e3', 'end', 'agent')]);
    expect(r.valid).toBe(false);
    expect(r.errors.join(' ')).toContain('Cycles are not allowed');
    expect(r.errors.join(' ')).toContain('End nodes cannot have outgoing connections');
  });
  it('rejects dangling and self-referential connections', () => {
    const g = validGraph();
    const r = validateWorkflowGraph(g.nodes, [...g.edges, edge('e3', 'agent', 'missing'), edge('e4', 'agent', 'agent')]);
    expect(r.valid).toBe(false);
    expect(r.errors.join(' ')).toContain('existing node');
    expect(r.errors.join(' ')).toContain('cannot connect to itself');
  });
  it('rejects a tool name outside the visible catalog', () => {
    const g = validGraph();
    const nodes = [g.nodes[0], node('tool', 'tool', 'Run tool', 'unlisted.tool'), g.nodes[2]];
    const r = validateWorkflowGraph(nodes, [edge('e1', 'start', 'tool'), edge('e2', 'tool', 'end')]);
    expect(r.valid).toBe(false);
    expect(r.errors.join(' ')).toContain('Tool nodes may reference only a tool shown in the client catalog');
  });
  it('caps node count', () => {
    const g = validGraph();
    const extra = Array.from({ length: MAX_WORKFLOW_NODES }, (_, i) => node('extra-' + i, 'agent'));
    const r = validateWorkflowGraph([...g.nodes, ...extra], g.edges);
    expect(r.valid).toBe(false);
    expect(r.errors.join(' ')).toContain('at most 12 nodes');
  });
});
