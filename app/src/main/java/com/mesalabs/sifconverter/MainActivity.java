package com.mesalabs.sifconverter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

// kang from Bin4ry Spr Converter

public class MainActivity extends AppCompatActivity {

    private int permission;
    private Button cnvButton;
    private ImageView imgPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ask for write permission for M >
        permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);

        setContentView(R.layout.activity_main);
        addListenerOnButton();
    }

    private void addListenerOnButton() {
        imgPreview = findViewById(R.id.sprPreview);
        cnvButton = findViewById(R.id.convertBtn);
        cnvButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Loop through all drawables to convert them
                final R.drawable drawableResources = new R.drawable();
                final Class<R.drawable> c = R.drawable.class;
                final Field[] fields = c.getDeclaredFields();

                for (int i = 0; i < fields.length; i++) {
                    final int resourceId;

                    try {
                        resourceId = fields[i].getInt(drawableResources);
                    } catch (IllegalAccessException e) {
                        continue;
                    }

                    // Make use of resourceId for access Drawables here
                    Drawable d = getResources().getDrawable(resourceId, getApplicationContext().getTheme());
                    imgPreview.setImageDrawable(d);
                    Bitmap bm = drawableToBitmap(d);
                    String name = getApplicationContext().getResources().getResourceEntryName(resourceId);
                    // You can get dir from many helpers like Environment.getExternalStorageDirectory() or getApplicationContext().getFilesDir(), depending on where you want to save the image.
                    File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "mesalabs");

                    boolean doSave = true;
                    if (!dir.exists()) {
                        doSave = dir.mkdirs();
                    }
                    if (doSave) {
                        // Make sure Bitmap isn't null to avoid app FC
                        try {
                            // Bitmap.CompressFormat can be PNG, JPEG or WEBP. Quality goes from 1 to 100.
                            saveBitmapToFile(dir, name + ".png", bm, Bitmap.CompressFormat.PNG, 100);
                        } catch (NullPointerException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Failed to convert. Make sure write permission has been granted.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    private static Bitmap drawableToBitmap (Drawable d) {
        Bitmap bm;

        if(d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0) {
            // Single color bitmap will be created of 1x1 pixel
            bm = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bm = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas c = new Canvas(bm);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);
        return bm;
    }

    private void saveBitmapToFile(File dir, String fileName, Bitmap bm, Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, fileName);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format, quality, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException el) {
                    el.printStackTrace();
                }
            }
        }
    }
}
