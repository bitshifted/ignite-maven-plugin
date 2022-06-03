package co.bitshifted.ignite.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OperatingSystem {
    LINUX ("linux"),
    WINDOWS ("windows"),
    MAC ("mac");

    private final String display;

    OperatingSystem(String display) {
        this.display = display;
    }

    @JsonValue
    public String getDisplay() {
        return display;
    }
}
