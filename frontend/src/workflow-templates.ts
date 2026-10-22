export type WorkflowTemplate = {
  id: string; title: string; description: string; workflowName: string; task: string;
  icon: 'research' | 'change' | 'approval'; accent: 'purple' | 'teal' | 'amber';
};
/** Curated local presets prefill the form; they never grant tool permissions. */
export const WORKFLOW_TEMPLATES: readonly WorkflowTemplate[] = [
  { id:'research-brief', title:'Research & synthesize', description:'Summarize supplied information without external calls',
    workflowName:'Research & synthesize', task:'Summarize the main points of the supplied task. Separate facts from assumptions and list any open questions. Do not call external tools.', icon:'research', accent:'purple' },
  { id:'change-review', title:'Review a change', description:'Identify risks and missing validation from a change description',
    workflowName:'Review a change', task:'Review the supplied change description. List likely risks, edge cases, security implications, and tests that should be run. Do not modify files or call external tools.', icon:'change', accent:'teal' },
  { id:'approval-planning', title:'Approval planning', description:'Identify side effects that need human approval',
    workflowName:'Approval planning', task:'Break down the proposed task and identify any sensitive side effects that would require explicit human approval before execution. Do not perform the action.', icon:'approval', accent:'amber' },
] as const;
