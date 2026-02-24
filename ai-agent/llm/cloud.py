import os
from openai import OpenAI
from .base import LLMClient

class CloudLLM(LLMClient):
    def __init__(self):
        self.client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

    def analyze(self, prompt: str) -> str:
        return self._ask(prompt)

    def generate_fix(self, prompt: str) -> str:
        return self._ask(prompt)

    def _ask(self, prompt):
        res = self.client.chat.completions.create(
            model="gpt-4.1",
            messages=[{"role": "user", "content": prompt}]
        )
        return res.choices[0].message.content