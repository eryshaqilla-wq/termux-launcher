package com.termux.app.terminal.io;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDragHandleView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.termux.R;
import com.termux.app.material.M3;
import com.termux.shared.termux.font.NerdFontSpans;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Bottom Sheet File Picker for custom scripts and binary programs.
 * Allows browsing system directories (/project, /root, ~/bin, /usr/local/bin, etc.),
 * choosing execution mode (Background vs Foreground/New Tab in Middle Canvas),
 * and selecting target OS environment (Auto, Chroot, Droidspaces, PRoot, Native).
 */
public final class FusionScriptFilePickerSheet {

    public interface OnScriptPicked {
        void onPicked(@NonNull ExtraKeysLayoutModel.Key key);
    }

    private final Context context;
    private final OnScriptPicked onPicked;
    private final float density;

    private File currentDir;
    private BottomSheetDialog dialog;
    private LinearLayout fileListContainer;
    private TextView currentPathView;
    private MaterialButton upButton;
    private TextInputEditText searchInput;

    private final int colorText;
    private final int colorSubtle;
    private final int colorPrimary;

    public static void show(@NonNull Context context, @NonNull OnScriptPicked onPicked) {
        new FusionScriptFilePickerSheet(context, onPicked).show();
    }

    private FusionScriptFilePickerSheet(@NonNull Context context, @NonNull OnScriptPicked onPicked) {
        this.context = context;
        this.onPicked = onPicked;
        this.density = context.getResources().getDisplayMetrics().density;
        this.colorText = M3.onSurface(context);
        this.colorSubtle = M3.onSurfaceVariant(context);
        this.colorPrimary = M3.primary(context);

        File projectDir = new File("/project");
        if (projectDir.exists() && projectDir.isDirectory()) {
            this.currentDir = projectDir;
        } else {
            File rootDir = new File("/root");
            if (rootDir.exists() && rootDir.isDirectory()) {
                this.currentDir = rootDir;
            } else {
                this.currentDir = new File("/data/data/com.termux/files/home");
            }
        }
    }

