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

package wqm.radio.SensorLink.packets;

import wqm.config.AtlasSensor;

import static wqm.radio.util.Util.*;

/**
 * Date: 10/31/13
 * Time: 7:19 AM
 *
 * @author NigelB
 */
public class CalibratePacket extends SensorLinkPacket {
    public static final byte PACKET_ID = 4;
    public static final byte HEADER_SIZE = 0x28;


    public static final int STOP_CALIBRATION = 1;
    public static final int START_CALIBRATION = 2;
    public static final int CALIBRATION_PHASE0 = 4;
    public static final int CALIBRATION_PHASE1 = 16;
    public static final int CALIBRATION_PHASE2 = 32;
    public static final int CALIBRATION_PHASE3 = 64;
    public static final int CALIBRATION_PHASE4 = 128;
    public static final int CALIBRATION_PHASE5 = 256;

    public static final float ORP_CALIBRATION_PLUS = 2;
    public static final float ORP_CALIBRATION_MINUS = 4;

    public static final float EC_CALIBRATION_SENSOR_TYPE_K_0_1 = 2;
    public static final float EC_CALIBRATION_SENSOR_TYPE_K_1_0 = 3;
    public static final float EC_CALIBRATION_SENSOR_TYPE_K_10_0 = 4;


    private static int SENSOR_OFFSET = 2;
    private static int FLAGS_OFFSET = SENSOR_OFFSET + 4;
    private static int VALUE1_OFFSET = FLAGS_OFFSET + 4;
    private static int VALUE2_OFFSET = VALUE1_OFFSET + 4;
    private static int VALUE3_OFFSET = VALUE2_OFFSET + 4;


    private int sensor;
    private int flags;
    private float value1;
    private float value2;
    private float value3;


    private long time = System.currentTimeMillis();


    public CalibratePacket(int sensor, int flags) {
        super(new int[HEADER_SIZE]);
        setPacket_type(PACKET_ID);
        setHeader_length(HEADER_SIZE);
        setSensor(sensor);
        setFlags(flags);
        setValue1(Float.POSITIVE_INFINITY);
        setValue2(Float.POSITIVE_INFINITY);
        setValue3(Float.POSITIVE_INFINITY);
    }

    public CalibratePacket(int[] data) {
        super(data);
        sensor = getInt(data, SENSOR_OFFSET);
        flags = getInt(data, FLAGS_OFFSET);
        value1 = getIEEE754(data, VALUE1_OFFSET);
        value2 = getIEEE754(data, VALUE2_OFFSET);
        value3 = getIEEE754(data, VALUE3_OFFSET);
    }

    public void setSensor(int sensor) {
        this.sensor = sensor;
        putInt(data, SENSOR_OFFSET, sensor);
    }

    @Override
    public int getPacketID() {
        return PACKET_ID;
    }


    public void setFlags(int flags) {
        this.flags = flags;
        putInt(data, FLAGS_OFFSET, flags);
    }

    public int getFlags() {
        return flags;
    }

    public void setValue1(float value) {
        this.value1 = value;
        putIEEE754(data, VALUE1_OFFSET, value);
    }

    public float getValue() {
        return value1;
    }

    public float getValue1() {
        return value1;
    }

    public float getValue2() {
        return value2;
    }

    public void setValue2(float value) {
        this.value1 = value;
        putIEEE754(data, VALUE2_OFFSET, value);
    }

    public float getValue3() {
        return value3;
    }

    public void setValue3(float value) {
        this.value1 = value;
        putIEEE754(data, VALUE3_OFFSET, value);
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("CalibratePacket{sensor=%s, flags=%d, value1=%s, value2=%s, value3=%s}", AtlasSensor.find(sensor), flags, value1, value2, value3);
    }
}
