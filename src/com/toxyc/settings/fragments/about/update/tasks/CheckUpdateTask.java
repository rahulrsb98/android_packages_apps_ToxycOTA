/*
 * Copyright (C) 2018 ToxycOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.toxyc.settings.fragments.about.update.tasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.toxyc.settings.fragments.about.Update;
import com.toxyc.settings.fragments.about.update.configs.AppConfig;
import com.toxyc.settings.fragments.about.update.configs.LinkConfig;
import com.toxyc.settings.fragments.about.update.configs.OTAConfig;
import com.toxyc.settings.fragments.about.update.configs.OTAVersion;
import com.toxyc.settings.fragments.about.update.dialogs.WaitDialogHandler;
import com.toxyc.settings.fragments.about.update.tasks.NotificationHelper;
import com.toxyc.settings.fragments.about.update.utils.OTAUtils;
import com.toxyc.settings.fragments.about.update.xml.OTADevice;
import com.toxyc.settings.fragments.about.update.xml.OTAParser;
import com.toxyc.settings.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class CheckUpdateTask extends AsyncTask<Context, Void, OTADevice> {

    private static CheckUpdateTask mInstance = null;
    private final Handler mHandler = new WaitDialogHandler();
    private Context mContext;
    private boolean mIsBackgroundThread;

    private static final int NOTI_PRIMARY = 1100;
    private NotificationHelper noti;

    private CheckUpdateTask(boolean isBackgroundThread) {
        this.mIsBackgroundThread = isBackgroundThread;
    }

    public static CheckUpdateTask getInstance(boolean isBackgroundThread) {
        if (mInstance == null) {
            mInstance = new CheckUpdateTask(isBackgroundThread);
        }
        return mInstance;
    }

    @Override
    protected OTADevice doInBackground(Context... params) {
        mContext = params[0];

        if (!isConnectivityAvailable(mContext)) {
            return null;
        }

        showWaitDialog();

        OTADevice device = null;
        String deviceName = OTAUtils.getDeviceName(mContext);
        OTAUtils.logInfo("deviceName: " + deviceName);
        if (!deviceName.isEmpty()) {
            try {
                String otaUrl = OTAConfig.getInstance(mContext).getOtaUrl();
                InputStream is = OTAUtils.downloadURL(otaUrl);
                if (is != null) {
                    device = OTAParser.getInstance(mContext).parse(is, deviceName);
                    is.close();
                }
            } catch (IOException | XmlPullParserException e) {
                OTAUtils.logError(e);
            }
        }

        return device;
    }

    @Override
    protected void onPostExecute(OTADevice device) {
        super.onPostExecute(device);

        if (!isConnectivityAvailable(mContext)) {
            showToast(R.string.check_update_failed);
            Update.showLinks(false);
        } else if (device == null) {
            showToast(R.string.check_update_failed);
            Update.showLinks(false);
        } else {
            String latestVersion = device.getLatestVersion();
            String maintainer = device.getMaintainer();
            boolean updateAvailable = OTAVersion.checkServerVersion(latestVersion, mContext);
            if (updateAvailable) {
                showNotification(mContext);
            } else {
                showToast(R.string.no_update_available);
            }
            AppConfig.persistLatestVersion(latestVersion, mContext);
            AppConfig.persistMaintainer(maintainer, mContext);
            LinkConfig.persistLinks(device.getLinks(), mContext);
            Update.showLinks(device.isDeviceSupported() && !device.getLinks().isEmpty());
        }

        AppConfig.persistLastCheck(mContext);
        Update.updatePreferences(mContext);

        hideWaitDialog();

        mInstance = null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mInstance = null;
    }

    private void showWaitDialog() {
        if (!mIsBackgroundThread) {
            Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_SHOW_DIALOG);
            msg.obj = mContext;
            mHandler.sendMessage(msg);
        }
    }

    private void hideWaitDialog() {
        if (!mIsBackgroundThread) {
            Message msg = mHandler.obtainMessage(WaitDialogHandler.MSG_CLOSE_DIALOG);
            mHandler.sendMessage(msg);
        }
    }

    private void showToast(int messageId) {
        if (!mIsBackgroundThread) {
            OTAUtils.toast(messageId, mContext);
        }
    }

    private static boolean isConnectivityAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void showNotification(Context context) {
        Notification.Builder nb = null;
        String title = context.getString(R.string.notification_title);
        String body = context.getString(R.string.notification_message);
        if (noti == null) {
            noti = new NotificationHelper(context);
        }
        nb = noti.getNotification(context, title, body);

        if (nb != null) {
            noti.notify(NOTI_PRIMARY, nb);
        }
    }
}
