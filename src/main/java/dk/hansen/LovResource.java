package dk.hansen;

import dk.hansen.ollama.OllamaClient;
import dk.hansen.ollama.OllamaRequest;
import dk.hansen.ollama.OllamaResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/lov")
public class LovResource {

    public static final String GEMMA = "gemma3:12b";
    @RestClient
    OllamaClient ollamaClient;

    @GET
    @Path("/sporg")
    public String ask(@QueryParam("q") String question) {
        OllamaRequest request = new OllamaRequest(
                GEMMA,
                question,
                false,
                new OllamaRequest.Options(0.1, 2048)
        );

        OllamaResponse response = ollamaClient.generate(request);
        return response.response();
    }
}