package mg.itu.utils;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Finds classes in a package from either an exploded application or a JAR. */
public final class PackageScanner {
    private PackageScanner() { }

    public static List<String> getClasses(String packageName) {
        String path = packageName.replace('.', '/');
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<String> classes = new ArrayList<>();
        try {
            Enumeration<URL> resources = loader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    scanDirectory(new File(resource.toURI()), packageName, classes);
                } else if ("jar".equals(resource.getProtocol())) {
                    scanJar(((JarURLConnection) resource.openConnection()).getJarFile(), path, classes);
                }
            }
            // Some servlet containers expose package resources only through the class path URLs.
            if (classes.isEmpty()) {
                for (URL root : java.util.Collections.list(loader.getResources(""))) {
                    if ("file".equals(root.getProtocol())) {
                        File packageDirectory = new File(new File(root.toURI()), path);
                        if (packageDirectory.isDirectory()) scanDirectory(packageDirectory, packageName, classes);
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to scan controller package " + packageName, exception);
        }
        return classes.stream().distinct().sorted().toList();
    }

    private static void scanDirectory(File directory, String packageName, List<String> classes) throws Exception {
        if (!directory.isDirectory()) return;
        try (var paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        String relative = directory.toPath().relativize(path).toString().replace(File.separatorChar, '.');
                        classes.add(packageName + "." + relative.substring(0, relative.length() - 6));
                    });
        }
    }

    private static void scanJar(JarFile jar, String packagePath, List<String> classes) {
        String prefix = packagePath.endsWith("/") ? packagePath : packagePath + "/";
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (name.startsWith(prefix) && name.endsWith(".class") && !name.contains("$")) {
                classes.add(name.substring(0, name.length() - 6).replace('/', '.'));
            }
        }
    }
}
