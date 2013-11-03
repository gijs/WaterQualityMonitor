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

package wqm.radio;

import com.rapplogic.xbee.api.*;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import org.apache.log4j.Logger;
import wqm.PluginManager;
import wqm.config.Port;
import wqm.config.Station;
import wqm.radio.SensorLink.handlers.DataUploadHandler;
import wqm.radio.SensorLink.handlers.PacketHandler;
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.packets.DataUpload;
import wqm.radio.SensorLink.packets.SensorLinkPacket;
import wqm.radio.SensorLink.packets.SinkSearch;
import wqm.web.server.WQMConfig;

import javax.naming.NamingException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static wqm.radio.Util.getInt;

/**
 * Date: 10/21/13
 * Time: 2:10 PM
 *
 * @author NigelB
 */
public class BaseStation implements PacketListener {
    private static Logger logger = Logger.getLogger(BaseStation.class);
    private final WQMConfig config;
    private final int retries;

    private int SH;
    private int SL;
    private XBee xbee = new XBee();

    private PacketHandlerContext ctx = new PacketHandlerContext();
    private Hashtable<Integer, PacketHandler> packetHandlers = new Hashtable<Integer, PacketHandler>();
    private Hashtable<XBeeAddress64, String> addresses = new Hashtable<XBeeAddress64, String>();

    private volatile boolean running = true;

    public BaseStation(Port port, WQMConfig config, List<PacketHandler> handlers) throws XBeeException, NamingException {
        String _port = port.getPort();
        int baud = port.getBaud();
        retries = port.getRetries();
        this.config = config;
        xbee.open(_port, baud);
        xbee.sendAtCommand(new AtCommand("SH"));
        XBeeResponse res = xbee.getResponse();

        SH = getInt(((AtCommandResponse) res).getValue(), 0);
        xbee.sendAtCommand(new AtCommand("SL"));
        res = xbee.getResponse();

        SL = getInt(((AtCommandResponse) res).getValue(), 0);
        ctx.setSinkPacket(new SinkSearch(SH, SL));

        xbee.addPacketListener(this);

        xbee.sendAtCommand(new AtCommand("AI"));
        res = xbee.getResponse();

        List<PacketHandler> _handlers = PluginManager.<PacketHandler>getPlugins(PacketHandler.class, null);
        for (PacketHandler handler : _handlers) {
            packetHandlers.put(handler.getPacketId(), handler);
        }

        DataUploadHandler hdl = (DataUploadHandler) packetHandlers.get(DataUpload.PACKET_ID);
        for (PacketHandler handler : handlers) {
            hdl.registerHandler(handler);
        }

        for (Station station : config.getStations()) {
            addresses.put(station.getAddress(), station.getCommonName() == null? "": station.getCommonName());
        }
    }

    public void run() throws InterruptedException, XBeeException {
        ctx.setSender(Thread.currentThread());
        LinkedBlockingQueue<TimedPacket> queue = ctx.getQueue();
        ArrayList<TimedPacket> toSend = new ArrayList<TimedPacket>();
        while (running) {
            while (running) {
                TimedPacket a = queue.poll();
                if (a != null) {
                    toSend.add(a);
                } else {
                    break;
                }
            }
            Collections.sort(toSend);
            if (toSend.size() >= 1 && System.currentTimeMillis() >= toSend.get(0).sendAt) {
                TimedPacket packet = toSend.get(0);


                XBeeResponse response;
                boolean success = false;
                for (int i = 0; i < retries; i++) {
                    response = xbee.sendSynchronous(packet.packet);
                    if (!response.isError()) {
                        success = true;
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException wakeup) {
                        logger.debug("Interrupted during retry.");
                    }
                }
                if(success)
                {
                    toSend.remove(0);
                }

            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException wakeup) {
                logger.debug("Woken");
            }

        }
    }

    public void processResponse(XBeeResponse xBeeResponse) {
        long time = System.currentTimeMillis();

        if (xBeeResponse.getApiId().getValue() == 0x90) {
            ZNetRxResponse res = (ZNetRxResponse) xBeeResponse;
            XBeeAddress64 addr = res.getRemoteAddress64();
            if(!addresses.containsKey(addr))
            {
                config.addStation(addr);
                addresses.put(addr, "");
                logger.warn(String.format("Added station: %s", addr));
            }
            try{
            SensorLinkPacket packet = SensorLinkPacket.constructPacket(res.getData(), time);
            if(packetHandlers.containsKey(packet.getPacketID()))
            {
                packetHandlers.get(packet.getPacketID()).handlePacket(ctx, res, packet);
            }else
            {
                logger.error(String.format("Could not find handler for packet: %s", packet));
            }
            }catch (Throwable e)
            {
                e.printStackTrace();
            }

        } else if (xBeeResponse.getApiId().getValue() == 0x88) {
            logger.trace(xBeeResponse);
        }
    }

    public void shutDown()
    {
        running = false;
        xbee.close();
    }
}