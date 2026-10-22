import { describe, expect, it } from 'vitest';
import { WORKFLOW_TEMPLATES } from './workflow-templates';
describe('curated workflow templates', () => {
  it('has unique identifiers and non-empty bounded form values', () => {
    const ids=WORKFLOW_TEMPLATES.map((template)=>template.id);
    expect(new Set(ids).size).toBe(ids.length);
    expect(WORKFLOW_TEMPLATES.length).toBeGreaterThanOrEqual(3);
    for (const template of WORKFLOW_TEMPLATES) {
      expect(template.title.trim().length).toBeGreaterThan(0);
      expect(template.workflowName.length).toBeLessThanOrEqual(80);
      expect(template.task.trim().length).toBeGreaterThan(0);
      expect(template.task.length).toBeLessThanOrEqual(1000);
    }
  });
  it('uses only known icon and accent variants', () => {
    for (const template of WORKFLOW_TEMPLATES) {
      expect(['research','change','approval']).toContain(template.icon);
      expect(['purple','teal','amber']).toContain(template.accent);
    }
  });
  it('keeps presets explicit about avoiding side effects', () => {
    expect(WORKFLOW_TEMPLATES.every((template)=>/do not|do not call|do not modify/i.test(template.task))).toBe(true);
  });
});
