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

/**
 * Date: 11/7/13
 * Time: 1:36 PM
 *
 * @author NigelB
 */
public class DORecord extends FloatRecord {
    public DORecord(int[] data) {
        super(data);
    }
    public float getPercent()
    {
        return getValue1();
    }

    public float getDO()
    {
        return getValue2();
    }

    @Override
    public String toString() {
        return String.format("DORecord{ id=%d, timestamp=%d, date=%s, percentSaturation=%s, do=%s}",
                id,
                time_stamp,
                date,
                getPercent(),
                getDO());
    }}
