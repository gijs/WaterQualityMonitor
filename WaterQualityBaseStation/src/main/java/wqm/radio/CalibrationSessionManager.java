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

import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import org.apache.log4j.Logger;
import wqm.Pair;
import wqm.config.AtlasSensor;
import wqm.config.Station;
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.handlers.PacketHandler;
import wqm.radio.SensorLink.packets.CalibratePacket;
import wqm.radio.util.AddressUtil;
import wqm.web.exceptions.AlreadyRunningAnotherCalibrationPhase;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Date: 11/5/13
 * Time: 7:43 PM
 *
 * @author NigelB
 */
public class CalibrationSessionManager implements PacketHandler<CalibratePacket> {
    private static Logger logger = Logger.getLogger(CalibrationSessionManager.class);
    private Hashtable<String, HttpSession> calibrationSessions = new Hashtable<String, HttpSession>();

    public boolean handlePacket(PacketHandlerContext ctx, ZNetRxResponse xbeeResponse, CalibratePacket packet) {
        String address = AddressUtil.getCompactStringAddress(xbeeResponse.getRemoteAddress64());
        if(calibrationSessions.containsKey(address))
        {
            ArrayList<CalibratePacket> data = getCalibrationSessionData(address);
            if(data != null)
            {
                data.add(packet);
            }else
            {
                logger.warn("Received calibration data when no calibration session exists: ");
                logger.trace(packet);
            }

        }
        return true;
    }

    public int getPacketId() {
        return CalibratePacket.PACKET_ID;
    }



    public ArrayList<CalibratePacket> getCalibrationSessionData(String address) {
            HttpSession session = calibrationSessions.get(address);
        ArrayList<CalibratePacket> data = null;
        if((data = (ArrayList<CalibratePacket>) session.getAttribute("calibration_data")) == null)
        {
            data = new ArrayList<CalibratePacket>();
            session.setAttribute("calibration_data", data);
        }
        return data;
    }



    public ArrayList<CalibratePacket> getCalibrationSessionData(HttpSession session) {
        ArrayList<CalibratePacket> data = null;
        if((data = (ArrayList<CalibratePacket>) session.getAttribute("calibration_data")) == null)
        {
            data = new ArrayList<CalibratePacket>();
            session.setAttribute("calibration_data", data);
        }
        return data;
    }

    public void startCalibrationSession(String compactAddress, HttpSession session) {
        calibrationSessions.put(compactAddress, session);
    }

    public void stopCalibrationSession(String compactAddress) {
        HttpSession session = calibrationSessions.remove(compactAddress);
        if(session != null)
        {
            session.removeAttribute("calibration_data");
        }
    }

    public boolean startCalibrationPhase(HttpSession session, BaseStation baseStation, Station station, AtlasSensor sensor, int phaseID) throws AlreadyRunningAnotherCalibrationPhase {
        Integer _phaseID = (Integer) session.getAttribute("lock_phase");
        if(_phaseID == null)
        {
            session.setAttribute("lock_phase", _phaseID);
        }else if(_phaseID != phaseID)
        {
            throw new AlreadyRunningAnotherCalibrationPhase(phaseID);
        }
        CalibratePacket packet = new CalibratePacket(sensor.getId(), CalibratePacket.START_CALIBRATION, 0, 0);
        if(baseStation.sendCalibrationPacket(station.getCompactAddress(), packet))
        {
            startCalibrationSession(station.getCompactAddress(), session);
            logger.info(String.format("Calibration quit command sent: %s", packet.toString()));
        }
        return false;
    }


    public boolean acceptCalibrationPhase(BaseStation baseStation, Station station, AtlasSensor sensor, int phaseID) {
        logger.info("Accepting calibration Phase: "+phaseID);
        int code = CalibratePacket.STOP_CALIBRATION;
        switch(phaseID)
        {
            case 0:
                code = CalibratePacket.CALIBRATION_PHASE0;
                break;
            case 1:
                code = CalibratePacket.CALIBRATION_PHASE1;
                break;
            case 2:
                code = CalibratePacket.CALIBRATION_PHASE2;
                break;
            case 3:
                code = CalibratePacket.CALIBRATION_PHASE3;
                break;
            case 4:
                code = CalibratePacket.CALIBRATION_PHASE4;
                break;
        }
        CalibratePacket packet = new CalibratePacket(sensor.getId(), code, 0, 0);
        if(baseStation.sendCalibrationPacket(station.getCompactAddress(), packet))
        {
            stopCalibrationSession(station.getCompactAddress());
            logger.info(String.format("Calibration Accept Command sent: %s", packet.toString()));
        }
        return true;
    }


    public boolean quitCalibrationPhase(BaseStation baseStation, Station station, AtlasSensor sensor) {
        CalibratePacket packet = new CalibratePacket(sensor.getId(), CalibratePacket.STOP_CALIBRATION, 0, 0);
        if(baseStation.sendCalibrationPacket(station.getCompactAddress(), packet))
        {
            stopCalibrationSession(station.getCompactAddress());
        }
        return true;
    }
}
