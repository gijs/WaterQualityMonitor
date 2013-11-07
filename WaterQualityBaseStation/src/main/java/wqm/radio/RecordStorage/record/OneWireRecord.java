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

/**
 * Date: 10/22/13
 * Time: 10:22 AM
 *
 * @author NigelB
 */
public class OneWireRecord extends BaseRecord {
    public static final int RECORD_TYPE = 2;

    public static final int ID_ADDRESS = 1;
    public static final int TIME_STAMP_ADDRESS = ID_ADDRESS + 8;
    public static final int VALUE_ADDRESS = TIME_STAMP_ADDRESS + 4;


//    int8_t id[8];
//    time_t time_stamp;
//    double value;

    int[] id = new int[8];
    long time_stamp;
    float value;

    public OneWireRecord(int[] data) {
        super(data);
        System.arraycopy(data, ID_ADDRESS, id, 0, id.length);
        time_stamp = Util.toUnsignedInt(Util.getInt(data, TIME_STAMP_ADDRESS));
        date = Util.createDate(time_stamp);
        value = Util.getIEEE754(data, VALUE_ADDRESS);
    }

    public double getValue() {
        return value;

    }

    @Override
    public String toString() {
        return String.format("OneWireRecord{ id=%s, timestamp=%d, date=%s, value=%s}", Util.toHexString(id), time_stamp, date, value);
    }

    public int[] getId() {
        return id;
    }


}
