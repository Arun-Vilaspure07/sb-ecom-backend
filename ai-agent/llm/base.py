from abc import ABC, abstractmethod

class LLMClient(ABC):

    @abstractmethod
    def analyze(self, prompt: str) -> str:
        pass

    @abstractmethod
    def generate_fix(self, prompt: str) -> str:
        pass