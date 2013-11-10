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

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date: 10/21/13
 * Time: 3:58 PM
 *
 * @author NigelB
 */
public class Util {

    public static int getInt(int[] data, int offset) {
        ByteBuffer buf = littleEndianGrab(data, offset, 4);
        return buf.getInt(0);
    }

    public static int _getInt(int[] data, int offset) {
        ByteBuffer buf = bigEndianGrab(data, offset, 4);
        return buf.getInt(0);
    }

    public static int getShort(int[] data, int offset) {
        ByteBuffer buf = littleEndianGrab(data, offset, 2);
        return buf.getShort(0);
    }

    public static long getLong(int[] data, int offset) {
        ByteBuffer buf = littleEndianGrab(data, offset, 8);
        return buf.getLong(0);
    }

    public static void putInt(int[] data, int offset, int value) {
        byte _data[] = new byte[4];
        ByteBuffer buf = ByteBuffer.wrap(_data);
        buf.putInt(value);

        for (int pos = offset, count = 3; count >= 0; pos++, count--) {
            data[pos] = _data[count];
        }
    }

    public static void putLong(int[] data, int offset, long value) {
        byte _data[] = new byte[8];
        ByteBuffer buf = ByteBuffer.wrap(_data);
        buf.putLong(value);

        for (int pos = offset, count = 7; count >= 0; pos++, count--) {
            data[pos] = _data[count];
        }
    }

//    public static void _putInt(int[] data, int offset, int value) {
//        byte _data[] = new byte[4];
//        ByteBuffer buf = ByteBuffer.wrap(_data);
//        buf.putInt(value);
//
//        for (int pos = offset, count = 0; count < 4; pos++, count++) {
//            data[pos] = _data[count];
//        }
//    }

    private static ByteBuffer littleEndianGrab(int[] data, int offset, int size)
    {
        byte _data[] = new byte[size];
        for (int pos = offset, count = size - 1; count >= 0; pos++, count--) {
            _data[count] = (byte) data[pos];
        }
        return ByteBuffer.wrap(_data);
    }

    private static ByteBuffer bigEndianGrab(int[] data, int offset, int size)
    {
        byte _data[] = new byte[size];
        for (int pos = offset, count = 0; count < size; pos++, count++) {
            _data[pos] = (byte) data[pos];
        }
        return ByteBuffer.wrap(_data);
    }

    public static long toUnsignedInt(int value) {
        if (value >= 0) {
            return value;
        } else {
            return ((long) value) >>> 32;
        }
    }

    public static String toHexString(int[] data) {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        String del = "";
        for (int i : data) {
            String s = Integer.toHexString(i);

            buf.append(del).append("0x");
            if (s.length() == 1) {
                buf.append("0");
            }
            buf.append(s);
            del = ", ";
        }
        buf.append("]");
        return buf.toString();
    }

    public static String toCompactHexString(int[] data) {
        StringBuilder buf = new StringBuilder();
        buf.append("0x");
        for (int i : data) {
            String s = Integer.toHexString(i);
            if (s.length() == 1) {
                buf.append("0");
            }
            buf.append(s);
        }
        return buf.toString();
    }

    public static Date createDate(long time_stamp)
    {
        Date _date = new Date(time_stamp * 1000);
        int offset = TimeZone.getDefault().getOffset(_date.getTime());
        return new Date(_date.getTime() - offset);

    }

    public static float getIEEE754(int[] data, int offset) {
        ByteBuffer buf = littleEndianGrab(data, offset, 4);
        return buf.getFloat(0);
    }

    public static void putIEEE754(int[] data, int offset, float value) {
        byte _data[] = new byte[4];
        ByteBuffer buf = ByteBuffer.wrap(_data);
        buf.putFloat(value);

        for (int pos = offset, count = 3; count >= 0; pos++, count--) {
            data[pos] = _data[count];
        }
    }
}
