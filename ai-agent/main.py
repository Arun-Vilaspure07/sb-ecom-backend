import sys
import io
import hashlib
from pathlib import Path

# --- Force UTF-8 output (Windows safe) ---
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8")

from maven_runner import run_tests
from error_parser import extract_failures
from fixer import apply_patch
from explainer import explain
from llm.router import get_llm
from config import MAX_RETRIES


# 🔧 UPDATE PATH IF PACKAGE CHANGES
SECURITY_CONFIG = Path(
    "../src/main/java/com/ecommerce/project/security/WebSecurityConfig.java"
)


def stable_hash(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def build_context(failures: list[str]) -> str:
    """
    Combine Java source + test failures
    """
    source = SECURITY_CONFIG.read_text(encoding="utf-8")

    return f"""
File: WebSecurityConfig.java
--------------------------------
{source}
--------------------------------

Test failure:
--------------------------------
{chr(10).join(failures)}
--------------------------------
"""


def build_fix_prompt(context: str) -> str:
    """
    🔥 HARD-CONSTRAIN THE LLM TO OUTPUT A PATCH ONLY
    """
    return f"""
You are an automated code-fixing agent.

STRICT RULES:
- Output ONLY a valid git diff
- Do NOT explain anything
- Do NOT include markdown
- Do NOT include comments or text
- The output MUST start with: diff --git

TASK:
Fix the failing tests by modifying the source code below.

{context}
"""


def main():
    llm = get_llm()
    previous_errors = set()

    for attempt in range(1, MAX_RETRIES + 1):
        print(f"\n[AI] Attempt {attempt}/{MAX_RETRIES}")

        code, log = run_tests()

        if code == 0:
            print("[AI] All tests passed ✅")
            return

        failures = extract_failures(log)

        if not failures:
            print("[AI] No actionable failures found — stopping")
            return

        failure_text = "\n".join(failures)
        error_signature = stable_hash(failure_text)

        if error_signature in previous_errors:
            print("[AI] Same error detected again — stopping to avoid loop")
            return

        previous_errors.add(error_signature)

        # 🔥 Build FULL context
        context = build_context(failures)
        fix_prompt = build_fix_prompt(context)

        print("[AI] Generating patch...")
        diff = llm.generate_fix(fix_prompt)

        if not diff or not diff.lstrip().startswith("diff --git"):
            print("[AI] Invalid or empty patch — retrying")
            continue

        try:
            apply_patch(diff)
            print("[AI] Patch applied successfully")
            print(explain(failure_text, diff))
        except Exception as e:
            print(f"[AI] Patch failed to apply: {e}")
            continue

    print("[AI] Max retries reached — manual intervention required ❌")


if __name__ == "__main__":
    main()