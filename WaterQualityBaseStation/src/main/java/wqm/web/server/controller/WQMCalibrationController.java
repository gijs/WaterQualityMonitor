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
import wqm.radio.SensorLink.message.CalibrationMessage;
import wqm.radio.SensorLink.packets.CalibratePacket;
import wqm.radio.StationManager;
import wqm.web.exceptions.AlreadyHaveLockOnAnotherSensor;
import wqm.web.exceptions.AlreadyRunningAnotherCalibrationPhase;
import wqm.web.exceptions.RedirectException;
import wqm.web.exceptions._404;
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

//    @RequestMapping(method = RequestMethod.GET, value = "/j/{view}")
//    public ModelAndView site(HttpServletRequest request, @PathVariable String view) {
//        String station = request.getParameter("station");
//        String sensor = request.getParameter("sensor");
//        String stage = request.getParameter("stage");
//        AtlasSensor s = null;
//        if (sensor != null) {
//            s = AtlasSensor.find(Integer.parseInt(sensor));
//        }
//
//        logger.error(request.getParameter("station"));
//        logger.error(config.getStation(station));
//
//        if (station != null && s != null) {
//            view = String.format("calibration/%s", s.name());
//            if (stage != null) {
//                view = String.format("%s/stage%s", view, stage);
//            }
//        }
//
//
//        ModelAndView _view = new ModelAndView(view);
//        if (stage != null) {
//            _view.addObject("stage", stage);
//        }
//        if (s != null) {
//            _view.addObject("sensor", s);
//        }
//
//        _view.addObject("stations", stationManager.getBaseStations());
//        _view.addObject("calibrate_sensors", AtlasSensor.values());
//        _view.addObject("station", config.getStation(station));
//
//        return _view;
//    }

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
        if (sensor == AtlasSensor.EC) {
            addECTypes(view);
        }
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
        HttpSession session = request.getSession(true);
        ModelAndView view = new ModelAndView(String.format("calibration/%s/stage%s", sensor.name(), phaseID));

        try {
            if (sensor == AtlasSensor.EC) {
                if (phaseID == 0) {
                    String type = request.getParameter("ec_sensor_type");
                    logger.error("Sensor type: " + type);
                    try {
                        float sensorType = Float.parseFloat(type);
                        session.setAttribute("ec_sensor_type", sensorType);
                        CalibratePacket packet = new CalibratePacket(sensor.getId(), CalibratePacket.START_CALIBRATION | CalibratePacket.CALIBRATION_PHASE0);
                        packet.setValue1(sensorType);
                        logger.error("Starting EC calibration......");
                        CalibrationMessage message = new CalibrationMessage(station, packet);
                        stationManager.startCalibrationPhase(session, message, phaseID);
                        throw new RedirectException("1");

                    } catch (NullPointerException ne) {
                        request.getSession().setAttribute(Messages.ERROR_MESSAGE, "You need to select a sensor type.");
                    } catch (NumberFormatException ne) {
                        request.getSession().setAttribute(Messages.ERROR_MESSAGE, "You need to select a valid sensor type.");
                    }
                    throw new RedirectException(".");
                }
                if(session.getAttribute("ec_sensor_type") != null)
                {
                    view.addObject("ec_sensor_type", session.getAttribute("ec_sensor_type"));
                }
                addECTypes(view);
            }
            logger.error(stationManager.startCalibrationPhase(request.getSession(true), station, sensor, phaseID));
        } catch (AlreadyRunningAnotherCalibrationPhase pe) {
            logger.error(pe);
        }


        view.addObject("station", station);
        view.addObject("sensor", sensor);
        view.addObject("phase", phaseID);
        addCommonParams(view, request);
        return view;
    }

    private void addECTypes(ModelAndView view) {
        view.addObject("k1", CalibratePacket.EC_CALIBRATION_SENSOR_TYPE_K_0_1);
        view.addObject("k2", CalibratePacket.EC_CALIBRATION_SENSOR_TYPE_K_1_0);
        view.addObject("k3", CalibratePacket.EC_CALIBRATION_SENSOR_TYPE_K_10_0);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}/{sensorID}/{phaseID}/{command}")
    public ModelAndView conductPhase(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable String stationAddress,
                                     @PathVariable int sensorID,
                                     @PathVariable int phaseID,
                                     @PathVariable String command) throws IOException {

        logger.error("Received command " + command);

        Station station = lockStation(request, stationAddress);
        AtlasSensor sensor = lockSensor(request, stationAddress, sensorID);


        if (command.equalsIgnoreCase("quit")) {
            stationManager.quitCalibrationPhase(request.getSession(true), station, sensor);
            request.getSession(true).setAttribute(Messages.WARNING_MESSAGE, "Calibration has been terminated.");
            throw new RedirectException("/wqm/c");
        }

        switch (sensor) {
            case PH:
                phCalibrateCommand(request, station, sensor, phaseID, command);
                break;
            case ORP:
                return orpCalibrateCommand(request, station, sensor, phaseID, command);
            case DO:
                break;
            case EC:
                ecCalibrateCommand(request, station, sensor, phaseID, command);
                break;
        }

        throw new _404();
    }

    private void ecCalibrateCommand(HttpServletRequest request, Station station, AtlasSensor sensor, int phaseID, String command) {

    }

    private void phCalibrateCommand(HttpServletRequest request, Station station, AtlasSensor sensor, int phaseID, String command) {
        if (command.equalsIgnoreCase("accept")) {
            stationManager.acceptCalibrationPhase(request.getSession(true), true, station, sensor, phaseID);
            if ((phaseID + 1) >= sensor.getCalibrationPhases()) {

                //We have finished all the phases of calibration for this sensor.
                stationManager.quitCalibrationPhase(request.getSession(true), station, sensor);
                request.getSession(true).setAttribute(Messages.SUCCESS_MESSAGE, "PH Sensor calibrated.");

                throw new RedirectException("/");
            } else {
                throw new RedirectException(String.format("/wqm/c/%s/%d/%d", station.getCompactAddress(), sensor.getId(), phaseID + 1));
            }
        }
    }

    private ModelAndView orpCalibrateCommand(HttpServletRequest request, Station station, AtlasSensor sensor, int phaseID, String command) {
        if (command.equalsIgnoreCase("up")) {
            logger.info("Received + for ORP");
            stationManager.acceptCalibrationPhase(request.getSession(true), false, station, sensor, phaseID, CalibratePacket.ORP_CALIBRATION_PLUS, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        } else if (command.equalsIgnoreCase("down")) {
            logger.info("Received - for ORP");
            stationManager.acceptCalibrationPhase(request.getSession(true), false, station, sensor, phaseID, CalibratePacket.ORP_CALIBRATION_MINUS, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        }
        return new ModelAndView("", "", "OK");
    }


    @RequestMapping(method = RequestMethod.GET, value = "/r/{stationAddress}")
    public ModelAndView renameStation(HttpServletRequest request, HttpServletResponse response,
                                      @PathVariable String stationAddress) throws IOException {

        Station station = validateStation(request, stationAddress);
        String name = request.getParameter("name");

        logger.debug(String.format("Renaming station: %s", stationAddress));
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


}
