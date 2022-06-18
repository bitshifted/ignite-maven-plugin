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
