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

        List<Dependency> dependencies = new ArrayList<>();

        try {

            File pomFile = new File(pomPath);

            if (!pomFile.exists()) {
                System.out.println("ERROR: No existe el pom.xml:");
                System.out.println(pomFile.getAbsolutePath());
                return dependencies;
            }

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            // Proteccion contra XXE
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
                    builder.parse(pomFile);

            document.getDocumentElement().normalize();

            Map<String, String> properties =
                    readProperties(document);

            Map<String, String> managedVersions =
                    readDependencyManagement(
                            document,
                            properties);

            Element project =
                    document.getDocumentElement();

            NodeList projectChildren =
                    project.getChildNodes();

            Element dependenciesElement = null;

            for (int i = 0;
                    i < projectChildren.getLength();
                    i++) {

                Node child =
                        projectChildren.item(i);

                if (child.getNodeType()
                        == Node.ELEMENT_NODE
                        && child.getNodeName()
                                .equals("dependencies")) {

                    dependenciesElement =
                            (Element) child;

                    break;
                }
            }

            if (dependenciesElement == null) {

                System.out.println(
                        "Dependencias encontradas: 0");

                return dependencies;
            }

            NodeList dependencyNodes =
                    dependenciesElement.getChildNodes();

            int dependencyCount = 0;

            for (int i = 0;
                    i < dependencyNodes.getLength();
                    i++) {

                Node node =
                        dependencyNodes.item(i);

                if (node.getNodeType()
                        != Node.ELEMENT_NODE
                        || !node.getNodeName()
                                .equals("dependency")) {

                    continue;
                }

                dependencyCount++;

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

                    dependencies.add(dependency);

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

    private Map<String, String> readProperties(
            Document document) {

        Map<String, String> properties =
                new HashMap<>();

        NodeList propertiesNodes =
                document.getElementsByTagName(
                        "properties");

        if (propertiesNodes.getLength() == 0) {
            return properties;
        }

        Node propertiesNode =
                propertiesNodes.item(0);

        NodeList children =
                propertiesNode.getChildNodes();

        for (int i = 0;
                i < children.getLength();
                i++) {

            Node child =
                    children.item(i);

            if (child.getNodeType()
                    == Node.ELEMENT_NODE) {

                properties.put(
                        child.getNodeName(),
                        child.getTextContent().trim());
            }
        }

        return properties;
    }

    private Map<String, String> readDependencyManagement(
            Document document,
            Map<String, String> properties) {

        Map<String, String> managedVersions =
                new HashMap<>();

        NodeList managementNodes =
                document.getElementsByTagName(
                        "dependencyManagement");

        if (managementNodes.getLength() == 0) {
            return managedVersions;
        }

        Element dependencyManagement =
                (Element) managementNodes.item(0);

        NodeList dependencyNodes =
                dependencyManagement
                        .getElementsByTagName(
                                "dependency");

        for (int i = 0;
                i < dependencyNodes.getLength();
                i++) {

            Element dependency =
                    (Element) dependencyNodes.item(i);

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

        value = value.trim();

        if (value.startsWith("${")
                && value.endsWith("}")) {

            String propertyName =
                    value.substring(
                            2,
                            value.length() - 1);

            String resolvedValue =
                    properties.get(propertyName);

            if (resolvedValue != null) {
                return resolvedValue;
            }
        }

        return value;
    }

    private String getDirectTagValue(
            Element element,
            String tagName) {

        NodeList children =
                element.getChildNodes();

        for (int i = 0;
                i < children.getLength();
                i++) {

            Node child =
                    children.item(i);

            if (child.getNodeType()
                    == Node.ELEMENT_NODE
                    && child.getNodeName()
                            .equals(tagName)) {

                return child
                        .getTextContent()
                        .trim();
            }
        }

        return null;
    }
}