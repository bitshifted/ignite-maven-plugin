package co.bitshifted.ignite.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IgniteConfig {

    private String id;
    @JsonProperty("server-url")
    private String serverUrl;
    @JsonProperty("application-info")
    private ApplicationInfo applicationInfo;
    @JsonProperty("jvm")
    private JvmConfiguration jvmConfiguration;
    private List<BasicResource> resources;
}
