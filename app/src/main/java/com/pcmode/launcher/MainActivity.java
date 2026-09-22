package com.pcmode.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import java.util.Locale;

public class MainActivity extends Activity {

    static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    // Premium Microsoft Dark Theme
    static final int BG_MAIN = 0xFF1E1E1E;
    static final int BG_MENU = 0xFF2D2D2D;
    static final int BG_INPUT = 0xFF252526;
    static final int TEXT_MAIN = 0xFFFFFFFF;
    static final int TEXT_MUTED = 0xFFAAAAAA;
    static final int ACCENT = 0xFF0078D4;
    static final int BORDER = 0xFF3C3C3C;
    static final int RED = 0xFFE53935;
    static final int GREEN = 0xFF4CAF50;

    FrameLayout root, content;
    LinearLayout taskbar, startMenu;
    TextView clockView;
    boolean startOpen = false;
    SharedPreferences prefs;
    Handler handler = new Handler(Looper.getMainLooper());
    String desktopWall = "0xFF0E4C8A";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs = getSharedPreferences("pcmode", MODE_PRIVATE);
        desktopWall = prefs.getString("wall", "0xFF0E4C8A");
        buildRoot();
        showDesktop();
        startClock();
    }

    int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    // ═══════════ ROOT ═══════════
    void buildRoot() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0E4C8A"));

        content = new FrameLayout(this);
        root.addView(content, new FrameLayout.LayoutParams(MATCH, MATCH));

        taskbar = new LinearLayout(this);
        taskbar.setOrientation(LinearLayout.HORIZONTAL);
        taskbar.setBackgroundColor(0xFF000000);
        taskbar.setPadding(dp(6), dp(4), dp(6), dp(4));
        taskbar.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(MATCH, dp(52));
        tp.gravity = Gravity.BOTTOM;
        root.addView(taskbar, tp);

        Button start = new Button(this);
        start.setText("⊞  START");
        start.setTextColor(TEXT_MAIN);
        start.setTextSize(13);
        start.setBackgroundColor(ACCENT);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { toggleStart(); }
        });
        taskbar.addView(start, new LinearLayout.LayoutParams(dp(115), dp(44)));

        View sp = new View(this);
        taskbar.addView(sp, new LinearLayout.LayoutParams(0, dp(1), 1));

        clockView = new TextView(this);
        clockView.setTextColor(TEXT_MAIN);
        clockView.setTextSize(12);
        clockView.setGravity(Gravity.CENTER);
        clockView.setPadding(dp(8), 0, dp(8), 0);
        taskbar.addView(clockView, new LinearLayout.LayoutParams(WRAP, dp(44)));

        Button exit = new Button(this);
        exit.setText("EXIT");
        exit.setTextColor(TEXT_MAIN);
        exit.setTextSize(11);
        exit.setBackgroundColor(RED);
        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        taskbar.addView(exit, new LinearLayout.LayoutParams(dp(75), dp(44)));

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
        startMenu.setBackgroundColor(0xF01F1F1F);
        startMenu.setPadding(dp(10), dp(10), dp(10), dp(10));

        TextView title = new TextView(this);
        title.setText("PC Mode");
        title.setTextColor(TEXT_MAIN);
        title.setTextSize(14);
        title.setPadding(dp(8), dp(4), dp(8), dp(10));
        startMenu.addView(title);

        String[][] items = {
            {"Terminal", ">_"},
            {"Browser", "🌐"},
            {"Files", "📁"},
            {"Notepad", "📝"},
            {"Calculator", "🔢"},
            {"Settings", "⚙"},
            {"About", "ℹ"}
        };

        for (String[] item : items) {
            final String name = item[0];
            Button b = new Button(this);
            b.setText(item[1] + "   " + name);
            b.setTextColor(TEXT_MAIN);
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
        lp.bottomMargin = dp(56);
        lp.leftMargin = dp(6);
        root.addView(startMenu, lp);
    }

    // ═══════════ DESKTOP ═══════════
    void showDesktop() {
        content.removeAllViews();

        LinearLayout desk = new LinearLayout(this);
        desk.setOrientation(LinearLayout.VERTICAL);
        desk.setBackgroundColor(Color.parseColor("#0E4C8A"));
        desk.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView logo = new TextView(this);
        logo.setText("PC MODE");
        logo.setTextColor(TEXT_MAIN);
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

    // ═══════════ HEADER ═══════════
    View makeHeader(String title) {
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setBackgroundColor(BG_MENU);
        head.setPadding(dp(10), dp(8), dp(10), dp(8));
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(TEXT_MAIN);
        t.setTextSize(14);
        t.setTypeface(null, Typeface.BOLD);
        head.addView(t, new LinearLayout.LayoutParams(0, WRAP, 1));

        Button close = new Button(this);
        close.setText("✕");
        close.setTextColor(TEXT_MAIN);
        close.setTextSize(12);
        close.setBackgroundColor(RED);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { showDesktop(); }
        });
        head.addView(close, new LinearLayout.LayoutParams(dp(50), dp(36)));

        return head;
    }

    // ═══════════ APP ROUTER ═══════════
    void openApp(String name) {
        if (name.equals("Terminal")) showTerminal();
        else if (name.equals("Browser")) showBrowser();
        else if (name.equals("Files")) showFiles();
        else if (name.equals("Notepad")) showNotepad();
        else if (name.equals("Calculator")) showCalculator();
        else if (name.equals("Settings")) showSettings();
        else if (name.equals("About")) showAbout();
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
        out.setTypeface(Typeface.MONOSPACE);
        out.setText("Android Shell [v1.0]\nType 'help' for commands\n\n");

        ScrollView sv = new ScrollView(this);
        sv.addView(out);
        v.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1));

        final EditText input = new EditText(this);
        input.setTextColor(0xFF00FF00);
        input.setHint("type command...");
        input.setHintTextColor(0xFF008800);
        input.setBackgroundColor(0xFF001100);
        input.setTypeface(Typeface.MONOSPACE);
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
                if (result == null || result.isEmpty()) out.append("(no output)\n\n");
                else out.append(result + "\n");
                return true;
            }
        });

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    String runShell(String cmd) {
        if (cmd.isEmpty()) return "";
        if (cmd.equals("help")) return "Commands:\n  ls, pwd, date, whoami, uname, ps, df, echo X, clear";
        if (cmd.equals("clear")) return "\n\n\n\n\n";
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

    // ═══════════ NOTEPAD (FIXED - DARK BG, WHITE TEXT) ═══════════
    void showNotepad() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(BG_MAIN);

        v.addView(makeHeader("Notepad"));

        // Menu bar
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.setBackgroundColor(BG_MENU);
        menu.setPadding(dp(6), dp(6), dp(6), dp(6));

        Button save = new Button(this);
        save.setText("💾 SAVE");
        save.setTextColor(TEXT_MAIN);
        save.setTextSize(11);
        save.setBackgroundColor(ACCENT);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View x) {
                // Save happens via reference below
            }
        });
        menu.addView(save, new LinearLayout.LayoutParams(0, WRAP, 1));

        Button clr = new Button(this);
        clr.setText("🗑 CLEAR");
        clr.setTextColor(TEXT_MAIN);
        clr.setTextSize(11);
        clr.setBackgroundColor(RED);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, WRAP, 1);
        cp.leftMargin = dp(6);
        menu.addView(clr, cp);

        v.addView(menu);

        // Editor — DARK background, WHITE text
        final EditText et = new EditText(this);
        et.setText(prefs.getString("notes", ""));
        et.setTextColor(TEXT_MAIN);
        et.setHintTextColor(TEXT_MUTED);
        et.setHint("Start typing...");
        et.setBackgroundColor(BG_INPUT);
        et.setGravity(Gravity.TOP | Gravity.LEFT);
        et.setPadding(dp(12), dp(12), dp(12), dp(12));
        et.setTextSize(14);
        et.setTypeface(Typeface.MONOSPACE);
        v.addView(et, new LinearLayout.LayoutParams(MATCH, 0, 1));

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View x) {
                prefs.edit().putString("notes", et.getText().toString()).apply();
                toast("Saved!");
            }
        });
        clr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View x) { et.setText(""); }
        });

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ BROWSER ═══════════
    void showBrowser() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(BG_MAIN);

        v.addView(makeHeader("Browser"));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(BG_MENU);
        bar.setPadding(dp(6), dp(6), dp(6), dp(6));

        final EditText url = new EditText(this);
        url.setText("https://www.google.com");
        url.setTextColor(TEXT_MAIN);
        url.setBackgroundColor(BG_INPUT);
        url.setTextSize(12);
        url.setSingleLine(true);
        url.setPadding(dp(10), dp(8), dp(10), dp(8));
        bar.addView(url, new LinearLayout.LayoutParams(0, WRAP, 1));

        Button go = new Button(this);
        go.setText("GO");
        go.setBackgroundColor(ACCENT);
        go.setTextColor(TEXT_MAIN);
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
        v.setBackgroundColor(BG_MAIN);

        v.addView(makeHeader("File Manager — /sdcard"));

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(8), dp(8), dp(8), dp(8));

        java.io.File dir = new java.io.File("/sdcard");
        java.io.File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            TextView t = new TextView(this);
            t.setText("Storage permission chahiye.\n\nSettings > Apps > PC Mode > Permissions > Files -> Allow");
            t.setTextColor(TEXT_MAIN);
            t.setTextSize(12);
            t.setPadding(dp(12), dp(12), dp(12), dp(12));
            list.addView(t);
        } else {
            for (java.io.File f : files) {
                if (f.getName().startsWith(".")) continue;
                TextView tv = new TextView(this);
                String icon = f.isDirectory() ? "📁  " : "📄  ";
                tv.setText(icon + f.getName() + "\n" + (f.length()/1024) + " KB");
                tv.setTextColor(TEXT_MAIN);
                tv.setTextSize(12);
                tv.setBackgroundColor(0xFF2A2A2A);
                tv.setPadding(dp(12), dp(10), dp(12), dp(10));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
                lp.setMargins(0, dp(3), 0, dp(3));
                tv.setLayoutParams(lp);
                list.addView(tv);
            }
        }

        sv.addView(list);
        v.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1));

        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    // ═══════════ CALCULATOR (FIXED - NO CRASH) ═══════════
    void showCalculator() {
        content.removeAllViews();

        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(0xFF000000);

        v.addView(makeHeader("Calculator"));

        final TextView display = new TextView(this);
        display.setText("0");
        display.setTextColor(TEXT_MAIN);
        display.setTextSize(36);
        display.setBackgroundColor(0xFF000000);
        display.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        display.setPadding(dp(20), dp(24), dp(20), dp(24));
        v.addView(display, new LinearLayout.LayoutParams(MATCH, dp(110)));

        final double[] prev = {0};
        final char[] op = {0};
        final boolean[] hasPrev = {false};
        final boolean[] newNum = {true};

        String[][] keys = {
            {"C", "÷", "×", "⌫"},
            {"7", "8", "9", "-"},
            {"4", "5", "6", "+"},
            {"1", "2", "3", "="},
            {"0", ".", "", ""}
        };

        for (String[] rowK : keys) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(MATCH, 0, 1);
            rlp.setMargins(dp(4), dp(4), dp(4), dp(4));
            row.setLayoutParams(rlp);

            for (final String k : rowK) {
                if (k.isEmpty()) {
                    View sp = new View(this);
                    row.addView(sp, new LinearLayout.LayoutParams(0, MATCH, 1));
                    continue;
                }
                Button b = new Button(this);
                b.setText(k);
                b.setTextColor(TEXT_MAIN);
                b.setTextSize(22);
                if (k.equals("=")) b.setBackgroundColor(ACCENT);
                else if (k.equals("C")) b.setBackgroundColor(RED);
                else if ("÷×-+".contains(k)) b.setBackgroundColor(0xFF3D3D3D);
                else b.setBackgroundColor(0xFF2A2A2A);

                b.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View x)
