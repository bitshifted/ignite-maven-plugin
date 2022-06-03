package co.bitshifted.ignite.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

import java.io.File;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JavaDependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    private String classifier;
    private String sha256;

    @JsonIgnore
    private File dependencyFile;

    public JavaDependency(Dependency mvnDependency) {
        this.groupId = mvnDependency.getGroupId();
        this.artifactId = mvnDependency.getArtifactId();
        this.version = mvnDependency.getVersion();
        this.type = mvnDependency.getType();
        this.classifier = mvnDependency.getClassifier();
    }

    public JavaDependency(Artifact artifact) {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion();
        this.type = artifact.getType();
        this.classifier = artifact.getClassifier();
        this.dependencyFile = artifact.getFile();
    }
}
