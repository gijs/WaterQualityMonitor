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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import wqm.radio.StationManager;
import wqm.web.server.WQMConfig;

import javax.servlet.http.HttpServletRequest;

/**
 * Date: 11/4/13
 * Time: 7:08 AM
 *
 * @author NigelB
 */
@Controller
public class WQMMonitorController extends BaseWQMController {

    public WQMMonitorController(StationManager stationManager, WQMConfig config) {
        super(stationManager, config);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/m")
    public ModelAndView monitor(HttpServletRequest request) {
        ModelAndView view = new ModelAndView("index");
        addCommonParams(view, request);
        return view;
    }
}
