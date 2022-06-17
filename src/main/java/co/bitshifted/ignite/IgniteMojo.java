package co.bitshifted.ignite;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import co.bitshifted.ignite.dto.DeploymentDTO;
import co.bitshifted.ignite.dto.DeploymentStatusDTO;
import co.bitshifted.ignite.dto.JvmConfigurationDTO;
import co.bitshifted.ignite.exception.CommunicationException;
import co.bitshifted.ignite.http.IgniteHttpClient;
import co.bitshifted.ignite.model.BasicResource;
import co.bitshifted.ignite.model.IgniteConfig;
import co.bitshifted.ignite.model.JavaDependency;
import co.bitshifted.ignite.util.ModuleChecker;
import co.bitshifted.ignite.resource.ResourceProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static co.bitshifted.ignite.IgniteConstants.*;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
@Mojo(name = MOJO_NAME, defaultPhase = LifecyclePhase.DEPLOY,
    requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class IgniteMojo extends AbstractMojo {

    private final ObjectMapper yamlObjectMapper;
    private final ObjectMapper jsonObjectMapper;
    private final DigestUtils digestUtils;

    IgniteMojo(MavenProject project, File configFile ) {
        this();
        this.mavenProject = project;
        this.configFile = configFile;
    }

    public IgniteMojo() {
        this.yamlObjectMapper = new ObjectMapper(new YAMLFactory());
        this.jsonObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
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
        deployment.setId(config.getId());
        deployment.setApplicationInfo(config.getApplicationInfo());
        // jvm configuration
        JvmConfigurationDTO jvmConfig = new JvmConfigurationDTO(config.getJvmConfiguration());
        try {
            List<JavaDependency> deps = calculateDependencies();
            jvmConfig.setDependencies(deps);
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

        try {
            IgniteHttpClient client = new IgniteHttpClient(config.getServerUrl(), getLog());
            String statusUrl = client.submitDeployment(deployment);
            Optional<DeploymentStatusDTO> status = client.waitForStageOneCompleted(statusUrl);
            if (status.isPresent()) {
                jsonObjectMapper.writeValue(System.out, status.get());
            }
//            jsonObjectMapper.writeValue(System.out, deployment);
        } catch(IOException | CommunicationException ex) {
            throw new MojoExecutionException("failed to communicate with server", ex);
        }

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
        List<JavaDependency> deps = mavenProject.getArtifacts().stream().filter(d -> !d.getScope().equals("test")).map(d -> new JavaDependency(d)).collect(Collectors.toList());
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
}
