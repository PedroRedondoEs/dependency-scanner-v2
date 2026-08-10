package es.pedroredondo.sca.version;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.pedroredondo.sca.model.Dependency;

public class VersionChecker {

    private static final String MAVEN_SEARCH_URL =
            "https://search.maven.org/solrsearch/select";

    public String getLatestVersion(Dependency dependency) {

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

            HttpClient client =
                    HttpClient.newBuilder()
                            .connectTimeout(
                                    Duration.ofSeconds(5))
                            .build();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(
                                    Duration.ofSeconds(8))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {

                System.out.println(
                        "ERROR consultando Maven Central: "
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
                return null;
            }

            JsonArray docs =
                    responseObject.getAsJsonArray("docs");

            if (docs == null || docs.size() == 0) {
                return null;
            }

            JsonObject artifact =
                    docs.get(0).getAsJsonObject();

            if (!artifact.has("latestVersion")) {
                return null;
            }

            return artifact
                    .get("latestVersion")
                    .getAsString();

        } catch (java.net.http.HttpTimeoutException e) {

            System.out.println(
                    "AVISO: Maven Central ha tardado demasiado en responder.");

            return null;

        } catch (Exception e) {

            System.out.println(
                    "ERROR comprobando version: "
                    + e.getMessage());

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