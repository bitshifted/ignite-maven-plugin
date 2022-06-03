package co.bitshifted.ignite.model;

import lombok.Data;

@Data
public class FileInfo {

    private String path;
    private String sha256;
    private long size;
}
