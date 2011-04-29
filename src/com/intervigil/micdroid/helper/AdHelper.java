/* AdHelper.java

   Copyright (c) 2010 Ethan Chen

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.intervigil.micdroid.helper;

import java.util.HashMap;
import java.util.Map;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class AdHelper {

    public static void GenerateAd(AdView ad, boolean enabled) {
        if (enabled) {
            ad.setEnabled(true);
            Map<String, Object> extras = new HashMap<String, Object>();
            extras.put("color_bg", "000000");
            extras.put("color_text", "FFFFFF");
            AdRequest adRequest = new AdRequest();
            adRequest.setExtras(extras);
            ad.loadAd(adRequest);
        } else {
            ad.setEnabled(false);
        }
    }
}
