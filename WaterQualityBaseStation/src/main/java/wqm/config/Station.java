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

package wqm.config;

import com.rapplogic.xbee.api.XBeeAddress64;
import wqm.radio.Util;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Date: 11/2/13
 * Time: 11:46 AM
 *
 * @author NigelB
 */
public class Station {
    private String commonName = "";
    private XBeeAddress64 address = new XBeeAddress64();
    private boolean locked = false;

    public Station() {
    }

    public Station(String commonName, XBeeAddress64 address) {
        this.commonName = commonName;
        this.address = address;
    }

    @XmlTransient
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @XmlAttribute
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }


    @XmlAttribute(name = "Address")
    public String get_Address() {
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

    public void set_Address(String address) {
        this.address = new XBeeAddress64(address);
    }

    public String getCompactAddress()
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

    public XBeeAddress64 getAddress() {
        return address;
    }

    public String getDisplayName()
    {
        if(commonName == null || commonName.length() == 0)
        {
            return get_Address();
        }
        return commonName;
    }
}
