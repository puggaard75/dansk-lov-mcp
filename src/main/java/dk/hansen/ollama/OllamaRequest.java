package dk.hansen.ollama;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaRequest(
        String model,
        String prompt,
        boolean stream,
        Options options
) {
    public record Options(
            double temperature,
            @JsonProperty("num_ctx") int numCtx
    ) {}
}