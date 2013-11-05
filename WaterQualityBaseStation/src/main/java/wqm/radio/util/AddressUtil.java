/*
 * Water Quality Monitor Java Basestation
 * Copyright (C) 2013  nigelb
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package wqm.radio.util;

import com.rapplogic.xbee.api.XBeeAddress64;

/**
 * Date: 11/4/13
 * Time: 7:25 AM
 *
 * @author NigelB
 */
public class AddressUtil {

    public static String getStringAddress(XBeeAddress64 address) {
        int[] addr = address.getAddress();
        return String.format("%02X %02X %02X %02X %02X %02X %02X %02X",
                addr[0],
                addr[1],
                addr[2],
                addr[3],
                addr[4],
                addr[5],
                addr[6],
                addr[7]);
    }

    public static String getCompactStringAddress(XBeeAddress64 address)
    {
        int[] addr = address.getAddress();
        return String.format("%02X%02X%02X%02X%02X%02X%02X%02X",
                addr[0],
                addr[1],
                addr[2],
                addr[3],
                addr[4],
                addr[5],
                addr[6],
                addr[7]);
    }
}
