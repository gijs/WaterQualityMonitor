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

package wqm.radio.SensorLink.packets;

import org.apache.log4j.Logger;
import wqm.data.RecordStorage.record.BaseRecord;

import java.util.ArrayList;

import static wqm.radio.util.Util.getInt;
import static wqm.radio.util.Util.toUnsignedInt;

/**
 * Date: 10/21/13
 * Time: 3:31 PM
 *
 * @author NigelB
 */
public class DataUpload extends SensorLinkPacket {
    private static Logger logger = Logger.getLogger(DataUpload.class);
    public static final int PACKET_ID = 2;
    public static final int HEADER_SIZE = 12;

    private static int FLAGS_OFFSET = 2;
    private static int SEQUENCE_OFFSET = 6;
    private static int ROW_SIZE_OFFSET = 10;
    private static int ROW_COUNT_OFFSET = 11;

    private ArrayList<BaseRecord> records = new ArrayList<BaseRecord>();

//    uint8_t  packet_type;
//    uint8_t  header_size;

//    uint32_t flags;
//    uint32_t senquence;
//    uint8_t  row_size;
//    uint8_t  row_count;

    private int flags;
    private long sequence;
    private int row_size;
    private int row_count;

    public DataUpload(int[] data) {
        super(data);
        flags = getInt(data, FLAGS_OFFSET);
        sequence = toUnsignedInt(getInt(data, SEQUENCE_OFFSET));
        row_size = data[ROW_SIZE_OFFSET];
        row_count = data[ROW_COUNT_OFFSET];

        for (int i = getHeader_length(), row_pos = 0; row_pos < row_count; i += row_size, row_pos++) {
            int record[] = new int[row_size];
            System.arraycopy(data, i, record, 0, row_size);
            try{
                BaseRecord rec = BaseRecord.constructPacket(record);
                logger.trace(rec);
                if (rec != null) {
                    records.add(rec);
                }
            }catch(Throwable t)
            {
                logger.error("Error constructing SensorLink Packet", t);
            }
        }
    }

    @Override
    public int getPacketID() {
        return PACKET_ID;
    }

    public ArrayList<BaseRecord> getRecords() {
        return records;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (BaseRecord record : records) {
            buf.append("\n\t").append(record);
        }
        return String.format("DataUpload{ headerLength=%d, flags=%d, sequence=%d, row size=%d, row count=%d, records =[%s]}",
                getHeader_length(), flags, sequence, row_size, row_count, buf.toString());
    }
}
