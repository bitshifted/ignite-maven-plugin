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


import co.bitshifted.ignite.common.model.BasicResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static co.bitshifted.ignite.IgniteConstants.*;

public class ResourceProducer {

    private final  Path basePath;

    public ResourceProducer() {
        basePath = Paths.get(System.getProperty("user.dir"));
    }

    public List<BasicResource> produceResources(BasicResource input) throws IOException {

        String[] parts = input.getSource().split("/");
        Path sourcePath = Paths.get(basePath.toFile().getAbsolutePath(), parts);
        if(Files.isDirectory(sourcePath)) {
            ResourceFileVisitor visitor = new ResourceFileVisitor(input, basePath);
            Files.walkFileTree(sourcePath, visitor);
            return visitor.getResources();
        } else {
            return Collections.singletonList(toResource(sourcePath, input));
        }
    }

    static BasicResource toResource(Path path, BasicResource input) throws IOException {
        BasicResource out = new BasicResource();
        out.setSha256(DIGEST_UTILS.digestAsHex(path.toFile()));
        out.setSize(path.toFile().length());
        out.setTarget(input.getTarget() != null ? input.getTarget() : input.getSource());
        out.setSource(input.getSource());
        out.setMimeType(input.getMimeType());
        return out;
    }

}
