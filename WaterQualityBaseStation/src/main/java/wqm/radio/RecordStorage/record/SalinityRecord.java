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
 * Time: 2:42 PM
 *
 * @author NigelB
 */
public class SalinityRecord extends BaseRecord{
    public static final int RECORD_TYPE = 3;

    public static final int ID_ADDRESS = 1;
    public static final int TIME_STAMP_ADDRESS = 2;
    public static final int uS_ADDRESS = 6;
    public static final int PPM_ADDRESS = 10;
    public static final int SALINITY_ADDRESS = 14;

    int id;
    long time_stamp;
    int us, ppm, salinity;

    public SalinityRecord(int[] data) {
        super(data);
        id = data[ID_ADDRESS];
        time_stamp = Util.toUnsignedInt(Util.getInt(data, TIME_STAMP_ADDRESS));
        us = Util.getInt(data, uS_ADDRESS);
        ppm = Util.getInt(data, PPM_ADDRESS);
        salinity = Util.getInt(data, SALINITY_ADDRESS);

        date = Util.createDate(time_stamp);
    }

    @Override
    public String toString() {
        return String.format("SalinityRecord{ id=%d, timestamp=%d, date=%s, us=%d, ppm=%d, salinity=%d}", id, time_stamp, date, us, ppm, salinity);
    }

    public int getUs() {
        return us;
    }

    public int getPpm() {
        return ppm;
    }

    public int getSalinity() {
        return salinity;
    }

    public int getId() {
        return id;
    }
}
