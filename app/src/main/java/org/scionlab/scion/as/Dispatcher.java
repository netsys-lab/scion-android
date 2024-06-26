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

import static org.scionlab.scion.as.Config.Dispatcher.*;

/**
 * Dispatches requests/responses from other SCION components to the outside world and vice-versa.
 */
class Dispatcher extends Component {
    @Override
    boolean prepare() {
        storage.prepareFile(SOCKET_PATH);
        storage.writeFile(CONFIG_PATH, String.format(
                storage.readAssetFile(CONFIG_TEMPLATE_PATH),
                storage.getAbsolutePath(SOCKET_PATH),
                LOG_LEVEL));
        return true;
    }

    @Override
    void run() {
        process.addArgument(BINARY_FLAG)
                .addConfigurationFile(CONFIG_PATH)
                .watchFor(READY_PATTERN,() -> setReady())
                .run();
    }
}
