/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.ignite.model;

import co.bitshifted.appforge.common.dto.JvmConfigurationDTO;
import co.bitshifted.appforge.common.model.JavaVersion;
import co.bitshifted.appforge.common.model.JvmVendor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JvmConfiguration {

    private JvmVendor vendor;
    @JsonProperty("major-version")
    private JavaVersion majorVersion;
    private String release;
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

    public JvmConfigurationDTO toDto() {
        JvmConfigurationDTO dto = new JvmConfigurationDTO();
        dto.setVendor(vendor);
        dto.setMajorVersion(majorVersion);
        dto.setRelease(release);
        dto.setJvmOptions(jvmOptions);
        dto.setSystemProperties(systemProperties);
        dto.setMainClass(mainClass);
        dto.setJar(jar);
        dto.setModuleName(moduleName);
        dto.setArguments(arguments);
        return dto;
    }
}
