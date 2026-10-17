package dev.agentforge.api;

/** A display-safe, structured step in the deterministic runtime plan. */
public record PlanStep(int order, String title, String description, String status) {
}
