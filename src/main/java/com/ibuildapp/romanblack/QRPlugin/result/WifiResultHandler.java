/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.QRPlugin.result;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import com.ibuildapp.romanblack.QRPlugin.QRPlugin;
import com.ibuildapp.romanblack.QRPlugin.R;
import com.ibuildapp.romanblack.QRPlugin.wifi.WifiConfigManager;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.WifiParsedResult;

/**
 * Handles address book entries.
 */
public final class WifiResultHandler extends ResultHandler {

    private final QRPlugin parent;

    public WifiResultHandler(QRPlugin activity, ParsedResult result) {
        super(activity, result);
        parent = activity;
    }

    @Override
    public int getButtonCount() {
        // We just need one button, and that is to configure the wireless.  This could change in the future.
        return 1;
    }

    @Override
    public int getButtonText(int index) {
        return R.string.button_wifi;
    }

    @Override
    public void handleButtonPress(int index) {
        // Get the underlying wifi config
        WifiParsedResult wifiResult = (WifiParsedResult) getResult();
        if (index == 0) {
            String ssid = wifiResult.getSsid();
            String password = wifiResult.getPassword();
            String networkType = wifiResult.getNetworkEncryption();
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            Toast.makeText(getActivity(), R.string.wifi_changing_network, Toast.LENGTH_LONG).show();
            WifiConfigManager.configure(wifiManager, ssid, password, networkType);
            parent.restartPreviewAfterDelay(0L);
        }
    }

    // Display the name of the network and the network type to the user.
    @Override
    public CharSequence getDisplayContents() {
        WifiParsedResult wifiResult = (WifiParsedResult) getResult();
        StringBuilder contents = new StringBuilder(50);
        String wifiLabel = parent.getString(R.string.wifi_ssid_label);
        ParsedResult.maybeAppend(wifiLabel + '\n' + wifiResult.getSsid(), contents);
        String typeLabel = parent.getString(R.string.wifi_type_label);
        ParsedResult.maybeAppend(typeLabel + '\n' + wifiResult.getNetworkEncryption(), contents);
        return contents.toString();
    }

    @Override
    public int getDisplayTitle() {
        return R.string.result_wifi;
    }
}