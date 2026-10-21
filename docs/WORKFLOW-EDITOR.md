# Workflow editor

The React Flow screen builds a client-side draft graph. It checks graph shape for early feedback, including supported node kinds, unique identifiers, bounded size, valid connections, acyclic structure, and a path from the trigger to an end node.

The editor does not execute or submit the graph. A visible tool-catalog entry does not grant permission. The backend remains authoritative for its registered tools, allowlist, approval policy, and execution decisions.

Draft state is currently held in the page only. Persisting, sharing, and executing a graph are not part of Session 9.
