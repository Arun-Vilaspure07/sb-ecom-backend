from pathlib import Path

BASE = Path(__file__).parent

def load_analyze_prompt(error_log: str) -> str:
    template = (BASE / "analyze_error.txt").read_text()
    return template.replace("{{ERROR_LOG}}", error_log)

def load_fix_prompt(analysis: str) -> str:
    template = (BASE / "generate_fix.txt").read_text()
    return template.replace("{{ANALYSIS}}", analysis)