package wqm.web.server.controller;

import org.springframework.web.servlet.ModelAndView;
import wqm.config.Messages;
import wqm.radio.StationManager;
import wqm.web.server.WQMConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
}
