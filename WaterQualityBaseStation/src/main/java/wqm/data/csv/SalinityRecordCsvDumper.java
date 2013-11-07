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

import wqm.radio.RecordStorage.record.SalinityRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Date: 11/4/13
 * Time: 5:35 PM
 *
 * @author NigelB
 */
public class SalinityRecordCsvDumper implements CsvDataDumper<SalinityRecord> {
    public Class getHandlerFor() {
        return SalinityRecord.class;
    }

    public synchronized void dumpData(File outputDir, String prefix, SalinityRecord record) throws IOException {
        File output = new File(outputDir, prefix+"__EC.csv");
        FileOutputStream fos;
        if (!output.exists())
        {
            fos = new FileOutputStream(output);
            fos.write("date,timestamp,id,ppm,salinity,uS\n".getBytes());
        }else
        {
            fos = new FileOutputStream(output, true);
        }
        String row = String.format("%s,%d,%d,%d,%d,%d\n", record.getDate().toString(), record.getDate().getTime(), record.getId(), record.getPpm(), record.getSalinity(), record.getUs());
        fos.write(row.getBytes());
        fos.close();
    }
}
