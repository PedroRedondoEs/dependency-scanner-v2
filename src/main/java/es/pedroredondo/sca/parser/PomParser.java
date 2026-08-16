package es.pedroredondo.sca.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.pedroredondo.sca.model.Dependency;

public class PomParser {

    public List<Dependency> parsePom(String pomPath) {

        List<Dependency> dependencies =
                new ArrayList<>();

        try {

            File pomFile =
                    new File(pomPath);

            if (!pomFile.exists()) {

                System.out.println(
                        "ERROR: No existe el pom.xml:");

                System.out.println(
                        pomFile.getAbsolutePath());

                return dependencies;
            }

            Document document =
                    readDocument(pomFile);

            Element project =
                    document.getDocumentElement();

            if (!"project".equals(
                    project.getNodeName())) {

                System.out.println(
                        "ERROR: El archivo no es un pom.xml valido de Maven.");

                return dependencies;
            }

            String modelVersion =
                    getDirectTagValue(
                            project,
                            "modelVersion");

            if (modelVersion == null
                    || !"4.0.0".equals(modelVersion)) {

                System.out.println(
                        "ERROR: El archivo no contiene un modelVersion Maven valido.");

                return dependencies;
            }

            Map<String, String> properties =
                    new HashMap<>();

            Map<String, String> managedVersions =
                    new HashMap<>();

            loadParentConfiguration(
                    pomFile,
                    document,
                    properties,
                    managedVersions);

            properties.putAll(
                    readProperties(document));

            managedVersions.putAll(
                    readDependencyManagement(
                            document,
                            properties));

            Element dependenciesElement =
                    getDirectChild(
                            project,
                            "dependencies");

            if (dependenciesElement == null) {

                System.out.println(
                        "Dependencias encontradas: 0");

                return dependencies;
            }

            NodeList dependencyNodes =
                    dependenciesElement
                            .getChildNodes();

            int dependencyCount = 0;

            for (int i = 0;
                    i < dependencyNodes.getLength();
                    i++) {

                Node node =
                        dependencyNodes.item(i);

                if (node.getNodeType()
                        != Node.ELEMENT_NODE
                        || !"dependency".equals(
                                node.getNodeName())) {

                    continue;
                }

                Element element =
                        (Element) node;

                String groupId =
                        getDirectTagValue(
                                element,
                                "groupId");

                String artifactId =
                        getDirectTagValue(
                                element,
                                "artifactId");

                String version =
                        getDirectTagValue(
                                element,
                                "version");

                String scope =
                        getDirectTagValue(
                                element,
                                "scope");

                if (scope == null
                        || scope.isEmpty()) {

                    scope = "compile";
                }

                version =
                        resolveProperty(
                                version,
                                properties);

                if (version == null
                        && groupId != null
                        && artifactId != null) {

                    String key =
                            groupId
                            + ":"
                            + artifactId;

                    version =
                            managedVersions.get(key);
                }

                if (groupId != null
                        && artifactId != null
                        && version != null) {

                    Dependency dependency =
                            new Dependency(
                                    groupId,
                                    artifactId,
                                    version,
                                    scope);

                    dependencies.add(
                            dependency);

                    dependencyCount++;

                } else {

                    System.out.println(
                            "AVISO: No se pudo resolver la version de "
                            + groupId
                            + ":"
                            + artifactId);
                }
            }

            System.out.println(
                    "Dependencias encontradas: "
                    + dependencyCount);

        } catch (Exception e) {

            System.out.println(
                    "ERROR leyendo el pom.xml: "
                    + e.getMessage());
        }

        return dependencies;
    }

    private void loadParentConfiguration(
            File childPom,
            Document childDocument,
            Map<String, String> properties,
            Map<String, String> managedVersions) {

        try {

            Element project =
                    childDocument
                            .getDocumentElement();

            Element parent =
                    getDirectChild(
                            project,
                            "parent");

            if (parent == null) {
                return;
            }

            String relativePath =
                    getDirectTagValue(
                            parent,
                            "relativePath");

            if (relativePath == null
                    || relativePath.isEmpty()) {

                relativePath =
                        "../pom.xml";
            }

            File parentPom =
                    new File(
                            childPom
                                    .getParentFile(),
                            relativePath)
                            .getCanonicalFile();

            if (!parentPom.exists()) {

                return;
            }

            Document parentDocument =
                    readDocument(
                            parentPom);

            loadParentConfiguration(
                    parentPom,
                    parentDocument,
                    properties,
                    managedVersions);

            properties.putAll(
                    readProperties(
                            parentDocument));

            managedVersions.putAll(
                    readDependencyManagement(
                            parentDocument,
                            properties));

        } catch (Exception e) {

            System.out.println(
                    "AVISO: No se pudo leer el POM padre: "
                    + e.getMessage());
        }
    }

