package es.pedroredondo.sca.model;

public class Dependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String scope;

    public Dependency(
            String groupId,
            String artifactId,
            String version) {

        this(groupId, artifactId, version, "compile");
    }

    public Dependency(
            String groupId,
            String artifactId,
            String version,
            String scope) {

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        if (scope == null || scope.isEmpty()) {
            this.scope = "compile";
        } else {
            this.scope = scope;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getScope() {
        return scope;
    }

    public String toString() {
        return groupId
                + ":"
                + artifactId
                + ":"
                + version
                + " ["
                + scope
                + "]";
    }
}