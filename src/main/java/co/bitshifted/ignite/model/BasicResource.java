package co.bitshifted.ignite.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BasicResource {

    private String source;
    private String target;
    private String sha256;
    private Long size;
}
