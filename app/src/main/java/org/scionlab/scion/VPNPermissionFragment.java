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

package org.scionlab.scion;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Objects;
import java.util.function.Consumer;

import de.blinkt.openvpn.api.IOpenVPNAPIService;
import timber.log.Timber;

import static org.scionlab.scion.as.Config.VPNClient.*;

public class VPNPermissionFragment extends Fragment {
    private Consumer<String> callback;
    private ServiceConnection serviceConnection;
    private FragmentActivity activity;

    private VPNPermissionFragment(Consumer<String> callback) {
        this.callback = (String errorMessage) -> {
            activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
            callback.accept(errorMessage);
        };
    }

    static void askPermission(FragmentActivity activity, Consumer<String> callback) {
        activity.getSupportFragmentManager().beginTransaction()
                .add(new VPNPermissionFragment(callback), null).commit();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = requireActivity();

        if (!isPackageInstalled(PACKAGE_NAME)) {
            Timber.e("This app requires OpenVPN for Android");
            CharSequence text = "This app requires OpenVPN for Android\nPlease install it and try again";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
//            callback.accept("Please install OpenVPN for Android to run SCION, then try again.");
//             startActivity(new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("market://details?id=" + PACKAGE_NAME)));
            return;
        }

        ActivityResultLauncher<Intent> launchActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                            Timber.i("VPN permission granted");
                            callback.accept(null);
                        } else {
                            Timber.i("VPN permission denied");
                            callback.accept("The VPN permission is required to run SCION.");
                        }
                });

        Intent intent = new Intent(IOpenVPNAPIService.class.getName()).setPackage(PACKAGE_NAME);
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                IOpenVPNAPIService openVPNAPIService = IOpenVPNAPIService.Stub.asInterface(service);

                try {
                    Intent intent = openVPNAPIService.prepare(activity.getPackageName());
                    if (intent != null)
                        launchActivity.launch(intent);
                    else{
                        Timber.i("VPN permission granted");
                        callback.accept(null);
                    }
                } catch (RemoteException e) {
                    Timber.e(e);
                    callback.accept(e.getLocalizedMessage());
                }
            }

            public void onServiceDisconnected(ComponentName className) {
            }
        };
        activity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (serviceConnection != null)
            activity.unbindService(serviceConnection);
    }



    @SuppressWarnings("SameParameterValue")
    private boolean isPackageInstalled(String packageName) {
        try {
            activity.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
