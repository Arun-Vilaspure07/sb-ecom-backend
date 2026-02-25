import subprocess
import re
from .base import LLMClient


class LocalLLM(LLMClient):
    MODEL = "codellama:13b"

    def _ask(self, prompt: str) -> str:
        process = subprocess.run(
            ["ollama", "run", self.MODEL],
            input=prompt,
            text=True,
            capture_output=True,
            encoding="utf-8",
            errors="ignore",
        )

        output = process.stdout.strip()

        # ❌ Remove [cmd] blocks if model prints them
        output = re.sub(r"\[cmd\].*?\[/cmd\]", "", output, flags=re.DOTALL).strip()

        # ✅ If already a valid unified diff, return it
        if output.startswith("diff --git"):
            return output

        # ✅ Convert raw git diff to unified git diff
        if output.startswith("---") and "+++" in output:
            return (
                "diff --git a/src/main/java/com/ecommerce/project/security/WebSecurityConfig.java "
                "b/src/main/java/com/ecommerce/project/security/WebSecurityConfig.java\n"
                "index 0000000..1111111 100644\n"
                + output
            )

        # ❌ Otherwise force retry
        return ""

    def analyze(self, prompt: str) -> str:
        return self._ask(prompt)

    def generate_fix(self, prompt: str) -> str:
        return self._ask(prompt)