package com.surrealdb;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

class Loader {

    static String SURREALDB = "surrealdb";

    static void load() throws RuntimeException {
        try {
            System.loadLibrary(SURREALDB);
        } catch (final UnsatisfiedLinkError e) {
            try {
                System.load(get_path().load().getAbsolutePath());
            } catch (Exception e2) {
                throw new RuntimeException("Couldn't load " + SURREALDB, e2);
            }
        }
    }

    private static Lib get_path() {
        final String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
        final String name = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (name.contains("nix") || name.contains("nux")) {
            final String libName = "lib" + SURREALDB + ".so";
            if (arch.contains("aarch64")) {
                return new Lib("natives/linux_arm64", libName);
            } else if (arch.contains("x86_64")) {
                return new Lib("natives/linux_64", libName);
            }
        } else if (name.contains("win")) {
            if (arch.contains("x86_64")) {
                return new Lib("natives/windows_64", SURREALDB + ".dll");
            }
        } else if (name.contains("mac")) {
            final String libName = "lib" + SURREALDB + ".dylib";
            if (arch.contains("aarch64")) {
                return new Lib("natives/osx_arm64", libName);
            } else if (arch.contains("x86_64")) {
                return new Lib("natives/osx_64", libName);
            }
        }
        throw new RuntimeException("Unsupported architecture: " + arch + " - name: " + name);
    }

}

class Lib {
    String path;
    String name;

    Lib(String path, String name) {
        this.path = path;
        this.name = System.mapLibraryName(name);
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    File load() throws IOException {
        final URL resource = Surreal.class.getClassLoader().getResource(path);
        if (resource == null) {
            throw new RuntimeException("Couldn't find resource: " + path);
        }
        final Path tempDir = Files.createTempDirectory("surrealdb-jni");
        final URLConnection connection = resource.openConnection();
        connection.setUseCaches(false);
        try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
            final File outfile = new File(tempDir.toFile(), this.name);
            try (FileOutputStream out = new FileOutputStream(outfile)) {
                copy(in, out);
            }
            return outfile;
        }
    }

}
