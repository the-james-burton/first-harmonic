package com.firstharmonic;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class Downloader implements Runnable {
    private static Logger logger = Logger.getLogger(Downloader.class.getName());
    private String        url;
    private String        output;

    public Downloader(String url, String output) {
        this.url = url;
        this.output = output;
    }

    @Override
    public void run() {
        String ric = Analyse.getRic();
        String html;
        File file = new File(output, ric + ".html");
        if (file.exists()) {
            logger.info(ric + ":found existing file");
            html = fileToString(file);
        } else {
            logger.info(ric + ":downloading from internet");
            html = download(url + ric + ".L");
            save(html, file);
        }
        Analyse.putDownload(ric, html);
    }

    public static void downloadFile(String url, String filename) throws Exception {
        if (!(new File(filename).exists())) {
            logger.info(filename + ": downloading");
            downloadBinary(url, new File(filename));
        } else {
            logger.info(filename + ": already exists, not downloading");
        }
    }

    private static String download(String url) {
        StringBuffer result = new StringBuffer();
        HttpURLConnection connection = null;
        BufferedReader rd;
        StringBuilder sb;
        String line;
        URL serverAddress = null;
        try {
            serverAddress = new URL(url);
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            // connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.connect();
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }
            result.append(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
            rd = null;
            sb = null;
            connection = null;
        }
        return result.toString();
    }

    private static void downloadBinary(String url, File filename) throws Exception {
        HttpURLConnection connection = null;
        BufferedOutputStream fOut = null;
        URL serverAddress = null;
        try {
            serverAddress = new URL(url);
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            // connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.connect();
            InputStream is = connection.getInputStream();
            fOut = new BufferedOutputStream(new FileOutputStream(filename));
            byte[] buffer = new byte[32 * 1024];
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer)) != -1) {
                fOut.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
            fOut.flush();
            fOut.close();
            connection = null;
        }
    }

    public static String fileToString(File file) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            String lineSearator = System.getProperty("line.separator");
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(lineSearator);
            }
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
}
