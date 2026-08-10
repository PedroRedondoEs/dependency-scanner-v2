package es.pedroredondo.sca.report;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import es.pedroredondo.sca.model.Dependency;
import es.pedroredondo.sca.model.Vulnerability;

public class ReportGenerator {

    private FileWriter writer;
    private String reportFileName;

    public void startReport() {

        try {

            String timestamp =
                    new SimpleDateFormat(
                            "yyyy-MM-dd-HHmmss")
                            .format(new Date());

            reportFileName =
                    "security-report-"
                    + timestamp
                    + ".txt";

            writer =
                    new FileWriter(
                            reportFileName);

            writer.write(
                    "=== DEPENDENCY SCANNER - SECURITY REPORT ===\n");

            writer.write(
                    "Fecha: "
                    + new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm:ss")
                            .format(new Date())
                    + "\n\n");

        } catch (IOException e) {

            System.out.println(
                    "ERROR creando el informe: "
                    + e.getMessage());
        }
    }

    public void addDependency(
            Dependency dependency,
            List<Vulnerability> vulnerabilities) {

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
                    "Version: "
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

            if (vulnerabilities.isEmpty()) {

                writer.write(
                        "Estado: SIN VULNERABILIDADES CONOCIDAS\n");

            } else {

                writer.write(
                        "Estado: VULNERABLE\n");

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