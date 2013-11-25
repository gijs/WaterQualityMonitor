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

package wqm.data.RecordStorage;

import wqm.radio.util.Util;

import java.nio.ByteBuffer;

/**
 * Date: 11/25/13
 * Time: 11:00 AM
 *
 * @author NigelB
 */
public class RecordHeader {
    public static final int MAX_ADDRESS = 8;
    public static final int UPLOADED_ADDRESS = MAX_ADDRESS + 4;
    public static final int ID_MSB_ADDRESS = UPLOADED_ADDRESS + 4;
    public static final int ID_LSB_ADDRESS = ID_MSB_ADDRESS + 4;
    public static final int RECORD_SIZE = 6 * 4;

//    uint64_t magicNumber;
//    int32_t maxRecordSize;
//    int32_t uploaded;
//    int32_t id_msb;
//    int32_t id_lsb;

    private byte[] magicNumber = new byte[8];
    private int maxRecordSize;
    private int uploaded;
    private int id_msb;
    private int id_lsb;

    public RecordHeader(byte[] header) {
        if(header.length != RECORD_SIZE)
        {
            throw new RuntimeException("Header data wrong size.");
        }
        ByteBuffer buf = ByteBuffer.wrap(header);
        buf.get(magicNumber);
        maxRecordSize = Integer.reverseBytes(buf.getInt());
        uploaded = Integer.reverseBytes(buf.getInt());
        id_msb = Integer.reverseBytes(buf.getInt());
        id_lsb = Integer.reverseBytes(buf.getInt());
    }

    public int getMaxRecordSize() {
        return maxRecordSize;
    }

    public int getUploaded() {
        return uploaded;
    }

    public int getId_msb() {
        return id_msb;
    }

    public int getId_lsb() {
        return id_lsb;
    }

    @Override
    public String toString() {
        return "RecordHeader{ magicNumber="+Util.toHexString(magicNumber)+", maxRecordSize="+maxRecordSize+", uploaded="+uploaded+", id_msb="+Integer.toHexString(id_msb)+", id_lsb="+Integer.toHexString(id_lsb)+" }";
    }
}
