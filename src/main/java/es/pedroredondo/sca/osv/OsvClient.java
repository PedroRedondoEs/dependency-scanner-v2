package es.pedroredondo.sca.osv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import es.pedroredondo.sca.model.Dependency;

public class OsvClient {

    private static final String OSV_URL = "https://api.osv.dev/v1/query";

    public String queryVulnerabilities(Dependency dependency) {

        try {
            String packageName =
                    dependency.getGroupId() + ":" + dependency.getArtifactId();

            String jsonBody =
                    "{"
                    + "\"version\":\"" + dependency.getVersion() + "\","
                    + "\"package\":{"
                    + "\"name\":\"" + packageName + "\","
                    + "\"ecosystem\":\"Maven\""
                    + "}"
                    + "}";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OSV_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            System.out.println("Código HTTP OSV: " + response.statusCode());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}