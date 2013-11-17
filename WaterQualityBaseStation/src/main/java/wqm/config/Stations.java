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

package wqm.config;

import com.rapplogic.xbee.api.XBeeAddress64;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Date: 11/2/13
 * Time: 11:41 AM
 *
 * @author NigelB
 */
@XmlRootElement(name="StationManager")
public class Stations {
    private static Logger logger = Logger.getLogger(Stations.class);
    private List<Station> stations = new ArrayList<Station>();
    private Hashtable<XBeeAddress64, String> addresses = new Hashtable<XBeeAddress64, String>();
    private Hashtable<String, Station> stat = new Hashtable<String, Station>();

    public Stations() {
    }

    @XmlElement(name="Station")
    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
        for (Station station : stations) {
            addresses.put(station.getAddress(), station.getCommonName());
            stat.put(station.getCompactAddress(), station);
        }
    }

    public boolean hasAddress(Object key) {
        return addresses.containsKey(key);
    }
    public Station getStation(String compactAddress)
    {
        return stat.get(compactAddress);
    }

    public void addAddress(XBeeAddress64 addr) {
        Station toAdd = new Station(null, addr);
        String compactAddress = toAdd.getCompactAddress();
        if(!stat.containsKey(compactAddress))
        {
            stations.add(toAdd);
            stat.put(compactAddress, toAdd);
        }
    }
}
