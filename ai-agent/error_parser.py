import re

def extract_failures(log):
    failures = []

    pattern = r"<<< FAILURE!.*?\n(.*?)\n\n"
    matches = re.findall(pattern, log, re.DOTALL)

    for m in matches:
        failures.append(m.strip())

    return failures