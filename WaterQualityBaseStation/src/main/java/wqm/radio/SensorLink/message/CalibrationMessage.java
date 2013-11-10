package wqm.radio.SensorLink.message;

import wqm.config.Station;
import wqm.radio.SensorLink.packets.CalibratePacket;

/**
 * Date: 11/10/13
 * Time: 1:35 PM
 *
 * @author NigelB
 */
public class CalibrationMessage {
    private Station to;
    private CalibratePacket packet;

    public CalibrationMessage(Station to, CalibratePacket packet) {
        this.to = to;
        this.packet = packet;
    }

    public Station getTo() {
        return to;
    }

    public void setTo(Station to) {
        this.to = to;
    }

    public CalibratePacket getPacket() {
        return packet;
    }

    public void setPacket(CalibratePacket packet) {
        this.packet = packet;
    }
}
