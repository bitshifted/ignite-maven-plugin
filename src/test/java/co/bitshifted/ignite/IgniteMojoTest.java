package co.bitshifted.ignite;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class IgniteMojoTest {

    @Test
    void executeMojoSuccess() throws Exception {
        MavenProject mockProject = new MavenProject();
        Dependency dep = new Dependency();
        dep.setGroupId("foo.group");
        dep.setArtifactId("foo-artifact");
        dep.setVersion("1.0.1");
        dep.setScope("compile");
        dep.setType("jar");

        mockProject.setDependencies(Collections.singletonList(dep));
        File config = new File(getClass().getResource("/test-config.yaml").toURI()) ;
        IgniteMojo mojo = new IgniteMojo(mockProject, config);
        mojo.execute();
    }
}
