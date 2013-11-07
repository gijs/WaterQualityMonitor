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

package wqm.data.highstock;

import wqm.radio.RecordStorage.record.SalinityRecord;

import java.util.*;

/**
 * Date: 11/7/13
 * Time: 7:12 AM
 *
 * @author NigelB
 */
public class SalinityRecordConverter extends AbstractRecordConverter implements HighStockRecordConverter<SalinityRecord> {
    public Class<SalinityRecord> getRecordType() {
        return SalinityRecord.class;
    }

    public void convert(SalinityRecord data, Map<String, List> result) {
        Hashtable<String, Integer> value = new Hashtable<String, Integer>();

        value.put("PPM", data.getPpm());
        value.put("uS", data.getUs());
        value.put("Salinity", data.getSalinity());

        Object[] PPM = new Object[]{data.getDate().getTime(), data.getPpm()};
        Object[] uS = new Object[]{data.getDate().getTime(), data.getUs()};
        Object[] Salinity = new Object[]{data.getDate().getTime(), data.getSalinity()};

        getSeries("PPM", result).add(PPM);
        getSeries("uS", result).add(uS);
        getSeries("Salinity", result).add(Salinity);
    }
}
