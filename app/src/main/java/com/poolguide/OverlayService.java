package com.poolguide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class OverlayService extends Service {
    WindowManager wm;
    TableOverlay grid;
    BtnView btn;
    WindowManager.LayoutParams gp, bp;
    int mode = 0; // 0 مخفي، 1 ضبط، 2 تثبيت
    float dp;
    static final int BASE = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        dp = getResources().getDisplayMetrics().density;
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        grid = new TableOverlay(this);
        gp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type, BASE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        grid.setVisibility(View.GONE);
        wm.addView(grid, gp);

        btn = new BtnView(this);
        int s = (int) (52 * dp);
        bp = new WindowManager.LayoutParams(s, s, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        bp.gravity = Gravity.TOP | Gravity.LEFT;
        bp.x = (int) (10 * dp);
        bp.y = (int) (120 * dp);
        wm.addView(btn, bp);
    }

    void applyMode() {
        grid.setVisibility(mode == 0 ? View.GONE : View.VISIBLE);
        gp.flags = BASE | (mode == 1 ? 0 : WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        wm.updateViewLayout(grid, gp);
        grid.invalidate();
        btn.invalidate();
    }

    @Override
    public void onDestroy() {
        try { wm.removeView(grid); } catch (Exception e) { }
        try { wm.removeView(btn); } catch (Exception e) { }
        super.onDestroy();
    }

    class BtnView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float dx, dy;
        int sx, sy;
        boolean moved;
        long t0;

        BtnView(Context c) { super(c); }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth() / 2f;
            p.setStyle(Paint.Style.FILL);
            p.setColor(mode == 0 ? 0xCC555555 : mode == 1 ? 0xCCFF9800 : 0xCC2E9E4F);
            c.drawCircle(w, w, w - 2 * dp, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2 * dp);
            p.setColor(Color.WHITE);
            c.drawCircle(w, w, w * 0.45f, p);
            c.drawLine(w, w * 0.25f, w, w * 1.75f, p);
            c.drawLine(w * 0.25f, w, w * 1.75f, w, p);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dx = e.getRawX(); dy = e.getRawY();
                    sx = bp.x; sy = bp.y;
                    moved = false;
                    t0 = e.getEventTime();
                    break;
                case MotionEvent.ACTION_MOVE: {
                    float mx = e.getRawX() - dx, my = e.getRawY() - dy;
                    if (Math.abs(mx) > 10 * dp || Math.abs(my) > 10 * dp) moved = true;
                    if (moved) {
                        bp.x = sx + (int) mx;
                        bp.y = sy + (int) my;
                        wm.updateViewLayout(this, bp);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                    if (!moved) {
                        if (e.getEventTime() - t0 > 800) {
                            stopSelf();
                        } else {
                            mode = (mode + 1) % 3;
                            applyMode();
                        }
                    }
                    break;
            }
            return true;
        }
    }

    class TableOverlay extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        PointF tl, br, a, b;
        PointF[] hs;
        int drag = -1;

        TableOverlay(Context c) { super(c); }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            if (tl == null) {
                tl = new PointF(w * 0.12f, h * 0.18f);
                br = new PointF(w * 0.88f, h * 0.85f);
                a = new PointF(w * 0.35f, h * 0.6f);
                b = new PointF(w * 0.6f, h * 0.4f);
                hs = new PointF[]{a, b, tl, br};
            }
        }

        @Override
        protected void onDraw(Canvas c) {
            if (tl == null) return;
            float R = Math.max(8 * dp, (br.x - tl.x) / 80f);

            if (mode == 1) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2 * dp);
                p.setColor(0xFF00E5FF);
                c.drawRect(tl.x, tl.y, br.x, br.y, p);
            }

            float l = tl.x + R, r = br.x - R, t = tl.y + R, bt = br.y - R;
            float px = a.x, py = a.y;
            float vx = b.x - a.x, vy = b.y - a.y;
            float len = (float) Math.hypot(vx, vy);
            if (len > 1) {
                vx /= len; vy /= len;
                for (int i = 0; i < 4; i++) {
                    float tx = vx > 0 ? (r - px) / vx : vx < 0 ? (l - px) / vx : 1e9f;
                    float ty = vy > 0 ? (bt - py) / vy : vy < 0 ? (t - py) / vy : 1e9f;
                    float s = Math.min(tx, ty);
                    if (s <= 0) break;
                    float nx = px + vx * s, ny = py + vy * s;
                    int col = i == 0 ? 0xFFFFFFFF : i == 1 ? 0xFFFFEB3B : 0xAA80DEEA;
                    p.setColor(col);
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth((i == 0 ? 3 : 2) * dp);
                    c.drawLine(px, py, nx, ny, p);
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(nx, ny, 3 * dp, p);
                    px = nx; py = ny;
                    if (tx <= ty) vx = -vx; else vy = -vy;
                }
            }

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2 * dp);
            p.setColor(Color.WHITE);
            c.drawCircle(a.x, a.y, R, p);
            p.setColor(0xFFFFEB3B);
            c.drawCircle(b.x, b.y, R, p);

            if (mode == 1) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(0x99FF9800);
                for (PointF h : hs) c.drawCircle(h.x, h.y, 12 * dp, p);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (hs == null) return true;
            float x = e.getX(), y = e.getY();
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    drag = -1;
                    float best = 60 * dp;
                    for (int i = 0; i < hs.length; i++) {
                        float d = (float) Math.hypot(x - hs[i].x, y - hs[i].y);
                        if (d < best) { best = d; drag = i; }
                    }
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                    if (drag >= 0) { hs[drag].set(x, y); invalidate(); }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    drag = -1;
                    break;
            }
            return true;
        }
    }
                                    }
