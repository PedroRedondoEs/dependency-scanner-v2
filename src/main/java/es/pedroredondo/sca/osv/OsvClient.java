package es.pedroredondo.sca.osv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonObject;

import es.pedroredondo.sca.model.Dependency;

public class OsvClient {

    private static final String OSV_URL =
            "https://api.osv.dev/v1/query";

    private static final Logger LOGGER =
            Logger.getLogger(
                    OsvClient.class.getName());

    private static final Duration TIMEOUT =
            Duration.ofSeconds(10);

    private final HttpClient client;

    public OsvClient() {

        client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
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

            JsonObject jsonBody =
                    new JsonObject();

            jsonBody.addProperty(
                    "version",
                    dependency.getVersion());

            jsonBody.add(
                    "package",
                    packageObject);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            OSV_URL))
                            .timeout(TIMEOUT)
                            .header(
                                    "Content-Type",
                                    "application/json")
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(
                                                    jsonBody.toString()))
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString());

            System.out.println(
                    "Código HTTP OSV: "
                    + response.statusCode());

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                LOGGER.warning(
                        "OSV devolvio codigo HTTP "
                        + response.statusCode());

                return null;
            }

            return response.body();

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            LOGGER.log(
                    Level.WARNING,
                    "Consulta a OSV interrumpida",
                    e);

            return null;

        } catch (Exception e) {

            LOGGER.log(
                    Level.WARNING,
                    "Error consultando OSV",
                    e);

            return null;
        }
    }
}