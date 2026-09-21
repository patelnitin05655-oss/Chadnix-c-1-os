package com.pcmode.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    static final int WIN_BG = 0xFF1E1E1E;
    static final int TASKBAR = 0xFF000000;
    static final int ACCENT = 0xFF0078D4;
    static final int GREEN = 0xFF4CAF50;
    static final int RED = 0xFFE53935;
    static final int WHITE = 0xFFFFFFFF;
    static final int GREY = 0xFFAAAAAA;
    static final int WALL = 0xFF0E4C8A;

    FrameLayout root, content;
    LinearLayout taskbar, startMenu;
    TextView clockView;
    boolean startOpen = false;
    SharedPreferences prefs;
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs = getSharedPreferences("pcmode", MODE_PRIVATE);
        buildRoot();
        showDesktop();
        startClock();
    }

    int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    // ═══════════ ROOT UI ═══════════

    void buildRoot() {
        root = new FrameLayout(this);
        root.setBackgroundColor(WALL);

        content = new FrameLayout(this);
        root.addView(content, new FrameLayout.LayoutParams(MATCH, MATCH));

        taskbar = new LinearLayout(this);
        taskbar.setOrientation(LinearLayout.HORIZONTAL);
        taskbar.setBackgroundColor(TASKBAR);
        taskbar.setPadding(dp(6), dp(4), dp(6), dp(4));
        taskbar.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(MATCH, dp(48));
        tp.gravity = Gravity.BOTTOM;
        root.addView(taskbar, tp);

        Button start = new Button(this);
        start.setText("⊞  START");
        start.setTextColor(WHITE);
        start.setTextSize(13);
        start.setBackgroundColor(ACCENT);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { toggleStart(); }
        });
        taskbar.addView(start, new LinearLayout.LayoutParams(dp(110), dp(40)));

        View spacer = new View(this);
        taskbar.addView(spacer, new LinearLayout.LayoutParams(0, dp(1), 1));

        clockView = new TextView(this);
        clockView.setTextColor(WHITE);
        clockView.setTextSize(12);
        clockView.setGravity(Gravity.CENTER);
        clockView.setPadding(dp(8), 0, dp(8), 0);
        taskbar.addView(clockView, new LinearLayout.LayoutParams(WRAP, dp(40)));

        Button exit = new Button(this);
        exit.setText("EXIT");
        exit.setTextColor(WHITE);
        exit.setTextSize(11);
        exit.setBackgroundColor(RED);
        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        taskbar.addView(exit, new LinearLayout.LayoutParams(dp(70), dp(40)));

        setContentView(root);
    }

    void startClock() {
        handler.post(new Runnable() {
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm  dd/MM", Locale.getDefault());
                clockView.setText(sdf.format(new Date()));
                handler.postDelayed(this, 1000);
            }
        });
    }

    // ═══════════ START MENU ═══════════

    void toggleStart() {
        if (startOpen) hideStart();
        else showStart();
    }

    void showStart() {
        if (startMenu == null) buildStartMenu();
        startMenu.setVisibility(View.VISIBLE);
        startOpen = true;
    }

    void hideStart() {
        if (startMenu != null) startMenu.setVisibility(View.GONE);
        startOpen = false;
    }

    void buildStartMenu() {
        startMenu = new LinearLayout(this);
        startMenu.setOrientation(LinearLayout.VERTICAL);
        startMenu.setBackgroundColor(0xF01A1A1A);
        startMenu.setPadding(dp(10), dp(10), dp(10), dp(10));

        TextView title = new TextView(this);
        title.setText("PC Mode");
        title.setTextColor(WHITE);
        title.setTextSize(14);
        title.setPadding(dp(8), dp(4), dp(8), dp(10));
        startMenu.addView(title);

        String[][] items = {
            {"Terminal", ">_"},
            {"Browser", "🌐"},
            {"Files", "📁"},
            {"Notes", "📝"},
            {"Calculator", "🔢"},
            {"Settings", "⚙"},
            {"About", "ℹ"}
        };

        for (String[] item : items) {
            final String name = item[0];
            Button b = new Button(this);
            b.setText(item[1] + "   " + name);
            b.setTextColor(WHITE);
            b.setTextSize(12);
            b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            b.setBackgroundColor(0xFF2A2A2A);
            b.setPadding(dp(14), dp(8), dp(14), dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(220), dp(46));
            lp.setMargins(0, dp(2), 0, dp(2));
            b.setLayoutParams(lp);
            b.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    hideStart();
                    openApp(name);
                }
            });
            startMenu.addView(b);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(240), WRAP);
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.bottomMargin = dp(52);
        lp.leftMargin = dp(6);
        root.addView(startMenu, lp);
    }

    // ═══════════ DESKTOP ═══════════

    void showDesktop() {
        content.removeAllViews();

        LinearLayout desk = new LinearLayout(this);
        desk.setOrientation(LinearLayout.VERTICAL);
        desk.setBackgroundColor(WALL);
        desk.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView logo = new TextView(this);
        logo.setText("PC MODE");
        logo.setTextColor(WHITE);
        logo.setTextSize(28);
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(0, dp(30), 0, dp(20));
        desk.addView(logo);

        TextView sub = new TextView(this);
        sub.setText("Tap ⊞ START to open apps");
        sub.setTextColor(0xCCFFFFFF);
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        desk.addView(sub);

        content.addView(desk, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ APP ROUTER ═══════════

    void openApp(String name) {
        if (name.equals("Terminal")) showTerminal();
        else if (name.equals("Browser")) showBrowser();
        else if (name.equals("Files")) showFiles();
        else if (name.equals("Notes")) showNotes();
        else if (name.equals("Calculator")) showCalculator();
        else if (name.equals("Settings")) showSettings();
        else if (name.equals("About")) showAbout();
    }

    View makeHeader(String title) {
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setBackgroundColor(ACCENT);
        head.setPadding(dp(10), dp(8), dp(10), dp(8));
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(WHITE);
        t.setTextSize(14);
        head.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1));

        Button close = new Button(this);
        close.setText("✕");
        close.setTextColor(WHITE);
        close.setTextSize(12);
        close.setBackgroundColor(RED);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showDesktop(); }
        });
        head.addView(close, new LinearLayout.LayoutParams(dp(50), dp(36)));

        return head;
    }

    // ═══════════ TERMINAL ═══════════

    void showTerminal() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(0xFF000000);

        v.addView(makeHeader("Terminal"));

        final TextView out = new TextView(this);
        out.setTextColor(0xFF00FF00);
        out.setTextSize(11);
        out.setTypeface(android.graphics.Typeface.MONOSPACE);
        out.setText("Android Shell [v1.0]\nType 'help' for commands\n\n");

        ScrollView sv = new ScrollView(this);
        sv.addView(out);
        v.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1));

        final EditText input = new EditText(this);
        input.setTextColor(0xFF00FF00);
        input.setHint("type command...");
        input.setHintTextColor(0xFF008800);
        input.setBackgroundColor(0xFF001100);
        input.setTypeface(android.graphics.Typeface.MONOSPACE);
        input.setTextSize(12);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        v.addView(input, new LinearLayout.LayoutParams(MATCH, WRAP));

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView tv, int action, android.view.KeyEvent e) {
                String cmd = input.getText().toString().trim();
                input.setText("");
                out.append("$ " + cmd + "\n");
                String result = runShell(cmd);
                if (result == null || result.isEmpty()) {
                    out.append("(no output)\n\n");
                } else {
                    out.append(result + "\n");
                }
                return true;
            }
        });

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    String runShell(String cmd) {
        if (cmd.isEmpty()) return "";
        if (cmd.equals("help")) {
            return "Commands:\n  ls       - list files\n  pwd      - current dir\n  date     - date\n  whoami   - user\n  uname    - kernel\n  ps       - processes\n  echo X   - print X\n  df       - disk usage\n  top      - tasks\n  reboot   - reboot (simulated)\n";
        }
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = er.readLine()) != null) sb.append("[err] ").append(line).append("\n");
            p.waitFor();
            return sb.toString();
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // ═══════════ BROWSER ═══════════

    void showBrowser() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(WIN_BG);

        v.addView(makeHeader("Browser"));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(0xFF2A2A2A);
        bar.setPadding(dp(6), dp(6), dp(6), dp(6));

        final EditText url = new EditText(this);
        url.setText("https://www.google.com");
        url.setTextColor(WHITE);
        url.setBackgroundColor(0xFF1A1A1A);
        url.setTextSize(12);
        url.setSingleLine(true);
        url.setPadding(dp(10), dp(8), dp(10), dp(8));
        bar.addView(url, new LinearLayout.LayoutParams(0, WRAP, 1));

        Button go = new Button(this);
        go.setText("GO");
        go.setBackgroundColor(ACCENT);
        go.setTextColor(WHITE);
        bar.addView(go, new LinearLayout.LayoutParams(dp(60), WRAP));

        v.addView(bar);

        final WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("https://www.google.com");
        v.addView(web, new LinearLayout.LayoutParams(MATCH, 0, 1));

        go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View x) {
                String u = url.getText().toString().trim();
                if (!u.startsWith("http")) u = "https://" + u;
                web.loadUrl(u);
            }
        });

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ FILES ═══════════

    void showFiles() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(WIN_BG);

        v.addView(makeHeader("File Manager - /sdcard"));

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        java.io.File dir = new java.io.File("/sdcard");
        java.io.File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            TextView t = new TextView(this);
            t.setText("Storage permission chahiye\n\nSettings > Apps > PC Mode > Permissions > Files -> Allow");
            t.setTextColor(WHITE);
            t.setTextSize(12);
            t.setPadding(dp(12), dp(12), dp(12), dp(12));
            list.addView(t);
        } else {
            for (java.io.File f : files) {
                if (f.getName().startsWith(".")) continue;
                Button b = new Button(this);
                String icon = f.isDirectory() ? "📁 " : "📄 ";
                b.setText(icon + f.getName() + "  (" + (f.length()/1024) + " KB)");
                b.setTextColor(WHITE);
                b.setTextSize(12);
                b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                b.setBackgroundColor(0xFF2A2A2A);
                b.setPadding(dp(12), dp(10), dp(12), dp(10));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
                lp.setMargins(0, dp(2), 0, dp(2));
                b.setLayoutParams(lp);
                list.addView(b);
            }
        }

        sv.addView(list);
        v.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1));

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ NOTES ═══════════

    void showNotes() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(WIN_BG);

        v.addView(makeHeader("Notes"));

        final EditText et = new EditText(this);
        et.setText(prefs.getString("notes", ""));
        et.setTextColor(WHITE);
        et.setBackgroundColor(0xFF1A1A1A);
        et.setGravity(Gravity.TOP | Gravity.LEFT);
        et.setPadding(dp(12), dp(12), dp(12), dp(12));
        et.setTextSize(14);
        v.addView(et, new LinearLayout.LayoutParams(MATCH, 0, 1));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));

        Button save = new Button(this);
        save.setText("SAVE");
        save.setBackgroundColor(GREEN);
        save.setTextColor(WHITE);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View x) {
                prefs.edit().putString("notes", et.getText().toString()).apply();
                toast("Saved!");
            }
        });
        row.addView(save, new LinearLayout.LayoutParams(0, WRAP, 1));

        Button clr = new Button(this);
        clr.setText("CLEAR");
        clr.setBackgroundColor(RED);
        clr.setTextColor(WHITE);
        clr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View x) { et.setText(""); }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, WRAP, 1);
        lp.leftMargin = dp(8);
        row.addView(clr, lp);

        v.addView(row);

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ CALCULATOR ═══════════

    void showCalculator() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(WIN_BG);

        v.addView(makeHeader("Calculator"));

        final TextView display = new TextView(this);
        display.setText("0");
        display.setTextColor(WHITE);
        display.setTextSize(32);
        display.setBackgroundColor(0xFF000000);
        display.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        display.setPadding(dp(16), dp(20), dp(16), dp(20));
        v.addView(display, new LinearLayout.LayoutParams(MATCH, dp(100)));

        final double[] prev = {0};
        final char[] op = {0};
        final boolean[] hasPrev = {false};
        final boolean[] newNum = {true};

        String[][] keys = {
            {"C", "+", "-", "/"},
            {"7", "8", "9", "*"},
            {"4", "5", "6", "="},
            {"1", "2", "3", "0"}
        };

        for (String[] rowK : keys) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(MATCH, 0, 1);
            rlp.setMargins(dp(4), dp(4), dp(4), dp(4));
            row.setLayoutParams(rlp);

            for (final String k : rowK) {
                Button b = new Button(this);
                b.setText(k);
                b.setTextColor(WHITE);
                b.setTextSize(20);
                if (k.equals("=")) b.setBackgroundColor(ACCENT);
                else if (k.equals("C")) b.setBackgroundColor(RED);
                else if ("+-*/".contains(k)) b.setBackgroundColor(0xFF3D3D3D);
                else b.setBackgroundColor(0xFF2A2A2A);

                b.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View x) {
                        if (k.equals("C")) {
                            display.setText("0");
                            prev[0] = 0; op[0] = 0; hasPrev[0] = false; newNum[0] = true;
                            return;
                        }

                        if (k.equals("=")) {
                            if (!hasPrev[0]) return;
                            double cur = Double.parseDouble(display.getText().toString());
                            double r = 0;
                            if (op[0] == '+') r = prev[0] + cur;
                            else if (op[0] == '-') r = prev[0] - cur;
                            else if (op[0] == '*') r = prev[0] * cur;
                            else if (op[0] == '/') {
                                if (cur == 0) {
                                    display.setText("Error");
                                    hasPrev[0] = false;
                                    newNum[0] = true;
                                    return;
                                }
                                r = prev[0] / cur;
                            }
                            String s = String.valueOf(Math.round(r * 1000000.0) / 1000000.0);
                            if (s.endsWith(".0")) s = s.replace(".0", "");
                            display.setText(s);
                            hasPrev[0] = false;
                            newNum[0] = true;
                            return;
                        }

                        if ("+-*/".contains(k)) {
                            if (!hasPrev[0]) {
                                prev[0] = Double.parseDouble(display.getText().toString());
                                op[0] = k.charAt(0);
                                hasPrev[0] = true;
                                newNum[0] = true;
                                return;
                            }

                            double cur = Double.parseDouble(display.getText().toString());
                            double r = 0;
                            if (op[0] == '+') r = prev[0] + cur;
                            else if (op[0] == '-') r = prev[0] - cur;
                            else if (op[0] == '*') r = prev[0] * cur;
                            else if (op[0] == '/') r = (cur == 0) ? 0 : prev[0] / cur;

                            String s = String.valueOf(Math.round(r * 1000000.0) / 1000000.0);
                            if (s.endsWith(".0")) s = s.replace(".0", "");
                            display.setText(s);
                            prev[0] = r;
                            op[0] = k.charAt(0);
                            hasPrev[0] = true;
                            newNum[0] = true;
                            return;
                        }

                        if (newNum[0] || display.getText().toString().equals("0")) {
                            display.setText(k);
                            newNum[0] = false;
                        } else {
                            display.append(k);
                        }
                    }
                });

                row.addView(b, new LinearLayout.LayoutParams(0, MATCH, 1));
            }

            v.addView(row);
        }

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ SETTINGS ═══════════

    void showSettings() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(WIN_BG);

        v.addView(makeHeader("Settings"));

        TextView info = new TextView(this);
        info.setText("PC Mode\n\nDark theme enabled\nFiles access: /sdcard\nNotes are saved locally\n");
        info.setTextColor(WHITE);
        info.setTextSize(13);
        info.setPadding(dp(16), dp(16), dp(16), dp(16));
        v.addView(info, new LinearLayout.LayoutParams(MATCH, WRAP));

        Button resetNotes = new Button(this);
        resetNotes.setText("RESET NOTES");
        resetNotes.setBackgroundColor(RED);
        resetNotes.setTextColor(WHITE);
        resetNotes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View x) {
                prefs.edit().remove("notes").apply();
                toast("Notes reset");
            }
        });
        v.addView(resetNotes, new LinearLayout.LayoutParams(MATCH, WRAP));

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ ABOUT ═══════════

    void showAbout() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(WIN_BG);

        v.addView(makeHeader("About"));

        TextView info = new TextView(this);
        info.setText("PC Mode\nVersion 1.0\n\nA lightweight desktop-like launcher for Android.\n\nCreated with Java + Android UI components.");
        info.setTextColor(WHITE);
        info.setTextSize(14);
        info.setPadding(dp(16), dp(16), dp(16), dp(16));
        v.addView(info, new LinearLayout.LayoutParams(MATCH, WRAP));

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }
}
