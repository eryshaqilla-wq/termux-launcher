package com.termux.webview;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.webkit.DownloadListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* JADX INFO: loaded from: /root/project/clean_build/classes21_new.dex */
public class FusionDownloadListener implements DownloadListener {
    private static final String TAG = "FusionDownload";
    private final Context mContext;
    private static volatile String sLastDownloadedFilePath = "";

    public FusionDownloadListener(Context context) {
        this.mContext = context;
    }

    public static String getLastDownloadedFilePath() {
        return sLastDownloadedFilePath;
    }

    public static void setLastDownloadedFilePath(String path) {
        sLastDownloadedFilePath = path;
    }

    @Override
    public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
        if (url == null || url.isEmpty()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File downloadDir = new File("/data/data/com.termux/files/home/downloads");
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }

                    String fileName = "download_" + System.currentTimeMillis();
                    if (contentDisposition != null && contentDisposition.contains("filename=")) {
                        int idx = contentDisposition.indexOf("filename=");
                        String extracted = contentDisposition.substring(idx + 9).trim().replace("\"", "").replace(";", "");
                        if (!extracted.isEmpty()) fileName = extracted;
                    } else {
                        try {
                            String path = new URL(url).getPath();
                            if (path != null && path.contains("/")) {
                                String leaf = path.substring(path.lastIndexOf('/') + 1);
                                if (!leaf.isEmpty()) fileName = leaf;
                            }
                        } catch (Throwable ignored) {}
                    }

                    File destFile = new File(downloadDir, fileName);

                    if (url.startsWith("data:")) {
                        int commaIdx = url.indexOf(',');
                        if (commaIdx != -1) {
                            String dataPart = url.substring(commaIdx + 1);
                            byte[] decoded = Base64.decode(dataPart, Base64.DEFAULT);
                            FileOutputStream fos = new FileOutputStream(destFile);
                            fos.write(decoded);
                            fos.flush();
                            fos.close();
                            sLastDownloadedFilePath = destFile.getAbsolutePath();
                            Log.d(TAG, "Data URL saved to: " + sLastDownloadedFilePath);
                        }
                    } else if (url.startsWith("http://") || url.startsWith("https://")) {
                        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                        if (userAgent != null && !userAgent.isEmpty()) {
                            conn.setRequestProperty("User-Agent", userAgent);
                        }
                        conn.connect();
                        if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                            InputStream is = conn.getInputStream();
                            FileOutputStream fos = new FileOutputStream(destFile);
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                            fos.flush();
                            fos.close();
                            is.close();
                            sLastDownloadedFilePath = destFile.getAbsolutePath();
                            Log.d(TAG, "HTTP file saved to: " + sLastDownloadedFilePath);
                        }
                        conn.disconnect();
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Error in onDownloadStart", t);
                }
            }
        }).start();
    }
}
