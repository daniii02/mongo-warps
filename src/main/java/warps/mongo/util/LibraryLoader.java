package warps.mongo.util;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import warps.mongo.MongoWarps;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.net.URLClassLoader;

public class LibraryLoader {
    public static void loadLibraryFromMaven(String name) throws Throwable {
        if (Strings.isBlank(name)) return;

        String[] parts = name.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato de biblioteca '"+name+"' incorrecto. Se esperan tres partes separadas por ':'");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];

        File libsFolder = new File(MongoWarps.get().getDataFolder(), "libs");
        File jarFile = new File(libsFolder, String.format("%s-%s.jar", artifactId, version));

        if (!jarFile.exists()) {
            // Descargar la biblioteca desde Maven
            downloadLibraryFromMaven(groupId, artifactId, version, jarFile);
        }

        if (jarFile.exists()) {
            // Cargar la biblioteca dinámicamente
            loadLibrary(jarFile);
        } else {
            throw new RuntimeException("No se pudo encontrar la biblioteca: " + jarFile.getAbsolutePath());
        }
    }

    private static void downloadLibraryFromMaven(String groupId, String artifactId, String version, File destination) throws IOException {
        String mavenUrl = String.format("https://repo.maven.apache.org/maven2/%s/%s/%s/%s-%s.jar",
                groupId.replace('.', '/'), artifactId, version, artifactId, version);

        try {
            FileUtils.copyURLToFile(new URL(mavenUrl), destination);

            MongoWarps.get().info("Biblioteca descargada: " + destination.getName());
        } catch (IOException e) {
            throw new IOException("Error al descargar la biblioteca desde Maven: " + e.getMessage(), e);
        }
    }

    private static void loadLibrary(File jarFile) throws Throwable {
        MethodHandle handle = Reflection.safeMethod(URLClassLoader.class, "protected", "addURL", URL.class);

        URL url = jarFile.toURI().toURL();
        handle.invoke(MongoWarps.class.getClassLoader(), url);
    }
}
