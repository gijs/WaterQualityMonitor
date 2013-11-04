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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import wqm.radio.DataSource;
import wqm.radio.StationManager;
import wqm.web.server.WQMConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;

/**
 * Date: 10/30/13
 * Time: 5:43 PM
 *
 * @author NigelB
 */
@Controller
public class WQMDataController {
    private static Logger logger = Logger.getLogger(WQMDataController.class);
    private final StationManager stations;
    private final WQMConfig config;


    public WQMDataController(StationManager stations, WQMConfig config, DataSource source) {
        this.stations = stations;
        this.config = config;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test/{zooParam}")
    public ModelAndView test(HttpServletRequest req, @PathVariable String zooParam)
    {
        Hashtable toRet = new Hashtable();
        toRet.put("test","value");
        return new ModelAndView("json", "map", toRet);
    }

//    @RequestMapping(method = RequestMethod.GET, value = "/calibrate/{station}/{sensor}")
//    public ModelAndView calibrate(HttpServletRequest req, @PathVariable String station, @PathVariable String sensor)
//    {
//        Hashtable<String, Boolean> toRet = new Hashtable<String, Boolean>();
//        toRet.put("acquired_lock", stations.acquireCalibrationLock(req.getSession(true), station, sensor));
//        return new ModelAndView("json", "map", toRet);
//    }

    @RequestMapping(method = RequestMethod.GET, value = "/calibrate")
    public ModelAndView listStations()
    {
        ModelAndView view = new ModelAndView("calibration");
//        view.addObject("basestations", basestation.listStations());
        return view;
    }

}
