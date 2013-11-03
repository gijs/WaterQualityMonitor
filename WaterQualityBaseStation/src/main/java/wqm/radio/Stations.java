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
import wqm.config.Port;
import wqm.config.Station;
import wqm.radio.SensorLink.handlers.PacketHandler;
import wqm.web.exceptions.AlreadyHaveLockOnAnotherSensor;
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
public class Stations implements DisposableBean, HttpSessionListener {
    private static Logger logger = Logger.getLogger(Stations.class);
    private List<Pair<Thread, BaseStation>> stations = new ArrayList<Pair<Thread, BaseStation>>();
    private WQMConfig config;

    private ServletContext servletContext;
    private Hashtable<HttpSession, Station> calibrationSessions = new Hashtable<HttpSession, Station>();

    public Stations(WQMConfig config, List<PacketHandler> handlers) throws NamingException, XBeeException {
        this.config = config;
        for (Port port : config.getRadioConfig().getPorts()) {
            final BaseStation st = new BaseStation(port, config, handlers);

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
            stations.add(new Pair<Thread, BaseStation>(t, st));
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
            for (Pair<Thread, BaseStation> station : stations) {
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


    public List<Station> getStations() {
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
}
