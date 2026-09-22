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
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    static final int DESKTOP = Color.rgb(15, 48, 82);
    static final int TASKBAR = Color.rgb(24, 24, 27);
    static final int PANEL = Color.rgb(38, 38, 42);
    static final int PANEL_2 = Color.rgb(48, 48, 53);
    static final int TITLE = Color.rgb(35, 35, 39);
    static final int TITLE_ACTIVE = Color.rgb(55, 55, 61);
    static final int TEXT = Color.WHITE;
    static final int MUTED = Color.rgb(190, 190, 195);
    static final int ACCENT = Color.rgb(0, 120, 212);
    static final int CLOSE = Color.rgb(196, 43, 28);

    FrameLayout root;
    FrameLayout desktop;
    LinearLayout taskbar;
    LinearLayout taskbarApps;
    LinearLayout startMenu;
    LinearLayout workspaceBar;
    EditText startSearch;
    TextView clock;
    TextView date;
    SharedPreferences prefs;
    Handler handler = new Handler(Looper.getMainLooper());

    boolean startOpen = false;
    final List<View> windows = new ArrayList<>();
    final List<String> windowNames = new ArrayList<>();

    int currentWorkspace = 0;
    final List<List<View>> workspaceWindows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestFullscreen();
        prefs = getSharedPreferences("winpcmode", MODE_PRIVATE);
        for (int i = 0; i < 4; i++) workspaceWindows.add(new ArrayList<>());
        buildDesktop();
        addDesktopIcons();
        startClock();
    }

    void requestFullscreen() {
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView label(String text, float size, int color) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    Button flatButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(TEXT);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        return b;
    }

    void buildDesktop() {
        root = new FrameLayout(this);
        root.setBackgroundColor(DESKTOP);

        desktop = new FrameLayout(this);
        desktop.setBackgroundColor(DESKTOP);
        root.addView(desktop, new FrameLayout.LayoutParams(MATCH, MATCH));

        View glow1 = new View(this);
        glow1.setBackgroundColor(Color.rgb(21, 69, 112));
        FrameLayout.LayoutParams gp = new FrameLayout.LayoutParams(dp(420), dp(420));
        gp.leftMargin = dp(-160);
        gp.topMargin = dp(80);
        desktop.addView(glow1, gp);

        taskbar = new LinearLayout(this);
        taskbar.setOrientation(LinearLayout.HORIZONTAL);
        taskbar.setGravity(Gravity.CENTER_VERTICAL);
        taskbar.setPadding(dp(6), dp(4), dp(6), dp(4));
        taskbar.setBackgroundColor(TASKBAR);

        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(MATCH, dp(54));
        tp.gravity = Gravity.BOTTOM;
        root.addView(taskbar, tp);

        Button start = flatButton("⊞");
        start.setTextSize(25);
        start.setOnClickListener(v -> toggleStart());
        taskbar.addView(start, new LinearLayout.LayoutParams(dp(48), dp(46)));

        Button search = flatButton("🔎");
        search.setTextSize(17);
        search.setOnClickListener(v -> {
            showStart();
            if (startSearch != null) startSearch.requestFocus();
        });
        taskbar.addView(search, new LinearLayout.LayoutParams(dp(46), dp(46)));

        taskbarApps = new LinearLayout(this);
        taskbarApps.setOrientation(LinearLayout.HORIZONTAL);
        taskbarApps.setGravity(Gravity.CENTER_VERTICAL);

        ScrollView taskScroll = new ScrollView(this);
        taskScroll.setHorizontalScrollBarEnabled(false);
        taskScroll.setFillViewport(false);
        taskScroll.addView(taskbarApps);
        taskbar.addView(taskScroll, new LinearLayout.LayoutParams(0, dp(46), 1));

        workspaceBar = new LinearLayout(this);
        workspaceBar.setOrientation(LinearLayout.HORIZONTAL);
        workspaceBar.setGravity(Gravity.CENTER_VERTICAL);
        for (int i = 0; i < 4; i++) {
            final int wi = i;
            Button wb = flatButton(String.valueOf(i + 1));
            wb.setTextSize(11);
            wb.setBackgroundColor(i == 0 ? ACCENT : PANEL_2);
            wb.setTag("ws_" + i);
            wb.setOnClickListener(v -> switchWorkspace(wi));
            workspaceBar.addView(wb, new LinearLayout.LayoutParams(dp(30), dp(40)));
        }
        taskbar.addView(workspaceBar, new LinearLayout.LayoutParams(WRAP, dp(46)));

        LinearLayout tray = new LinearLayout(this);
        tray.setOrientation(LinearLayout.VERTICAL);
        tray.setGravity(Gravity.CENTER);
        clock = label("--:--", 11, TEXT);
        clock.setGravity(Gravity.CENTER);
        date = label("--/--/----", 9, MUTED);
        date.setGravity(Gravity.CENTER);
        tray.addView(clock, new LinearLayout.LayoutParams(WRAP, dp(19)));
        tray.addView(date, new LinearLayout.LayoutParams(WRAP, dp(17)));
        taskbar.addView(tray, new LinearLayout.LayoutParams(dp(86), dp(46)));

        setContentView(root);
    }

    void switchWorkspace(int idx) {
        currentWorkspace = idx;
        for (int i = 0; i < workspaceWindows.size(); i++) {
            for (View w : workspaceWindows.get(i)) {
                w.setVisibility(i == idx ? View.VISIBLE : View.GONE);
            }
        }
        for (int i = 0; i < workspaceBar.getChildCount(); i++) {
            View v = workspaceBar.getChildAt(i);
            if (v instanceof Button) {
                Button b = (Button) v;
                int wi = Integer.parseInt(b.getTag().toString().substring(3));
                b.setBackgroundColor(wi == idx ? ACCENT : PANEL_2);
            }
        }
        toast("Workspace " + (idx + 1));
    }

    void startClock() {
        handler.post(new Runnable() {
            @Override public void run() {
                Date now = new Date();
                clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
                date.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now));
                handler.postDelayed(this, 1000);
            }
        });
    }

    void addDesktopIcons() {
        String[][] apps = {
                {"This PC", "🖥"},
                {"Files", "📁"},
                {"Browser", "🌐"},
                {"Notepad", "📝"},
                {"Terminal", ">_"},
                {"Calculator", "🔢"},
                {"Settings", "⚙"},
                {"About", "ⓘ"}
        };

        int col = 0, row = 0;
        for (String[] a : apps) {
            final String name = a[0];

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(5), dp(5), dp(5), dp(5));

            TextView icon = label(a[1], 34, TEXT);
            icon.setGravity(Gravity.CENTER);
            item.addView(icon, new LinearLayout.LayoutParams(MATCH, dp(50)));

            TextView nameView = label(name, 10, TEXT);
            nameView.setGravity(Gravity.CENTER);
            item.addView(nameView, new LinearLayout.LayoutParams(MATCH, dp(28)));

            item.setOnClickListener(v -> openApp(name));

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(92), dp(84));
            lp.leftMargin = dp(10 + col * 98);
            lp.topMargin = dp(12 + row * 92);
            desktop.addView(item, lp);

            col++;
            if (col >= 5) {
                col = 0;
                row++;
            }
        }
    }

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
    }void buildStartMenu() {
    startMenu = new LinearLayout(this);
    startMenu.setOrientation(LinearLayout.VERTICAL);
    startMenu.setPadding(dp(16), dp(16), dp(16), dp(14));
    startMenu.setBackgroundColor(Color.rgb(31, 31, 35));

    TextView title = label("Start", 20, TEXT);
    title.setTypeface(Typeface.DEFAULT_BOLD);
    startMenu.addView(title, new LinearLayout.LayoutParams(MATCH, dp(36)));

    startSearch = new EditText(this);
    startSearch.setHint("Search apps");
    startSearch.setHintTextColor(MUTED);
    startSearch.setTextColor(TEXT);
    startSearch.setSingleLine(true);
    startSearch.setTextSize(12);
    startSearch.setPadding(dp(12), 0, dp(12), 0);
    startSearch.setBackgroundColor(PANEL_2);
    startMenu.addView(startSearch, new LinearLayout.LayoutParams(MATCH, dp(42)));

    TextView recommended = label("Pinned", 13, TEXT);
    recommended.setTypeface(Typeface.DEFAULT_BOLD);
    recommended.setPadding(0, dp(14), 0, dp(6));
    startMenu.addView(recommended);

    String[][] apps = {
            {"This PC", "🖥"}, {"Files", "📁"}, {"Browser", "🌐"},
            {"Notepad", "📝"}, {"Terminal", ">_"}, {"Calculator", "🔢"},
            {"Settings", "⚙"}, {"About", "ⓘ"}
    };

    LinearLayout grid = new LinearLayout(this);
    grid.setOrientation(LinearLayout.VERTICAL);

    for (int i = 0; i < apps.length; i += 2) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (int j = i; j < Math.min(i + 2, apps.length); j++) {
            final String name = apps[j][0];
            Button b = flatButton(apps[j][1] + "  " + name);
            b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            b.setBackgroundColor(PANEL_2);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(44), 1);
            bp.setMargins(dp(3), dp(3), dp(3), dp(3));
            row.addView(b, bp);
            b.setOnClickListener(v -> {
                hideStart();
                openApp(name);
            });
        }
        grid.addView(row, new LinearLayout.LayoutParams(MATCH, dp(50)));
    }

    startMenu.addView(grid, new LinearLayout.LayoutParams(MATCH, dp(208)));

    LinearLayout footer = new LinearLayout(this);
    footer.setGravity(Gravity.CENTER_VERTICAL);
    TextView user = label("PC Mode", 11, MUTED);
    footer.addView(user, new LinearLayout.LayoutParams(0, dp(40), 1));

    Button power = flatButton("⏻");
    power.setTextSize(18);
    power.setOnClickListener(v -> finish());
    footer.addView(power, new LinearLayout.LayoutParams(dp(48), dp(40)));
    startMenu.addView(footer);

    startSearch.setOnEditorActionListener((v, action, event) -> {
        String q = startSearch.getText().toString().trim().toLowerCase(Locale.getDefault());
        if (!q.isEmpty()) {
            for (String[] a : apps) {
                if (a[0].toLowerCase(Locale.getDefault()).contains(q)) {
                    hideStart();
                    openApp(a[0]);
                    return true;
                }
            }
        }
        return false;
    });

    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(430), dp(400));
    lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
    lp.leftMargin = dp(8);
    lp.bottomMargin = dp(60);
    root.addView(startMenu, lp);
    startMenu.setVisibility(View.GONE);
}

