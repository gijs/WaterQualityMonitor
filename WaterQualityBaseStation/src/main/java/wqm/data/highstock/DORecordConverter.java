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

import wqm.data.RecordStorage.record.BaseRecord;
import wqm.data.RecordStorage.record.DORecord;

import java.util.*;

/**
 * Date: 11/7/13
 * Time: 3:21 PM
 *
 * @author NigelB
 */
public class DORecordConverter<T extends BaseRecord> extends AbstractRecordConverter implements HighStockRecordConverter<DORecord> {
    public Class<DORecord> getRecordType() {
        return DORecord.class;
    }

    public void convert(DORecord data, Map<String, List> result) {
        Object[] DO = new Object[]{data.getDate().getTime(), data.getDO()};
        Object[] Percent = new Object[]{data.getDate().getTime(), data.getPercent()};

        getSeries("DO", result).add(DO);
        getSeries("Percent",result).add(Percent);
    }
}
