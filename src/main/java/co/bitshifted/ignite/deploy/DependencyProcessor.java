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

import co.bitshifted.ignite.common.model.OperatingSystem;
import co.bitshifted.ignite.model.JavaDependency;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyProcessor {

    private static final String[] JAVAFX_PLATFORM_VALUES = new String[]{"linux", "mac", "win"};
    private static final String JAVAFX_GROUP = "org.openjfx";

    private final MavenProject project;
    private final MavenSession session;
    private final BuildPluginManager manager;
    private final Log loger;
    private final DigestUtils digestUtils;

    public DependencyProcessor(MavenProject project, MavenSession session, BuildPluginManager manager, Log logger) {
        this.project = project;
        this.loger = logger;
        this.session = session;
        this.manager = manager;
        this.digestUtils = new DigestUtils(MessageDigestAlgorithms.SHA_256);
    }

    public DependencyResolutionResult resolveDependencies(Set<OperatingSystem> supported) throws IOException {
        DependencyResolutionResult result = new DependencyResolutionResult();
        Set<JavaDependency> allDeps = calculateDependenciesInt();
        Set<JavaDependency> javafxDeps = javafxDependencies(allDeps);
        if(!javafxDeps.isEmpty()) {
            allDeps.removeAll(javafxDeps);
            downloadJavafxDependencies(javafxDeps);
            for(OperatingSystem os : supported) {
                result.setDependencies(os, resolveJavafxDependencies(javafxDeps, os));
            }
        }
        result.setCommon(cleanup(result, allDeps));

       return result;
    }

    private Set<JavaDependency> calculateDependenciesInt() throws IOException {
        Set<JavaDependency> deps = project.getArtifacts().stream().filter(d -> !d.getScope().equals("test")).map(JavaDependency::new).collect(Collectors.toSet());
        for(JavaDependency d : deps) {
            if (d.getDependencyFile() != null) {
                d.setSha256(digestUtils.digestAsHex(d.getDependencyFile()));
            }
        }
        // add build artifact to dependencies
        Artifact artifact = project.getArtifact();
        if (artifact.getFile() != null) {
            JavaDependency mainArtifact = new JavaDependency(artifact);
            mainArtifact.setSha256(digestUtils.digestAsHex(artifact.getFile()));
            deps.add(mainArtifact);
        }
        return deps;
    }

    private void downloadJavafxDependencies(Set<JavaDependency> input) {
        Plugin depPlugin = project.getPluginManagement().getPlugins().stream().filter(p -> "org.apache.maven.plugins".equals(p.getGroupId()) && "maven-dependency-plugin".equals(p.getArtifactId()))
            .findFirst()
            .orElse(MojoExecutor.plugin("org.apache.maven.plugins", "maven-dependency-plugin", "3.3.0"));
        if(!input.isEmpty()) {
            for(String platform : JAVAFX_PLATFORM_VALUES) {
                loger.info("Collecting JavaFX dependencies for platform: " + platform);
                input.stream().forEach(d -> {
                    String artifact = String.format("%s:%s:%s:%s:%s",d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getType(), platform);
                    loger.info("Getting artifact " + artifact);
                    try {
                        MojoExecutor.executeMojo(
                            depPlugin,
                            MojoExecutor.goal("get"),
                            MojoExecutor.configuration(MojoExecutor.element("artifact", artifact)),
                            MojoExecutor.executionEnvironment(session, manager)
                        );
                    } catch (MojoExecutionException ex) {
                        loger.error("Failed to get artifact " + artifact, ex);
//                        ex.printStackTrace();
                    }
                });

            }
        }

    }

    private Set<JavaDependency> resolveJavafxDependencies(Set<JavaDependency> input, OperatingSystem os) throws IOException {
        Set<JavaDependency> out = new HashSet<>();
        String classifier = javafxOperatingSystemClassifier(os);
        for(JavaDependency d : input) {
            if(classifier.equals(d.getClassifier())) {
                out.add(d);
            } else {
                Artifact artifact = new DefaultArtifact(d.getGroupId(), d.getArtifactId(), d.getVersion(), null, d.getType(), classifier, d.getArtifact().getArtifactHandler());
                String path = session.getLocalRepository().pathOf(artifact);
                Path repoDirPath = Paths.get(session.getLocalRepository().getBasedir());
                Path artifactPath = repoDirPath.resolve(path);
                JavaDependency dep = new JavaDependency(artifact, artifactPath.toFile());
                dep.setSha256(digestUtils.digestAsHex(artifactPath.toFile()));
                out.add(dep);
            }
        }
        return out;
    }

    private String javafxOperatingSystemClassifier(OperatingSystem os) {
        switch (os) {
            case LINUX:
                return OperatingSystem.LINUX.getDisplay();
            case MAC:
                return OperatingSystem.MAC.getDisplay();
            case WINDOWS:
                return "win";
            default:
                throw new IllegalArgumentException("Unsupported operating system: " + os);
        }
    }

    private Set<JavaDependency> javafxDependencies(Set<JavaDependency> input) {
        return input.stream().filter(i -> JAVAFX_GROUP.equals(i.getGroupId()) && i.getClassifier() != null).collect(Collectors.toSet());
    }

   private Set<JavaDependency> cleanup(DependencyResolutionResult result, Set<JavaDependency> allDeps) {
       return allDeps.stream().filter(d -> !result.getLinux().contains(d) && !result.getMac().contains(d) && !result.getWindows().contains(d)).collect(Collectors.toSet());
   }
}
