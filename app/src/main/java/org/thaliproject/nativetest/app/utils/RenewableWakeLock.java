package org.thaliproject.nativetest.app.utils;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

//import javax.annotation.Nullable;
//import javax.annotation.concurrent.ThreadSafe;

import static android.content.Context.POWER_SERVICE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.INFO;

//@ThreadSafe
//@NotNullByDefault
public class RenewableWakeLock {

    private static final Logger LOG =
            Logger.getLogger(RenewableWakeLock.class.getName());

    /**
     * Automatically release the lock this many milliseconds after it's due
     * to have been replaced and released.
     */
    private static final int SAFETY_MARGIN_MS = 10_000;

    private final Context context;
    private final PowerManager powerManager;
    private String tag;
    private long duration;
    private final Object lock = new Object();

    @NonNull
    private PowerManager.WakeLock wakeLock; // Locking: lock

    /**
     *  Constructor for RenewableWakeLock
     * @param context The context of the activity, used to grab powerManager
     * @param tag A string used to describe the WakeLock
     * @param duration An integer of the time for the lock you want to grab for, in milliseconds
     */
    public RenewableWakeLock(Context context, String tag,
                             long duration) {
        this.context = context;
        this.powerManager = (PowerManager) this.context.getSystemService(POWER_SERVICE);
        this.tag = tag;
        setDuration(duration);
    }

    /**
     * Acquire the wakeLock, return instantly if it has been required
     */
    public void acquire() {
        if (LOG.isLoggable(INFO)) LOG.info("Acquiring wake lock " + tag);
        synchronized (lock) {
            if (wakeLock.isHeld()) {
                LOG.info("Already acquired");
                return;
            }

            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP, tag);
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire(duration);
        }
    }

    /**
     * Create a new wake lock, acquire it and release the old one, return instantly if the lock was
     * not held
     */
    private void renew() {
        if (LOG.isLoggable(INFO)) LOG.info("Renewing wake lock " + tag);
        synchronized (lock) {
            if (!wakeLock.isHeld()) {
                LOG.info("Already released");
                return;
            }
            PowerManager.WakeLock oldWakeLock = wakeLock;
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP, tag);
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire(duration);
            oldWakeLock.release();
        }
    }

    /**
     * Release the lock, return instantly if the lock has been released
     */
    public void release() {
        if (LOG.isLoggable(INFO)) LOG.info("Releasing wake lock " + tag);
        synchronized (lock) {
            if (!wakeLock.isHeld()) {
                LOG.info("Already released");
                return;
            }
            wakeLock.release();
        }
    }

    /**
     * Set WakeLock the Duration. The final duration set would be min{SAFTY_MARGIN(10ms), duration}
     * @param duration An integer of the time you want to grab the lock for, in milliseconds
     */
    public void setDuration(long duration) {
        if(duration < SAFETY_MARGIN_MS){
            this.duration = SAFETY_MARGIN_MS;
        }else{
            this.duration = duration;
        }
    }

    /**
     * Set the tag of the wakeLock
     * @param tag A string used to describe the wakeLock
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Return the duration of the wakeLock.
     * @return A long as the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Return the tag describing the wakeLock
     * @return A string used to describe the wakelock
     */
    public String getTag() {
        return tag;
    }
}

