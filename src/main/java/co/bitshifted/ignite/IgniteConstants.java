/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

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
    public static final String DEFAULT_IGNITE_OUTPUT_DIR = "ignite";

    public static final String DEPLOYMENT_JSON_FILE_NAME = "deployment.json";
    public static final String DEPLOYMENT_ARCHIVE_FILE_NAME = "deployment.zip";

    public static final String JSON_MIME_TYPE = "application/json";
    public static final String ZIP_MIME_TYPE = "application/zip";

    public static final DigestUtils DIGEST_UTILS = new DigestUtils(MessageDigestAlgorithms.SHA_256);
}
