/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite.dto;

import co.bitshifted.ignite.model.JavaDependency;
import co.bitshifted.ignite.model.JavaMajorVersion;
import co.bitshifted.ignite.model.JvmConfiguration;
import co.bitshifted.ignite.model.JvmVendor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JvmConfigurationDTO {

    private JvmVendor vendor;
    @JsonProperty("major-version")
    private JavaMajorVersion majorVersion;
    @JsonProperty("fixed-version")
    private String fixedVersion;
    @JsonProperty("jvm-options")
    private String jvmOptions;
    @JsonProperty("system-properties")
    private String systemProperties;
    @JsonProperty("main-class")
    private String mainClass;
    private String jar;
    @JsonProperty("module-name")
    private String moduleName;
    private String arguments;

    private List<JavaDependency> dependencies;

    public JvmConfigurationDTO(JvmConfiguration jvmConfig) {
        this.vendor = jvmConfig.getVendor();
        this.majorVersion = jvmConfig.getMajorVersion();
        this.fixedVersion = jvmConfig.getFixedVersion();
        this.jvmOptions = jvmConfig.getJvmOptions();
        this.systemProperties = jvmConfig.getSystemProperties();
        this.mainClass = jvmConfig.getMainClass();
        this.jar = jvmConfig.getJar();
        this.moduleName = jvmConfig.getModuleName();
        this.arguments = jvmConfig.getArguments();
    }
}
