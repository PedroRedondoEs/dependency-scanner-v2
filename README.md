# Dependency Scanner

Dependency Scanner es una herramienta SCA (Software Composition Analysis)
desarrollada en Java para analizar las dependencias de proyectos Maven y
detectar vulnerabilidades conocidas.

El scanner analiza un archivo `pom.xml`, obtiene sus dependencias y consulta
la base de datos de OSV para identificar vulnerabilidades de seguridad.

## Características

- Análisis de archivos `pom.xml`
- Detección automática de dependencias Maven
- Consulta de vulnerabilidades mediante OSV
- Identificación de CVE y GHSA
- Clasificación de severidad
- Detección de versiones corregidas
- Soporte para propiedades Maven `${...}`
- Soporte para `dependencyManagement`
- Detección del `scope` de las dependencias
- Exclusión de dependencias con `scope=test`
- Protección del parser XML frente a XXE
- Generación automática de informes
- Informes con fecha y hora
- Ejecución mediante JAR
- Soporte para `--help`
- Soporte para `--version`

## Tecnologías

- Java 17
- Maven
- Gson
- OSV API
- Eclipse

## Ejemplo de análisis

```text
=== Dependency Scanner ===

Dependencia: org.apache.commons:commons-lang3
Version: 3.17.0
Scope: compile

Vulnerabilidad: CVE-2025-48924
ID OSV/GHSA: GHSA-j288-q9x7-2f5v
Severidad: MODERATE
Version corregida: 3.18.0