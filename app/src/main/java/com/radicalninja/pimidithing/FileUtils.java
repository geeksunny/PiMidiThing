package com.radicalninja.pimidithing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils {

    public static String readAsString(final String path) throws IOException {
        final File file = new File(path);
        return readAsString(file);
    }

    public static String readAsString(final File file) throws IOException {
        final StringBuilder sb = new StringBuilder();
        // TODO: refactor to use FileInputStream ???
        final BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        br.close();
        return sb.toString();
    }

    public static File saveToFile(final String path, final String content) throws IOException {
        final File file = new File(path);
        return saveToFile(file, content);
    }

    public static File saveToFile(final File file, final String content) throws IOException {
        final FileOutputStream out = new FileOutputStream(file);
        out.write(content.getBytes());
        out.close();
        return file;
    }

}
