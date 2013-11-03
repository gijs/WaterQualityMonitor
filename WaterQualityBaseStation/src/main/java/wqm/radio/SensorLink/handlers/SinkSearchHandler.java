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

import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import org.apache.log4j.Logger;
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.packets.SensorLinkPacket;
import wqm.radio.SensorLink.packets.SinkSearch;
import wqm.radio.TimedPacket;

/**
 * Date: 10/22/13
 * Time: 8:23 AM
 *
 * @author NigelB
 */
public class SinkSearchHandler implements PacketHandler<SinkSearch> {
    private static Logger logger = Logger.getLogger(SinkSearchHandler.class);
    public boolean handlePacket(PacketHandlerContext ctx, ZNetRxResponse xbeeResponse, SinkSearch packet) {
        if (packet.getPacketID() == SinkSearch.PACKET_ID) {

            XBeeAddress64 address = xbeeResponse.getRemoteAddress64();
            ZNetTxRequest tx = new ZNetTxRequest(address, ctx.getSinkPacket().getData());
            try {
                ctx.getQueue().put(new TimedPacket(tx, System.currentTimeMillis() + 500));
                logger.trace(tx);
                ctx.getSender().interrupt();
            } catch (InterruptedException e) {
                logger.error("Error adding packet to queue", e);
            }
        }
        return false;
    }

    public int getPacketId() {
        return SinkSearch.PACKET_ID;
    }
}
