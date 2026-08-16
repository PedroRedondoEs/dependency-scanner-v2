package es.pedroredondo.sca.osv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.pedroredondo.sca.model.Vulnerability;

public class OsvResponseParserTest {

    @Test
    void debeDevolverListaVaciaSiNoHayVulnerabilidades() {

        String json = "{}";

        OsvResponseParser parser =
                new OsvResponseParser();

        List<Vulnerability> vulnerabilities =
                parser.parse(json);

        assertTrue(vulnerabilities.isEmpty());
    }

    @Test
    void debeParsearVulnerabilidadBasica() {

        String json =
                "{"
                + "\"vulns\":["
                + "{"
                + "\"id\":\"GHSA-test-1234\","
                + "\"aliases\":[\"CVE-2025-0001\"],"
                + "\"database_specific\":{"
                + "\"severity\":\"HIGH\""
                + "},"
                + "\"affected\":["
                + "{"
                + "\"ranges\":["
                + "{"
                + "\"events\":["
                + "{\"fixed\":\"3.18.0\"}"
                + "]"
                + "}"
                + "]"
                + "}"
                + "]"
                + "}"
                + "]"
                + "}";

        OsvResponseParser parser =
                new OsvResponseParser();

        List<Vulnerability> vulnerabilities =
                parser.parse(json);

        assertEquals(
                1,
                vulnerabilities.size());

        Vulnerability vulnerability =
                vulnerabilities.get(0);

        assertEquals(
                "GHSA-test-1234",
                vulnerability.getId());

        assertEquals(
                "CVE-2025-0001",
                vulnerability.getCve());

        assertEquals(
                "HIGH",
                vulnerability.getSeverity());

        assertEquals(
                "3.18.0",
                vulnerability.getFixedVersion());
    }

    @Test
    void debeSoportarVulnerabilidadSinCve() {

        String json =
                "{"
                + "\"vulns\":["
                + "{"
                + "\"id\":\"GHSA-test-no-cve\","
                + "\"database_specific\":{"
                + "\"severity\":\"MODERATE\""
                + "}"
                + "}"
                + "]"
                + "}";

        OsvResponseParser parser =
                new OsvResponseParser();

        List<Vulnerability> vulnerabilities =
                parser.parse(json);

        assertEquals(
                1,
                vulnerabilities.size());

        assertEquals(
                "GHSA-test-no-cve",
                vulnerabilities.get(0).getId());
    }

    @Test
    void debeDevolverListaVaciaConJsonNulo() {

        OsvResponseParser parser =
                new OsvResponseParser();

        List<Vulnerability> vulnerabilities =
                parser.parse(null);

        assertTrue(vulnerabilities.isEmpty());
    }
}