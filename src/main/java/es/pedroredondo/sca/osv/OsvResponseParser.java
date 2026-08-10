package es.pedroredondo.sca.osv;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.pedroredondo.sca.model.Vulnerability;

public class OsvResponseParser {

    public List<Vulnerability> parse(String jsonResponse) {

        List<Vulnerability> vulnerabilities = new ArrayList<>();

        if (jsonResponse == null || jsonResponse.isBlank()) {
            return vulnerabilities;
        }

        JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if (!root.has("vulns")) {
            return vulnerabilities;
        }

        JsonArray vulns = root.getAsJsonArray("vulns");

        for (JsonElement vulnElement : vulns) {

            JsonObject vuln = vulnElement.getAsJsonObject();

            String id = vuln.has("id")
                    ? vuln.get("id").getAsString()
                    : "N/A";

            String cve = "N/A";

            if (vuln.has("aliases")) {
                JsonArray aliases = vuln.getAsJsonArray("aliases");

                for (JsonElement aliasElement : aliases) {
                    String alias = aliasElement.getAsString();

                    if (alias.startsWith("CVE-")) {
                        cve = alias;
                        break;
                    }
                }
            }

            String severity = "N/A";

            if (vuln.has("database_specific")) {
                JsonObject databaseSpecific =
                        vuln.getAsJsonObject("database_specific");

                if (databaseSpecific.has("severity")) {
                    severity =
                            databaseSpecific.get("severity").getAsString();
                }
            }

            String fixedVersion = "N/A";

            if (vuln.has("affected")) {

                JsonArray affected = vuln.getAsJsonArray("affected");

                for (JsonElement affectedElement : affected) {

                    JsonObject affectedObject =
                            affectedElement.getAsJsonObject();

                    if (!affectedObject.has("ranges")) {
                        continue;
                    }

                    JsonArray ranges =
                            affectedObject.getAsJsonArray("ranges");

                    for (JsonElement rangeElement : ranges) {

                        JsonObject range =
                                rangeElement.getAsJsonObject();

                        if (!range.has("events")) {
                            continue;
                        }

                        JsonArray events =
                                range.getAsJsonArray("events");

                        for (JsonElement eventElement : events) {

                            JsonObject event =
                                    eventElement.getAsJsonObject();

                            if (event.has("fixed")) {
                                fixedVersion =
                                        event.get("fixed").getAsString();
                                break;
                            }
                        }

                        if (!fixedVersion.equals("N/A")) {
                            break;
                        }
                    }

                    if (!fixedVersion.equals("N/A")) {
                        break;
                    }
                }
            }

            Vulnerability vulnerability =
                    new Vulnerability(
                            id,
                            cve,
                            severity,
                            fixedVersion);

            vulnerabilities.add(vulnerability);
        }

        return vulnerabilities;
    }
}