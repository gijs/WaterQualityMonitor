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

package wqm.constants;

import wqm.radio.SensorLink.packets.CalibratePacket;

/**
 * Date: 11/11/13
 * Time: 11:35 AM
 *
 * @author NigelB
 */
public enum ECSensorProbe {
    K_0_1("K0.1", 1,(int) CalibratePacket.EC_CALIBRATION_SENSOR_TYPE_K_0_1, 220, 3000),
    K_1_0("K1.0", 2,(int) CalibratePacket.EC_CALIBRATION_SENSOR_TYPE_K_1_0, 10500, 40000),
    K_10_0("K10.0", 3,(int) CalibratePacket.EC_CALIBRATION_SENSOR_TYPE_K_10_0, 62000, 90000);

    private final String name;
    private int atlasID;
    private final int packetVariable;
    private final int lowSide;
    private final int highSide;

    ECSensorProbe(String name, int atlasID, int packetVariable, int lowSide, int highSide) {
        this.name = name;
        this.atlasID = atlasID;
        this.packetVariable = packetVariable;
        this.lowSide = lowSide;
        this.highSide = highSide;
    }

    public String getName() {
        return name;
    }

    public int getAtlasID() {
        return atlasID;
    }

    public int getPacketVariable() {
        return packetVariable;
    }

    public int getLowSide() {
        return lowSide;
    }

    public int getHighSide() {
        return highSide;
    }

    public String getVariableName() {
        return String.format("k%d", atlasID);
    }

    public static ECSensorProbe findByAtlasID(Integer selected) {
        for (ECSensorProbe ecSensorProbe : values()) {
            if(selected == ecSensorProbe.atlasID )
            {
                return ecSensorProbe;
            }
        }
        return null;
    }

}
