/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite.deploy;

import co.bitshifted.ignite.IgniteConstants;
import co.bitshifted.ignite.dto.DeploymentDTO;
import co.bitshifted.ignite.dto.RequiredResourcesDTO;
import co.bitshifted.ignite.model.JavaDependency;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Packer {

    private final ObjectMapper objectMapper;
    private final Path baseDirectory;
    private final List<JavaDependency> dependencies;

    public Packer(Path baseDirectory, List<JavaDependency> dependencies) {
        this.objectMapper = new ObjectMapper();
        this.baseDirectory = baseDirectory;
        this.dependencies = dependencies;
    }

    public Path createDeploymentPackage(DeploymentDTO deployment, RequiredResourcesDTO requiredResources, Path outputDir) throws IOException {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        Path deploymentFile = Paths.get(outputDir.toFile().getAbsolutePath(), IgniteConstants.DEPLOYMENT_JSON_FILE_NAME);
        Files.write(deploymentFile, objectMapper.writeValueAsBytes(deployment));
        return createArchive(outputDir, requiredResources);
    }

    private Path  createArchive(Path outputDir, RequiredResourcesDTO requiredResourcesDTO) throws IOException {
        Path archiveFile = Paths.get(outputDir.toFile().getAbsolutePath(), IgniteConstants.DEPLOYMENT_ARCHIVE_FILE_NAME);
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(archiveFile.toFile()));
        // add deployment.jzon entry
        ZipEntry deploymentEntry = new ZipEntry(IgniteConstants.DEPLOYMENT_JSON_FILE_NAME);
        zout.putNextEntry(deploymentEntry);
        Files.copy(Paths.get(outputDir.toFile().getAbsolutePath(), IgniteConstants.DEPLOYMENT_JSON_FILE_NAME), zout);
        // add resource entries
        requiredResourcesDTO.getResources().stream().forEach(res -> {
            ZipEntry entry = new ZipEntry("resources/" + res.getTarget());
            Path entryPath = baseDirectory.resolve(res.getSource());
            try {
                zout.putNextEntry(entry);
                Files.copy(entryPath, zout);
            } catch(IOException ex) {
                System.err.println("Failed to add entry " + entry.getName());
            }
        });
        // add missing dependencies
        List<JavaDependency> deps = dependencies.stream().filter(d -> requiredResourcesDTO.getDependencies().stream().anyMatch(dp -> dp.getSha256().equals(d.getSha256()))).collect(Collectors.toList());
        deps.stream().forEach(d -> {
            ZipEntry entry = new ZipEntry("dependencies/" + d.getSha256());
            try {
                zout.putNextEntry(entry);
                Files.copy(d.getDependencyFile().toPath(), zout);
            } catch(IOException ex) {
                System.err.println("Failed to add dependency " + d.getArtifactId());
            }
        });
        zout.close();
        return archiveFile;
    }
}
