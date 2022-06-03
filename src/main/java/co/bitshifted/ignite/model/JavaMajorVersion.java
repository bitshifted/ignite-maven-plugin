package co.bitshifted.ignite.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum JavaMajorVersion {
    JDK_8 ("8"),
    JDK_11 ("11"),
    JDK_17 ("17");

    private String display;

    JavaMajorVersion(String display) {
        this.display = display;
    }

    @JsonValue
    public String getDisplay() {
        return display;
    }
}
