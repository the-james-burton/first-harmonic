package com.firstharmonic.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
    /**
     * Recursively delete all files in a directory.
     * 
     * @param dir
     *            the directory to delete
     * @return boolean indication of success or failure
     * @author the.james.burton
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static void deleteContents(File dir) {
        String[] children = dir.list();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
    }

    public static String fileToString(File file) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            String lineSearator = System.getProperty("line.separator");
            while ((line = br.readLine()) != null) { // while not at the end of
                // the file stream do
                sb.append(line);
                sb.append(lineSearator);
            }// next line
        } catch (Exception e) {
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                }
            }
        }
        return sb.toString();
    }

    /** Fast & simple file copy. */
    public static void copy(File source, File dest) throws IOException {
        FileChannel in = null, out = null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();
            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
            out.write(buf);
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
        dest.setLastModified(source.lastModified());
    }

    /**
     * Saves the given string to the given filename. Public for convenience.
     * 
     * @param text
     *            An string to save
     * @param filename
     *            The filename to save to
     * @throws Exception
     */
    public static void save(String text, File file) {
        try {
            if (text != null && text.trim() != "") {
                file.getParentFile().mkdirs();
                FileWriter fw = new FileWriter(file);
                fw.write(text);
                fw.flush();
                fw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encodeHTML(String s) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>') {
                out.append("&#" + (int) c + ";");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String stackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return sw.toString();
    }

    public static void replace(File file, String regex, String replacement) throws IOException {
        File tempFile = File.createTempFile("replace", "tmp");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.replaceAll(regex, replacement);
            writer.write(line);
            writer.newLine();
        }
        writer.close();
        reader.close();
        file.delete();
        tempFile.renameTo(file);
    }
}