void openApp(String name) {
    if ("This PC".equals(name)) {
        createWindow(name, buildThisPC(), dp(600), dp(420));
    } else if ("Terminal".equals(name)) {
        createWindow(name, buildTerminal(), dp(600), dp(430));
    } else if ("Browser".equals(name)) {
        createWindow(name, buildBrowser(), dp(700), dp(500));
    } else if ("Files".equals(name)) {
        createWindow(name, buildFiles(), dp(560), dp(430));
    } else if ("Notepad".equals(name)) {
        createWindow(name, buildNotepad(), dp(560), dp(430));
    } else if ("Calculator".equals(name)) {
        createWindow(name, buildCalculator(), dp(340), dp(390));
    } else if ("Settings".equals(name)) {
        createWindow(name, buildSettings(), dp(520), dp(430));
    } else if ("About".equals(name)) {
        createWindow(name, buildAbout(), dp(420), dp(340));
    }
}

void createWindow(final String title, View content, int width, int height) {
    final LinearLayout win = new LinearLayout(this);
    win.setOrientation(LinearLayout.VERTICAL);
    win.setBackgroundColor(PANEL);
    win.setElevation(dp(8));

    final LinearLayout titleBar = new LinearLayout(this);
    titleBar.setGravity(Gravity.CENTER_VERTICAL);
    titleBar.setPadding(dp(10), 0, dp(5), 0);
    titleBar.setBackgroundColor(TITLE_ACTIVE);

    TextView icon = label("▣", 13, TEXT);
    titleBar.addView(icon, new LinearLayout.LayoutParams(dp(28), dp(38)));

    TextView titleView = label(title, 12, TEXT);
    titleView.setTypeface(Typeface.DEFAULT_BOLD);
    titleBar.addView(titleView, new LinearLayout.LayoutParams(0, dp(38), 1));

    Button min = flatButton("—");
    Button max = flatButton("□");
    Button close = flatButton("×");
    close.setTextSize(19);

    titleBar.addView(min, new LinearLayout.LayoutParams(dp(42), dp(38)));
    titleBar.addView(max, new LinearLayout.LayoutParams(dp(42), dp(38)));
    titleBar.addView(close, new LinearLayout.LayoutParams(dp(42), dp(38)));

    win.addView(titleBar, new LinearLayout.LayoutParams(MATCH, dp(38)));
    win.addView(content, new LinearLayout.LayoutParams(MATCH, 0, 1));

    final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
    int n = windows.size();
    lp.leftMargin = dp(35 + (n % 4) * 24);
    lp.topMargin = dp(25 + (n % 4) * 20);
    desktop.addView(win, lp);
    windows.add(win);
    windowNames.add(title);
    workspaceWindows.get(currentWorkspace).add(win);

    final int[] old = {width, height};
    final boolean[] maximized = {false};

    min.setOnClickListener(v -> win.setVisibility(View.GONE));

    max.setOnClickListener(v -> {
        if (!maximized[0]) {
            old[0] = win.getWidth();
            old[1] = win.getHeight();
            lp.leftMargin = 0;
            lp.topMargin = 0;
            lp.width = MATCH;
            lp.height = MATCH;
            win.setLayoutParams(lp);
            max.setText("❐");
            maximized[0] = true;
        } else {
            lp.width = old[0];
            lp.height = old[1];
            lp.leftMargin = dp(35);
            lp.topMargin = dp(25);
            win.setLayoutParams(lp);
            max.setText("□");
            maximized[0] = false;
        }
        win.bringToFront();
    });

    close.setOnClickListener(v -> {
        desktop.removeView(win);
        int idx = windows.indexOf(win);
        if (idx >= 0) {
            windows.remove(idx);
            windowNames.remove(idx);
        }
        for (List<View> ws : workspaceWindows) ws.remove(win);
        removeTaskbarButton(win);
    });

    titleBar.setOnTouchListener(new View.OnTouchListener() {
        float downX, downY;
        @Override public boolean onTouch(View v, MotionEvent e) {
            if (maximized[0]) return true;
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = e.getRawX() - lp.leftMargin;
                    downY = e.getRawY() - lp.topMargin;
                    win.bringToFront();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int x = (int)(e.getRawX() - downX);
                    int y = (int)(e.getRawY() - downY);
                    x = Math.max(0, x);
                    y = Math.max(0, y);
                    int maxX = desktop.getWidth() - win.getWidth();
                    int maxY = desktop.getHeight() - dp(54) - win.getHeight();
                    if (maxX > 0) x = Math.min(x, maxX);
                    if (maxY > 0) y = Math.min(y, maxY);
                    lp.leftMargin = x;
                    lp.topMargin = y;
                486:  win.setLayoutParams(lp);
487:  return true;
     case MotionEvent.ACTION_UP:                           ← NAYA
         checkSnap(win, lp, old, maximized, max);          ← NAYA
         return true;                                      ← NAYA
488:  }
            return false;
        }
    });

    final boolean[] dragMode = {false};
    final float[] dragOffsetX = {0};
    final float[] dragOffsetY = {0};

    win.setOnLongClickListener(new View.OnLongClickListener() {
        @Override public boolean onLongClick(View v) {
            dragMode[0] = true;
            toast("Drag mode ON - move finger");
            return true;
        }
    });

    win.setOnTouchListener(new View.OnTouchListener() {
        @Override public boolean onTouch(View v, MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                dragOffsetX[0] = e.getRawX() - lp.leftMargin;
                dragOffsetY[0] = e.getRawY() - lp.topMargin;
            }
            if (dragMode[0]) {
                if (e.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    int nx = (int)(e.getRawX() - dragOffsetX[0]);
                    int ny = (int)(e.getRawY() - dragOffsetY[0]);
                    nx = Math.max(0, nx);
                    ny = Math.max(0, ny);
                    int maxX = desktop.getWidth() - win.getWidth();
                    int maxY = desktop.getHeight() - dp(54) - win.getHeight();
                    if (maxX > 0) nx = Math.min(nx, maxX);
                    if (maxY > 0) ny = Math.min(ny, maxY);
                    lp.leftMargin = nx;
                    lp.topMargin = ny;
                    win.setLayoutParams(lp);
                    return true;
                }
                if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                    dragMode[0] = false;
                    checkSnap(win, lp, old, maximized, max);
                    return true;
                }
            }
            return false;
        }
    });

    final View[] resizeHandle = new View[4];
    resizeHandle[0] = makeResizeHandle(win, lp, 0);
    resizeHandle[1] = makeResizeHandle(win, lp, 1);
    resizeHandle[2] = makeResizeHandle(win, lp, 2);
    resizeHandle[3] = makeResizeHandle(win, lp, 3);
    for (int ri = 0; ri < 4; ri++) {
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(dp(24), dp(24));
        switch (ri) {
            case 0: hp.gravity = Gravity.TOP | Gravity.LEFT; break;
            case 1: hp.gravity = Gravity.TOP | Gravity.RIGHT; break;
            case 2: hp.gravity = Gravity.BOTTOM | Gravity.LEFT; break;
            case 3: hp.gravity = Gravity.BOTTOM | Gravity.RIGHT; break;
        }
        win.addView(resizeHandle[ri], hp);
    }

    addTaskbarButton(title, win);
    win.bringToFront();
}

