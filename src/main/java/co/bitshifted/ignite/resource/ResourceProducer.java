package co.bitshifted.ignite.resource;

import co.bitshifted.ignite.IgniteConstants;
import co.bitshifted.ignite.model.BasicResource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        return out;
    }

}
