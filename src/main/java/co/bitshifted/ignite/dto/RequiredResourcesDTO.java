package co.bitshifted.ignite.dto;

import co.bitshifted.ignite.model.BasicResource;
import co.bitshifted.ignite.model.JavaDependency;
import lombok.Data;

import java.util.List;

@Data
public class RequiredResourcesDTO {
    private List<JavaDependency> dependencies;
    private List<BasicResource> resources;
}
