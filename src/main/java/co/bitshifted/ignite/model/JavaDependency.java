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

import co.bitshifted.ignite.util.ModuleChecker;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JavaDependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String type;
    private String classifier;
    private String sha256;
    private long size;
    private boolean modular;

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
        if (artifact.getFile() != null) {
            this.size = artifact.getFile().length();
            this.modular = ModuleChecker.checkForModuleInfo(artifact.getFile());
        }
    }
}
