package com.limelight.binding.video;

import android.os.SystemClock;

import java.util.Arrays;

public class FrameIntervalTracker {
    private final float[] intervals;
    private int writeIndex;
    private int count;
    private volatile long lastTimestamp;

    public FrameIntervalTracker(int capacity) {
        intervals = new float[capacity];
    }

    public void recordFrame() {
        long now = SystemClock.uptimeMillis();
        long last = lastTimestamp;
        lastTimestamp = now;
        if (last <= 0) return;

        float interval = (float)(now - last);
        synchronized (this) {
            intervals[writeIndex] = interval;
            writeIndex = (writeIndex + 1) % intervals.length;
            if (count < intervals.length) count++;
        }
    }

    public float getOnePercentLowFps() {
        float[] snapshot;
        synchronized (this) {
            if (count < 10) return 0f;
            snapshot = new float[count];
            int start = (count < intervals.length) ? 0 : writeIndex;
            for (int i = 0; i < count; i++) {
                snapshot[i] = intervals[(start + i) % intervals.length];
            }
        }
        Arrays.sort(snapshot);
        int p99Index = (int)(snapshot.length * 0.99);
        if (p99Index >= snapshot.length) p99Index = snapshot.length - 1;
        float p99 = snapshot[p99Index];
        return p99 > 0f ? 1000f / p99 : 0f;
    }

    public void clear() {
        synchronized (this) {
            count = 0;
            writeIndex = 0;
            lastTimestamp = 0L;
        }
    }
}
