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

package wqm.data.RecordStorage.record;

import wqm.radio.util.Util;

/**
 * Date: 10/22/13
 * Time: 10:03 AM
 *
 * @author NigelB
 */
public class FloatRecord extends BaseRecord{
    public static final int RECORD_TYPE = 1;

    public static final int ID_ADDRESS = 1;
    public static final int TIME_STAMP_ADDRESS = 2;
    public static final int VALUE1_ADDRESS = TIME_STAMP_ADDRESS + 4;
    public static final int VALUE2_ADDRESS = VALUE1_ADDRESS + 4;

    int id;
    long time_stamp;
    float value1;
    float value2;

    public FloatRecord(int[] data) {
        super(data);
        id = data[ID_ADDRESS];
        time_stamp = Util.toUnsignedInt(Util.getInt(data, TIME_STAMP_ADDRESS));
        date = Util.createDate(time_stamp);
        value1 = Util.getIEEE754(data, VALUE1_ADDRESS);
        value2 = Util.getIEEE754(data, VALUE2_ADDRESS);
    }

    public float getValue1() {
        return value1;
    }

    public float getValue2() {
        return value2;
    }

    @Override
    public String toString() {
        return String.format("FloatRecord{ id=%d, timestamp=%d, date=%s, value1=%s, value2=%s}",
                id,
                time_stamp,
                date,
                value1,
                value2);
    }

    public int getId() {
        return id;
    }

}
