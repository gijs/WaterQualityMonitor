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

package wqm.data;

import wqm.radio.RecordStorage.record.FloatRecord;

import java.io.File;

/**
 * Date: 11/4/13
 * Time: 5:33 PM
 *
 * @author NigelB
 */
public class FloatRecordCsvDumper implements CsvDataDumper<FloatRecord> {
    public Integer getPacketType() {
        return FloatRecord.RECORD_TYPE;
    }

    public void dumpData(File outputDir, String prefix, FloatRecord record) {

    }
}