View makeResizeHandle(final View win, final FrameLayout.LayoutParams lp, final int corner) {
    View handle = new View(this);
    handle.setBackgroundColor(0x880078D4);
    final float[] startX = {0};
    final float[] startY = {0};
    final int[] startW = {0};
    final int[] startH = {0};
    final int[] startL = {0};
    final int[] startT = {0};

    handle.setOnTouchListener(new View.OnTouchListener() {
        @Override public boolean onTouch(View v, MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX[0] = e.getRawX();
                    startY[0] = e.getRawY();
                    startW[0] = win.getWidth();
                    startH[0] = win.getHeight();
                    startL[0] = lp.leftMargin;
                    startT[0] = lp.topMargin;
                    win.bringToFront();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int)(e.getRawX() - startX[0]);
                    int dy = (int)(e.getRawY() - startY[0]);
                    int minW = dp(200);
                    int minH = dp(150);
                    if (corner == 0) {
                        int nw = Math.max(minW, startW[0] - dx);
                        int nh = Math.max(minH, startH[0] - dy);
                        lp.width = nw;
                        lp.height = nh;
                        lp.leftMargin = startL[0] + (startW[0] - nw);
                        lp.topMargin = startT[0] + (startH[0] - nh);
                    } else if (corner == 1) {
                        lp.width = Math.max(minW, startW[0] + dx);
                        lp.height = Math.max(minH, startH[0] - dy);
                        lp.topMargin = startT[0] + (startH[0] - lp.height);
                    } else if (corner == 2) {
                        int nw = Math.max(minW, startW[0] - dx);
                        lp.width = nw;
                        lp.height = Math.max(minH, startH[0] + dy);
                        lp.leftMargin = startL[0] + (startW[0] - nw);
                    } else {
                        lp.width = Math.max(minW, startW[0] + dx);
                        lp.height = Math.max(minH, startH[0] + dy);
                    }
                    win.setLayoutParams(lp);
                    return true;
            }
            return false;
        }
    });
    return handle;
}    void addTaskbarButton(String title, View win) {
        Button b = flatButton(title);
        b.setBackgroundColor(PANEL_2);
        b.setTextSize(10);
        b.setTag(win);
        b.setOnClickListener(v -> {
            win.setVisibility(View.VISIBLE);
            win.bringToFront();
        });
        taskbarApps.addView(b, new LinearLayout.LayoutParams(dp(125), dp(40)));
    }

    void removeTaskbarButton(View win) {
        for (int i = 0; i < taskbarApps.getChildCount(); i++) {
            View c = taskbarApps.getChildAt(i);
            if (c.getTag() == win) {
                taskbarApps.removeViewAt(i);
                return;
            }
        }
    }

    View buildThisPC() {
        LinearLayout v = basePanel();
        TextView h = label("This PC", 18, TEXT);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        v.addView(h, new LinearLayout.LayoutParams(MATCH, dp(48)));

        String[] drives = {"📱  Internal storage", "💾  Downloads", "📷  DCIM", "📄  Documents"};
        for (String d : drives) {
            Button b = flatButton(d);
            b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            b.setBackgroundColor(PANEL_2);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(MATCH, dp(54));
            p.setMargins(0, dp(3), 0, dp(3));
            v.addView(b, p);
        }
        return v;
    }

    View buildTerminal() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(Color.BLACK);
        v.setPadding(dp(7), dp(7), dp(7), dp(7));

        final TextView out = label("Microsoft-style command shell\nType 'help' for commands.\n\n", 11, Color.rgb(100, 255, 100));
        out.setTypeface(Typeface.MONOSPACE);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(out);
        v.addView(scroll, new LinearLayout.LayoutParams(MATCH, 0, 1));

        final EditText input = new EditText(this);
        input.setTextColor(Color.rgb(100, 255, 100));
        input.setHintTextColor(Color.rgb(40, 120, 40));
        input.setHint("command...");
        input.setSingleLine(true);
        input.setTypeface(Typeface.MONOSPACE);
        input.setTextSize(12);
        input.setBackgroundColor(Color.rgb(8, 20, 8));
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        v.addView(input, new LinearLayout.LayoutParams(MATCH, dp(44)));

        input.setOnEditorActionListener((tv, action, event) -> {
            String cmd = input.getText().toString().trim();
            input.setText("");
            out.append("\n> " + cmd + "\n" + runShell(cmd) + "\n");
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            return true;
        });
        return v;
    }

    String runShell(String cmd) {
        if (cmd.isEmpty()) return "";
        if ("help".equals(cmd))
            return "help  cls  date  whoami  uname  pwd  ls  ps  df  echo TEXT";
        if ("cls".equals(cmd) || "clear".equals(cmd))
            return "\n\n\n\n\n\n\n";
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line).append('\n');
            while ((line = err.readLine()) != null) sb.append(line).append('\n');
            p.waitFor();
            return sb.length() == 0 ? "(no output)" : sb.toString();
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    View buildBrowser() {
        LinearLayout v = basePanel();
        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(5), dp(5), dp(5), dp(5));
        bar.setBackgroundColor(TITLE);

        final EditText url = new EditText(this);
        url.setText("https://www.google.com");
        url.setTextColor(TEXT);
        url.setSingleLine(true);
        url.setTextSize(12);
        url.setBackgroundColor(Color.rgb(24, 24, 27));
        bar.addView(url, new LinearLayout.LayoutParams(0, dp(42), 1));

        Button go = flatButton("Go");
        go.setBackgroundColor(ACCENT);
        bar.addView(go, new LinearLayout.LayoutParams(dp(60), dp(42)));
        v.addView(bar);

        final WebView web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("https://www.google.com");
        v.addView(web, new LinearLayout.LayoutParams(MATCH, 0, 1));

        go.setOnClickListener(x -> {
            String u = url.getText().toString().trim();
            if (!u.startsWith("http://") && !u.startsWith("https://"))
                u = "https://" + u;
            web.loadUrl(u);
        });
        return v;
    }

    View buildFiles() {
        LinearLayout v = basePanel();
        TextView path = label("📁  /sdcard", 12, TEXT);
        path.setBackgroundColor(TITLE);
        path.setPadding(dp(10), 0, dp(10), 0);
        v.addView(path, new LinearLayout.LayoutParams(MATCH, dp(42)));

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(5), dp(5), dp(5), dp(5));

        java.io.File dir = new java.io.File("/sdcard");
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            TextView t = label("Android storage access is restricted on newer Android versions.\nUse the system file picker where required.", 12, MUTED);
            t.setPadding(dp(12), dp(12), dp(12), dp(12));
            list.addView(t);
        } else {
            for (java.io.File f : files) {
                if (f.getName().startsWith(".")) continue;
                Button b = flatButton((f.isDirectory() ? "📁  " : "📄  ") + f.getName());
                b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                b.setBackgroundColor(PANEL_2);
                list.addView(b, new LinearLayout.LayoutParams(MATCH, dp(42)));
            }
        }
        sv.addView(list);
        v.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1));
        return v;
    }

    View buildNotepad() {
        LinearLayout v = basePanel();
        LinearLayout bar = new LinearLayout(this);

        Button save = flatButton("Save");
        save.setBackgroundColor(ACCENT);
        Button clear = flatButton("Clear");
        clear.setBackgroundColor(CLOSE);
        bar.addView(save, new LinearLayout.LayoutParams(0, dp(42), 1));
        bar.addView(clear, new LinearLayout.LayoutParams(0, dp(42), 1));
        v.addView(bar);

        final EditText editor = new EditText(this);
        editor.setText(prefs.getString("notes", ""));
        editor.setHint("Type here...");
        editor.setTextColor(TEXT);
        editor.setHintTextColor(MUTED);
        editor.setGravity(Gravity.TOP | Gravity.LEFT);
        editor.setBackgroundColor(Color.rgb(24, 24, 27));
        editor.setPadding(dp(10), dp(10), dp(10), dp(10));
        editor.setTextSize(13);
        v.addView(editor, new LinearLayout.LayoutParams(MATCH, 0, 1));

        save.setOnClickListener(x -> {
            prefs.edit().putString("notes", editor.getText().toString()).apply();
            toast("Saved");
        });
        clear.setOnClickListener(x -> editor.setText(""));
        return v;
    }

    View buildCalculator() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(Color.BLACK);

        final TextView display = label("0", 28, TEXT);
        display.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        display.setPadding(dp(12), 0, dp(12), 0);
        v.addView(display, new LinearLayout.LayoutParams(MATCH, dp(52)));

        String[][] keys = {
                {"C", "⌫", "÷", "×"},
                {"7", "8", "9", "-"},
                {"4", "5", "6", "+"},
                {"1", "2", "3", "="},
                {"0", ".", "", ""}
        };

        final double[] left = {0};
        final char[] op = {0};
        final boolean[] fresh = {true};

        for (String[] rowKeys : keys) {
            LinearLayout row = new LinearLayout(this);
            for (String key : rowKeys) {
                if (key.isEmpty()) {
                    row.addView(new View(this), new LinearLayout.LayoutParams(0, MATCH, 1));
                    continue;
                }
                Button b = flatButton(key);
                b.setTextSize(17);
                b.setBackgroundColor(key.equals("=") ? ACCENT : PANEL_2);
                row.addView(b, new LinearLayout.LayoutParams(0, MATCH, 1));

                b.setOnClickListener(x -> {
                    String cur = display.getText().toString();
                    try {
                        if ("C".equals(key)) {
                            display.setText("0"); left[0] = 0; op[0] = 0; fresh[0] = true; return;
                        }
                        if ("⌫".equals(key)) {
                            display.setText(cur.length() > 1 ? cur.substring(0, cur.length()-1) : "0");
                            return;
                        }
                        if ("÷×-+".contains(key)) {
                            left[0] = Double.parseDouble(cur);
                            op[0] = key.equals("÷") ? '/' : key.equals("×") ? '*' : key.charAt(0);
                            fresh[0] = true; return;
                        }
                        if ("=".equals(key)) {
                            double r = Double.parseDouble(cur);
                            if (op[0] == '+') r = left[0] + r;
                            else if (op[0] == '-') r = left[0] - r;
                            else if (op[0] == '*') r = left[0] * r;
                            else if (op[0] == '/') r = r == 0 ? 0 : left[0] / r;
                            display.setText(formatNumber(r));
                            fresh[0] = true; return;
                        }
                        if (".".equals(key)) {
                            if (!cur.contains(".")) display.setText(cur + ".");
                            return;
                        }
                        if (fresh[0] || "0".equals(cur)) display.setText(key);
                        else display.setText(cur + key);
                        fresh[0] = false;
                    } catch (Exception e) {
                        display.setText("0"); fresh[0] = true;
                    }
                });
            }
            v.addView(row, new LinearLayout.LayoutParams(MATCH, 0, 1));
        }
        return v;
    }

    String formatNumber(double n) {
        String s = String.valueOf(Math.round(n * 1000000d) / 1000000d);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    View buildSettings() {
        LinearLayout v = basePanel();
        TextView info = label(
                "PC Mode settings\n\nAndroid: " + Build.VERSION.RELEASE +
                "\nDevice: " + Build.MODEL +
                "\n\nThis app is a Windows-style desktop shell running on Android.",
                13, TEXT);
        info.setPadding(dp(10), dp(10), dp(10), dp(10));
        v.addView(info, new LinearLayout.LayoutParams(MATCH, 0, 1));

        Button permissions = flatButton("Open app permissions");
        permissions.setBackgroundColor(ACCENT);
        permissions.setOnClickListener(x -> {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        v.addView(permissions, new LinearLayout.LayoutParams(MATCH, dp(48)));
        return v;
    }

    View buildAbout() {
        LinearLayout v = basePanel();
        v.setGravity(Gravity.CENTER);
        TextView logo = label("⊞", 58, ACCENT);
        logo.setGravity(Gravity.CENTER);
        v.addView(logo, new LinearLayout.LayoutParams(MATCH, dp(80)));

        TextView t = label("WinPCMode", 22, TEXT);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        v.addView(t, new LinearLayout.LayoutParams(MATCH, dp(45)));

        TextView sub = label(
                "Windows-style desktop environment for Android\n\n" +
                "Desktop • Start • Taskbar • Workspaces • Windows\n" +
                "Browser • Files • Terminal • Notepad • Calculator • Settings",
                11, MUTED);
        sub.setGravity(Gravity.CENTER);
        v.addView(sub, new LinearLayout.LayoutParams(MATCH, dp(120)));
        return v;
    }

    LinearLayout basePanel() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(dp(8), dp(8), dp(8), dp(8));
        v.setBackgroundColor(PANEL);
        return v;
    }

    void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (startOpen) {
            hideStart();
            return;
        }
        if (!windows.isEmpty()) {
            View w = windows.get(windows.size() - 1);
            desktop.removeView(w);
            windows.remove(w);
            for (List<View> ws : workspaceWindows) ws.remove(w);
            removeTaskbarButton(w);
            return;
        }
        super.onBackPressed();
    }
void checkSnap(final View win, final FrameLayout.LayoutParams lp,
               final int[] old, final boolean[] maximized, final Button max) {
    int screenW = desktop.getWidth();
    int screenH = desktop.getHeight() - dp(54);
    int threshold = dp(30);

    if (lp.leftMargin < threshold) {
        lp.leftMargin = 0;
        lp.topMargin = 0;
        lp.width = screenW / 2;
        lp.height = screenH;
        win.setLayoutParams(lp);
        toast("Snapped Left");
    } else if (lp.leftMargin + win.getWidth() > screenW - threshold) {
        lp.leftMargin = screenW / 2;
        lp.topMargin = 0;
        lp.width = screenW / 2;
        lp.height = screenH;
        win.setLayoutParams(lp);
        toast("Snapped Right");
    } else if (lp.topMargin < threshold) {
        old[0] = win.getWidth();
        old[1] = win.getHeight();
        lp.leftMargin = 0;
        lp.topMargin = 0;
        lp.width = MATCH;
        lp.height = MATCH;
        win.setLayoutParams(lp);
        max.setText("❐");
        maximized[0] = true;
        toast("Maximized");
    }
}
    
    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }}

