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

import wqm.config.AtlasSensor;
import wqm.radio.RecordStorage.record.FloatRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/7/13
 * Time: 7:11 AM
 *
 * @author NigelB
 */
public class FloatRecordConverter extends AbstractRecordConverter implements HighStockRecordConverter<FloatRecord> {
    public Class<FloatRecord> getRecordType() {
        return FloatRecord.class;
    }

    public void convert(FloatRecord data, Map<String, List> result) {

        AtlasSensor sensor = AtlasSensor.find(data.getId());
        Object[] _result = new Object[]{data.getDate().getTime(), data.getValue1()};
        getSeries(sensor.name(), result).add(_result);
    }
}
