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

import wqm.config.AtlasSensor;
import wqm.radio.RecordStorage.record.DORecord;
import wqm.radio.RecordStorage.record.FloatRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Date: 11/7/13
 * Time: 1:40 PM
 *
 * @author NigelB
 */
public class DORecordCSVDumper implements CsvDataDumper<DORecord>  {
    public Class getHandlerFor()
    {
            return DORecord.class;
    }
    public synchronized void dumpData(File outputDir, String prefix, DORecord record) throws IOException {
        AtlasSensor sensor = AtlasSensor.find(record.getId());
        dump(outputDir, prefix, record, sensor);
    }


    public void dump(File outputDir, String prefix, DORecord record, AtlasSensor sensor) throws IOException {
        File output = new File(outputDir, String.format("%s__%s.csv", prefix, sensor.name()));
        FileOutputStream fos;
        if (!output.exists())
        {
            fos = new FileOutputStream(output);
            fos.write("date,timestamp,id,percent_saturation,do\n".getBytes());
        }else
        {
            fos = new FileOutputStream(output, true);
        }
        String row = String.format("%s,%d,%s,%s,%s\n", record.getDate().toString(), record.getDate().getTime(), record.getId(), record.getPercent(), record.getDO());
        fos.write(row.getBytes());
        fos.close();
    }
}
