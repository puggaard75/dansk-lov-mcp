package dk.hansen.retsinformation;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class RetsinformationService {

    @RestClient
    RetsinformationClient retsinformationClient;

    public RetsinformationDocument hentLov(String id) {
        return retsinformationClient.getDocument(id);
    }
}