package cc.maicra999.lunalike.util;

import java.nio.file.Path;

public final class PathUtil {

    private static final Path PLUGIN_HOME = Path.of("plugins", "lunalike");

    // Private constructor to prevent instantiation
    private PathUtil() {}

    public static Path fromPluginHome(String... subPaths) {
        return PLUGIN_HOME.resolve(Path.of("", subPaths));
    }

    public static Path fromPluginHome(String subPath) {
        return PLUGIN_HOME.resolve(subPath);
    }
}
