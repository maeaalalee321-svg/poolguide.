package com.poolguide;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(new TableView(this));
    }

    static class TableView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx, cy, ox, oy, R = 24, L, T, Rt, B;
        float[][] pk = new float[6][2];
        int pocket = 2, drag = 0;

        TableView(Context c) { super(c); }

        @Override
        protected void onSizeChanged(int w, int h, int a, int b) {
            float m = 40;
            L = m; T = m; Rt = w - m; B = h - m;
            R = Math.min(w, h) / 28f;
            float mx = (L + Rt) / 2;
            pk = new float[][]{{L, T}, {mx, T}, {Rt, T}, {L, B}, {mx, B}, {Rt, B}};
            cx = L + (Rt - L) * 0.25f; cy = (T + B) / 2;
            ox = L + (Rt - L) * 0.6f;  oy = (T + B) / 2 - 60;
        }

        @Override
        protected void onDraw(Canvas c) {
            c.drawColor(0xFF3E2723);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF1B7F3B);
            c.drawRect(L, T, Rt, B, p);

            for (int i = 0; i < 6; i++) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.BLACK);
                c.drawCircle(pk[i][0], pk[i][1], R * 1.4f, p);
                if (i == pocket) {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(6);
                    p.setColor(Color.YELLOW);
                    c.drawCircle(pk[i][0], pk[i][1], R * 1.6f, p);
                }
            }

            float dx = pk[pocket][0] - ox, dy = pk[pocket][1] - oy;
            float d = (float) Math.hypot(dx, dy);
            if (d < 1) d = 1;
            float gx = ox - dx / d * 2 * R, gy = oy - dy / d * 2 * R;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(Color.WHITE);
            c.drawLine(cx, cy, gx, gy, p);
            p.setColor(Color.YELLOW);
            c.drawLine(ox, oy, pk[pocket][0], pk[pocket][1], p);
            p.setColor(Color.WHITE);
            c.drawCircle(gx, gy, R, p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            c.drawCircle(cx, cy, R, p);
            p.setColor(0xFFFFC107);
            c.drawCircle(ox, oy, R, p);

            double a1 = Math.atan2(gy - cy, gx - cx);
            double a2 = Math.atan2(dy, dx);
            double deg = Math.abs(Math.toDegrees(a1 - a2)) % 360;
            if (deg > 180) deg = 360 - deg;
            p.setColor(Color.WHITE);
            p.setTextSize(44);
            c.drawText("Cut angle: " + Math.round(deg) + "°", L + 20, T + 60, p);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX(), y = e.getY();
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                drag = 0;
                if (Math.hypot(x - cx, y - cy) < R * 3) drag = 1;
                else if (Math.hypot(x - ox, y - oy) < R * 3) drag = 2;
                else {
                    for (int i = 0; i < 6; i++)
                        if (Math.hypot(x - pk[i][0], y - pk[i][1]) < R * 3) pocket = i;
                }
            } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                if (drag == 1) { cx = x; cy = y; }
                if (drag == 2) { ox = x; oy = y; }
            }
            invalidate();
            return true;
        }
    }
}
