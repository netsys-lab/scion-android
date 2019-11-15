/*
 * Copyright (C) 2019  Vera Clemens, Tom Kranz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.scionlab.endhost;

import android.content.Intent;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

public class DispatcherService extends BackgroundService {
    // Depends on DISPATCHER_DIR and DEFAULT_DISPATCHER_ID from CMakeLists.txt
    public static final String DEFAULT_DISP_SOCKET_PATH = "run/shm/dispatcher/default.sock";
    private static final int NID = 1;
    private static final String TAG = "dispatcher";
    private static final Pattern LOG_DELETER_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}\\+\\d{4} \\[[A-Z]+] \\(\\d+:dispatcher:\\.\\./\\.\\./\\.\\./\\.\\./src/main/cpp/gobind-scion/c/dispatcher/dispatcher\\.c:\\d+\\)\\s+");
    private static final String CONFIG_PATH = "dispatcher.zlog.conf";
    private static final String LOG_PATH = "logs/dispatcher.zlog";
    private static final String[] LOG_FLAVOURS = { "DEBUG", "INFO", "WARN", "ERROR", "FATAL" };

    static {
        System.loadLibrary("dispatcher-wrapper");
    }

    public DispatcherService() {
        super("DispatcherService");
    }

    @Override
    protected int getNotificationId() {
        return NID;
    }

    @NonNull
    @Override
    protected String getTag() {
        return TAG;
    }

    @NonNull
    @Override
    protected Pattern getLogDeleter() {
        return LOG_DELETER_PATTERN;
    }

    @Override
    protected void onHandleIntent (Intent intent) {
        if (intent == null) return;
        super.onHandleIntent(intent);

        log(R.string.servicesetup);

        try {
            File logRoot = getExternalFilesDir(null);
            File conf = createConfigFile(logRoot);
            mkfile(DEFAULT_DISP_SOCKET_PATH);
            delete(DEFAULT_DISP_SOCKET_PATH);

            String logPath = getLogPath(logRoot, LOG_FLAVOURS[0]);

            delete(logPath);
            File log = mkfile(logPath);

            log(R.string.servicestart);
            setupLogUpdater(log).start();

            int ret = main(conf.getAbsolutePath(), getFilesDir().getAbsolutePath());
            die(R.string.servicereturn, ret);
        } catch (Exception e) {
            die(R.string.serviceexception, e.getLocalizedMessage());
        }
    }

    private File createConfigFile(File logRoot) throws IOException {
        delete(CONFIG_PATH);
        File conf = mkfile(CONFIG_PATH);
        FileWriter w = new FileWriter(conf);
        w.write(
            "[global]\n" +
            "default format = \"%d(%F %T).%us%d(%z) [%V] (%p:%c:%F:%L) %m%n\"\n" +
            "file perms = 644\n" +
            "\n" +
            "[rules]\n" +
            "default.* >stdout\n"
        );
        for (String flavour : LOG_FLAVOURS) {
            w.write(String.format("dispatcher.%1$s \"%2$s\", 10MB*2\n", flavour, getLogPath(logRoot, flavour)));
        }
        w.close();
        return conf;
    }

    private String getLogPath(File root, String flavour) {
        return String.format("%s.%s", new File(root, LOG_PATH).getAbsolutePath(), flavour);
    }

    public native int main(String confFileName, String workingDir);
}
