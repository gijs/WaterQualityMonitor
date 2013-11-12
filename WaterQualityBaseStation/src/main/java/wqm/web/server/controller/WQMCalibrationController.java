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
import wqm.constants.AtlasSensor;
import wqm.config.Messages;
import wqm.config.Station;
import wqm.constants.CalibrationCommands;
import wqm.constants.ECSensorProbe;
import wqm.radio.SensorLink.message.CalibrationMessage;
import wqm.radio.SensorLink.packets.CalibratePacket;
import wqm.radio.StationManager;
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
    public ModelAndView selectStationForCalibration(HttpServletRequest request, HttpSession session,
                                                    @PathVariable String stationAddress) throws IOException {

        Station station = lockStation(session, stationAddress);
        ModelAndView view = new ModelAndView("calibration/sensor");
        view.addObject("station", station);
        view.addObject("sensors", AtlasSensor.values());
        addCommonParams(view, request);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}/{sensorID}")
    public ModelAndView selectSensorForCalibration(HttpServletRequest request, HttpSession session,
                                                   @PathVariable String stationAddress,
                                                   @PathVariable int sensorID) throws IOException {

        AtlasSensor sensor = lockSensor(session, stationAddress, sensorID);
        Station station = lockStation(session, stationAddress);

        stationManager.getCalibrationSessionManager().stopCalibrationSession(stationAddress);
        ModelAndView view = new ModelAndView(String.format("calibration/%s", sensor.name()));
        view.addObject("station", station);
        view.addObject("sensor", sensor);
        if (sensor == AtlasSensor.EC) {
            addECTypes(view, null);
        }
        addCommonParams(view, request);
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}/{sensorID}/{phaseID}")
    public ModelAndView conductPhase(HttpServletRequest request, HttpSession session,
                                     @PathVariable String stationAddress,
                                     @PathVariable int sensorID,
                                     @PathVariable int phaseID) throws IOException {
        validatePhase(session, stationAddress, sensorID, phaseID);
        AtlasSensor sensor = lockSensor(session, stationAddress, sensorID);
        Station station = lockStation(session, stationAddress);


        ModelAndView view = new ModelAndView(String.format("calibration/%s/stage%s", sensor.name(), phaseID));

        try {
            if (sensor == AtlasSensor.EC) {
                handleECPhase(request, session, station, sensor, phaseID, view);
            } else {
                logger.error(stationManager.startCalibrationPhase(session, station, sensor, phaseID));
            }
        } catch (AlreadyRunningAnotherCalibrationPhase pe) {
            logger.error(pe);
        }


        view.addObject("station", station);
        view.addObject("sensor", sensor);
        view.addObject("phase", phaseID);
        addCommonParams(view, request);
        return view;
    }

    private void handleECPhase(HttpServletRequest request, HttpSession session, Station station, AtlasSensor sensor, int phaseID, ModelAndView view) throws AlreadyRunningAnotherCalibrationPhase {
        if (phaseID == 0) {
            String type = request.getParameter("ec_sensor_type");
            logger.error("Sensor type: " + type);
            try {
                int sensorType = Integer.parseInt(type);
                session.setAttribute("ec_sensor_type", sensorType);
                CalibratePacket packet = new CalibratePacket(sensor.getId(), CalibratePacket.START_CALIBRATION | CalibratePacket.CALIBRATION_PHASE0);
                packet.setValue1(ECSensorProbe.findByAtlasID(sensorType).getPacketVariable());
                logger.error("Starting EC calibration......");
                CalibrationMessage message = new CalibrationMessage(station, packet);
                stationManager.startCalibrationPhase(session, message, phaseID);
                throw new RedirectException("1");

            } catch (NullPointerException ne) {
                session.setAttribute(Messages.ERROR_MESSAGE, "You need to select a sensor type.");
            } catch (NumberFormatException ne) {
                session.setAttribute(Messages.ERROR_MESSAGE, "You need to select a valid sensor type.");
            } catch (AlreadyRunningAnotherCalibrationPhase alreadyRunningAnotherCalibrationPhase) {
                logger.error("", alreadyRunningAnotherCalibrationPhase);
            }
            throw new RedirectException(".");

        } else
        {
            logger.error(stationManager.startCalibrationPhase(session, station, sensor, phaseID));
        }
        Integer k = null;
        if (session.getAttribute("ec_sensor_type") != null) {

            int sensorType = (Integer) session.getAttribute("ec_sensor_type");
            k = sensorType;
            view.addObject("ec_sensor_type", session.getAttribute("ec_sensor_type"));
            ECSensorProbe probe = ECSensorProbe.findByAtlasID(sensorType);
            view.addObject("ecLow", probe.getLowSide());
            view.addObject("ecHigh", probe.getHighSide());
        }
        addECTypes(view, k);


    }


    private void addECTypes(ModelAndView view, Integer selected) {
        for (ECSensorProbe probe : ECSensorProbe.values()) {
            view.addObject(probe.getVariableName(), probe.getAtlasID());
        }
        if (selected != null) {
            ECSensorProbe probe = ECSensorProbe.findByAtlasID(selected);
            view.addObject("kSelected", probe.getName());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/c/{stationAddress}/{sensorID}/{phaseID}/{command}")
    public ModelAndView conductPhase(HttpServletRequest request, HttpSession session,
                                     @PathVariable String stationAddress,
                                     @PathVariable int sensorID,
                                     @PathVariable int phaseID,
                                     @PathVariable String command) throws IOException {

        logger.error("Received command " + command);

        validatePhase(session, stationAddress, sensorID, phaseID);
        AtlasSensor sensor = lockSensor(session, stationAddress, sensorID);
        Station station = lockStation(session, stationAddress);


        if (command.equalsIgnoreCase("quit")) {
            stationManager.quitCalibrationPhase(request.getSession(true), station, sensor);
            request.getSession(true).setAttribute(Messages.WARNING_MESSAGE, "Calibration has been terminated.");
            throw new RedirectException("/wqm/c");
        }

        if (command.equalsIgnoreCase("done") && sensor == AtlasSensor.ORP) {
            stationManager.quitCalibrationPhase(request.getSession(true), station, sensor);
            request.getSession(true).setAttribute(Messages.SUCCESS_MESSAGE, "ORP Sensor calibrated.");
            throw new RedirectException("/wqm/c");
        }

        switch (sensor) {
            case DO:
            case PH:
                baseCalibrateCommand(request, station, sensor, phaseID, command);
                break;
            case ORP:
                return orpCalibrateCommand(request, station, sensor, phaseID, command);
            case EC:
                ecCalibrateCommand(session, station, sensor, phaseID, command, ECSensorProbe.findByAtlasID((Integer) session.getAttribute("ec_sensor_type")));
                break;
        }

        throw new _404();
    }

    private void ecCalibrateCommand(HttpSession session, Station station, AtlasSensor sensor, int phaseID, String command, ECSensorProbe ec_sensor_type) {
        logger.error("EC Calibrate Command.");
        if(CalibrationCommands.Accept.commandEquals(command))
        {
            stationManager.acceptCalibrationPhase(session, true, station, sensor, phaseID, ec_sensor_type.getPacketVariable(), Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
            if ((phaseID + 1) >= sensor.getCalibrationPhases()) {
                //We have finished all the phases of calibration for this sensor.
                stationManager.quitCalibrationPhase(session, station, sensor);
                session.setAttribute(Messages.SUCCESS_MESSAGE, "EC Sensor calibrated.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
                throw new RedirectException("/");
            }
            throw new RedirectException(String.format("/wqm/c/%s/%d/%d", station.getCompactAddress(), sensor.getId(), phaseID + 1));
        }


    }

    private void baseCalibrateCommand(HttpServletRequest request, Station station, AtlasSensor sensor, int phaseID, String command) {
        if (command.equalsIgnoreCase("accept")) {
            stationManager.acceptCalibrationPhase(request.getSession(true), true, station, sensor, phaseID);
            if ((phaseID + 1) >= sensor.getCalibrationPhases()) {

                //We have finished all the phases of calibration for this sensor.
                stationManager.quitCalibrationPhase(request.getSession(true), station, sensor);
                request.getSession(true).setAttribute(Messages.SUCCESS_MESSAGE, String.format("%s Sensor calibrated.", sensor.name()));

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
    public ModelAndView renameStation(HttpServletRequest request, HttpServletResponse response, HttpSession session,
                                      @PathVariable String stationAddress) throws IOException {

        Station station = validateStation(session, stationAddress);
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
