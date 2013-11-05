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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import wqm.Pair;
import wqm.config.AtlasSensor;
import wqm.config.Port;
import wqm.config.Station;
import wqm.radio.SensorLink.handlers.PacketHandler;
import wqm.radio.SensorLink.packets.CalibratePacket;
import wqm.web.exceptions.AlreadyHaveLockOnAnotherSensor;
import wqm.web.exceptions.AlreadyRunningAnotherCalibrationPhase;
import wqm.web.server.WQMConfig;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 11/2/13
 * Time: 12:32 PM
 *
 * @author NigelB
 */
public class StationManager implements DisposableBean, HttpSessionListener /*, PacketHandler<? extends SensorLinkPacket>*/ {
    private static Logger logger = Logger.getLogger(StationManager.class);
    private List<Pair<Thread, BaseStation>> baseStations = new ArrayList<Pair<Thread, BaseStation>>();
    private WQMConfig config;

    private CalibrationSessionManager calibrationSessionManager = new CalibrationSessionManager();

    private ServletContext servletContext;


    public StationManager(WQMConfig config, List<PacketHandler> handlersToRegister) throws NamingException, XBeeException {
        this.config = config;

        for (Port port : config.getRadioConfig().getPorts()) {
            final BaseStation st = new BaseStation(port, config);

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
        for (PacketHandler packetHandler : handlersToRegister) {
            registerPacketHandler(packetHandler);
        }
        registerPacketHandler(calibrationSessionManager);
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
        try {
            for (Pair<Thread, BaseStation> station : baseStations) {
                try {
                    station.getB().shutDown();
                    station.getA().interrupt();
                    station.getA().join();
                } catch (Throwable t) {
                    logger.error("Error Shutting down basestation", t);
                }
            }
        } catch (Throwable t) {
            logger.error("Error Shutting down basestation", t);
        }
    }


    public List<Station> getBaseStations() {
        return config.getStations();
    }


    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        logger.error("Session Created");
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        logger.error("Session Destroyed");
        try {
            HttpSession session = event.getSession();

            quitCalibrationPhase(session, getStation(session), getSensor(session));
            getStation(session).setLocked(false);
            unlock(session, getStation(session));

        } catch (Throwable t) {
            logger.error("Error unlocking station", t);
        }

    }

    public Station getStation(HttpSession session) {
        return (Station) session.getAttribute("lock_station");
    }

    public AtlasSensor getSensor(HttpSession session) {
        return AtlasSensor.find((Integer) session.getAttribute("lock_sensor"));
    }

    public Integer getPhase(HttpSession session) {
        return (Integer) session.getAttribute("lock_phase");
    }

    public synchronized boolean lockStation(HttpSession session, String stationAddress) {
        Station station = (Station) session.getAttribute("lock_station");

        if (station == null) {
            station = config.getStation(stationAddress);
            if (!station.isLocked()) {
                session.setAttribute("lock_station", station);
                station.setLocked(true);
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    public synchronized boolean lockSensor(HttpSession session, String stationAddress, int sensorID) throws AlreadyHaveLockOnAnotherSensor {
        if (lockStation(session, stationAddress)) {
            Integer _sensorID = (Integer) session.getAttribute("lock_sensor");
            if (_sensorID == null) {
                session.setAttribute("lock_sensor", sensorID);
                return true;
            }
            if (_sensorID == sensorID) {
                return true;
            } else {
                throw new AlreadyHaveLockOnAnotherSensor(_sensorID);
            }

        }
        return false;
    }

    public boolean startCalibrationPhase(HttpSession session, Station station, AtlasSensor sensor, int phaseID) throws AlreadyRunningAnotherCalibrationPhase {
        for (Pair<Thread, BaseStation> baseStation : baseStations) {
            if (baseStation.getB().hasStation(station.getCompactAddress())) {
                return calibrationSessionManager.startCalibrationPhase(session, baseStation.getB(), station, sensor, phaseID);
            }
        }
        return false;
    }


    public boolean acceptCalibrationPhase(HttpSession session, Station station, AtlasSensor sensor, int phaseID) {
        for (Pair<Thread, BaseStation> baseStation : baseStations) {
            if (baseStation.getB().hasStation(station.getCompactAddress())) {
                boolean toRet = calibrationSessionManager.acceptCalibrationPhase(baseStation.getB(), station, sensor, phaseID);
                session.removeAttribute("lock_phase");
                return toRet;
            }
        }
        return false;
    }


    public boolean quitCalibrationPhase(HttpSession session, Station station, AtlasSensor sensor) {
        for (Pair<Thread, BaseStation> baseStation : baseStations) {
            if (baseStation.getB().hasStation(station.getCompactAddress())) {
                boolean toRet = calibrationSessionManager.quitCalibrationPhase(baseStation.getB(), station, sensor);
                unlock(session, station);
                return toRet;
            }
        }
        return false;
    }

    private void unlock(HttpSession session, Station station) {
        session.removeAttribute("lock_station");
        session.removeAttribute("lock_sensor");
        session.removeAttribute("lock_phase");
        station.setLocked(false);
    }

    public void registerPacketHandler(PacketHandler handler) {
        for (Pair<Thread, BaseStation> baseStation : baseStations) {
            baseStation.getB().registerPacketHandler(handler);
        }
    }


    public CalibrationSessionManager getCalibrationSessionManager() {
        return calibrationSessionManager;
    }
}
