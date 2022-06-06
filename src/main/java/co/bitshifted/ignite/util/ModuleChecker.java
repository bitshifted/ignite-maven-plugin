package co.bitshifted.ignite.util;


import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleChecker {

    private static final String MODULE_INFO_ENTRY_NAME = "module-info.class";
    private static Log logger;

    private ModuleChecker() {

    }

    public static void initLogger(Log logger) {
        ModuleChecker.logger = logger;
    }

    public static boolean checkForModuleInfo(File jarFile) {
        boolean result = false;
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry(MODULE_INFO_ENTRY_NAME);
            if (entry != null) {
                result = true;
            }
        } catch(IOException ex) {
            if(logger != null) {
                logger.error("failed to  open jat file " + jarFile.getAbsolutePath());
            }
        }
        return result;
    }
}