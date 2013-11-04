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

import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import wqm.Pair;
import wqm.config.AtlasSensor;
import wqm.config.Port;
import wqm.config.Station;
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.handlers.PacketHandler;
import wqm.radio.SensorLink.packets.CalibratePacket;
import wqm.radio.util.AddressUtil;
import wqm.web.exceptions.AlreadyHaveLockOnAnotherSensor;
import wqm.web.exceptions.AlreadyRunningAnotherCalibrationPhase;
import wqm.web.exceptions.RedirectException;
import wqm.web.server.WQMConfig;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Date: 11/2/13
 * Time: 12:32 PM
 *
 * @author NigelB
 */
public class StationManager implements DisposableBean, HttpSessionListener, PacketHandler<CalibratePacket>{
    private static Logger logger = Logger.getLogger(StationManager.class);
    private List<Pair<Thread, BaseStation>> baseStations = new ArrayList<Pair<Thread, BaseStation>>();
    private WQMConfig config;

    private ServletContext servletContext;
//    private Hashtable<HttpSession, Station> calibrationSessions = new Hashtable<HttpSession, Station>();
    private Hashtable<String, HttpSession> calibrationSessions = new Hashtable<String, HttpSession>();

    public StationManager(WQMConfig config, List<PacketHandler> handlers) throws NamingException, XBeeException {
        this.config = config;

        for (Port port : config.getRadioConfig().getPorts()) {
            final BaseStation st = new BaseStation(port, config, handlers);
            st.registerPacketHandler(this);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        st.run();
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    } catch (XBeeException e) {
                        logger.error("", e);
                    }
                }
            });
            baseStations.add(new Pair<Thread, BaseStation>(t, st));
            t.start();
        }
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        servletContext.addListener(this);
    }

    public void destroy() throws Exception {
        try{
            for (Pair<Thread, BaseStation> station : baseStations) {
                try{
                    station.getB().shutDown();
                    station.getA().interrupt();
                    station.getA().join();
                }catch(Throwable t)
                {
                    logger.error("Error Shutting down basestation",t);
                }
            }
        }catch(Throwable t)
        {
            logger.error("Error Shutting down basestation",t);
        }
    }


    public List<Station> getBaseStations() {
        logger.error(servletContext);
        return config.getStations();
    }


    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        logger.error("Session Created");
    }

    public void sessionDestroyed(HttpSessionEvent event) {
//        event.getSession().getAttribute()
        logger.error("Session Destroyed");
    }

    public synchronized boolean lockStation(HttpSession session, String stationAddress) {
        Station station = (Station) session.getAttribute("lock_station");

        if(station == null)
        {
            station = config.getStation(stationAddress);
            if(!station.isLocked())
            {
                session.setAttribute("lock_station", station);
                station.setLocked(true);
                return true;
            }
            return false;
        }else
        {
            return true;
        }
    }

    public synchronized boolean lockSensor(HttpSession session, String stationAddress, int sensorID) throws AlreadyHaveLockOnAnotherSensor {
        if(lockStation(session, stationAddress))
        {
            Integer _sensorID = (Integer) session.getAttribute("lock_sensor");
            if(_sensorID == null)
            {
                session.setAttribute("lock_sensor", sensorID);
                return true;
            }
            if(_sensorID == sensorID)
            {
                return true;
            }else
            {
                throw new AlreadyHaveLockOnAnotherSensor(_sensorID);
            }

        }
        return false;
    }

    public boolean startCalibrationPhase(HttpSession session, Station station, AtlasSensor sensor, int phaseID) throws AlreadyRunningAnotherCalibrationPhase {
        for (Pair<Thread, BaseStation> baseStation : baseStations) {
            if(baseStation.getB().hasStation(station.getCompactAddress()))
            {
                return startCalibrationPhase(session, baseStation.getB(), station, sensor, phaseID);
            }
        }
        return false;
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
            calibrationSessions.put(station.getCompactAddress(), session);
        }
        return false;
    }

    public boolean handlePacket(PacketHandlerContext ctx, ZNetRxResponse xbeeResponse, CalibratePacket packet) {
//        logger.error(packet);
        String address = AddressUtil.getCompactStringAddress(xbeeResponse.getRemoteAddress64());
        if(calibrationSessions.containsKey(address))
        {
            HttpSession session = calibrationSessions.get(address);
            ArrayList<CalibratePacket> data;
            if((data = (ArrayList<CalibratePacket>) session.getAttribute("calibration_data")) == null)
            {
                data = new ArrayList<CalibratePacket>();
                session.setAttribute("calibration_data", data);
            }
            data.add(packet);
        }
        return false;
    }

    public int getPacketId() {
        return CalibratePacket.PACKET_ID;
    }

    public boolean acceptCalibrationPhase(HttpSession session, Station station, AtlasSensor sensor, int phaseID) {
        for (Pair<Thread, BaseStation> baseStation : baseStations) {
            if(baseStation.getB().hasStation(station.getCompactAddress()))
            {
                return acceptCalibrationPhase(session, baseStation.getB(), station, sensor, phaseID);
            }
        }
        return false;
    }

    private boolean acceptCalibrationPhase(HttpSession session, BaseStation baseStation, Station station, AtlasSensor sensor, int phaseID) {
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
            logger.error("=====================================================================================");
            calibrationSessions.remove(station.getCompactAddress());
            session.removeAttribute("lock_station");
            session.removeAttribute("lock_sensor");
            session.removeAttribute("lock_phase");
            station.setLocked(false);
        }
        return true;
    }

    public boolean quitCalibrationPhase(HttpSession session, Station station, AtlasSensor sensor) {
        for (Pair<Thread, BaseStation> baseStation : baseStations) {
            if(baseStation.getB().hasStation(station.getCompactAddress()))
            {
                return quitCalibrationPhase(session, baseStation.getB(), station, sensor);
            }
        }
        return false;
    }

    public boolean quitCalibrationPhase(HttpSession session, BaseStation baseStation, Station station, AtlasSensor sensor) {
        CalibratePacket packet = new CalibratePacket(sensor.getId(), CalibratePacket.STOP_CALIBRATION, 0, 0);
        if(baseStation.sendCalibrationPacket(station.getCompactAddress(), packet))
        {
            logger.info("===============================");
            calibrationSessions.remove(station.getCompactAddress());
            session.removeAttribute("lock_station");
            session.removeAttribute("lock_sensor");
            session.removeAttribute("lock_phase");
            station.setLocked(false);
        }
        return true;
    }

    public Object getCalibrationData(HttpSession session, Station station, AtlasSensor sensor, int phaseID, int count) {

        switch(sensor)
        {
            case PH:
                ArrayList<CalibratePacket> packets = (ArrayList<CalibratePacket>) session.getAttribute("calibration_data");
                ArrayList data = new ArrayList();
                for (int i = count; i < packets.size(); i++) {
                    CalibratePacket packet = packets.get(i);

                    data.add(new Object[]{packet.getTime(), packet.getValue(packet.getValue1(), packet.getExponent1())});
                }
                return data;

            case DO:
                break;
        }
        return null;
    }
}
