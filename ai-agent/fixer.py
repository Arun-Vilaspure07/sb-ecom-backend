import subprocess
import tempfile

def apply_patch(diff_text):
    if not diff_text:
        print("⚠️ Empty diff received. Skipping patch.")
        return

    if "diff --git" not in diff_text:
        print("⚠️ No valid git diff detected. Skipping patch.")
        print(diff_text)
        return

    with tempfile.NamedTemporaryFile(delete=False, mode="w", encoding="utf-8") as f:
        f.write(diff_text)
        patch_file = f.name

    subprocess.run(
        ["git", "apply", patch_file],
        check=True
    )