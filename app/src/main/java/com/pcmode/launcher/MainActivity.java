package com.pcmode.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int BG = 0xFF1E1E1E;
    private static final int MENU = 0xFF2D2D2D;
    private static final int INPUT = 0xFF252526;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int BLUE = 0xFF0078D4;
    private static final int RED = 0xFFE53935;

    private FrameLayout root, content;
    private LinearLayout startMenu;
    private TextView clock;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs = getSharedPreferences("pcmode", MODE_PRIVATE);
        buildRoot();
        showDesktop();
        updateClock();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_SHORT).show(); }

    private void buildRoot() {
        root = new FrameLayout(this);
        content = new FrameLayout(this);
        root.addView(content, new FrameLayout.LayoutParams(MATCH, MATCH));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(6), dp(4), dp(6), dp(4));
        bar.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams barParams = new FrameLayout.LayoutParams(MATCH, dp(52), Gravity.BOTTOM);
        root.addView(bar, barParams);

        Button start = button("⊞  START", BLUE, 13);
        start.setOnClickListener(v -> toggleStart());
        bar.addView(start, new LinearLayout.LayoutParams(dp(115), dp(44)));
        bar.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));

        clock = new TextView(this);
        clock.setTextColor(WHITE);
        clock.setGravity(Gravity.CENTER);
        clock.setTextSize(12);
        bar.addView(clock, new LinearLayout.LayoutParams(WRAP, dp(44)));

        Button exit = button("EXIT", RED, 11);
        exit.setOnClickListener(v -> finish());
        bar.addView(exit, new LinearLayout.LayoutParams(dp(75), dp(44)));
        setContentView(root);
    }

    private Button button(String text, int color, float size) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(WHITE);
        b.setTextSize(size);
        b.setBackgroundColor(color);
        return b;
    }

    private void updateClock() {
        if (clock == null) return;
        clock.setText(new SimpleDateFormat("HH:mm  dd/MM", Locale.getDefault()).format(new Date()));
        handler.postDelayed(this::updateClock, 1000);
    }

    private void toggleStart() {
        if (startMenu == null) buildStartMenu();
        startMenu.setVisibility(startMenu.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void buildStartMenu() {
        startMenu = new LinearLayout(this);
        startMenu.setOrientation(LinearLayout.VERTICAL);
        startMenu.setPadding(dp(10), dp(10), dp(10), dp(10));
        startMenu.setBackgroundColor(0xF01F1F1F);
        TextView title = text("PC Mode", WHITE, 14);
        title.setTypeface(null, Typeface.BOLD);
        startMenu.addView(title, new LinearLayout.LayoutParams(MATCH, dp(40)));

        String[][] apps = {{"Terminal", ">_"}, {"Browser", "🌐"}, {"Files", "📁"},
                {"Notepad", "📝"}, {"Calculator", "🔢"}, {"Settings", "⚙"}, {"About", "ℹ"}};
        for (String[] app : apps) {
            final String name = app[0];
            Button item = button(app[1] + "   " + name, MENU, 12);
            item.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            item.setOnClickListener(v -> { startMenu.setVisibility(View.GONE); openApp(name); });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(240), dp(46));
            p.setMargins(0, dp(2), 0, dp(2));
            startMenu.addView(item, p);
        }
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(dp(260), WRAP, Gravity.BOTTOM | Gravity.LEFT);
        p.leftMargin = dp(6);
        p.bottomMargin = dp(56);
        root.addView(startMenu, p);
        startMenu.setVisibility(View.GONE);
    }

    private TextView text(String value, int color, float size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setPadding(dp(8), dp(4), dp(8), dp(4));
        return t;
    }

    private void showDesktop() {
        content.removeAllViews();
        LinearLayout desk = new LinearLayout(this);
        desk.setOrientation(LinearLayout.VERTICAL);
        desk.setGravity(Gravity.CENTER);
        desk.setBackgroundColor(Color.parseColor(prefs.getString("wall", "#0E4C8A")));
        TextView logo = text("PC MODE", WHITE, 28);
        logo.setGravity(Gravity.CENTER);
        desk.addView(logo, new LinearLayout.LayoutParams(MATCH, dp(70)));
        TextView help = text("Tap ⊞ START to open apps", 0xCCFFFFFF, 13);
        help.setGravity(Gravity.CENTER);
        desk.addView(help, new LinearLayout.LayoutParams(MATCH, dp(45)));
        content.addView(desk, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private View header(String title) {
        LinearLayout h = new LinearLayout(this);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(dp(10), dp(8), dp(10), dp(8));
        h.setBackgroundColor(MENU);
        TextView t = text(title, WHITE, 14);
        t.setTypeface(null, Typeface.BOLD);
        h.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1));
        Button close = button("✕", RED, 12);
        close.setOnClickListener(v -> showDesktop());
        h.addView(close, new LinearLayout.LayoutParams(dp(50), dp(36)));
        return h;
    }

    private void openApp(String name) {
        switch (name) {
            case "Terminal": showTerminal(); break;
            case "Browser": showBrowser(); break;
            case "Files": showFiles(); break;
            case "Notepad": showNotepad(); break;
            case "Calculator": showCalculator(); break;
            case "Settings": showSettings(); break;
            case "About": showAbout(); break;
            default: toast("Unknown app: " + name);
        }
    }

    private void showTerminal() {
        content.removeAllViews();
        LinearLayout box = vertical(Color.BLACK);
        box.addView(header("Terminal"));
        TextView output = text("Android Shell [v1.0]\nType 'help' for commands\n\n", 0xFF00FF00, 11);
        output.setTypeface(Typeface.MONOSPACE);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(output);
        box.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setTextColor(0xFF00FF00);
        input.setHintTextColor(0xFF008800);
        input.setHint("type command...");
        input.setTypeface(Typeface.MONOSPACE);
        input.setBackgroundColor(0xFF001100);
        input.setOnEditorActionListener((v, action, event) -> {
            String command = input.getText().toString().trim();
            input.setText("");
            output.append("$ " + command + "\n" + runShell(command) + "\n");
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            return true;
        });
        box.addView(input, new LinearLayout.LayoutParams(MATCH, WRAP));
        content.addView(box, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private String runShell(String command) {
        if (command.isEmpty()) return "";
        if (command.equals("help")) return "Commands: ls, pwd, date, whoami, uname, ps, df, echo X, clear";
        if (command.equals("clear")) return "\n\n\n\n\n";
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = out.readLine()) != null) result.append(line).append('\n');
            while ((line = err.readLine()) != null) result.append("[err] ").append(line).append('\n');
            process.waitFor();
            return result.length() == 0 ? "(no output)" : result.toString();
        } catch (Exception e) { return "error: " + e.getMessage(); }
    }

    private LinearLayout vertical(int color) {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(color);
        return v;
    }

    private void showNotepad() {
        content.removeAllViews();
        LinearLayout box = vertical(BG);
        box.addView(header("Notepad"));
        LinearLayout menu = new LinearLayout(this);
        Button save = button("💾 SAVE", BLUE, 11);
        Button clear = button("🗑 CLEAR", RED, 11);
        menu.addView(save, new LinearLayout.LayoutParams(0, WRAP, 1));
        menu.addView(clear, new LinearLayout.LayoutParams(0, WRAP, 1));
        box.addView(menu);
        EditText editor = new EditText(this);
        editor.setText(prefs.getString("notes", ""));
        editor.setTextColor(WHITE);
        editor.setHintTextColor(MUTED);
        editor.setHint("Start typing...");
        editor.setGravity(Gravity.TOP | Gravity.LEFT);
        editor.setTypeface(Typeface.MONOSPACE);
        editor.setBackgroundColor(INPUT);
        save.setOnClickListener(v -> { prefs.edit().putString("notes", editor.getText().toString()).apply(); toast("Saved!"); });
        clear.setOnClickListener(v -> editor.setText(""));
        box.addView(editor, new LinearLayout.LayoutParams(MATCH, 0, 1));
        content.addView(box, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showBrowser() {
        content.removeAllViews();
        LinearLayout box = vertical(BG);
        box.addView(header("Browser"));
        LinearLayout bar = new LinearLayout(this);
        EditText address = new EditText(this);
        address.setSingleLine(true);
        address.setText("https://www.google.com");
        address.setTextColor(WHITE);
        address.setBackgroundColor(INPUT);
        Button go = button("GO", BLUE, 11);
        bar.addView(address, new LinearLayout.LayoutParams(0, WRAP, 1));
        bar.addView(go, new LinearLayout.LayoutParams(dp(65), WRAP));
        box.addView(bar);
        WebView web = new WebView(this);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl(address.getText().toString());
        go.setOnClickListener(v -> { String url = address.getText().toString().trim(); if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) url = "https://" + url; web.loadUrl(url); });
        box.addView(web, new LinearLayout.LayoutParams(MATCH, 0, 1));
        content.addView(box, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showFiles() {
        content.removeAllViews();
        LinearLayout box = vertical(BG);
        box.addView(header("Files"));
        Button open = button("OPEN DEVICE FILES", BLUE, 12);
        open.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE)));
        box.addView(open, new LinearLayout.LayoutParams(MATCH, WRAP));
        TextView info = text("Use the Android file picker to open files safely.\nLegacy /sdcard access is restricted on modern Android.", MUTED, 13);
        box.addView(info);
        content.addView(box, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showCalculator() {
        content.removeAllViews();
        LinearLayout box = vertical(Color.BLACK);
        box.addView(header("Calculator"));
        TextView display = text("0", WHITE, 36);
        display.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        box.addView(display, new LinearLayout.LayoutParams(MATCH, dp(100)));
        final double[] left = {0};
        final char[] operator = {0};
        final boolean[] fresh = {true};
        String[][] keys = {{"C", "÷", "×", "⌫"}, {"7", "8", "9", "-"}, {"4", "5", "6", "+"}, {"1", "2", "3", "="}, {"0", "."}};
        for (String[] rowKeys : keys) {
            LinearLayout row = new LinearLayout(this);
            for (String key : rowKeys) {
                Button b = button(key, key.equals("=") ? BLUE : key.equals("C") ? RED : MENU, 20);
                row.addView(b, new LinearLayout.LayoutParams(0, MATCH, 1));
                b.setOnClickListener(v -> calculatorKey(display, key, left, operator, fresh));
            }
            box.addView(row, new LinearLayout.LayoutParams(MATCH, 0, 1));
        }
        content.addView(box, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void calculatorKey(TextView display, String key, double[] left, char[] op, boolean[] fresh) {
        String current = display.getText().toString();
        if (key.matches("[0-9]") || key.equals(".")) {
            if (fresh[0] || current.equals("0")) { display.setText(key.equals(".") ? "0." : key); fresh[0] = false; }
            else if (!key.equals(".") || !current.contains(".")) display.append(key);
            return;
        }
        if (key.equals("C")) { display.setText("0"); left[0] = 0; op[0] = 0; fresh[0] = true; return; }
        if (key.equals("⌫")) { display.setText(current.length() > 1 ? current.substring(0, current.length() - 1) : "0"); return; }
        if ("÷×-+".contains(key)) { left[0] = Double.parseDouble(current); op[0] = key.charAt(0); fresh[0] = true; return; }
        if (key.equals("=") && op[0] != 0) {
            double right = Double.parseDouble(current), result;
            if (op[0] == '+') result = left[0] + right; else if (op[0] == '-') result = left[0] - right; else if (op[0] == '×') result = left[0] * right; else { if (right == 0) { toast("Cannot divide by zero"); return; } result = left[0] / right; }
            display.setText(formatNumber(result)); left[0] = result; op[0] = 0; fresh[0] = true;
        }
    }

    private String formatNumber(double n) { return n == (long) n ? Long.toString((long) n) : Double.toString(n); }

    private void showSettings() {
        content.removeAllViews();
        LinearLayout box = vertical(BG);
        box.addView(header("Settings"));
        Button wall = button("BLUE WALLPAPER", BLUE, 12);
        wall.setOnClickListener(v -> { prefs.edit().putString("wall", "#0E4C8A").apply(); showDesktop(); });
        Button dark = button("DARK WALLPAPER", MENU, 12);
        dark.setOnClickListener(v -> { prefs.edit().putString("wall", "#101010").apply(); showDesktop(); });
        Button android = button("OPEN ANDROID APP SETTINGS", MENU, 12);
        android.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))));
        box.addView(wall); box.addView(dark); box.addView(android);
        content.addView(box, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showAbout() {
        content.removeAllViews();
        LinearLayout box = vertical(BG);
        box.addView(header("About"));
        TextView about = text("PC Mode\nVersion 1.0\n\nA lightweight desktop-style Android launcher.\nTerminal, browser, files, notes, calculator and settings are included.", WHITE, 16);
        about.setGravity(Gravity.CENTER);
        box.addView(about, new LinearLayout.LayoutParams(MATCH, 0, 1));
        content.addView(box, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
