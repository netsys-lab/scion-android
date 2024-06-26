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

import java.util.regex.Pattern;

import static org.scionlab.scion.as.Logger.*;

public class Config {
    public static final String SCION_VERSION = "v2020.12-386-gfc081beb";
    static class Process {
        static final String WORKING_DIRECTORY_PATH = "EXTERNAL/workdir"; // working directory of SCION processes
        static final String CONFIG_FLAG = "--config"; // flag that specifies a configuration file
        static final String DISPATCHER_SOCKET_ENV = "DISPATCHER_SOCKET"; // environment variable that specifies the dispatcher socket
    }

    static class Component {
        static final int READY_INTERVAL = 250; // how frequently (in ms) to check whether required components are ready
        static final int READY_RETRIES = 120; // when to give up and stop the component
    }

    public static class Logger {
        public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO; // default log level on startup
        static final LogLevel DEFAULT_LINE_LOG_LEVEL = LogLevel.INFO; // log level for lines that do not match
        static final String TRACE_PREFIX = "[TRACE] [DBUG] "; // prefix for lines with the trace log level
        static final String DEBUG_PREFIX = "[DEBUG] "; // prefix for lines with the debug log level
        static final String INFO_PREFIX = "[INFO] "; // prefix for lines with the info log level
        static final String WARN_PREFIX = "[WARN] "; // prefix for lines with the warn log level
        static final String ERROR_PREFIX = "[EROR] "; // prefix for lines with the error log level
        static final String CRIT_PREFIX = "[CRIT] "; // prefix for lines with the crit log level
        static final String SKIP_LINE_PREFIX = "> "; // skip setting the message log level for lines starting with this prefix
        static final Pattern DELETE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{6}\\+\\d{4} "); // trims information from log output
        static final long UPDATE_INTERVAL = 1000; // how often (in ms) to poll the log file for updates
    }

    static class BorderRouter {
        static final String BINARY_FLAG = "border"; // value of binary's first argument to run the border router
        static final String CONFIG_TEMPLATE_PATH = "border_router.toml"; // path to configuration file template, located in assets folder
        static final String CONFIG_PATH = "EXTERNAL/config/border_router.toml"; // path to configuration file
        static final String LOG_LEVEL = "info"; // log level passed to process (log messages are later filtered by the Logger.Tree class)
        static final Pattern READY_PATTERN = Pattern.compile("^.*Service started SCION Router.*$"); // when encountered, consider component ready
        static final Pattern VPN_NOT_READY_PATTERN = Pattern.compile("^.*bind: cannot assign requested address.*$"); // occurs when VPN connection is not ready
    }

    static class ControlServer {
        static final String BINARY_FLAG = "cs"; // value of binary's first argument to run the control server
        static final String CONFIG_TEMPLATE_PATH = "control_server.toml"; // path to configuration file template, located in assets folder
        static final String CONFIG_PATH = "EXTERNAL/config/control_server.toml"; // path to configuration file
        static final String TRUST_DATABASE_PATH = "EXTERNAL/databases/control_server.trust.db"; // path to trust SQLite database created in external storage
        static final String PATH_DATABASE_PATH = "EXTERNAL/databases/control_server.path.db"; // path to path SQLite database created in external storage
        static final String BEACON_DATABASE_PATH = "EXTERNAL/databases/control_server.beacon.db"; // path to beacon SQLite database created in external storage
        static final String LOG_LEVEL = "info"; // log level passed to process (log messages are later filtered by the Logger.Tree class)
        static final Pattern READY_PATTERN = Pattern.compile("^.*Started periodic tasks.*$"); // when encountered, consider component ready
    }

    static class Daemon {
        static final String BINARY_FLAG = "sciond"; // value of binary's first argument to run the daemon
        static final String CONFIG_TEMPLATE_PATH = "daemon.toml"; // path to configuration file template, located in assets folder
        static final String CONFIG_PATH = "EXTERNAL/config/daemon.toml"; // path to configuration file
        static final String TRUST_DATABASE_PATH = "EXTERNAL/databases/daemon.trust.db"; // path to trust SQLite database created in external storage
        static final String PATH_DATABASE_PATH = "EXTERNAL/databases/daemon.path.db"; // path to path SQLite database created in external storage
        static final String LOG_LEVEL = "info"; // log level passed to process (log messages are later filtered by the Logger.Tree class)
        static final Pattern READY_PATTERN = Pattern.compile("^.*Service started SCION Daemon.*$"); // when encountered, consider component ready
    }

    public static class Dispatcher {
        public static final String BINARY_FLAG = "dispatcher"; // value of binary's first argument to run the dispatcher
        public static final String CONFIG_TEMPLATE_PATH = "dispatcher.toml"; // path to configuration file template, located in assets folder
        public static final String CONFIG_PATH = "EXTERNAL/config/dispatcher.toml"; // path to configuration file
        public static final String SOCKET_PATH = "INTERNAL/dispatcher.sock"; // path to socket
        public static final String LOG_LEVEL = "info"; // log level passed to process (log messages are later filtered by the Logger.Tree class)
        public static final Pattern READY_PATTERN = Pattern.compile("^.*Service started SCION Dispatcher.*$"); // when encountered, consider component ready
    }

