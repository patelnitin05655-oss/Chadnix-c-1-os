package com.pcmode.launcher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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
    private static final int WIN_BG = 0xFF1E1E1E;
    private static final int TASKBAR = 0xFF000000;
    private static final int ACCENT = 0xFF0078D4;
    private static final int GREEN = 0xFF4CAF50;
    private static final int RED = 0xFFE53935;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int WALL = 0xFF0E4C8A;

    private FrameLayout root;
    private FrameLayout content;
    private LinearLayout startMenu;
    private TextView clockView;
    private boolean startOpen;
    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable clockTask = new Runnable() {
        @Override public void run() {
            if (clockView != null) {
                clockView.setText(new SimpleDateFormat("HH:mm  dd/MM", Locale.getDefault()).format(new Date()));
                handler.postDelayed(this, 1000L);
            }
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs = getSharedPreferences("pcmode", MODE_PRIVATE);
        buildRoot();
        showDesktop();
        handler.post(clockTask);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(clockTask);
        super.onDestroy();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void buildRoot() {
        root = new FrameLayout(this);
        root.setBackgroundColor(WALL);
        content = new FrameLayout(this);
        root.addView(content, new FrameLayout.LayoutParams(MATCH, MATCH));

        LinearLayout taskbar = new LinearLayout(this);
        taskbar.setOrientation(LinearLayout.HORIZONTAL);
        taskbar.setGravity(Gravity.CENTER_VERTICAL);
        taskbar.setPadding(dp(6), dp(4), dp(6), dp(4));
        taskbar.setBackgroundColor(TASKBAR);
        FrameLayout.LayoutParams taskbarParams = new FrameLayout.LayoutParams(MATCH, dp(48));
        taskbarParams.gravity = Gravity.BOTTOM;
        root.addView(taskbar, taskbarParams);

        Button start = button("⊞  START", ACCENT, 13);
        start.setOnClickListener(v -> toggleStart());
        taskbar.addView(start, new LinearLayout.LayoutParams(dp(110), dp(40)));

        taskbar.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        clockView = new TextView(this);
        clockView.setTextColor(WHITE);
        clockView.setGravity(Gravity.CENTER);
        clockView.setTextSize(12);
        clockView.setPadding(dp(8), 0, dp(8), 0);
        taskbar.addView(clockView, new LinearLayout.LayoutParams(WRAP, dp(40)));

        Button exit = button("EXIT", RED, 11);
        exit.setOnClickListener(v -> finish());
        taskbar.addView(exit, new LinearLayout.LayoutParams(dp(70), dp(40)));
        setContentView(root);
    }

    private Button button(String text, int color, int size) {
        Button result = new Button(this);
        result.setText(text);
        result.setTextColor(WHITE);
        result.setTextSize(size);
        result.setBackgroundColor(color);
        return result;
    }

    private void toggleStart() {
        if (startOpen) hideStart(); else showStart();
    }

    private void showStart() {
        if (startMenu == null) buildStartMenu();
        startMenu.setVisibility(View.VISIBLE);
        startOpen = true;
    }

    private void hideStart() {
        if (startMenu != null) startMenu.setVisibility(View.GONE);
        startOpen = false;
    }

    private void buildStartMenu() {
        startMenu = new LinearLayout(this);
        startMenu.setOrientation(LinearLayout.VERTICAL);
        startMenu.setPadding(dp(10), dp(10), dp(10), dp(10));
        startMenu.setBackgroundColor(0xF01A1A1A);

        TextView title = new TextView(this);
        title.setText("PC Mode");
        title.setTextColor(WHITE);
        title.setTextSize(14);
        title.setPadding(dp(8), dp(4), dp(8), dp(10));
        startMenu.addView(title);

        String[][] items = {{"Terminal", ">_"}, {"Browser", "Web"}, {"Files", "Files"},
                {"Notes", "Notes"}, {"Calculator", "Calc"}, {"Settings", "Settings"},
                {"About", "About"}};
        for (String[] item : items) {
            final String name = item[0];
            Button app = button(item[1] + "   " + name, 0xFF2A2A2A, 12);
            app.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            app.setPadding(dp(14), dp(8), dp(14), dp(8));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(220), dp(46));
            params.setMargins(0, dp(2), 0, dp(2));
            startMenu.addView(app, params);
            app.setOnClickListener(v -> { hideStart(); openApp(name); });
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(240), WRAP);
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        params.leftMargin = dp(6);
        params.bottomMargin = dp(52);
        root.addView(startMenu, params);
    }

    private void showDesktop() {
        content.removeAllViews();
        LinearLayout desktop = new LinearLayout(this);
        desktop.setOrientation(LinearLayout.VERTICAL);
        desktop.setGravity(Gravity.CENTER_HORIZONTAL);
        desktop.setPadding(dp(20), dp(20), dp(20), dp(20));
        desktop.setBackgroundColor(WALL);

        TextView logo = text("PC MODE", WHITE, 28);
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(0, dp(30), 0, dp(20));
        desktop.addView(logo, new LinearLayout.LayoutParams(MATCH, WRAP));
        TextView hint = text("Tap ⊞ START to open apps", 0xCCFFFFFF, 13);
        hint.setGravity(Gravity.CENTER);
        desktop.addView(hint, new LinearLayout.LayoutParams(MATCH, WRAP));
        content.addView(desktop, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private TextView text(String value, int color, int size) {
        TextView result = new TextView(this);
        result.setText(value);
        result.setTextColor(color);
        result.setTextSize(size);
        return result;
    }

    private void openApp(String name) {
        if ("Terminal".equals(name)) showTerminal();
        else if ("Browser".equals(name)) showBrowser();
        else if ("Files".equals(name)) showFiles();
        else if ("Notes".equals(name)) showNotes();
        else if ("Calculator".equals(name)) showCalculator();
        else if ("Settings".equals(name)) showSettings();
        else if ("About".equals(name)) showAbout();
    }

    private View makeHeader(String title) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(10), dp(8), dp(10), dp(8));
        header.setBackgroundColor(ACCENT);
        header.addView(text(title, WHITE, 14), new LinearLayout.LayoutParams(0, WRAP, 1));
        Button close = button("X", RED, 12);
        close.setOnClickListener(v -> showDesktop());
        header.addView(close, new LinearLayout.LayoutParams(dp(50), dp(36)));
        return header;
    }

    private void showTerminal() {
        content.removeAllViews();
        LinearLayout layout = vertical(0xFF000000);
        layout.addView(makeHeader("Terminal"));
        TextView output = text("Android Shell [v1.0]\nType 'help' for commands\n\n", 0xFF00FF00, 11);
        output.setTypeface(android.graphics.Typeface.MONOSPACE);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(output);
        layout.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));
        EditText input = new EditText(this);
        input.setTextColor(0xFF00FF00);
        input.setHint("type command...");
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        layout.addView(input, new LinearLayout.LayoutParams(MATCH, WRAP));
        input.setOnEditorActionListener((v, action, event) -> {
            String command = input.getText().toString().trim();
            input.setText("");
            output.append("$ " + command + "\n");
            String result = runShell(command);
            output.append((result.isEmpty() ? "(no output)" : result) + "\n");
            return true;
        });
        content.addView(layout, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private String runShell(String command) {
        if (command.isEmpty()) return "";
        if ("help".equals(command)) {
            return "Commands:\n  ls - list files\n  pwd - current directory\n  date - date\n  whoami - user\n  uname - kernel\n  ps - processes\n  echo X - print X\n  df - disk usage";
        }
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            StringBuilder output = new StringBuilder();
            try (BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = out.readLine()) != null) output.append(line).append('\n');
                while ((line = err.readLine()) != null) output.append("[err] ").append(line).append('\n');
            }
            process.waitFor();
            return output.toString();
        } catch (Exception exception) {
            return "error: " + exception.getMessage();
        }
    }

    private LinearLayout vertical(int color) {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        result.setBackgroundColor(color);
        return result;
    }

    private void showBrowser() {
        content.removeAllViews();
        LinearLayout layout = vertical(WIN_BG);
        layout.addView(makeHeader("Browser"));
        LinearLayout bar = new LinearLayout(this);
        EditText url = new EditText(this);
        url.setText("https://www.google.com");
        url.setSingleLine(true);
        Button go = button("GO", ACCENT, 12);
        bar.addView(url, new LinearLayout.LayoutParams(0, WRAP, 1));
        bar.addView(go, new LinearLayout.LayoutParams(dp(60), WRAP));
        layout.addView(bar);
        WebView web = new WebView(this);
        web.setWebViewClient(new WebViewClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(url.getText().toString());
        go.setOnClickListener(v -> {
            String address = url.getText().toString().trim();
            if (!address.startsWith("http://") && !address.startsWith("https://")) address = "https://" + address;
            web.loadUrl(address);
        });
        layout.addView(web, new LinearLayout.LayoutParams(MATCH, 0, 1));
        content.addView(layout, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showFiles() {
        content.removeAllViews();
        LinearLayout layout = vertical(WIN_BG);
        layout.addView(makeHeader("File Manager - /sdcard"));
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = vertical(WIN_BG);
        java.io.File[] files = new java.io.File("/sdcard").listFiles();
        if (files == null || files.length == 0) {
            list.addView(text("No files available or storage permission is unavailable.", WHITE, 12));
        } else {
            for (java.io.File file : files) {
                if (file.getName().startsWith(".")) continue;
                Button item = button((file.isDirectory() ? "[DIR] " : "[FILE] ") + file.getName(), 0xFF2A2A2A, 12);
                item.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                list.addView(item, new LinearLayout.LayoutParams(MATCH, WRAP));
            }
        }
        scroll.addView(list);
        layout.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));
        content.addView(layout, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showNotes() {
        content.removeAllViews();
        LinearLayout layout = vertical(WIN_BG);
        layout.addView(makeHeader("Notes"));
        EditText notes = new EditText(this);
        notes.setText(prefs.getString("notes", ""));
        notes.setTextColor(WHITE);
        notes.setGravity(Gravity.TOP | Gravity.LEFT);
        layout.addView(notes, new LinearLayout.LayoutParams(MATCH, 0, 1));
        LinearLayout actions = new LinearLayout(this);
        Button save = button("SAVE", GREEN, 12);
        Button clear = button("CLEAR", RED, 12);
        save.setOnClickListener(v -> { prefs.edit().putString("notes", notes.getText().toString()).apply(); toast("Saved"); });
        clear.setOnClickListener(v -> notes.setText(""));
        actions.addView(save, new LinearLayout.LayoutParams(0, WRAP, 1));
        actions.addView(clear, new LinearLayout.LayoutParams(0, WRAP, 1));
        layout.addView(actions);
        content.addView(layout, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showCalculator() {
        content.removeAllViews();
        LinearLayout layout = vertical(WIN_BG);
        layout.addView(makeHeader("Calculator"));
        TextView display = text("0", WHITE, 32);
        display.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        display.setBackgroundColor(0xFF000000);
        layout.addView(display, new LinearLayout.LayoutParams(MATCH, dp(100)));
        final double[] previous = {0};
        final char[] operator = {0};
        final boolean[] hasPrevious = {false};
        final boolean[] newNumber = {true};
        String[][] keys = {{"C", "+", "-", "/"}, {"7", "8", "9", "*"}, {"4", "5", "6", "="}, {"1", "2", "3", "0"}};
        for (String[] keyRow : keys) {
            LinearLayout row = new LinearLayout(this);
            for (String key : keyRow) {
                Button button = button(key, "C".equals(key) ? RED : ("=+-*/".contains(key) ? ACCENT : 0xFF2A2A2A), 20);
                row.addView(button, new LinearLayout.LayoutParams(0, MATCH, 1));
                button.setOnClickListener(v -> calculatorInput(key, display, previous, operator, hasPrevious, newNumber));
            }
            layout.addView(row, new LinearLayout.LayoutParams(MATCH, 0, 1));
        }
        content.addView(layout, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void calculatorInput(String key, TextView display, double[] previous, char[] operator,
                                 boolean[] hasPrevious, boolean[] newNumber) {
        if ("C".equals(key)) {
            display.setText("0"); previous[0] = 0; operator[0] = 0; hasPrevious[0] = false; newNumber[0] = true; return;
        }
        if ("+-*/".contains(key)) {
            if (!hasPrevious[0]) previous[0] = Double.parseDouble(display.getText().toString());
            operator[0] = key.charAt(0); hasPrevious[0] = true; newNumber[0] = true; return;
        }
        if ("=".equals(key)) {
            if (!hasPrevious[0]) return;
            double current = Double.parseDouble(display.getText().toString());
            if (operator[0] == '/' && current == 0) { display.setText("Error"); hasPrevious[0] = false; newNumber[0] = true; return; }
            double result = calculate(previous[0], current, operator[0]);
            display.setText(formatNumber(result)); hasPrevious[0] = false; newNumber[0] = true; return;
        }
        if (newNumber[0] || "0".equals(display.getText().toString()) || "Error".equals(display.getText().toString())) {
            display.setText(key); newNumber[0] = false;
        } else display.append(key);
    }

    private double calculate(double left, double right, char operator) {
        if (operator == '+') return left + right;
        if (operator == '-') return left - right;
        if (operator == '*') return left * right;
        return left / right;
    }

    private String formatNumber(double value) {
        String result = String.valueOf(Math.round(value * 1000000.0) / 1000000.0);
        return result.endsWith(".0") ? result.substring(0, result.length() - 2) : result;
    }

    private void showSettings() {
        content.removeAllViews();
        LinearLayout layout = vertical(WIN_BG);
        layout.addView(makeHeader("Settings"));
        layout.addView(text("PC Mode\n\nDark theme enabled\nFiles access: /sdcard\nNotes are saved locally", WHITE, 13));
        Button reset = button("RESET NOTES", RED, 12);
        reset.setOnClickListener(v -> { prefs.edit().remove("notes").apply(); toast("Notes reset"); });
        layout.addView(reset);
        content.addView(layout, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void showAbout() {
        content.removeAllViews();
        LinearLayout layout = vertical(WIN_BG);
        layout.addView(makeHeader("About"));
        layout.addView(text("PC Mode\nVersion 1.0\n\nA lightweight desktop-like launcher for Android.", WHITE, 14));
        content.addView(layout, new FrameLayout.LayoutParams(MATCH, MATCH));
    }
}
