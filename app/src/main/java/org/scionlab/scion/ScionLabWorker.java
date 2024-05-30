package org.scionlab.scion;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.io.InputStream;

// work around for using foreground services
public class ScionLabWorker extends Worker {
    public ScionLabWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String scionLabConfigurationUri = getInputData().getString(ScionService.SCIONLAB_CONFIGURATION_URI);
        String pingAddress = getInputData().getString(ScionService.PING_ADDRESS);

        if (scionLabConfigurationUri == null || pingAddress == null) {
            return Result.failure();
        }

        InputStream scionLabConfigurationInputStream = null;
        try {
            scionLabConfigurationInputStream = getApplicationContext().getContentResolver().openInputStream(Uri.parse(scionLabConfigurationUri));
            if (scionLabConfigurationInputStream != null) {
                ScionService.scionLabAS.start(scionLabConfigurationInputStream, pingAddress);
                return Result.success();
            } else {
                return Result.failure();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}

