package com.surrealdb;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

class Loader {

    static String SURREALDB = "surrealdb";
    static String SURREALDB_LIBNAME = System.mapLibraryName(SURREALDB);

    static void load() throws RuntimeException {
        try {
            System.loadLibrary(SURREALDB);
        } catch (final UnsatisfiedLinkError e) {
            try {
                System.load(extract(get_path()).getAbsolutePath());
            } catch (Exception e2) {
                throw new RuntimeException("Couldn't load " + SURREALDB, e2);
            }
        }
    }

    private static String get_path() {
        final String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
        final String name = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (name.contains("nix") || name.contains("nux")) {
            if (arch.contains("aarch64")) {
                return "linux_arm64";
            } else if (arch.contains("x86_64")) {
                return "linux_64";
            }
        } else if (name.contains("win")) {
            if (arch.contains("x86_64")) {
                return "windows_64";
            }
        } else if (name.contains("mac")) {
            if (arch.contains("aarch64")) {
                return "osx_arm64";
            } else if (arch.contains("x86_64")) {
                return "osx_64";
            }
        }
        throw new RuntimeException("Unsupported architecture: " + arch + " - name: " + name);
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private static File extract(String path) throws IOException {
        final String resourcePath = "/natives/" + path + "/" + SURREALDB_LIBNAME;
        final URL resource = Surreal.class.getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new RuntimeException("Couldn't find resource: " + resourcePath);
        }
        final Path tempDir = Files.createTempDirectory("surrealdb-jni");
        final URLConnection connection = resource.openConnection();
        connection.setUseCaches(false);
        try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
            final File outfile = new File(tempDir.toFile(), SURREALDB_LIBNAME);
            try (FileOutputStream out = new FileOutputStream(outfile)) {
                copy(in, out);
            }
            return outfile;
        }
    }

}