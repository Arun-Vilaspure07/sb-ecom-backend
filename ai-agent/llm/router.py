import os
import subprocess
from .cloud import CloudLLM
from .local import LocalLLM


def _ollama_available() -> bool:
    try:
        subprocess.run(
            ["ollama", "list"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
            check=True
        )
        return True
    except Exception:
        return False


def get_llm():
    mode = os.getenv("AI_MODE", "cloud").lower()

    if mode == "local":
        if not _ollama_available():
            raise RuntimeError(
                "Ollama is not available. "
                "Start it with: ollama serve"
            )

        print("[AI] Using Local LLM (Ollama)")
        return LocalLLM()   # ✅ INSTANCE, not class

    print("[AI] Using Cloud LLM")
    return CloudLLM()       # ✅ INSTANCE