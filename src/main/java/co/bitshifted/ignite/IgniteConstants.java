package co.bitshifted.ignite;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

/**
 * Definitions of constants used in the plugin.
 */
public class IgniteConstants {

    private IgniteConstants() {}

    public static final String MOJO_NAME = "ignite";

    public static final String CONFIG_FILE = "configFile";
    public static final String CONFIG_FILE_PROPERTY = "ignite.configFile";
    public static final String DEFAULT_CONFIG_FILE_NAME = "ignite-config.yml";

    public static final DigestUtils DIGEST_UTILS = new DigestUtils(MessageDigestAlgorithms.SHA_256);
}
