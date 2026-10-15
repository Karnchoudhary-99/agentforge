"""Offline structural checks for the initial AgentForge scaffold."""
from pathlib import Path
import json
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
required = [
    "README.md", "docs/14-SESSION-PLAN.md", "docs/ARCHITECTURE.md",
    ".github/workflows/ci.yml", "backend/pom.xml",
    "backend/src/main/java/dev/agentforge/AgentForgeApplication.java",
    "backend/src/main/java/dev/agentforge/api/HealthController.java",
    "backend/src/test/java/dev/agentforge/api/HealthControllerTest.java",
    "frontend/package.json", "frontend/src/main.tsx", "compose.yaml",
]
missing = [p for p in required if not (ROOT / p).is_file()]
assert not missing, f"Missing required files: {missing}"
ET.parse(ROOT / "backend/pom.xml")
json.loads((ROOT / "frontend/package.json").read_text())
for path in (ROOT / ".github/workflows/ci.yml", ROOT / "docs/14-SESSION-PLAN.md"):
    assert path.stat().st_size > 100, f"Unexpectedly short file: {path}"
assert "OPENAI_API_KEY" in (ROOT / ".env.example").read_text()
assert ".env" in (ROOT / ".gitignore").read_text()
print(f"AgentForge scaffold checks passed: {len(required)} required files, XML and JSON parsed.")
