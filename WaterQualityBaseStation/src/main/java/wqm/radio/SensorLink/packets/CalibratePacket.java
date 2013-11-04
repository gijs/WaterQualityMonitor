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


    private static int SENSOR_OFFSET = 2;
    private static int FLAGS_OFFSET = SENSOR_OFFSET + 4;
    private static int VALUE1_OFFSET = FLAGS_OFFSET + 4;
    private static int EXPONENT1_OFFSET = VALUE1_OFFSET + 8;
    private static int VALUE2_OFFSET = EXPONENT1_OFFSET + 2;
    private static int EXPONENT2_OFFSET = VALUE2_OFFSET + 8;
    private static int VALUE3_OFFSET = EXPONENT2_OFFSET + 2;
    private static int EXPONENT3_OFFSET = VALUE3_OFFSET + 8;



    private int sensor;
    private int flags;
    private long value1;
    private int exponent1;
    private long value2;
    private int exponent2;
    private long value3;
    private int exponent3;


    private long time = System.currentTimeMillis();


    public CalibratePacket(int sensor, int flags, int value, int exponent) {
        super(new int[HEADER_SIZE]);
        setPacket_type(PACKET_ID);
        setHeader_length(HEADER_SIZE);
        setSensor(sensor);
        setFlags(flags);
        setValue1(value);
        setExponent1(exponent);
    }

    public CalibratePacket(int[] data) {
        super(data);
        sensor = getInt(data, SENSOR_OFFSET);
        flags = getInt(data, FLAGS_OFFSET);
        value1  = getLong(data, VALUE1_OFFSET);
        exponent1  = getShort(data, EXPONENT1_OFFSET);
        value2  = getLong(data, VALUE2_OFFSET);
        exponent2  = getShort(data, EXPONENT2_OFFSET);
        value3  = getLong(data, VALUE3_OFFSET);
        exponent3  = getShort(data, EXPONENT3_OFFSET);
    }

    public void setSensor(int sensor){
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

    public void setValue1(long value) {
        this.value1 = value;
        putLong(data, VALUE1_OFFSET, value);
    }

    public long getValue() {
        return value1;
    }

    public void setExponent1(int exponent) {
        this.exponent1 = exponent;
        putInt(data, EXPONENT1_OFFSET, exponent);
    }

    public int getExponent1() {
        return exponent1;
    }

    public double getValue(long value, int exponent) {
        return ((double)value) * Math.pow(10, exponent);

    }

    public long getValue1() {
        return value1;
    }

    public long getValue2() {
        return value2;
    }

    public int getExponent2() {
        return exponent2;
    }

    public long getValue3() {
        return value3;
    }

    public int getExponent3() {
        return exponent3;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("CalibratePacket{sensor=%s, flags=%d, value1=%s, value2=%s, value3=%s}", AtlasSensor.find(sensor), flags, getValue(value1, exponent1), getValue(value2, exponent2), getValue(value3, exponent3));
    }
}
