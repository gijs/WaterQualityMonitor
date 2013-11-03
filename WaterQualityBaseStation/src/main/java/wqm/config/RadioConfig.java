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
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 11/2/13
 * Time: 12:20 PM
 *
 * @author NigelB
 */
@XmlRootElement(name="Radios")
public class RadioConfig {

    private List<Port> ports = new ArrayList<Port>();

    @XmlElement(name="Port")
    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    public static void main(String[] args) throws JAXBException {
        RadioConfig c = new RadioConfig();
        Port p = new Port("/dev/ttyUSB1", 57600, 5);
        c.ports.add(p);
        JAXBContext ctx = JAXBContext.newInstance(RadioConfig.class);
        ctx.createMarshaller().marshal(c, System.out);

    }

}
