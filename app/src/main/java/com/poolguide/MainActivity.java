package com.poolguide;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);

        TextView tv = new TextView(this);
        tv.setTextSize(18);
        tv.setGravity(Gravity.CENTER);
        tv.setText("Pool Guide\n\nالزر العائم:\nضغطة = إخفاء / ضبط / تثبيت\nضغط مطول = إغلاق\nيمكنك سحب الزر لتغيير مكانه\n");
        root.addView(tv);

        Button perm = new Button(this);
        perm.setText("1) السماح بالعرض فوق التطبيقات");
        perm.setOnClickListener(v -> startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()))));
        root.addView(perm);

        Button start = new Button(this);
        start.setText("2) تشغيل الشبكة");
        start.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "اضغط الزر الأول وفعّل الصلاحية", Toast.LENGTH_LONG).show();
                return;
            }
            startService(new Intent(this, OverlayService.class));
            moveTaskToBack(true);
        });
        root.addView(start);

        Button stop = new Button(this);
        stop.setText("إيقاف");
        stop.setOnClickListener(v -> stopService(new Intent(this, OverlayService.class)));
        root.addView(stop);

        setContentView(root);
    }
}
