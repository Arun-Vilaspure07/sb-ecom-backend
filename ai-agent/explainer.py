def explain(analysis, diff):
    return f"""
❌ Error detected:
{analysis}

✅ Fix applied:
{diff}

📍 Files changed:
- Extracted from diff

✔ Result:
Tests should now pass
"""