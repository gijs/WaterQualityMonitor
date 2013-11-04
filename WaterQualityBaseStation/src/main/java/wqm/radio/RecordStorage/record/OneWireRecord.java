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

package wqm.radio.RecordStorage.record;

import wqm.radio.util.Util;

import java.util.Date;

/**
 * Date: 10/22/13
 * Time: 10:22 AM
 *
 * @author NigelB
 */
public class OneWireRecord extends BaseRecord {
    public static final int RECORD_TYPE = 2;

    public static final int ID_ADDRESS = 1;
    public static final int TIME_STAMP_ADDRESS = 9;
    public static final int CHARACTERISTIC_ADDRESS = 13;
    public static final int EXPONENT_ADDRESS = CHARACTERISTIC_ADDRESS + 8;

//    int8_t id[8];
//    time_t time_stamp;
//    int8_t characteristic;
//    int8_t mantissa;

    int[] id = new int[8];
    long time_stamp;
    long characteristic;
    int exponent;
    Date date;

    public OneWireRecord(int[] data) {
        super(data);
        System.arraycopy(data, ID_ADDRESS, id, 0, id.length);
        time_stamp = Util.toUnsignedInt(Util.getInt(data, TIME_STAMP_ADDRESS));
        characteristic = data[CHARACTERISTIC_ADDRESS];
        characteristic = Util.getLong(data, CHARACTERISTIC_ADDRESS);
        exponent = Util.getShort(data, EXPONENT_ADDRESS);
        date = Util.createDate(time_stamp);
    }

    public double getValue() {
        return ((double)characteristic) * Math.pow(10, exponent);

    }

    @Override
    public String toString() {
        return String.format("OneWireRecord{ id=%s, timestamp=%d, date=%s, characteristic=%d, exponent=%d, value=%s}", Util.toHexString(id), time_stamp, date, characteristic, exponent, getValue());
    }

    public int[] getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }
}
