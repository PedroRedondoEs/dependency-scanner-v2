package es.pedroredondo.sca.report;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import es.pedroredondo.sca.model.Dependency;
import es.pedroredondo.sca.model.Vulnerability;

public class ReportGenerator {

    private FileWriter writer;
    private String reportFileName;

    public void startReport() {

        try {

            String timestamp =
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd-HHmmss")
                            .format(new java.util.Date());

            reportFileName =
                    "security-report-"
                    + timestamp
                    + ".txt";

            writer =
                    new FileWriter(reportFileName);

            writer.write(
                    "=== DEPENDENCY SCANNER - SECURITY REPORT ===\n\n");

        } catch (IOException e) {

            System.out.println(
                    "ERROR creando el informe: "
                    + e.getMessage());
        }
    }

    public void addDependency(
            Dependency dependency,
            List<Vulnerability> vulnerabilities,
            String latestVersion,
            boolean outdated) {

        try {

            writer.write(
                    "----------------------------------------\n");

            writer.write(
                    "Dependencia: "
                    + dependency.getGroupId()
                    + ":"
                    + dependency.getArtifactId()
                    + "\n");

            writer.write(
                    "Version actual: "
                    + dependency.getVersion()
                    + "\n");

            writer.write(
                    "Scope: "
                    + dependency.getScope()
                    + "\n");

            if ("test".equalsIgnoreCase(
                    dependency.getScope())) {

                writer.write(
                        "Estado: IGNORADA (scope test)\n");

                return;
            }

            if (latestVersion != null) {

                writer.write(
                        "Ultima version disponible: "
                        + latestVersion
                        + "\n");

                if (outdated) {

                    writer.write(
                            "Estado version: OBSOLETA\n");

                    writer.write(
                            "Recomendacion: actualizar a "
                            + latestVersion
                            + "\n");

                } else {

                    writer.write(
                            "Estado version: ACTUALIZADA\n");
                }

            } else {

                writer.write(
                        "Estado version: NO COMPROBADA\n");
            }

            if (vulnerabilities.isEmpty()) {

                writer.write(
                        "Estado seguridad: SIN VULNERABILIDADES CONOCIDAS\n");

            } else {

                writer.write(
                        "Estado seguridad: VULNERABLE\n");

                for (Vulnerability vulnerability
                        : vulnerabilities) {

                    writer.write(
                            "Vulnerabilidad: "
                            + vulnerability.getCve()
                            + "\n");

                    writer.write(
                            "ID OSV/GHSA: "
                            + vulnerability.getId()
                            + "\n");

                    writer.write(
                            "Severidad: "
                            + vulnerability.getSeverity()
                            + "\n");

                    writer.write(
                            "Version corregida: "
                            + vulnerability.getFixedVersion()
                            + "\n");
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "ERROR escribiendo el informe: "
                    + e.getMessage());
        }
    }

    public void addDependency(
            Dependency dependency,
            List<Vulnerability> vulnerabilities) {

        addDependency(
                dependency,
                vulnerabilities,
                null,
                false);
    }

    public void finishReport(
            int totalDependencies,
            int vulnerableDependencies,
            int totalVulnerabilities) {

        try {

            writer.write(
                    "\n========== RESUMEN ==========\n");

            writer.write(
                    "Dependencias analizadas: "
                    + totalDependencies
                    + "\n");

            writer.write(
                    "Dependencias vulnerables: "
                    + vulnerableDependencies
                    + "\n");

            writer.write(
                    "Vulnerabilidades encontradas: "
                    + totalVulnerabilities
                    + "\n");

            writer.write(
                    "=============================\n");

            writer.close();

            System.out.println();
            System.out.println(
                    "Informe generado: "
                    + reportFileName);

        } catch (IOException e) {

            System.out.println(
                    "ERROR cerrando el informe: "
                    + e.getMessage());
        }
    }
}