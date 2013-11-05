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

import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import wqm.config.AtlasSensor;
import wqm.config.Station;
import wqm.radio.RecordStorage.record.BaseRecord;
import wqm.radio.RecordStorage.record.FloatRecord;
import wqm.radio.RecordStorage.record.SalinityRecord;
import wqm.radio.SensorLink.PacketHandlerContext;
import wqm.radio.SensorLink.handlers.PacketHandler;
import wqm.radio.SensorLink.packets.CalibratePacket;
import wqm.radio.SensorLink.packets.DataUpload;
import wqm.radio.StationManager;
import wqm.radio.util.AddressUtil;
import wqm.web.exceptions._404;
import wqm.web.server.WQMConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Date: 10/30/13
 * Time: 5:43 PM
 *
 * @author NigelB
 */
@Controller
public class WQMDataController extends BaseWQMController {
    private static Logger logger = Logger.getLogger(WQMDataController.class);

    private Map<String, List<BaseRecord>> sensorData = new Hashtable<String, List<BaseRecord>>();

    public WQMDataController(StationManager stationManager, WQMConfig config) {
        super(stationManager, config);

        stationManager.registerPacketHandler(new PacketHandler<DataUpload>() {

            public boolean handlePacket(PacketHandlerContext ctx, ZNetRxResponse xbeeResponse, DataUpload packet) {
                return handleDataUploadPacket(ctx, xbeeResponse, packet);
            }

            public int getPacketId() {
                return DataUpload.PACKET_ID;
            }
        });
    }

    @RequestMapping(method = RequestMethod.GET, value = "/d/{stationAddress}/{sensorID}/{phaseID}")
    public ModelAndView calibrationData(HttpServletRequest request, HttpServletResponse response,
                                        @PathVariable String stationAddress,
                                        @PathVariable int sensorID,
                                        @PathVariable int phaseID) throws IOException {
        int offset = 0;
        if (request.getParameter("offset") != null) {
            try {
                offset = Integer.parseInt(request.getParameter("offset"));
            } catch (Throwable t) {
                logger.error("Could not parse offset: " + request.getParameter("offset"));
            }
        }

        return new ModelAndView("", "", getCalibrationData(request.getSession(true), AtlasSensor.find(sensorID), offset));
    }

    private Object getCalibrationData(HttpSession session, AtlasSensor sensor, int count) {

        switch (sensor) {
            case PH:
                ArrayList<CalibratePacket> packets = stationManager.getCalibrationSessionManager().getCalibrationSessionData(session);
                if (packets == null) {
                    throw new _404();
                }
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


    public boolean handleDataUploadPacket(PacketHandlerContext ctx, ZNetRxResponse xbeeResponse, DataUpload packet) {
        String address = AddressUtil.getCompactStringAddress(xbeeResponse.getRemoteAddress64());

        for (BaseRecord record : packet.getRecords()) {
            List<BaseRecord> data = getSensorData(String.format("%s_%s", address, getSensorName(record)));
            data.add(record);
            while(data.size() > config.getMaxDataSize())
            {
                data.remove(0);
            }
        }
        return true;
    }

    private List<BaseRecord> getSensorData(String sensorName) {
        List<BaseRecord> data = sensorData.get(sensorName);
        if(data == null)
        {
            sensorData.put(sensorName, data = new ArrayList<BaseRecord>());
        }
        return data;
    }

    private String getSensorName(BaseRecord record) {

        if (record instanceof FloatRecord) {
            FloatRecord rec = (FloatRecord) record;
            return AtlasSensor.find(rec.getId()).name();
        } else if (record instanceof SalinityRecord) {
            SalinityRecord rec = (SalinityRecord) record;
            return AtlasSensor.find(rec.getId()).name();
        } else {
            return "Temperature";
        }
    }

}
