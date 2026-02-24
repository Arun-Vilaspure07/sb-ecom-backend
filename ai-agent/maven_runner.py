import subprocess
import os

def run_tests():
    project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

    result = subprocess.run(
        ["mvn.cmd", "test"],
        cwd=project_root,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",     # 🔥 FIX
        errors="replace",     # 🔥 FIX
        shell=True
    )

    return result.returncode, result.stdout + result.stderr