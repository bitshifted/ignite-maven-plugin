/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite.deploy;

import co.bitshifted.ignite.common.model.OperatingSystem;
import co.bitshifted.ignite.model.JavaDependency;

import java.util.HashSet;
import java.util.Set;

public class DependencyResolutionResult {

    private Set<JavaDependency> common = new HashSet<>();
    private Set<JavaDependency> linux = new HashSet<>();
    private Set<JavaDependency> mac = new HashSet<>();
    private Set<JavaDependency> windows = new HashSet<>();


    public Set<JavaDependency> getCommon() {
        return common;
    }

    public void setCommon(Set<JavaDependency> common) {
        this.common = common;
    }

    public Set<JavaDependency> getLinux() {
        return linux;
    }

    public void setLinux(Set<JavaDependency> linux) {
        this.linux = linux;
    }

    public Set<JavaDependency> getMac() {
        return mac;
    }

    public void setMac(Set<JavaDependency> mac) {
        this.mac = mac;
    }

    public Set<JavaDependency> getWindows() {
        return windows;
    }

    public void setWindows(Set<JavaDependency> windows) {
        this.windows = windows;
    }

    public void setDependencies(OperatingSystem os, Set<JavaDependency> dependencies) {
        switch (os) {
            case LINUX:
                linux = dependencies;
                break;
            case MAC:
                mac = dependencies;
                break;
            case WINDOWS:
                windows = dependencies;
                break;
        }
    }
}
