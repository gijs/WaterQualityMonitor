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

package wqm.radio.SensorLink;

import wqm.radio.SensorLink.packets.SinkSearch;
import wqm.radio.TimedPacket;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Date: 10/22/13
 * Time: 8:20 AM
 *
 * @author NigelB
 */
public class PacketHandlerContext {
    private LinkedBlockingQueue<TimedPacket> queue = new LinkedBlockingQueue<TimedPacket>();
    private Thread sender;

    public SinkSearch getSinkPacket() {
        return sinkPacket;
    }

    public void setSinkPacket(SinkSearch sinkPacket) {
        this.sinkPacket = sinkPacket;
    }

    private SinkSearch sinkPacket;

    public LinkedBlockingQueue<TimedPacket> getQueue() {
        return queue;
    }

    public void setQueue(LinkedBlockingQueue<TimedPacket> queue) {
        this.queue = queue;
    }

    public Thread getSender() {
        return sender;
    }

    public void setSender(Thread sender) {
        this.sender = sender;
    }
}
