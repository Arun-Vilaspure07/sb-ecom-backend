import subprocess
from .base import LLMClient


class LocalLLM(LLMClient):
    MODEL = "codellama:13b"

    def _ask(self, prompt: str) -> str:
        process = subprocess.run(
            [
                "ollama",
                "run",
                self.MODEL,
            ],
            input=prompt,
            text=True,
            capture_output=True,
            encoding="utf-8",
            errors="ignore"
        )

        if process.returncode != 0:
            raise RuntimeError(process.stderr)

        # 🚨 STRIP EVERYTHING BEFORE FIRST diff
        out = process.stdout
        if "diff --git" in out:
            return out[out.index("diff --git"):]

        return ""  # forces retry if model misbehaves

    def analyze(self, prompt: str) -> str:
        return self._ask(prompt)

    def generate_fix(self, prompt: str) -> str:
        return self._ask(prompt)