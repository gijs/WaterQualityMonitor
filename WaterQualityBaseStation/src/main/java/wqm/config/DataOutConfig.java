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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;

/**
 * Date: 11/4/13
 * Time: 4:51 PM
 *
 * @author NigelB
 */
@XmlRootElement(name="Radios")
public class DataOutConfig {
    private String dataOutputDirectory;
    private long rotatePeriod = 24 * 60 * 60 * 1000;
    private SimpleDateFormat prefixFormat;

    @XmlAttribute(name="dataDir")
    public String getDataOutputDirectory() {
        return dataOutputDirectory;
    }

    public void setDataOutputDirectory(String dataOutputDirectory) {
        this.dataOutputDirectory = dataOutputDirectory;
    }

    @XmlAttribute(name="rotatePeriod")
    public long getRotatePeriod() {
        return rotatePeriod;
    }
    public void setRotatePeriod(long rotatePeriod) {
        this.rotatePeriod = rotatePeriod;
    }

    @XmlAttribute(name="prefixFormat")
    public String getPrefixFormat() {
        return prefixFormat.toPattern();
    }

    public void setPrefixFormat(String prefixFormat) {
        this.prefixFormat = new SimpleDateFormat(prefixFormat);
    }

    public SimpleDateFormat toFormat()
    {
        return prefixFormat;
    }
}
