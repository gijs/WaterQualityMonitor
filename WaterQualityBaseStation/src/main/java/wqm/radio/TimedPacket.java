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

package wqm.radio;

import com.rapplogic.xbee.api.XBeeRequest;

/**
 * Date: 10/22/13
 * Time: 8:17 AM
 *
 * @author NigelB
 */
public class TimedPacket implements Comparable<TimedPacket> {
    XBeeRequest packet;
    long sendAt;

    public TimedPacket(XBeeRequest packet, long sendAt) {
        this.packet = packet;
        this.sendAt = sendAt;
    }

    public int compareTo(TimedPacket o) {
        return (int) (sendAt - ((TimedPacket) o).sendAt);
    }

    @Override
    public String toString() {
        return String.valueOf(sendAt);
    }
}
