/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite.model;

import co.bitshifted.ignite.common.model.ApplicationInfo;
import co.bitshifted.ignite.common.model.BasicResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class IgniteConfig {

    @JsonProperty("application-id")
    private String applicationId;
    @JsonProperty("server-url")
    private String serverUrl;
    @JsonProperty("application-info")
    private ApplicationInfo applicationInfo;
    @JsonProperty("jvm")
    private JvmConfiguration jvmConfiguration;
    private List<BasicResource> resources;
}
