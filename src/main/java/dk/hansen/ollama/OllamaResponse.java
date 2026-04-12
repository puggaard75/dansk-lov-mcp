package dk.hansen.ollama;

public record OllamaResponse(
        String model,
        String response,
        boolean done
) {}