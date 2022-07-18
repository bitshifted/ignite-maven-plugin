/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite.model;

import co.bitshifted.ignite.common.dto.JavaDependencyDTO;
import co.bitshifted.ignite.util.ModuleChecker;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.apache.maven.artifact.Artifact;

import java.io.File;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JavaDependency {

    @EqualsAndHashCode.Include
    private String groupId;
    @EqualsAndHashCode.Include
    private String artifactId;
    @EqualsAndHashCode.Include
    private String version;
    @EqualsAndHashCode.Include
    private String type;
    @EqualsAndHashCode.Include
    private String classifier;
    @EqualsAndHashCode.Include
    private String sha256;
    private long size;
    private boolean modular;
    private String mimeType;

    @JsonIgnore
    private File dependencyFile;
    @JsonIgnore
    private Artifact artifact;

    public JavaDependency(Artifact artifact) {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion();
        this.type = artifact.getType();
        this.classifier = artifact.getClassifier();
        this.dependencyFile = artifact.getFile();
        this.artifact = artifact;
        if (artifact.getFile() != null) {
            this.size = artifact.getFile().length();
            this.modular = ModuleChecker.checkForModuleInfo(artifact.getFile());
        }
    }

    public JavaDependency(Artifact artifact, File file) {
        this.groupId = artifact.getGroupId();
        this.artifactId = artifact.getArtifactId();
        this.version = artifact.getVersion();
        this.type = artifact.getType();
        this.classifier = artifact.getClassifier();
        this.dependencyFile = artifact.getFile();
        this.artifact = artifact;
        this.size = file.length();
        this.modular = ModuleChecker.checkForModuleInfo(file);
    }

    public JavaDependencyDTO toDto() {
        JavaDependencyDTO dto = new JavaDependencyDTO();
        dto.setGroupId(groupId);
        dto.setArtifactId(artifactId);
        dto.setVersion(version);
        dto.setType(type);
        dto.setClassifier(classifier);
        dto.setSha256(sha256);
        dto.setSize(size);
        dto.setModular(modular);
        return dto;
    }
}
