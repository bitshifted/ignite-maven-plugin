/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite;

import co.bitshifted.ignite.common.dto.DeploymentDTO;
import co.bitshifted.ignite.common.dto.DeploymentStatusDTO;
import co.bitshifted.ignite.common.dto.JvmConfigurationDTO;
import co.bitshifted.ignite.common.dto.RequiredResourcesDTO;
import co.bitshifted.ignite.common.model.BasicResource;
import co.bitshifted.ignite.deploy.Packer;
import co.bitshifted.ignite.exception.CommunicationException;
import co.bitshifted.ignite.http.IgniteHttpClient;
import co.bitshifted.ignite.http.SubmitDeploymentResponse;
import co.bitshifted.ignite.model.IgniteConfig;
import co.bitshifted.ignite.model.JavaDependency;
import co.bitshifted.ignite.resource.ResourceProducer;
import co.bitshifted.ignite.util.ModuleChecker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static co.bitshifted.ignite.IgniteConstants.*;

/**
 * Goal which deploys application to configured server.
 *
 * @goal touch
 * @phase process-sources
 */
@Mojo(name = MOJO_NAME, defaultPhase = LifecyclePhase.DEPLOY,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class IgniteMojo extends AbstractMojo {

    private final ObjectMapper yamlObjectMapper;
    private final DigestUtils digestUtils;

    IgniteMojo(MavenProject project, File configFile ) {
        this();
        this.mavenProject = project;
        this.configFile = configFile;
    }

    public IgniteMojo() {
        this.yamlObjectMapper = new ObjectMapper(new YAMLFactory());
        this.digestUtils = new DigestUtils(MessageDigestAlgorithms.SHA_256);
    }

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    @Parameter(name = CONFIG_FILE, required = true, readonly = true, property = CONFIG_FILE_PROPERTY, defaultValue = DEFAULT_CONFIG_FILE_NAME)
    private File configFile;

    @Override
    public void execute() throws MojoExecutionException {
        ModuleChecker.initLogger(getLog());
        // load config file
        IgniteConfig config = loadConfiguration();
        if (config == null) {
            throw new MojoExecutionException("Aborting due to invalid or missing configuration file");
        }

        DeploymentDTO deployment = new DeploymentDTO();
        deployment.setApplicationId(config.getApplicationId());
        deployment.setApplicationInfo(config.getApplicationInfo());
        // jvm configuration
        JvmConfigurationDTO jvmConfig = config.getJvmConfiguration().toDto();
        try {
            List<JavaDependency> deps = calculateDependencies();
            jvmConfig.setDependencies(deps.stream().map(JavaDependency::toDto).collect(Collectors.toList()));
            deployment.setJvmConfiguration(jvmConfig);

            // process app info resource
            ResourceProducer producer = new ResourceProducer();
            List<BasicResource> splash = producer.produceResources(config.getApplicationInfo().getSplashScreen());
            deployment.getApplicationInfo().setSplashScreen(splash.get(0));
            // process resources section
            config.getResources().stream().forEach(r -> {
                try {
                    List<BasicResource> resources = producer.produceResources(r);
                    deployment.addResources(resources);
                } catch(IOException ex) {
                    getLog().error("Failed to process resources", ex);
                }

            });
        } catch(IOException ex) {
            getLog().error("Failed to process dependencies", ex);
            throw new MojoExecutionException(ex);
        }


        SubmitDeploymentResponse response = submitDeployment(deployment, config.getServerUrl());
        // create deployment package
        String targetDir = mavenProject.getBuild().getDirectory();
        Path deploymentArchive;
        try {
            Packer packer = new Packer(mavenProject.getBasedir().toPath(), calculateDependencies());
            deploymentArchive =  packer.createDeploymentPackage(deployment, response.getRequiredResourcesDTO(), Paths.get(targetDir, DEFAULT_IGNITE_OUTPUT_DIR));
           getLog().info("Created deployment archive at " + deploymentArchive.toFile().getAbsolutePath());
        } catch(IOException ex) {
            getLog().error("Failed to create deployment package", ex);
            throw new MojoExecutionException(ex);
        }
        // submit deployment archive
        submitDeploymentArchive(response.getUrl(), deploymentArchive);

    }

    private IgniteConfig loadConfiguration() {
        try(InputStream in = new FileInputStream(configFile)) {
            return yamlObjectMapper.readValue(in, IgniteConfig.class);
        } catch (IOException ex) {
            getLog().error("Failed to load configuration file", ex);
            return null;
        }
    }

    private List<JavaDependency> calculateDependencies() throws IOException {
        List<JavaDependency> deps = mavenProject.getArtifacts().stream().filter(d -> !d.getScope().equals("test")).map(JavaDependency::new).collect(Collectors.toList());
        for(JavaDependency d : deps) {
            if (d.getDependencyFile() != null) {
                d.setSha256(digestUtils.digestAsHex(d.getDependencyFile()));
            }
        }
        // add build artifact to dependencies
        Artifact artifact = mavenProject.getArtifact();
        if (artifact.getFile() != null) {
            JavaDependency mainArtifact = new JavaDependency(artifact);
            mainArtifact.setSha256(digestUtils.digestAsHex(artifact.getFile()));
            deps.add(mainArtifact);
        }

        return deps;
    }

    private SubmitDeploymentResponse submitDeployment(DeploymentDTO deployment, String serverUrl) throws MojoExecutionException {
        try {
            IgniteHttpClient client = new IgniteHttpClient(serverUrl, getLog());
            String statusUrl = client.submitDeployment(deployment);
            Optional<DeploymentStatusDTO> status = client.waitForStageOneCompleted(statusUrl);
            RequiredResourcesDTO resource = status.get().getRequiredResources();
            SubmitDeploymentResponse response = new SubmitDeploymentResponse();
            response.setRequiredResourcesDTO(resource);
            response.setUrl(statusUrl);

            return response;
        } catch(CommunicationException ex) {
            throw new MojoExecutionException("failed to communicate with server", ex);
        }
    }

    private void submitDeploymentArchive(String url, Path archive) throws MojoExecutionException {
        try {
            IgniteHttpClient client = new IgniteHttpClient(getLog());
            String statusUrl = client.submitDeploymentArchive(url, archive);
            Optional<DeploymentStatusDTO> status = client.waitForStageTwoCompleted(statusUrl);
            getLog().info("Deployment completed. Status: " + status.get().getStatus());
        } catch(CommunicationException ex) {
            throw new MojoExecutionException("failed to communicate with server", ex);
        }
    }
}
