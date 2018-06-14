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
package com.toxyc.settings.fragments.about.update.xml;

import java.util.ArrayList;
import java.util.List;

public class OTADevice {

    private List<OTALink> mLinks;
    private String mLatestVersion;
    private String mMaintainer;
    private boolean mDeviceSupported;

    public OTADevice() {
        mLinks = new ArrayList<>();
    }

    public void addLink(OTALink link) {
        mLinks.add(link);
    }

    public List<OTALink> getLinks() {
        return mLinks;
    }

    public void setLatestVersion(String latestVersion) {
        this.mLatestVersion = latestVersion;
    }

    public String getLatestVersion() {
        return mLatestVersion;
    }

    public void setMaintainer(String maintainer) {
        this.mMaintainer = maintainer;
    }

    public String getMaintainer() {
        return mMaintainer;
    }

    public void setDeviceFound(boolean supported) {
        this.mDeviceSupported = supported;
    }

    public boolean isDeviceSupported() {
        return mDeviceSupported;
    }
}
