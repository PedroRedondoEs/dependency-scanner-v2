package es.pedroredondo.sca.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.pedroredondo.sca.model.Dependency;

public class PomParserTest {

    @TempDir
    Path tempDir;

    @Test
    void debeLeerDependenciaConVersionDirecta()
            throws IOException {

        String pom =
                "<project>"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<dependencies>"
                + "<dependency>"
                + "<groupId>org.apache.commons</groupId>"
                + "<artifactId>commons-lang3</artifactId>"
                + "<version>3.17.0</version>"
                + "</dependency>"
                + "</dependencies>"
                + "</project>";

        Path pomFile =
                tempDir.resolve("pom.xml");

        Files.writeString(
                pomFile,
                pom);

        PomParser parser =
                new PomParser();

        List<Dependency> dependencies =
                parser.parsePom(
                        pomFile.toString());

        assertEquals(
                1,
                dependencies.size());

        Dependency dependency =
                dependencies.get(0);

        assertEquals(
                "org.apache.commons",
                dependency.getGroupId());

        assertEquals(
                "commons-lang3",
                dependency.getArtifactId());

        assertEquals(
                "3.17.0",
                dependency.getVersion());

        assertEquals(
                "compile",
                dependency.getScope());
    }

    @Test
    void debeResolverVersionDesdeProperties()
            throws IOException {

        String pom =
                "<project>"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<properties>"
                + "<gson.version>2.11.0</gson.version>"
                + "</properties>"
                + "<dependencies>"
                + "<dependency>"
                + "<groupId>com.google.code.gson</groupId>"
                + "<artifactId>gson</artifactId>"
                + "<version>${gson.version}</version>"
                + "</dependency>"
                + "</dependencies>"
                + "</project>";

        Path pomFile =
                tempDir.resolve("pom-properties.xml");

        Files.writeString(
                pomFile,
                pom);

        PomParser parser =
                new PomParser();

        List<Dependency> dependencies =
                parser.parsePom(
                        pomFile.toString());

        assertEquals(
                1,
                dependencies.size());

        assertEquals(
                "2.11.0",
                dependencies.get(0).getVersion());
    }

    @Test
    void debeLeerScopeTest()
            throws IOException {

        String pom =
                "<project>"
                + "<modelVersion>4.0.0</modelVersion>"
                + "<dependencies>"
                + "<dependency>"
                + "<groupId>org.junit.jupiter</groupId>"
                + "<artifactId>junit-jupiter</artifactId>"
                + "<version>5.10.2</version>"
                + "<scope>test</scope>"
                + "</dependency>"
                + "</dependencies>"
                + "</project>";

        Path pomFile =
                tempDir.resolve("pom-test.xml");

        Files.writeString(
                pomFile,
                pom);

        PomParser parser =
                new PomParser();

        List<Dependency> dependencies =
                parser.parsePom(
                        pomFile.toString());

        assertEquals(
                1,
                dependencies.size());

        assertEquals(
                "test",
                dependencies.get(0).getScope());
    }

    @Test
    void debeRechazarArchivoQueNoEsPom()
            throws IOException {

        String xml =
                "<archivo>"
                + "<dato>123</dato>"
                + "</archivo>";

        Path xmlFile =
                tempDir.resolve("invalido.xml");

        Files.writeString(
                xmlFile,
                xml);

        PomParser parser =
                new PomParser();

        List<Dependency> dependencies =
                parser.parsePom(
                        xmlFile.toString());

        assertTrue(
                dependencies.isEmpty());
    }
}