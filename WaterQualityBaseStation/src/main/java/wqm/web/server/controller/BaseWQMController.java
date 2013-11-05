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

package wqm.web.server.controller;

import org.springframework.web.servlet.ModelAndView;
import wqm.config.AtlasSensor;
import wqm.config.Messages;
import wqm.config.Station;
import wqm.radio.StationManager;
import wqm.web.exceptions.AlreadyHaveLockOnAnotherSensor;
import wqm.web.exceptions.RedirectException;
import wqm.web.server.WQMConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Date: 11/4/13
 * Time: 7:10 AM
 *
 * @author NigelB
 */
public class BaseWQMController {

    protected StationManager stationManager;
    protected WQMConfig config;

    public BaseWQMController(StationManager stationManager, WQMConfig config) {

        this.stationManager = stationManager;
        this.config = config;
    }

    protected void addCommonParams(ModelAndView view, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        view.addObject("stations", stationManager.getBaseStations());
        for (String sessionField : Messages.SESSION_FIELDS) {
            Object val = session.getAttribute(sessionField);
            if (val != null) {
                view.addObject(sessionField, val);
                session.removeAttribute(sessionField);
            }
        }
    }

    protected  Station lockStation(HttpServletRequest request, String stationAddress) throws IOException {
        Station _station = validateStation(request, stationAddress);
        HttpSession session = request.getSession(true);
        if (!stationManager.lockStation(session, stationAddress)) {
            session.setAttribute(Messages.ERROR_MESSAGE, String.format("%s is currently being calibrated by somebody else.", _station.getDisplayName()));
            throw new RedirectException(".");
        }
        return _station;
    }

    protected  AtlasSensor lockSensor(HttpServletRequest request, String stationAddress, int sensorID) throws IOException {
        AtlasSensor sensor = validateSensor(request, sensorID);
        HttpSession session = request.getSession(true);
        try {
            stationManager.lockSensor(session, stationAddress, sensorID);
        } catch (AlreadyHaveLockOnAnotherSensor alreadyHaveLockOnAnotherSensor) {
            AtlasSensor s = AtlasSensor.find(alreadyHaveLockOnAnotherSensor.getSensorID());
            session.setAttribute(Messages.WARNING_MESSAGE, "Since you were in the process of calibrating the " + s.getLongName() + " sensor we brought you back.");
            throw new RedirectException(String.format("../%s/%d", stationAddress, s.getId()));
        }

        return sensor;
    }


    protected  Station validateStation(HttpServletRequest request, String stationAddress) throws IOException {
        Station station = config.getStation(stationAddress);
        if (station == null) {
            request.getSession().setAttribute(Messages.ERROR_MESSAGE, "Invalid station.");
            throw new RedirectException(".");
        }
        return station;
    }

    protected  AtlasSensor validateSensor(HttpServletRequest request, int sensor) throws IOException {
        AtlasSensor _sensor = AtlasSensor.find(sensor);
        if (_sensor == null) {
            request.getSession().setAttribute(Messages.ERROR_MESSAGE, "Invalid sensor.");
            throw new RedirectException(".");
        }
        return _sensor;
    }
}
