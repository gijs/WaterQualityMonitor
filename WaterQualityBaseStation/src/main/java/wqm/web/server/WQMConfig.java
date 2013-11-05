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

package wqm.web.server;

import com.rapplogic.xbee.api.XBeeAddress64;
import org.apache.log4j.Logger;
import wqm.JAXBHelper;
import wqm.config.*;
import wqm.web.exceptions.InvalidRadioException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 10/30/13
 * Time: 5:34 PM
 *
 * @author NigelB
 */
public class WQMConfig {
    private static Logger logger = Logger.getLogger(WQMConfig.class);

    private String address;
    private int port;
    private String configDir;

    private JAXBContext ctx;

    private Stations stations = null;
    private RadioConfig radioConfig = null;
    private Runnable shutdownCallback;
    private DataOutConfig dataConfig;

    private int maxDataSize;

    public WQMConfig() throws JAXBException {
        ctx = JAXBContext.newInstance(Stations.class, RadioConfig.class, WQMConfiguration.class);

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setConfigDir(String configDir) throws JAXBException, InvalidRadioException {
        this.configDir = configDir;
        initializeConfig();
    }

    private void initializeConfig() throws JAXBException, InvalidRadioException {
        File map = new File(configDir, ConfigFiles.StationMap.getFileName());
        if (map.exists()) {
            stations = (Stations) ctx.createUnmarshaller().unmarshal(map);
            stations.setStations(stations.getStations());
        } else {
            stations = new Stations();
        }

        File wqmConfig = new File(configDir, ConfigFiles.WQMConfig.getFileName());
        if (wqmConfig.exists()) {
            WQMConfiguration cfg = (WQMConfiguration) ctx.createUnmarshaller().unmarshal(wqmConfig);
            radioConfig = cfg.getRadios();
            dataConfig = cfg.getData();
            maxDataSize = cfg.getMaxWebDataSize();
        } else {
            throw new InvalidRadioException("The file " + wqmConfig + " does not exist.");
        }
    }

    public String getConfigDir() {
        return configDir;
    }

    public List<Station> getStations() {
        return new ArrayList<Station>(stations.getStations());
    }

    public RadioConfig getRadioConfig() {
        return radioConfig;
    }

    public void addShutdownCallback(Runnable shutdownCallback) {

        this.shutdownCallback = shutdownCallback;
    }

    public void shutdown() {
        if (shutdownCallback != null) {
            this.shutdownCallback.run();
        }
    }

    public void addStation(XBeeAddress64 addr) {
        if(!stations.hasAddress(addr))
        {
            logger.error(addr);
            stations.addAddress(addr);
            storeStations();
        }
    }

    public void renameStation(Station station, String name) {
        station.setCommonName(name);
        storeStations();
    }

    private void storeStations()
    {
        try{
            JAXBHelper.createMarshaller(ctx, true).marshal(stations, new File(configDir, ConfigFiles.StationMap.getFileName()));
        } catch (JAXBException e) {
            logger.error("Error writing station file.", e);
        }

    }

    public Station getStation(String station) {
        if(station != null){
        return stations.getStation(station);
        }
        return null;
    }

    public DataOutConfig getDataConfig() {
        return dataConfig;
    }

    public int getMaxDataSize() {
        return maxDataSize;
    }

    public void setMaxDataSize(int maxDataSize) {
        this.maxDataSize = maxDataSize;
    }
}
