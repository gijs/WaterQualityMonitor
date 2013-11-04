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
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.packets.DataUpload;
import wqm.radio.SensorLink.packets.SensorLinkPacket;
import wqm.radio.exceptions.InvalidPacketHandler;

import java.util.ArrayList;

/**
 * Date: 10/22/13
 * Time: 8:43 AM
 *
 * @author NigelB
 */
public class DataUploadHandler implements PacketHandler<DataUpload> {
    private static Logger logger = Logger.getLogger(DataUploadHandler.class);
    private ArrayList<PacketHandler> handlers = new ArrayList<PacketHandler>();


    public boolean handlePacket(PacketHandlerContext ctx, ZNetRxResponse xbeeResponse, DataUpload packet) {
        logger.info(packet);
        for (PacketHandler handler : handlers) {
            handler.handlePacket(ctx, xbeeResponse, packet);
        }
        return false;
    }

    public int getPacketId() {
        return DataUpload.PACKET_ID;
    }

    public void registerHandler(PacketHandler handler) {
        if(handler.getPacketId() == getPacketId())
        {
            handlers.add(handler);
        }else
        {
            logger.error(handler);
            throw new InvalidPacketHandler(String.format("Expecting packet id: %d got: %d", getPacketId(), handler.getPacketId()));
        }
    }
}
