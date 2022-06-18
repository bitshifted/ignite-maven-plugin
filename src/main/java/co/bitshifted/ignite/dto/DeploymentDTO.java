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

import co.bitshifted.ignite.model.ApplicationInfo;
import co.bitshifted.ignite.model.BasicResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeploymentDTO {
    private String id;
    @JsonProperty("application-info")
    private ApplicationInfo applicationInfo;
    @JsonProperty("jvm")
    private JvmConfigurationDTO jvmConfiguration;

    private List<BasicResource> resources = new ArrayList<>();

    public void addResources(List<BasicResource> res) {
        resources.addAll(res);
    }
}
