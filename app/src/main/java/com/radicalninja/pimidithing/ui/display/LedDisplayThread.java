package com.radicalninja.pimidithing.ui.display;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.eon.androidthings.sensehatdriverlibrary.devices.LedMatrix;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LedDisplayThread extends Thread {

    private static final String TAG = LedDisplayThread.class.getCanonicalName();

    private final ConcurrentLinkedQueue<Job> jobs = new ConcurrentLinkedQueue<>();
    private final LedMatrix ledMatrix;

    private boolean parked = false;
    private Job currentJob;

    public LedDisplayThread(@NonNull final LedMatrix ledMatrix) {
        this.ledMatrix = ledMatrix;
    }

    public LedDisplayThread(@NonNull final LedMatrix ledMatrix,
                            @IntRange(from = -3, to = 3) final int rotations) {

        this.ledMatrix = ledMatrix;
        this.ledMatrix.setRotation(rotations);
    }

    protected void park() throws InterruptedException {
        if (parked) {
            // is there anything we should do here? block until its park call can take place?
            return;
        }
        parked = true;
        LockSupport.park();
        if (this.isInterrupted()) {
            throw new InterruptedException("Thread was interrupted while parked.");
        } else if (parked) {
            throw new InterruptedException("Thread was spuriously unparked.");
        }
    }

    protected void unpark() {
        if (!parked) {
            // anything to be done here in this case?
            return;
        }
        parked = false;
        LockSupport.unpark(this);
    }

    public boolean isParked() {
        return parked;
    }

    public void queueJob(@NonNull final Job job) {
        if (null == job) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        // TODO: logic for expiring otherwise "endless" loops?
        jobs.add(job);
        if (parked) {
            unpark();
        }
    }

    public void queueJobs(@NonNull final Job[] jobs) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null array.");
        }
        for (final Job job : jobs) {
            if (null == job) {
                // TODO: log or throw?
            } else {
                this.jobs.add(job);
            }
        }
        if (parked) {
            unpark();
        }
    }

    public void queueJobs(@NonNull final Collection<Job> jobs) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null collection.");
        }
        queueJobs(jobs.toArray(new Job[jobs.size()]));
    }

    public void execJobImmediately(@NonNull final Job job, final boolean clearQueue) {
        if (null == job) {
            throw new IllegalArgumentException("Job must not be null.");
        }
        if (clearQueue) {
            jobs.clear();
        }
        jobs.add(job);
        if (null != currentJob) {
            currentJob.stop();
        }
        if (parked) {
            unpark();
        }
    }

    public void execJobsImmediately(@NonNull final Job[] jobs, final boolean clearQueue) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null array.");
        }
        if (clearQueue) {
            this.jobs.clear();
        }
        for (final Job job : jobs) {
            if (null == job) {
                // TODO: log or throw?
            } else {
                this.jobs.add(job);
            }
        }
        if (null != currentJob) {
            currentJob.stop();
        }
        if (parked) {
            unpark();
        }
    }

    public void execJobsImmediately(@NonNull final Collection<Job> jobs, final boolean clearQueue) {
        if (null == jobs) {
            throw new IllegalArgumentException("Jobs must not be a null collection.");
        }
        execJobsImmediately(jobs.toArray(new Job[jobs.size()]), clearQueue);
    }

    /**
     *
     * @param frame
     * @param bounds Rect defining the range of the bitmap to draw. May be null.
     * @param sleepDuration
     * @return True if no exceptions occurred during the draw and sleep opearations.
     */
    protected boolean draw(final Bitmap frame, @Nullable final Rect bounds, final long sleepDuration) {
        boolean success = true;
        try {
            if (null != bounds) {
                ledMatrix.draw(frame, bounds.left, bounds.top, bounds.width(), bounds.height());
            } else {
                ledMatrix.draw(frame);
            }
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        try {
            if (sleepDuration > 0) {
                Thread.sleep(sleepDuration);
            }
            // TODO: Log zero sleep duration events here?
        } catch (InterruptedException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void run() {
        while (true) {
            if (jobs.peek() == null) {
                Log.d(TAG, "Job queue is empty; Parking thread.");
                try {
                    park();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    parked = false;
                }
            }
            currentJob = jobs.poll();
            if (null == currentJob) {
                continue;
            }
            while (currentJob.hasNextFrame() && !currentJob.isExpired()) {
                final Bitmap currentFrame = currentJob.getNextFrame();
                if (null == currentFrame) {
                    // No frame retrieved.
                    // TODO: How do we handle this?
                } else if (currentJob.needsScrolling()) {
                    while (currentJob.hasNextScroll() && !currentJob.isExpired()) {
                        final Rect scroll = currentJob.getNextScroll();
                        if (null == scroll) {
                            // No scroll retrieved.
                            // TODO: How do we handle this?
                        } else {
                            draw(currentFrame, scroll, currentJob.getSleepDuration());
                        }
                    }
                } else {
                    draw(currentFrame, null, currentJob.getSleepDuration());
                }
            }
            currentJob.recycle();
            currentJob = null;
        }
    }

}
