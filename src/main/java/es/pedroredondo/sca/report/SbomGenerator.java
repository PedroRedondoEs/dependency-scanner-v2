package es.pedroredondo.sca.report;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import es.pedroredondo.sca.model.Dependency;

public class SbomGenerator {

    public void generate(List<Dependency> dependencies) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

        String timestamp =
                LocalDateTime.now().format(formatter);

        String fileName =
                "sbom-" + timestamp + ".txt";

        try (PrintWriter writer =
                new PrintWriter(new FileWriter(fileName))) {

            writer.println("======================================");
            writer.println("        SOFTWARE BILL OF MATERIALS");
            writer.println("======================================");
            writer.println();

            writer.println(
                    "Componentes encontrados: "
                    + dependencies.size());

            writer.println();

            int number = 1;

            for (Dependency dependency : dependencies) {

                writer.println(
                        "Componente " + number);

                writer.println(
                        "GroupId: "
                        + dependency.getGroupId());

                writer.println(
                        "ArtifactId: "
                        + dependency.getArtifactId());

                writer.println(
                        "Version: "
                        + dependency.getVersion());

                writer.println(
                        "Scope: "
                        + dependency.getScope());

                writer.println(
                        "--------------------------------------");

                number++;
            }

            System.out.println();
            System.out.println(
                    "SBOM generado: " + fileName);

        } catch (IOException e) {

            System.out.println(
                    "ERROR generando SBOM: "
                    + e.getMessage());
        }
    }
}