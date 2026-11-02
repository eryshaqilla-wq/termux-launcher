package com.termux.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class FusionBookmarkHelper {
    private static final String PREF_NAME = "fusion_bookmarks";
    private static final String KEY_ITEMS = "items";

    public static class BookmarkItem {
        public String title;
        public String url;
        public BookmarkItem(String title, String url) {
            this.title = title;
            this.url = url;
        }
    }

    public static ArrayList<BookmarkItem> getBookmarks(Context context) {
        ArrayList<BookmarkItem> list = new ArrayList<>();
        if (context == null) return list;
        try {
            SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonStr = sp.getString(KEY_ITEMS, "[]");
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new BookmarkItem(obj.optString("title"), obj.optString("url")));
            }
        } catch (Throwable unused) {}
        return list;
    }

    public static void saveBookmarks(Context context, ArrayList<BookmarkItem> list) {
        if (context == null || list == null) return;
        try {
            JSONArray arr = new JSONArray();
            for (BookmarkItem item : list) {
                JSONObject obj = new JSONObject();
                obj.put("title", item.title);
                obj.put("url", item.url);
                arr.put(obj);
            }
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_ITEMS, arr.toString())
                    .apply();
        } catch (Throwable unused) {}
    }

    public static void addBookmark(Context context, String title, String url) {
        if (context == null || url == null || url.isEmpty()) return;
        ArrayList<BookmarkItem> list = getBookmarks(context);
        for (int i = 0; i < list.size(); i++) {
            if (url.equalsIgnoreCase(list.get(i).url)) {
                list.remove(i);
                break;
            }
        }
        list.add(0, new BookmarkItem((title == null || title.isEmpty()) ? url : title, url));
        saveBookmarks(context, list);
    }

    public static void showBookmarksDialog(Context context, FusionWebViewManager manager) {
        if (context == null || manager == null) return;
        ArrayList<BookmarkItem> list = getBookmarks(context);
        if (list.isEmpty()) {
            Toast.makeText(context, "Belum ada bookmark tersimpan. Ketuk ⭐ di baris atas untuk simpan.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] displayItems = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            displayItems[i] = list.get(i).title + "\n" + list.get(i).url;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("📁 Bookmark Tersimpan (" + list.size() + ")");
        b.setItems(displayItems, (dialog, which) -> {
            if (which >= 0 && which < list.size()) {
                String targetUrl = list.get(which).url;
                manager.closeAllDrawers();
                if (manager.mActiveWebView != null) {
                    manager.mActiveWebView.loadUrl(targetUrl);
                }
            }
        });
        b.setNeutralButton("Hapus Semua", (dialog, which) -> {
            saveBookmarks(context, new ArrayList<>());
            Toast.makeText(context, "Semua bookmark telah dibersihkan", Toast.LENGTH_SHORT).show();
        });
        b.setNegativeButton("Tutup", null);
        b.show();
    }
}