    private Document readDocument(
            File pomFile)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory
                        .newInstance();

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true);

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false);

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false);

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);

        factory.setXIncludeAware(false);

        factory.setExpandEntityReferences(false);

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD,
                "");

        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                "");

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        Document document =
                builder.parse(
                        pomFile);

        document
                .getDocumentElement()
                .normalize();

        return document;
    }

    private Map<String, String> readProperties(
            Document document) {

        Map<String, String> properties =
                new HashMap<>();

        Element project =
                document
                        .getDocumentElement();

        Element propertiesElement =
                getDirectChild(
                        project,
                        "properties");

        if (propertiesElement == null) {
            return properties;
        }

        NodeList children =
                propertiesElement
                        .getChildNodes();

        for (int i = 0;
                i < children.getLength();
                i++) {

            Node child =
                    children.item(i);

            if (child.getNodeType()
                    == Node.ELEMENT_NODE) {

                properties.put(
                        child.getNodeName(),
                        child.getTextContent()
                                .trim());
            }
        }

        return properties;
    }

    private Map<String, String>
            readDependencyManagement(
                    Document document,
                    Map<String, String> properties) {

        Map<String, String> managedVersions =
                new HashMap<>();

        Element project =
                document
                        .getDocumentElement();

        Element dependencyManagement =
                getDirectChild(
                        project,
                        "dependencyManagement");

        if (dependencyManagement == null) {
            return managedVersions;
        }

        Element dependencies =
                getDirectChild(
                        dependencyManagement,
                        "dependencies");

        if (dependencies == null) {
            return managedVersions;
        }

        NodeList dependencyNodes =
                dependencies
                        .getChildNodes();

        for (int i = 0;
                i < dependencyNodes.getLength();
                i++) {

            Node node =
                    dependencyNodes.item(i);

            if (node.getNodeType()
                    != Node.ELEMENT_NODE
                    || !"dependency".equals(
                            node.getNodeName())) {

                continue;
            }

            Element dependency =
                    (Element) node;

            String groupId =
                    getDirectTagValue(
                            dependency,
                            "groupId");

            String artifactId =
                    getDirectTagValue(
                            dependency,
                            "artifactId");

            String version =
                    getDirectTagValue(
                            dependency,
                            "version");

            version =
                    resolveProperty(
                            version,
                            properties);

            if (groupId != null
                    && artifactId != null
                    && version != null) {

                String key =
                        groupId
                        + ":"
                        + artifactId;

                managedVersions.put(
                        key,
                        version);
            }
        }

        return managedVersions;
    }

    private String resolveProperty(
            String value,
            Map<String, String> properties) {

        if (value == null) {
            return null;
        }

        value =
                value.trim();

        if (value.startsWith("${")
                && value.endsWith("}")) {

            String propertyName =
                    value.substring(
                            2,
                            value.length() - 1);

            String resolvedValue =
                    properties.get(
                            propertyName);

            if (resolvedValue != null) {

                return resolvedValue;
            }
        }

        return value;
    }

    private Element getDirectChild(
            Element parent,
            String tagName) {

        NodeList children =
                parent.getChildNodes();

        for (int i = 0;
                i < children.getLength();
                i++) {

            Node child =
                    children.item(i);

            if (child.getNodeType()
                    == Node.ELEMENT_NODE
                    && tagName.equals(
                            child.getNodeName())) {

                return (Element) child;
            }
        }

        return null;
    }

    private String getDirectTagValue(
            Element element,
            String tagName) {

        Element child =
                getDirectChild(
                        element,
                        tagName);

        if (child == null) {
            return null;
        }

        return child
                .getTextContent()
                .trim();
    }
}