    public void show() {
        dialog = new BottomSheetDialog(context);
        LinearLayout root = column();
        root.setPadding(pad(16), 0, pad(16), pad(12));

        root.addView(new BottomSheetDragHandleView(context), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Header Title
        TextView title = new TextView(context);
        title.setText("📁 Pilih Script / Program");
        M3.textAppearance(title, com.google.android.material.R.attr.textAppearanceTitleMedium);
        title.setTextColor(colorText);
        title.setPadding(0, 0, 0, pad(6));
        root.addView(title);

        // Quick Navigation Chips
        HorizontalScrollView quickScroll = new HorizontalScrollView(context);
        quickScroll.setHorizontalScrollBarEnabled(false);
        quickScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout quickRow = row();
        quickRow.setPadding(0, 0, 0, pad(8));

        addQuickChip(quickRow, "📁 /project", "/project");
        addQuickChip(quickRow, "📁 /root", "/root");
        addQuickChip(quickRow, "📦 /root/wallet", "/root/wallet");
        addQuickChip(quickRow, "⚙️ /usr/local/bin", "/usr/local/bin");
        addQuickChip(quickRow, "💻 ~/bin", "/data/data/com.termux/files/home/bin");
        addQuickChip(quickRow, "🏠 $HOME", "/data/data/com.termux/files/home");

        quickScroll.addView(quickRow);
        root.addView(quickScroll);

        // Path Header & Up Button
        LinearLayout pathRow = row();
        pathRow.setGravity(Gravity.CENTER_VERTICAL);
        pathRow.setPadding(0, 0, 0, pad(8));

        upButton = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        upButton.setText("⬆ Naik");
        upButton.setTextSize(12f);
        upButton.setOnClickListener(v -> navigateUp());
        LinearLayout.LayoutParams upParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        upParams.setMarginEnd(pad(8));
        upButton.setLayoutParams(upParams);
        pathRow.addView(upButton);

        currentPathView = new TextView(context);
        currentPathView.setText(currentDir.getAbsolutePath());
        M3.textAppearance(currentPathView, com.google.android.material.R.attr.textAppearanceBodyMedium);
        currentPathView.setTextColor(colorPrimary);
        currentPathView.setSingleLine(true);
        currentPathView.setEllipsize(TextUtils.TruncateAt.START);
        pathRow.addView(currentPathView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(pathRow);

        // Search / Filter Input
        TextInputLayout searchLayout = new TextInputLayout(context);
        searchLayout.setHint("Filter berkas di folder ini...");
        searchLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        searchLayout.setPadding(0, 0, 0, pad(6));
        searchInput = new TextInputEditText(searchLayout.getContext());
        searchInput.setSingleLine(true);
        searchLayout.addView(searchInput, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(searchLayout);

        // File List Scroller
        ScrollView scroller = new ScrollView(context);
        scroller.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, pad(350)));
        scroller.setOverScrollMode(View.OVER_SCROLL_NEVER);

        fileListContainer = column();
        scroller.addView(fileListContainer);
        root.addView(scroller);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                renderFileList(s != null ? s.toString().trim() : "");
            }
        });

        dialog.setContentView(root);
        BottomSheetBehavior<?> behavior = dialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        renderFileList("");
        dialog.show();
    }

    private void addQuickChip(@NonNull LinearLayout parent, @NonNull String label, @NonNull String path) {
        MaterialButton chip = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        chip.setText(label);
        chip.setTextSize(11f);
        chip.setInsetTop(0);
        chip.setInsetBottom(0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(pad(6));
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> {
            File target = new File(path);
            if (target.exists() && target.isDirectory()) {
                currentDir = target;
                if (searchInput != null && searchInput.getText() != null) {
                    searchInput.getText().clear();
                }
                renderFileList("");
            }
        });
        parent.addView(chip);
    }

    private void navigateUp() {
        File parent = currentDir.getParentFile();
        if (parent != null && parent.exists() && parent.canRead()) {
            currentDir = parent;
            if (searchInput != null && searchInput.getText() != null) {
                searchInput.getText().clear();
            }
            renderFileList("");
        }
    }

    private void renderFileList(@NonNull String filter) {
        if (fileListContainer == null) return;
        fileListContainer.removeAllViews();

        if (currentPathView != null) {
            currentPathView.setText(currentDir.getAbsolutePath());
        }
        if (upButton != null) {
            upButton.setEnabled(currentDir.getParentFile() != null);
        }

        List<FileItem> items = listDirectory(currentDir);
        String needle = filter.toLowerCase(Locale.ROOT);

        int count = 0;
        for (FileItem item : items) {
            if (!needle.isEmpty() && !item.name.toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }
            count++;
            fileListContainer.addView(createItemCard(item));
        }

        if (count == 0) {
            TextView empty = new TextView(context);
            empty.setText(items.isEmpty() ? "(Folder kosong atau tidak dapat diakses)" : "Tidak ada berkas yang cocok");
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, pad(24), 0, pad(24));
            empty.setTextColor(colorSubtle);
            fileListContainer.addView(empty);
        }
    }

    private static class FileItem {
        final File file;
        final String name;
        final boolean isDir;
        final long length;
        final long lastModified;

        FileItem(File file, String name, boolean isDir, long length, long lastModified) {
            this.file = file;
            this.name = name;
            this.isDir = isDir;
            this.length = length;
            this.lastModified = lastModified;
        }
    }

    @NonNull
    private List<FileItem> listDirectory(@NonNull File dir) {
        List<FileItem> dirs = new ArrayList<>();
        List<FileItem> files = new ArrayList<>();

        File[] list = dir.listFiles();
        if (list != null) {
            for (File f : list) {
                String name = f.getName();
                if (name.startsWith(".") && !name.equals(".backup") && !name.equals(".zshrc")) continue;
                if (f.isDirectory()) {
                    dirs.add(new FileItem(f, name, true, 0, f.lastModified()));
                } else {
                    files.add(new FileItem(f, name, false, f.length(), f.lastModified()));
                }
            }
        } else {
            // Fallback via su if root available
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "ls -pa " + dir.getAbsolutePath()});
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.equals("./") || line.equals("../")) continue;
                    boolean isSubDir = line.endsWith("/");
                    String cleanName = isSubDir ? line.substring(0, line.length() - 1) : line;
                    if (cleanName.startsWith(".") && !cleanName.equals(".backup") && !cleanName.equals(".zshrc")) continue;
                    File f = new File(dir, cleanName);
                    if (isSubDir) {
                        dirs.add(new FileItem(f, cleanName, true, 0, 0));
                    } else {
                        files.add(new FileItem(f, cleanName, false, 0, 0));
                    }
                }
                reader.close();
                p.waitFor();
            } catch (Throwable ignored) {}
        }

        Collections.sort(dirs, (a, b) -> a.name.compareToIgnoreCase(b.name));
        Collections.sort(files, (a, b) -> a.name.compareToIgnoreCase(b.name));

        List<FileItem> all = new ArrayList<>(dirs.size() + files.size());
        all.addAll(dirs);
        all.addAll(files);
        return all;
    }

    @NonNull
    private View createItemCard(@NonNull FileItem item) {
        MaterialCardView card = M3.clickableCard(context, true);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, pad(2), 0, pad(2));
        card.setLayoutParams(cardParams);

        LinearLayout row = row();
        row.setMinimumHeight(pad(44));
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(pad(12), pad(6), pad(12), pad(6));

        // Icon
        TextView iconView = new TextView(context);
        iconView.setTextSize(16f);
        iconView.setText(getIconForItem(item));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconParams.setMarginEnd(pad(12));
        iconView.setLayoutParams(iconParams);
        row.addView(iconView);

        // Name and details
        LinearLayout textCol = column();
        TextView nameView = new TextView(context);
        nameView.setText(item.name);
        M3.textAppearance(nameView, com.google.android.material.R.attr.textAppearanceBodyLarge);
        nameView.setTextColor(item.isDir ? colorPrimary : colorText);
        nameView.setSingleLine(true);
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        textCol.addView(nameView);

        if (!item.isDir) {
            TextView detailView = new TextView(context);
            String sizeStr = formatFileSize(item.length);
            detailView.setText(sizeStr);
            M3.textAppearance(detailView, com.google.android.material.R.attr.textAppearanceBodySmall);
            detailView.setTextColor(colorSubtle);
            textCol.addView(detailView);
        }
        row.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        card.addView(row);

        card.setOnClickListener(v -> {
            if (item.isDir) {
                currentDir = item.file;
                if (searchInput != null && searchInput.getText() != null) {
                    searchInput.getText().clear();
                }
                renderFileList("");
            } else {
                openScriptConfigSheet(item.file);
            }
        });

        return card;
    }

    @NonNull
    private String getIconForItem(@NonNull FileItem item) {
        if (item.isDir) return "📁";
        String n = item.name.toLowerCase(Locale.ROOT);
        if (n.endsWith(".sh") || n.endsWith(".bash") || n.endsWith(".zsh")) return "📜";
        if (n.endsWith(".py")) return "🐍";
        if (n.endsWith(".cjs") || n.endsWith(".js") || n.endsWith(".ts")) return "📦";
        if (n.endsWith(".pl") || n.endsWith(".rb")) return "💎";
        if (item.file.canExecute() || currentDir.getAbsolutePath().contains("bin")) return "⚙️";
        return "📄";
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.ROOT, "%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Sub-sheet dialog to configure execution mode (Background vs Foreground/New Tab),
     * OS environment, and button label.
     */
    private void openScriptConfigSheet(@NonNull File scriptFile) {
        BottomSheetDialog configDialog = new BottomSheetDialog(context);
        LinearLayout root = column();
        root.setPadding(pad(20), 0, pad(20), pad(16));

        root.addView(new BottomSheetDragHandleView(context), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(context);
        title.setText("⚙️ Konfigurasi Shortcut Script");
        M3.textAppearance(title, com.google.android.material.R.attr.textAppearanceTitleMedium);
        title.setTextColor(colorText);
        title.setPadding(0, 0, 0, pad(8));
        root.addView(title);

        // Path Display
        TextView pathView = new TextView(context);
        pathView.setText(scriptFile.getAbsolutePath());
        M3.textAppearance(pathView, com.google.android.material.R.attr.textAppearanceBodySmall);
        pathView.setTextColor(colorPrimary);
        pathView.setPadding(0, 0, 0, pad(12));
        root.addView(pathView);

        // Label Input Field
        String defaultLabel = deriveDefaultLabel(scriptFile.getName());
        TextInputLayout labelLayout = new TextInputLayout(context);
        labelLayout.setHint("Label Shortcut (Nama Tombol)");
        labelLayout.setPadding(0, 0, 0, pad(12));
        TextInputEditText labelField = new TextInputEditText(labelLayout.getContext());
        labelField.setSingleLine(true);
        labelField.setText(defaultLabel);
        labelLayout.addView(labelField, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(labelLayout);

        // Execution Mode Selector (Background vs Foreground)
        final String[] modeKeys = new String[]{"background", "foreground"};
        final String[] modeLabels = new String[]{
            "⚡ Background (Latar Belakang - Tanpa Terminal)",
            "🖥️ Foreground (Tab Baru di Kanvas Tengah)"
        };
        final int[] selectedModeIdx = new int[]{0}; // Default background

        TextView modeTitle = new TextView(context);
        modeTitle.setText("Mode Eksekusi:");
        M3.textAppearance(modeTitle, com.google.android.material.R.attr.textAppearanceLabelMedium);
        modeTitle.setTextColor(colorSubtle);
        modeTitle.setPadding(0, pad(4), 0, pad(2));
        root.addView(modeTitle);

        MaterialCardView modeCard = M3.clickableCard(context, true);
        LinearLayout modeRow = row();
        modeRow.setMinimumHeight(pad(48));
        modeRow.setGravity(Gravity.CENTER_VERTICAL);
        modeRow.setPadding(pad(12), pad(6), pad(12), pad(6));

        TextView modeSummary = new TextView(context);
        modeSummary.setText(modeLabels[selectedModeIdx[0]]);
        M3.textAppearance(modeSummary, com.google.android.material.R.attr.textAppearanceBodyMedium);
        modeSummary.setTextColor(colorText);
        modeRow.addView(modeSummary, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView modeChevron = new TextView(context);
        modeChevron.setText("▼");
        modeChevron.setTextColor(colorSubtle);
        modeRow.addView(modeChevron);
        modeCard.addView(modeRow);

        modeCard.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Pilih Mode Eksekusi")
                .setSingleChoiceItems(modeLabels, selectedModeIdx[0], (d, which) -> {
                    selectedModeIdx[0] = which;
                    modeSummary.setText(modeLabels[which]);
                    d.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        });
        root.addView(modeCard);

        // OS Environment Selector
        final String[] envKeys = new String[]{"auto", "chroot", "droidspaces", "proot", "native"};
        final String[] envLabels = new String[]{
            "⚡ Auto-Detect (Chroot / Droidspaces / PRoot / Native)",
            "🐉 Kali NetHunter Chroot",
            "📦 Droidspaces Container (Ubuntu)",
            "🐧 PRoot Distro",
            "💻 Native Termux"
        };
        final int[] selectedEnvIdx = new int[]{0};

        TextView envTitle = new TextView(context);
        envTitle.setText("Lingkungan Sistem (OS):");
        M3.textAppearance(envTitle, com.google.android.material.R.attr.textAppearanceLabelMedium);
        envTitle.setTextColor(colorSubtle);
        envTitle.setPadding(0, pad(10), 0, pad(2));
        root.addView(envTitle);

        MaterialCardView envCard = M3.clickableCard(context, true);
        LinearLayout envRow = row();
        envRow.setMinimumHeight(pad(48));
        envRow.setGravity(Gravity.CENTER_VERTICAL);
        envRow.setPadding(pad(12), pad(6), pad(12), pad(6));

        TextView envSummary = new TextView(context);
        envSummary.setText(envLabels[selectedEnvIdx[0]]);
        M3.textAppearance(envSummary, com.google.android.material.R.attr.textAppearanceBodyMedium);
        envSummary.setTextColor(colorText);
        envRow.addView(envSummary, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView envChevron = new TextView(context);
        envChevron.setText("▼");
        envChevron.setTextColor(colorSubtle);
        envRow.addView(envChevron);
        envCard.addView(envRow);

        envCard.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Pilih Lingkungan Sistem (OS)")
                .setSingleChoiceItems(envLabels, selectedEnvIdx[0], (d, which) -> {
                    selectedEnvIdx[0] = which;
                    envSummary.setText(envLabels[which]);
                    d.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        });
        root.addView(envCard);

        // Action Buttons: Batal & Pasang
        LinearLayout actions = row();
        actions.setPadding(0, pad(16), 0, 0);

        MaterialButton cancelBtn = new MaterialButton(context, null, androidx.appcompat.R.attr.borderlessButtonStyle);
        cancelBtn.setText("Batal");
        cancelBtn.setOnClickListener(v -> configDialog.dismiss());
        actions.addView(cancelBtn);

        View spacer = new View(context);
        actions.addView(spacer, new LinearLayout.LayoutParams(0, 0, 1f));

        MaterialButton addBtn = new MaterialButton(context);
        addBtn.setText("Pasang Shortcut");
        addBtn.setOnClickListener(v -> {
            String label = (labelField.getText() != null && !labelField.getText().toString().trim().isEmpty())
                ? labelField.getText().toString().trim()
                : defaultLabel;
            String execPath = scriptFile.getAbsolutePath();
            String targetKey = execPath.endsWith("\n") ? execPath : (execPath + "\n");
            String targetMode = modeKeys[selectedModeIdx[0]];
            String targetEnv = envKeys[selectedEnvIdx[0]];

            ExtraKeysLayoutModel.Key newKey = new ExtraKeysLayoutModel.Key(
                targetKey, false, label, null, null, targetMode, targetEnv);

            configDialog.dismiss();
            if (dialog != null) dialog.dismiss();
            onPicked.onPicked(newKey);
        });
        actions.addView(addBtn);
        root.addView(actions);

        configDialog.setContentView(root);
        configDialog.show();
    }

    @NonNull
    private String deriveDefaultLabel(@NonNull String fileName) {
        String base = fileName;
        String icon = "📜 ";
        if (fileName.endsWith(".sh") || fileName.endsWith(".bash") || fileName.endsWith(".zsh")) {
            base = fileName.substring(0, fileName.lastIndexOf('.'));
            icon = "📜 ";
        } else if (fileName.endsWith(".py")) {
            base = fileName.substring(0, fileName.lastIndexOf('.'));
            icon = "🐍 ";
        } else if (fileName.endsWith(".cjs") || fileName.endsWith(".js") || fileName.endsWith(".ts")) {
            base = fileName.substring(0, fileName.lastIndexOf('.'));
            icon = "📦 ";
        } else if (fileName.endsWith(".pl") || fileName.endsWith(".rb")) {
            base = fileName.substring(0, fileName.lastIndexOf('.'));
            icon = "💎 ";
        } else {
            icon = "⚙️ ";
        }
        return icon + base;
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private LinearLayout column() {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private int pad(float dp) {
        return Math.round(dp * density);
    }
}