    static class Scmp {
        static final String BINARY_FLAG = "scion"; // value of binary's first argument to run the scmp tool
        static final String ECHO_FLAG = "ping"; // value of scmp's first argument to run an echo request
        static final String DISPATCHER_SOCKET_FLAG = "--dispatcher"; // flag that specifies the dispatcher socket
        static final Pattern READY_PATTERN = Pattern.compile("^.*bytes from.*$"); // when encountered, consider component ready
        static final long HEALTH_TIMEOUT = 2000; // how long the component is considered healthy after the last received ping
    }

    public static class SensorFetcher {
        public static final String BINARY_FLAG = "sensorfetcher"; // value of binary's first argument to run the sensorfetcher tool
        public static final String SERVER_FLAG = "-scion-android_s"; // flag that specifies the remote address
        public static final String DISPATCHER_SOCKET_ENV = "SCION_DISPATCHER_SOCKET"; // flag that specifies the dispatcher socket
    }

    public static class Scion {
        public static final String SCIONLAB_BINARY_PATH = "libscion-scionlab.so"; // file name of SCION's scionlab binary in jniLibs (shipped and used by default)
        public static final String VERSION_FLAG = "version"; // flag to obtain version information
        public static final String CONFIG_DIRECTORY_PATH = "EXTERNAL/config"; // path to config directory where all configuration files are stored
        public static final String TMP_DIRECTORY_PATH = "EXTERNAL/tmp"; // path to temporary directory used for extracting SCIONLab configuration
        public static final String TMP_GEN_DIRECTORY_PATH = TMP_DIRECTORY_PATH + "/etc/scion"; // path to gen directory extracted from configuration
        public static final String TMP_VPN_DIRECTORY_PATH = TMP_DIRECTORY_PATH + "/etc/openvpn"; // path to vpn config directory extracted from configuration
        public static final String TMP_VPN_CONFIG_PATH_REGEX = "^client.*\\.conf$"; // regex for OpenVPN configuration extracted from configuration
        public static final String GEN_DIRECTORY_PATH = "EXTERNAL/gen"; // path to gen directory created in external storage
        public static final String CERTS_DIRECTORY_PATH = CONFIG_DIRECTORY_PATH + "/certs"; // path to certs directory created in external storage
        public static final String KEYS_DIRECTORY_PATH = CONFIG_DIRECTORY_PATH + "/keys"; // path to keys directory created in external storage
        public static final String CRYPTO_DIRECTORY_PATH = CONFIG_DIRECTORY_PATH + "/crypto"; // path to crypto directory created in external storage
        public static final String TOPOLOGY_PATH = CONFIG_DIRECTORY_PATH + "/topology.json"; // path to topology file created in external storage
        public static final String TOPOLOGY_TEMPLATE_PATH = "topology.json"; // path to topology file template, located in assets folder
        public static final int GEN_DIRECTORY_FILE_LIMIT = 100; // number of files allowed in imported directory (failsafe if the user chooses wrong)
        public static final String CERTS_DIRECTORY_PATH_REGEX = "^certs$"; // regex for the certs directory
        public static final String KEYS_DIRECTORY_PATH_REGEX = "^keys$"; // regex for the keys directory
        public static final String CRYPTO_DIRECTORY_PATH_REGEX = "^crypto$"; // regex for crypto directory
        public static final String TOPOLOGY_PATH_REGEX = "^topology\\.json$"; // regex for the topology file
        public static final String BORDER_ROUTERS_JSON_PATH = "border_routers"; // JSON path for border routers object in topology file
        public static final String INTERFACES_JSON_PATH = "interfaces"; // JSON path for interfaces object in topology file
        public static final String IA_JSON_PATH = "isd_as"; // JSON path for IA string in topology file
        public static final String UNDERLAY_JSON_PATH = "underlay"; // JSON path for underlay object in topology file
        public static final String REMOTE_UNDERLAY_JSON_PATH = "remote"; // JSON path for public overlay object in topology file
        public static final String PUBLIC_UNDERLAY_JSON_PATH = "public"; // JSON path for public overlay object in topology file
    }

    public static class VPNClient {
        public static final String PACKAGE_NAME = "de.blinkt.openvpn"; // package name of OpenVPN application
        static final int CRASH_INTERVAL = 1000; // how frequently (in ms) to check whether OpenVPN has crashed
        static final String NOPROCESS_STATE = "NOPROCESS"; // state of OpenVPN application when no process is running
        static final String VPN_GENERATE_CONFIG = "VPN_GENERATE_CONFIG"; // state of OpenVPN application when VPN configuration is generated
        static final String CONNECTED_STATE = "CONNECTED"; // OpenVPN connected state (see https://openvpn.net/community-resources/management-interface/)
    }
}
