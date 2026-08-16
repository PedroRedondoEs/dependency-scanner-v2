package es.pedroredondo.sca.version;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.pedroredondo.sca.model.Dependency;

public class VersionChecker {

    private static final String MAVEN_SEARCH_URL =
            "https://search.maven.org/solrsearch/select";

    private static final Logger LOGGER =
            Logger.getLogger(
                    VersionChecker.class.getName());

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(5);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(8);

    private final HttpClient client;

    public VersionChecker() {

        client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    public String getLatestVersion(
            Dependency dependency) {

        try {

            String query =
                    "g:\""
                    + dependency.getGroupId()
                    + "\" AND a:\""
                    + dependency.getArtifactId()
                    + "\"";

            String encodedQuery =
                    URLEncoder.encode(
                            query,
                            StandardCharsets.UTF_8);

            String url =
                    MAVEN_SEARCH_URL
                    + "?q="
                    + encodedQuery
                    + "&rows=1&wt=json";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(REQUEST_TIMEOUT)
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {

                LOGGER.warning(
                        "Maven Central devolvio codigo HTTP "
                        + response.statusCode());

                return null;
            }

            JsonObject root =
                    JsonParser
                            .parseString(response.body())
                            .getAsJsonObject();

            JsonObject responseObject =
                    root.getAsJsonObject("response");

            if (responseObject == null) {

                LOGGER.warning(
                        "Respuesta de Maven Central sin objeto response");

                return null;
            }

            JsonArray docs =
                    responseObject.getAsJsonArray("docs");

            if (docs == null || docs.size() == 0) {

                LOGGER.info(
                        "No se encontro la dependencia en Maven Central: "
                        + dependency.getGroupId()
                        + ":"
                        + dependency.getArtifactId());

                return null;
            }

            JsonObject artifact =
                    docs.get(0).getAsJsonObject();

            if (!artifact.has("latestVersion")) {

                LOGGER.warning(
                        "Maven Central no devolvio latestVersion para "
                        + dependency.getGroupId()
                        + ":"
                        + dependency.getArtifactId());

                return null;
            }

            return artifact
                    .get("latestVersion")
                    .getAsString();

        } catch (java.net.http.HttpTimeoutException e) {

            LOGGER.warning(
                    "Maven Central ha tardado demasiado en responder");

            return null;

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            LOGGER.log(
                    Level.WARNING,
                    "Consulta a Maven Central interrumpida",
                    e);

            return null;

        } catch (Exception e) {

            LOGGER.log(
                    Level.WARNING,
                    "Error comprobando version",
                    e);

            return null;
        }
    }

    public boolean isOutdated(
            Dependency dependency,
            String latestVersion) {

        if (latestVersion == null) {
            return false;
        }

        return !dependency
                .getVersion()
                .equals(latestVersion);
    }
}