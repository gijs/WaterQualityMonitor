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
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import org.apache.log4j.Logger;
import wqm.PluginManager;
import wqm.config.Port;
import wqm.config.Station;
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.handlers.PacketHandler;
import wqm.radio.SensorLink.packets.CalibratePacket;
import wqm.radio.SensorLink.packets.SensorLinkPacket;
import wqm.radio.SensorLink.packets.SinkSearch;
import wqm.radio.util.AddressUtil;
import wqm.radio.util.Util;
import wqm.web.server.WQMConfig;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static wqm.radio.util.Util.getInt;

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
    private Hashtable<Integer, List<PacketHandler>> packetHandlers = new Hashtable<Integer, List<PacketHandler>>();
    private Hashtable<XBeeAddress64, String> addresses = new Hashtable<XBeeAddress64, String>();
    private Hashtable<String, XBeeAddress64> seenStations = new Hashtable<String, XBeeAddress64>();


    private volatile boolean running = true;

    public BaseStation(Port port, WQMConfig config) throws XBeeException, NamingException {
        String _port = port.getPort();
        int baud = port.getBaud();
        ctx.setConfig(config);
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

        List<PacketHandler> _handlers = PluginManager.<PacketHandler>getPlugins(PacketHandler.class, new Object[]{config});
        for (PacketHandler handler : _handlers) {
            registerPacketHandler(handler);
        }

        for (Station station : config.getStations()) {
            addresses.put(station.getAddress(), station.getCommonName() == null ? "" : station.getCommonName());
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
                    try {
                        logger.info("Attempt: "+i+", "+packet.packet);

                        response = xbee.sendSynchronous(packet.packet);

                        if (!response.isError()) {
                            success = true;
                            i = retries + 1;
                        }

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException wakeup) {
                            logger.debug("Interrupted during retry.");
                        }
                    } catch (XBeeTimeoutException xe) {
                        logger.error("XBee exception", xe);
                        Thread.sleep(200);
                    }
                    if (success) {
                        toSend.remove(0);
                    }
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
            seenStations.put(AddressUtil.getCompactStringAddress(addr), addr);
            if (!addresses.containsKey(addr)) {
                config.addStation(addr);
                addresses.put(addr, "");
                logger.warn(String.format("Added station: %s", addr));
            }
            try {
                SensorLinkPacket packet = SensorLinkPacket.constructPacket(res.getData(), time);
                if (packet != null) {
                    if (packetHandlers.containsKey(packet.getPacketID())) {
                        List<PacketHandler> handlers = packetHandlers.get(packet.getPacketID());
                        for (PacketHandler handler : handlers) {
                            handler.handlePacket(ctx, res, packet);
                        }
                    } else {
                        logger.error(String.format("Could not find handler for packet: %s", packet));
                    }
                } else {
                    logger.error(String.format("Could not construct a packet from data: %s", Util.toHexString(packet.getData())));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        } else if (xBeeResponse.getApiId().getValue() == 0x88) {
            logger.trace(xBeeResponse);
        }
    }

    public boolean hasStation(String stationAddress) {
        return seenStations.get(stationAddress) != null;
    }

    public void shutDown() {
        running = false;
        xbee.close();
    }

    public boolean sendCalibrationPacket(String stationAddress, CalibratePacket packet) {
        logger.trace("Calibrateion Packet: " + Util.toHexString(packet.getData()));
        if (hasStation(stationAddress)) {
            XBeeAddress64 address = seenStations.get(stationAddress);
            logger.info(address);
            ZNetTxRequest tx = new ZNetTxRequest(address, packet.getData());
            try {
                ctx.getQueue().put(new TimedPacket(tx, System.currentTimeMillis()));
            } catch (InterruptedException e) {
                logger.error("Error adding packet to queue", e);
            }
        }
        return true;
    }

    public void registerPacketHandler(PacketHandler hndl) {
        List<PacketHandler> handlers = packetHandlers.get(hndl.getPacketId());
        if (handlers == null) {
            handlers = new ArrayList<PacketHandler>();
            packetHandlers.put(hndl.getPacketId(), handlers);
        }
        handlers.add(hndl);
    }
}
