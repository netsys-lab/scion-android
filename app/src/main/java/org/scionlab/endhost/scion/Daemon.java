/*
 * Copyright (C) 2019-2020 Vera Clemens, Tom Kranz, Tom Heimbrodt, Elias Kuiter
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

package org.scionlab.endhost.scion;

import android.util.Log;

import com.moandjiezana.toml.Toml;

import org.scionlab.endhost.Logger;

import java.io.File;
import java.util.Optional;
import static org.scionlab.endhost.scion.Config.Daemon.*;

/**
 * Performs requests to the SCION network and acts as an endhost.
 */
public class Daemon extends Component {
    private String configDirectorySourcePath;
    private String configPath;

    public Daemon(String configDirectorySourcePath) {
        this.configDirectorySourcePath = configDirectorySourcePath;
    }

    @Override
    protected String getTag() {
        return "Daemon";
    }

    @Override
    boolean prepare() {
        // copy configuration folder provided by the user and find daemon configuration file
        if (storage.countFilesInDirectory(new File(configDirectorySourcePath)) > CONFIG_DIRECTORY_FILE_LIMIT) {
            Log.e(getTag(), "too many files in configuration directory, did you choose the right directory?");
            return false;
        }
        storage.deleteFileOrDirectory(CONFIG_DIRECTORY_PATH);
        storage.copyFileOrDirectory(new File(configDirectorySourcePath), CONFIG_DIRECTORY_PATH);
        Optional<String> _configPath = storage.findFirstMatchingFileInDirectory(
                CONFIG_DIRECTORY_PATH, CONFIG_PATH_REGEX);
        if (!_configPath.isPresent()) {
            Log.e(getTag(), "could not find SCION daemon configuration file sciond.toml or sd.toml");
            return false;
        }
        configPath = _configPath.get();
        Toml config = new Toml().read(storage.getInputStream(configPath));
        String publicAddress = config.getString(CONFIG_PUBLIC_TOML_PATH);
        // TODO: for now, we assume the topology file is present at the correct location and has the right values
        // TODO: import certs and keys directories

        storage.prepareFiles(RELIABLE_SOCKET_PATH, UNIX_SOCKET_PATH, PATH_DATABASE_PATH, TRUST_DATABASE_PATH);
        storage.writeFile(configPath, String.format(
                storage.readAssetFile(CONFIG_TEMPLATE_PATH),
                storage.getAbsolutePath(CONFIG_DIRECTORY_PATH),
                storage.getAbsolutePath(LOG_PATH),
                LOG_LEVEL,
                publicAddress,
                storage.getAbsolutePath(RELIABLE_SOCKET_PATH),
                storage.getAbsolutePath(UNIX_SOCKET_PATH),
                storage.getAbsolutePath(PATH_DATABASE_PATH),
                storage.getAbsolutePath(TRUST_DATABASE_PATH)));
        setupLogThread(LOG_PATH, WATCH_PATTERN);

        return true;
    }

    @Override
    void run() {
        Binary.runDaemon(getContext(),
                Logger.createLogThread(getTag()),
                storage.getAbsolutePath(configPath),
                storage.getAbsolutePath(Config.Dispatcher.SOCKET_PATH));
    }
}
