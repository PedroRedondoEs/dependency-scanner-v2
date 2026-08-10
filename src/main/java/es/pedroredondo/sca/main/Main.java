package es.pedroredondo.sca.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import es.pedroredondo.sca.model.Dependency;
import es.pedroredondo.sca.model.Vulnerability;
import es.pedroredondo.sca.osv.OsvClient;
import es.pedroredondo.sca.osv.OsvResponseParser;
import es.pedroredondo.sca.parser.PomParser;
import es.pedroredondo.sca.report.ReportGenerator;

public class Main {

    private static final String VERSION = "0.0.1";

    public static void main(String[] args) {

        System.out.println("=== Dependency Scanner ===");

        /*
         * Comando --help
         */
        if (args.length > 0 && "--help".equalsIgnoreCase(args[0])) {

            System.out.println();
            System.out.println("Uso:");
            System.out.println("  java -jar dependency-scanner-v2-0.0.1-SNAPSHOT.jar <ruta-pom>");
            System.out.println();
            System.out.println("Opciones:");
            System.out.println("  --help       Muestra esta ayuda");
            System.out.println("  --version    Muestra la version del programa");
            System.out.println();
            System.out.println("Ejemplo:");
            System.out.println("  java -jar dependency-scanner-v2-0.0.1-SNAPSHOT.jar C:\\proyecto\\pom.xml");

            return;
        }

        /*
         * Comando --version
         */
        if (args.length > 0 && "--version".equalsIgnoreCase(args[0])) {

            System.out.println("Dependency Scanner " + VERSION);

            return;
        }

        String pomPath;
        Scanner scanner = null;

        /*
         * Si recibimos una ruta como argumento,
         * usamos esa ruta directamente.
         *
         * Si no recibimos argumento,
         * preguntamos por consola.
         */
        if (args.length > 0) {

            pomPath = args[0];

            System.out.println(
                    "POM recibido por argumento:");

            System.out.println(pomPath);

        } else {

            scanner = new Scanner(System.in);

            System.out.println(
                    "Introduce la ruta del pom.xml:");

            pomPath = scanner.nextLine();
        }

        PomParser parser =
                new PomParser();

        List<Dependency> dependencies =
                parser.parsePom(pomPath);

        if (dependencies.isEmpty()) {

            System.out.println(
                    "No se encontraron dependencias para analizar.");

            if (scanner != null) {
                scanner.close();
            }

            return;
        }

        OsvClient osvClient =
                new OsvClient();

        OsvResponseParser responseParser =
                new OsvResponseParser();

        ReportGenerator reportGenerator =
                new ReportGenerator();

        reportGenerator.startReport();

        int analyzedDependencies = 0;
        int ignoredTestDependencies = 0;
        int vulnerableDependencies = 0;
        int totalVulnerabilities = 0;

        for (Dependency dependency : dependencies) {

            System.out.println(
                    "------------------------------");

            System.out.println(
                    "Dependencia: "
                    + dependency.getGroupId()
                    + ":"
                    + dependency.getArtifactId());

            System.out.println(
                    "Version: "
                    + dependency.getVersion());

            System.out.println(
                    "Scope: "
                    + dependency.getScope());

            if ("test".equalsIgnoreCase(
                    dependency.getScope())) {

                ignoredTestDependencies++;

                System.out.println(
                        "Ignorada: dependencia de test.");

                reportGenerator.addDependency(
                        dependency,
                        new ArrayList<Vulnerability>());

                continue;
            }

            analyzedDependencies++;

            String osvResponse =
                    osvClient.queryVulnerabilities(
                            dependency);

            List<Vulnerability> vulnerabilities =
                    responseParser.parse(
                            osvResponse);

            if (vulnerabilities.isEmpty()) {

                System.out.println(
                        "Sin vulnerabilidades conocidas.");

            } else {

                vulnerableDependencies++;

                totalVulnerabilities +=
                        vulnerabilities.size();

                for (Vulnerability vulnerability
                        : vulnerabilities) {

                    System.out.println(
                            "Vulnerabilidad: "
                            + vulnerability.getCve());

                    System.out.println(
                            "ID OSV/GHSA: "
                            + vulnerability.getId());

                    System.out.println(
                            "Severidad: "
                            + vulnerability.getSeverity());

                    System.out.println(
                            "Version corregida: "
                            + vulnerability.getFixedVersion());
                }
            }

            reportGenerator.addDependency(
                    dependency,
                    vulnerabilities);
        }

        System.out.println();

        System.out.println(
                "========== RESUMEN ==========");

        System.out.println(
                "Dependencias encontradas: "
                + dependencies.size());

        System.out.println(
                "Dependencias analizadas: "
                + analyzedDependencies);

        System.out.println(
                "Dependencias test ignoradas: "
                + ignoredTestDependencies);

        System.out.println(
                "Dependencias vulnerables: "
                + vulnerableDependencies);

        System.out.println(
                "Vulnerabilidades encontradas: "
                + totalVulnerabilities);

        System.out.println(
                "=============================");

        reportGenerator.finishReport(
                analyzedDependencies,
                vulnerableDependencies,
                totalVulnerabilities);

        if (scanner != null) {
            scanner.close();
        }
    }
}