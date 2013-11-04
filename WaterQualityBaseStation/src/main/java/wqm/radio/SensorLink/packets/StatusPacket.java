package wqm.radio.SensorLink.packets;

/**
 * Date: 11/4/13
 * Time: 9:58 AM
 *
 * @author NigelB
 */
public class StatusPacket  extends SensorLinkPacket {
    public static final byte PACKET_ID = 8;
    public static final byte HEADER_SIZE = 18;

    public StatusPacket(int[] data) {
        super(data);
    }

    @Override
    public int getPacketID() {
        return PACKET_ID;
    }
}
