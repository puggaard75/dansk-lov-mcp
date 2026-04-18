package dk.hansen.retsinformation;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "retsinformation-api")
@Path("/api")
public interface RetsinformationClient {

    @GET
    @Path("/document/{id}")
    RetsinformationDocument getDocument(@PathParam("id") String id);
}