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

package wqm.radio.SensorLink.handlers;

import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import org.apache.log4j.Logger;
import wqm.PluginManager;
import wqm.data.csv.CsvDataDumper;
import wqm.radio.DataSource;
import wqm.data.RecordStorage.record.BaseRecord;
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.packets.DataUpload;
import wqm.radio.util.AddressUtil;
import wqm.web.server.WQMConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * Date: 11/2/13
 * Time: 1:47 PM
 *
 * @author NigelB
 */
public class FileSystemDataHandler implements PacketHandler<DataUpload>, DataSource {
    private static Logger logger = Logger.getLogger(FileSystemDataHandler.class);
    private WQMConfig config;
    private File dataDir;
    private long rotatePeriod;

    private Hashtable<Class, CsvDataDumper> dumpers = new Hashtable<Class, CsvDataDumper>();

    private SimpleDateFormat fmt;

    public FileSystemDataHandler(WQMConfig config) {
            this.config = config;
            dataDir = new File(config.getDataConfig().getDataOutputDirectory(), "WQMData");
            rotatePeriod = config.getDataConfig().getRotatePeriod();
            fmt = config.getDataConfig().toFormat();
            dataDir.mkdirs();
            List<CsvDataDumper> _handlers = PluginManager.<CsvDataDumper>getPlugins(CsvDataDumper.class, null);
            for (CsvDataDumper handler : _handlers) {
                dumpers.put(handler.getHandlerFor(), handler);
            }

    }


    public boolean handlePacket(PacketHandlerContext ctx, ZNetRxResponse xbeeResponse, DataUpload packet) {

        File outputDir = new File(dataDir, AddressUtil.getCompactStringAddress(xbeeResponse.getRemoteAddress64()));
        outputDir.mkdirs();
        long currentTime, remainder;
//        long remainder = currentTime % rotatePeriod;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyy_MMMMM_dd_HH-mm-ss");
        String prefix;

        for (BaseRecord rec : packet.getRecords()) {
            if (dumpers.containsKey(rec.getClass())) {
                try {
                    currentTime = rec.getDate().getTime();
                    remainder = currentTime % rotatePeriod;
                    prefix = fmt.format(new Date(currentTime - remainder));
                    dumpers.get(rec.getClass()).dumpData(outputDir, prefix, rec);
                } catch (IOException e) {
                    logger.error("Error writing data:", e);
                }
            } else {

                logger.error(String.format("Cannot find data handler for: %s", rec.toString()));
            }

        }
        return true;
    }

    public int getPacketId() {
        return DataUpload.PACKET_ID;
    }

}
