package co.bitshifted.ignite.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class IconInfo extends FileInfo {
    @JsonProperty("os")
    private OperatingSystem operatingSystem;
}
