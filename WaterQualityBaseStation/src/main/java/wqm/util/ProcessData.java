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

package wqm.util;

import com.rapplogic.xbee.api.XBeeAddress64;
import org.apache.commons.cli.*;
import wqm.PluginManager;
import wqm.data.RecordStorage.RecordHeader;
import wqm.data.RecordStorage.record.BaseRecord;
import wqm.data.csv.CsvDataDumper;
import wqm.radio.util.AddressUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import static wqm.radio.util.Util.createXBeeAddress64;
import static wqm.radio.util.Util.toIntArray;

/**
 * Date: 11/25/13
 * Time: 10:33 AM
 *
 * @author NigelB
 */
public class ProcessData {

    public static void main(String[] args) throws ParseException, IOException {
        Option help = OptionBuilder.withLongOpt("help")
                .withDescription("Display this help message.")
                .create('h');

        Options options = new Options();

        options.addOption(help);
        CommandLineParser p = new GnuParser();
        CommandLine toRet = p.parse(options, args);
        if(toRet.getArgs().length != 2)
        {
            System.err.printf("Error: only expected 2 parameters, found %d%n", toRet.getArgs().length);
            System.err.flush();
        }
        if (toRet.hasOption('h') || toRet.getArgs().length != 2) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("process_data <input_file> <output_directory>", options);
            System.exit(0);
        }

        Hashtable<Class, CsvDataDumper> dumpers = new Hashtable<Class, CsvDataDumper>();
        List<CsvDataDumper> _handlers = PluginManager.<CsvDataDumper>getPlugins(CsvDataDumper.class, null);
        for (CsvDataDumper handler : _handlers) {
            dumpers.put(handler.getHandlerFor(), handler);
        }

        File dataDir = new File(toRet.getArgs()[1]);
        FileInputStream fis = new FileInputStream(new File(toRet.getArgs()[0]));

        byte[] _header = new byte[RecordHeader.RECORD_SIZE];
        fis.read(_header);
        RecordHeader header = new RecordHeader(_header);

        XBeeAddress64 addr = new XBeeAddress64();
        File outputDir = new File(dataDir, AddressUtil.getCompactStringAddress(createXBeeAddress64(header.getId_msb(), header.getId_lsb())));
        outputDir.mkdirs();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyy_MMMMM_dd_HH-mm-ss");

        System.out.println(header);
        byte[] recordData = new byte[header.getMaxRecordSize()];
        int count = 0;
        long current, remainder;
        long rotatePeriod = 24 * 60 * 60 * 1000;
        String prefix;
        while((count = fis.read(recordData)) == recordData.length)
        {
            BaseRecord rec = BaseRecord.constructPacket(toIntArray(recordData));
            current = rec.getDate().getTime();
            remainder = current % rotatePeriod;
            prefix = fmt.format(new Date(current - remainder));
            dumpers.get(rec.getClass()).dumpData(outputDir, prefix, rec);
        }
        System.out.println(count);
    }
}
