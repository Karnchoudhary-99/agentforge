import type { Edge, Node } from 'reactflow';

export type WorkflowNodeKind = 'trigger' | 'agent' | 'tool' | 'approval' | 'end';
export type WorkflowNodeData = { kind: WorkflowNodeKind; label: string; description: string; toolName?: string };
export type WorkflowGraphValidation = { valid: boolean; errors: string[] };

export const MAX_WORKFLOW_NODES = 12;
export const MAX_WORKFLOW_EDGES = 20;
export const CLIENT_VISIBLE_TOOL_ALLOWLIST = ['text.word-count'] as const;

/** Validates draft shape only; backend policy and approval checks remain authoritative. */
export function validateWorkflowGraph(nodes: Node<WorkflowNodeData>[], edges: Edge[]): WorkflowGraphValidation {
  const errors: string[] = [];
  const nodeIds = new Set<string>();
  const edgeIds = new Set<string>();
  const nodeById = new Map<string, Node<WorkflowNodeData>>();
  const knownKinds = new Set<WorkflowNodeKind>(['trigger', 'agent', 'tool', 'approval', 'end']);
  if (nodes.length < 2) errors.push('A workflow needs at least a trigger and an end node.');
  if (nodes.length > MAX_WORKFLOW_NODES) errors.push('A workflow can contain at most 12 nodes.');
  if (edges.length > MAX_WORKFLOW_EDGES) errors.push('A workflow can contain at most 20 connections.');

  for (const node of nodes) {
    if (nodeIds.has(node.id)) errors.push('Node IDs must be unique.');
    nodeIds.add(node.id);
    nodeById.set(node.id, node);
    if (!knownKinds.has(node.data?.kind)) errors.push('Every node must have a supported node type.');
    const label = node.data?.label?.trim() ?? '';
    if (!label) errors.push('Every node needs a name.');
    if (label.length > 80) errors.push('Node names must be 80 characters or fewer.');
    if ((node.data?.description?.length ?? 0) > 240) errors.push('Node descriptions must be 240 characters or fewer.');
    if (node.data?.kind === 'tool') {
      const name = node.data.toolName;
      if (!name || !CLIENT_VISIBLE_TOOL_ALLOWLIST.includes(name as (typeof CLIENT_VISIBLE_TOOL_ALLOWLIST)[number])) {
        errors.push('Tool nodes may reference only a tool shown in the client catalog; the server still decides whether it is allowed.');
      }
    }
  }

  const triggers = nodes.filter((node) => node.data?.kind === 'trigger');
  const ends = nodes.filter((node) => node.data?.kind === 'end');
  if (triggers.length !== 1) errors.push('A workflow must have exactly one trigger node.');
  if (ends.length < 1) errors.push('A workflow must have at least one end node.');

  const outgoing = new Map<string, string[]>();
  const incoming = new Map<string, string[]>();
  for (const id of nodeIds) { outgoing.set(id, []); incoming.set(id, []); }
  const pairs = new Set<string>();
  for (const edge of edges) {
    if (edgeIds.has(edge.id)) errors.push('Connection IDs must be unique.');
    edgeIds.add(edge.id);
    if (!nodeById.has(edge.source) || !nodeById.has(edge.target)) {
      errors.push('Every connection must point to an existing node.');
      continue;
    }
    if (edge.source === edge.target) { errors.push('A node cannot connect to itself.'); continue; }
    const pair = edge.source + '->' + edge.target;
    if (pairs.has(pair)) { errors.push('Duplicate connections are not allowed.'); continue; }
    pairs.add(pair);
    outgoing.get(edge.source)?.push(edge.target);
    incoming.get(edge.target)?.push(edge.source);
  }
  for (const trigger of triggers) if ((incoming.get(trigger.id) ?? []).length) errors.push('The trigger node cannot have incoming connections.');
  for (const end of ends) if ((outgoing.get(end.id) ?? []).length) errors.push('End nodes cannot have outgoing connections.');

  if (nodes.length > 0 && nodeIds.size === nodes.length) {
    const indegree = new Map<string, number>();
    for (const id of nodeIds) indegree.set(id, (incoming.get(id) ?? []).length);
    const queue = [...nodeIds].filter((id) => indegree.get(id) === 0);
    let visited = 0;
    while (queue.length) {
      const current = queue.shift()!;
      visited++;
      for (const target of outgoing.get(current) ?? []) {
        const degree = (indegree.get(target) ?? 0) - 1;
        indegree.set(target, degree);
        if (degree === 0) queue.push(target);
      }
    }
    if (visited !== nodeIds.size) errors.push('Cycles are not allowed; workflows must remain acyclic.');
    if (triggers.length === 1 && walk(triggers[0].id, outgoing).size !== nodeIds.size) errors.push('Every node must be reachable from the trigger.');
    if (ends.length) {
      const canReachEnd = new Set<string>();
      for (const end of ends) for (const id of walk(end.id, incoming)) canReachEnd.add(id);
      if (canReachEnd.size !== nodeIds.size) errors.push('Every node must lead to an end node.');
    }
  }
  const uniqueErrors = [...new Set(errors)];
  return { valid: uniqueErrors.length === 0, errors: uniqueErrors };
}

function walk(start: string, graph: Map<string, string[]>): Set<string> {
  const visited = new Set<string>();
  const pending = [start];
  while (pending.length) {
    const current = pending.pop()!;
    if (visited.has(current)) continue;
    visited.add(current);
    for (const next of graph.get(current) ?? []) if (!visited.has(next)) pending.push(next);
  }
  return visited;
}
