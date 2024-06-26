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

import timber.log.Timber;

import static org.scionlab.scion.as.Config.BorderRouter.*;

class BorderRouter extends Component {
    @Override
    Class[] dependsOn() {
        return new Class[]{Dispatcher.class, VPNClient.class};
    }

    @Override
    boolean prepare() {
        storage.writeFile(CONFIG_PATH, String.format(
                storage.readAssetFile(CONFIG_TEMPLATE_PATH),
                storage.getAbsolutePath(Config.Scion.CONFIG_DIRECTORY_PATH),
                LOG_LEVEL));
        return true;
    }

    @Override
    void run() {
        process.connectToDispatcher()
                .addArgument(BINARY_FLAG)
                .addConfigurationFile(CONFIG_PATH)
                .watchFor(READY_PATTERN,() -> setReady()).watchFor(VPN_NOT_READY_PATTERN, () ->
                Timber.e("could not start border router, please check VPN connection"))
                .run();
    }
}
