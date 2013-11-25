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

package wqm.data.csv;

import org.apache.log4j.Logger;
import wqm.constants.AtlasSensor;
import wqm.data.RecordStorage.record.FloatRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Date: 11/4/13
 * Time: 5:33 PM
 *
 * @author NigelB
 */
public class FloatRecordCsvDumper implements CsvDataDumper<FloatRecord> {
    private static Logger logger = Logger.getLogger(FloatRecordCsvDumper.class);
    public Class getHandlerFor()
    {
        return FloatRecord.class;
    }

    public synchronized void dumpData(File outputDir, String prefix, FloatRecord record) throws IOException {
        AtlasSensor sensor = AtlasSensor.find(record.getId());
                dump(outputDir, prefix, record, sensor);
    }


    private void dump(File outputDir, String prefix, FloatRecord record, AtlasSensor sensor) throws IOException {
        File output = new File(outputDir, String.format("%s__%s.csv", prefix, sensor.name()));
        FileOutputStream fos;
        if (!output.exists())
        {
            fos = new FileOutputStream(output);
            fos.write(String.format("date,timestamp,id,%s\n", sensor.name().toLowerCase()).getBytes());
        }else
        {
            fos = new FileOutputStream(output, true);
        }
        String row = String.format("%s,%d,%s,%s\n", record.getDate().toString(), record.getDate().getTime(), record.getId(), record.getValue1());
        fos.write(row.getBytes());
        fos.close();
    }
}
