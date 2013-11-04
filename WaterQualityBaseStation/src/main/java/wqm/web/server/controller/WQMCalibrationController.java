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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import wqm.config.AtlasSensor;
import wqm.config.Messages;
import wqm.config.Station;
import wqm.radio.StationManager;
import wqm.web.exceptions.AlreadyHaveLockOnAnotherSensor;
import wqm.web.exceptions.AlreadyRunningAnotherCalibrationPhase;
import wqm.web.exceptions.RedirectException;
import wqm.web.server.WQMConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Date: 11/2/13
 * Time: 11:07 AM
 *
 * @author NigelB
 */
@Controller
public class WQMCalibrationController extends BaseWQMController {
    private static Logger logger = Logger.getLogger(WQMDataController.class);

    public WQMCalibrationController(StationManager stationManager, WQMConfig config) {
        super(stationManager, config);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/shutdown")
    public ModelAndView shutdown() {
        config.shutdown();
        return new ModelAndView("stringView", "string", "OK");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/j/{view}")
    public ModelAndView site(HttpServletRequest request, @PathVariable String view) {
        String station = request.getParameter("station");
        String sensor = request.getParameter("sensor");
        String stage = request.getParameter("stage");
        AtlasSensor s = null;
        if (sensor != null) {
            s = AtlasSensor.find(Integer.parseInt(sensor));
        }

        logger.error(request.getParameter("station"));
        logger.error(config.getStation(station));

        if (station != null && s != null) {
            view = String.format("calibration/%s", s.name());
            if (stage != null) {
                view = String.format("%s/stage%s", view, stage);
            }
        }


        ModelAndView _view = new ModelAndView(view);
        if (stage != null) {
            _view.addObject("stage", stage);
        }
        if (s != null) {
            _view.addObject("sensor", s);
        }

        _view.addObject("stations", stationManager.getBaseStations());
        _view.addObject("calibrate_sensors", AtlasSensor.values());
        _view.addObject("station", config.getStation(station));

        return _view;
    }

    @ExceptionHandler(RedirectException.class)
    public void handleException(HttpServletResponse response, RedirectException redirect) throws IOException {
        response.sendRedirect(redirect.getRedirectTo());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c")
    public ModelAndView selectStationForCalibration(HttpServletRequest request) {
        ModelAndView view = new ModelAndView("calibration/station");
        addCommonParams(view, request);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}")
    public ModelAndView selectStationForCalibration(HttpServletRequest request,
                                                    @PathVariable String stationAddress) throws IOException {

        Station station = lockStation(request, stationAddress);
        ModelAndView view = new ModelAndView("calibration/sensor");
        view.addObject("station", station);
        view.addObject("sensors", AtlasSensor.values());
        addCommonParams(view, request);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}/{sensorID}")
    public ModelAndView selectSensorForCalibration(HttpServletRequest request, HttpServletResponse response,
                                                   @PathVariable String stationAddress,
                                                   @PathVariable int sensorID) throws IOException {
        Station station = lockStation(request, stationAddress);
        AtlasSensor sensor = lockSensor(request, stationAddress, sensorID);

        ModelAndView view = new ModelAndView(String.format("calibration/%s", sensor.name()));
        view.addObject("station", station);
        view.addObject("sensor", sensor);
        addCommonParams(view, request);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}/{sensorID}/{phaseID}")
    public ModelAndView conductPhase(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable String stationAddress,
                                     @PathVariable int sensorID,
                                     @PathVariable int phaseID) throws IOException {
        Station station = lockStation(request, stationAddress);
        AtlasSensor sensor = lockSensor(request, stationAddress, sensorID);

        try {
            logger.error(stationManager.startCalibrationPhase(request.getSession(true), station, sensor, phaseID));
        } catch (AlreadyRunningAnotherCalibrationPhase pe) {
            logger.error(pe);
        }

        ModelAndView view = new ModelAndView(String.format("calibration/%s/stage%s", sensor.name(), phaseID));
        view.addObject("station", station);
        view.addObject("sensor", sensor);
        view.addObject("phase", phaseID);
        addCommonParams(view, request);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/d/{stationAddress}/{sensorID}/{phaseID}")
    public ModelAndView calibrationData(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable String stationAddress,
                                     @PathVariable int sensorID,
                                     @PathVariable int phaseID) throws IOException {
        Station station = lockStation(request, stationAddress);
        AtlasSensor sensor = lockSensor(request, stationAddress, sensorID);
        int offset = 0;
        if(request.getParameter("offset") != null)
        {
            try{
                offset = Integer.parseInt(request.getParameter("offset"));
            }catch(Throwable t)
            {
                logger.error("Could not parse offset: "+request.getParameter("offset"));
            }
        }

        return new ModelAndView("","", stationManager.getCalibrationData(request.getSession(true), station, sensor, phaseID, offset));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}/{sensorID}/{phaseID}/{command}")
    public ModelAndView conductPhase(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable String stationAddress,
                                     @PathVariable int sensorID,
                                     @PathVariable int phaseID,
                                     @PathVariable String command) throws IOException {
        Station station = lockStation(request, stationAddress);
        AtlasSensor sensor = lockSensor(request, stationAddress, sensorID);

        String name = "";
        if(command.equalsIgnoreCase("accept"))
        {
            stationManager.acceptCalibrationPhase(request.getSession(true), station, sensor, phaseID);
            if((phaseID + 1) >= sensor.getCalibrationPhases())
            {
                //We have finished all the phases of calibration for this sensor.
                stationManager.quitCalibrationPhase(request.getSession(true), station, sensor);
                request.getSession(true).setAttribute(Messages.SUCCESS_MESSAGE, "PH Sensor calibrated.");

                throw new RedirectException("/");
            }else{
                throw new RedirectException(String.format("/wqm/c/%s/%d/%d", stationAddress, sensorID, phaseID + 1));
            }
        }else if(command.equalsIgnoreCase("quit"))
        {
            stationManager.quitCalibrationPhase(request.getSession(true), station, sensor);
            request.getSession(true).setAttribute(Messages.WARNING_MESSAGE, "Calibration has been terminated.");
            throw new RedirectException("/wqm/c");
        }

        ModelAndView view = new ModelAndView(name);
        view.addObject("station", station);
        view.addObject("sensor", sensor);
        view.addObject("phase", phaseID);
        addCommonParams(view, request);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/r/{stationAddress}")
    public ModelAndView renameStation(HttpServletRequest request, HttpServletResponse response,
                                      @PathVariable String stationAddress) throws IOException {
        Station station = validateStation(request, stationAddress);
        String name = request.getParameter("name");

        if (name != null) {
            config.renameStation(station, request.getParameter("name"));
            response.sendRedirect("/");
            return null;
        }
        ModelAndView view = new ModelAndView("rename");
        view.addObject("station", station);
        addCommonParams(view, request);
        return view;
    }


    private Station lockStation(HttpServletRequest request, String stationAddress) throws IOException {
        Station _station = validateStation(request, stationAddress);
        HttpSession session = request.getSession(true);
        if (!stationManager.lockStation(session, stationAddress)) {
            session.setAttribute(Messages.ERROR_MESSAGE, String.format("%s is currently being calibrated by somebody else.", _station.getDisplayName()));
            throw new RedirectException(".");
        }
        return _station;
    }

    private AtlasSensor lockSensor(HttpServletRequest request, String stationAddress, int sensorID) throws IOException {
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


    private Station validateStation(HttpServletRequest request, String stationAddress) throws IOException {
        Station station = config.getStation(stationAddress);
        if (station == null) {
            request.getSession().setAttribute(Messages.ERROR_MESSAGE, "Invalid station.");
            throw new RedirectException(".");
        }
        return station;
    }

    private AtlasSensor validateSensor(HttpServletRequest request, int sensor) throws IOException {
        AtlasSensor _sensor = AtlasSensor.find(sensor);
        if (_sensor == null) {
            request.getSession().setAttribute(Messages.ERROR_MESSAGE, "Invalid sensor.");
            throw new RedirectException(".");
        }
        return _sensor;
    }

}
