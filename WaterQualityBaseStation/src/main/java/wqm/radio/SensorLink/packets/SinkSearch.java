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

import static wqm.radio.util.Util._putInt;
import static wqm.radio.util.Util.getInt;
import static wqm.radio.util.Util.putInt;

/**
 * Date: 10/21/13
 * Time: 3:28 PM
 *
 * @author NigelB
 */
public class SinkSearch extends SensorLinkPacket{
    public static final int PACKET_ID = 1;
    public static final int HEADER_SIZE = 14;

    private static final int DH_OFFSET = 6;
    private static final int DL_OFFSET = 10;

    public SinkSearch(int DH, int DL)
    {
        data = new int[14];
        data[0] = PACKET_ID;
        data[1] = HEADER_SIZE;
        setDH(DH);
        setDL(DL);
    }

    public SinkSearch(int[] data) {
         super(data);
    }

    public int getDH()
    {
        return getInt(data, DH_OFFSET);
    }

    public void setDH(int DH){
//        putInt(data, DH_OFFSET, DH);
        _putInt(data, DH_OFFSET, DH);
    }

    public void setDL(int DL)
    {
//        putInt(data, DL_OFFSET, DL);
        _putInt(data, DL_OFFSET, DL);
    }

    public int getDL()
    {
        return getInt(data, DL_OFFSET);
    }

    @Override
    public int getPacketID() {
        return PACKET_ID;
    }

    @Override
    public String toString() {
        return String.format("SinkSearch{ headerLength=%s, DH=0x%x, DL=0x%x}", getHeader_length(), getDH(), getDL());
    }
}
