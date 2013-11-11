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

package wqm.radio.SensorLink.message;

import wqm.config.Station;
import wqm.radio.SensorLink.packets.CalibratePacket;

/**
 * Date: 11/10/13
 * Time: 1:35 PM
 *
 * @author NigelB
 */
public class CalibrationMessage {
    private Station to;
    private CalibratePacket packet;

    public CalibrationMessage(Station to, CalibratePacket packet) {
        this.to = to;
        this.packet = packet;
    }

    public Station getTo() {
        return to;
    }

    public void setTo(Station to) {
        this.to = to;
    }

    public CalibratePacket getPacket() {
        return packet;
    }

    public void setPacket(CalibratePacket packet) {
        this.packet = packet;
    }
}
