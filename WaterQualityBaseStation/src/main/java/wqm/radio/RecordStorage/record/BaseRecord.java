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

package wqm.radio.RecordStorage.record;

import org.apache.log4j.Logger;
import wqm.constants.AtlasSensor;
import wqm.radio.util.Util;

import java.util.Date;

/**
 * Date: 10/22/13
 * Time: 10:03 AM
 *
 * @author NigelB
 */
public class BaseRecord {
    private static Logger logger = Logger.getLogger(BaseRecord.class);
//    int8_t record_type;
    protected int record_type;
    protected int data[];
    protected Date date;

    public BaseRecord(int[] data) {
        this.data = data;
        record_type = data[0];
        logger.trace("Received Data packet: "+ Util.toHexString(data));
    }

    public BaseRecord() {
    }

    public static BaseRecord constructPacket(int[] data)
    {
        switch(data[0])
        {

            case FloatRecord.RECORD_TYPE:

                if(data[1] == AtlasSensor.DO.getId())
                {
                    return new DORecord(data);
                }
                return new FloatRecord(data);
            case OneWireRecord.RECORD_TYPE:
                return new OneWireRecord(data);
            case SalinityRecord.RECORD_TYPE:
                return new SalinityRecord(data);
        }
        return null;
    }

    public int getRecord_type() {
        return record_type;
    }

    public Date getDate() {
        return date;
    }
}
