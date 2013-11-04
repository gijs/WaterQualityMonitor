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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Date: 11/4/13
 * Time: 4:51 PM
 *
 * @author NigelB
 */
@XmlRootElement(name = "Config")
public class WQMConfiguration {
    private RadioConfig radios;
    private DataOutConfig data;


    @XmlElement(name = "Radios")
    public RadioConfig getRadios() {
        return radios;
    }

    public void setRadios(RadioConfig radios) {
        this.radios = radios;
    }

    @XmlElement(name = "Data")
    public DataOutConfig getData() {
        return data;
    }

    public void setData(DataOutConfig data) {
        this.data = data;
    }

    public static void main(String[] args) throws JAXBException {
        WQMConfiguration c = new WQMConfiguration();

        c.setData(new DataOutConfig());
        c.getData().setDataOutputDirectory("data");
        c.getData().setPrefixFormat("yyyy_MMMMM_dd_HH-mm-ss.S_Z");

        RadioConfig rc = new RadioConfig();
        Port p = new Port("/dev/ttyUSB1", 57600, 5);
        rc.getPorts().add(p);
        c.setRadios(rc);
        JAXBContext ctx = JAXBContext.newInstance(WQMConfiguration.class);
        ctx.createMarshaller().marshal(c, System.out);

    }
}
