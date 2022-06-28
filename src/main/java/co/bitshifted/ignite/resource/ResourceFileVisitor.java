/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.ignite.resource;

import co.bitshifted.ignite.model.BasicResource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ResourceFileVisitor extends SimpleFileVisitor<Path> {

    private final List<BasicResource> resources = new ArrayList();
    private final BasicResource input;
    private final Path workDir;

    public ResourceFileVisitor(BasicResource input, Path workDir) {
        this.input = input;
        this.workDir = workDir;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path relativePath = workDir.relativize(file);
        input.setTarget(relativePath.toString());
        input.setSource(relativePath.toString());
        BasicResource resource = ResourceProducer.toResource(file, input);
        resources.add(resource);
        return FileVisitResult.CONTINUE;
    }

    public List<BasicResource> getResources() {
        return resources;
    }
}
