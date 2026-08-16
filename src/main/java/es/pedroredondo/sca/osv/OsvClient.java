package es.pedroredondo.sca.osv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonObject;

import es.pedroredondo.sca.model.Dependency;

public class OsvClient {

    private static final String OSV_URL =
            "https://api.osv.dev/v1/query";

    private final HttpClient client;

    public OsvClient() {

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public String queryVulnerabilities(
            Dependency dependency) {

        try {

            String packageName =
                    dependency.getGroupId()
                    + ":"
                    + dependency.getArtifactId();

            JsonObject packageObject =
                    new JsonObject();

            packageObject.addProperty(
                    "name",
                    packageName);

            packageObject.addProperty(
                    "ecosystem",
                    "Maven");

            JsonObject requestBody =
                    new JsonObject();

            requestBody.addProperty(
                    "version",
                    dependency.getVersion());

            requestBody.add(
                    "package",
                    packageObject);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(OSV_URL))
                            .timeout(Duration.ofSeconds(10))
                            .header(
                                    "Content-Type",
                                    "application/json")
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    requestBody.toString()))
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            System.out.println(
                    "Código HTTP OSV: "
                    + response.statusCode());

            if (response.statusCode() != 200) {

                System.out.println(
                        "ERROR: OSV respondió con código "
                        + response.statusCode());

                return null;
            }

            return response.body();

        } catch (java.net.http.HttpTimeoutException e) {

            System.out.println(
                    "AVISO: OSV ha tardado demasiado en responder.");

            return null;

        } catch (Exception e) {

            System.out.println(
                    "ERROR consultando OSV: "
                    + e.getMessage());

            return null;
        }
    }
}