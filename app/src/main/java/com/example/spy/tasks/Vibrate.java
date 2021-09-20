package com.example.spy.tasks;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.example.spy.utils.AppController;

import static android.content.Context.VIBRATOR_SERVICE;

public class Vibrate {
    public Vibrate() {
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        Vibrator vibrator = (Vibrator) new AppController().getContext().getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE));
            }else {
                vibrator.vibrate(pattern,-1);
            }

        }
    }
}
