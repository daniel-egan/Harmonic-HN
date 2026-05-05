package com.simon.harmonichackernews.network;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class ReplyNotificationJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        RepliesChecker.checkNow(this, success -> jobFinished(params, !success));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
