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

package org.scionlab.scion.as;

import android.app.Service;
import android.content.Context;

import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import timber.log.Timber;

import static org.scionlab.scion.as.Config.Scion.*;

/**
 * Starts a SCION AS from a given scionlab.org .tar.gz configuration file.
 */
public class ScionLabAS extends ScionAS {
    public ScionLabAS(Service service, BiConsumer<State, Map<String, State>> stateCallback) {
        super(service, stateCallback);
    }

    public void start(InputStream scionLabConfigurationInputStream, String pingAddress) throws IOException {
        Timber.i("extracting SCIONLab configuration");
        ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
                .extract(scionLabConfigurationInputStream, storage.getFile(TMP_DIRECTORY_PATH));
        Optional<String> vpnConfigPath = storage.findInDirectory(TMP_VPN_DIRECTORY_PATH, TMP_VPN_CONFIG_PATH_REGEX);
        start(Version.SCIONLAB,
                storage.getAbsolutePath(TMP_GEN_DIRECTORY_PATH),
                vpnConfigPath.map(storage::getAbsolutePath).orElse(null),
                pingAddress);
        storage.deleteFileOrDirectory(TMP_DIRECTORY_PATH);
    }

    public static String getScionVersion(Context context) {
        Process.initialize(context);
        return Version.SCIONLAB.getScionVersion(Storage.from(context));
    }
}
