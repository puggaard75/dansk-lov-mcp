package dk.hansen.ollama;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "ollama-api")
@Path("/api")
public interface OllamaClient {

    @POST
    @Path("/generate")
    OllamaResponse generate(OllamaRequest request);
}