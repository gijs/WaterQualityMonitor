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

/**
 * Date: 10/21/13
 * Time: 3:28 PM
 *
 * @author NigelB
 */
public abstract class SensorLinkPacket {
    private int packet_type;
    private int header_length;
    protected int[] data;


    protected SensorLinkPacket(int packet_type, int header_length) {
        this.packet_type = packet_type;
        this.header_length = header_length;
    }

    protected SensorLinkPacket(int[] data) {
        this.data = data;
        packet_type =  data[0];
        header_length = data[1];
    }

    protected SensorLinkPacket() {
    }

    public abstract int getPacketID();


    public int[] getData() {
        return data;
    }

    public int getPacket_type() {
        return packet_type;
    }

    public void setPacket_type(byte packet_type) {
        this.packet_type = packet_type;
    }

    public int getHeader_length() {
        return header_length;
    }

    public void setHeader_length(byte header_length) {
        this.header_length = header_length;
    }

    public static SensorLinkPacket constructPacket(int[] data, long receivedTime)
    {
        switch(data[0])
        {
            case SinkSearch.PACKET_ID:
                return new SinkSearch(data);
            case DataUpload.PACKET_ID:
                return new DataUpload(data);
            default:
                return null;
        }
    }
}
