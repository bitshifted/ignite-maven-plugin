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

import co.bitshifted.ignite.model.BasicResource;
import co.bitshifted.ignite.model.JavaDependency;
import lombok.Data;

import java.util.List;

@Data
public class RequiredResourcesDTO {
    private String url;
    private List<JavaDependency> dependencies;
    private List<BasicResource> resources;
}
