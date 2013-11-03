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

/**
 * Date: 11/3/13
 * Time: 10:42 AM
 *
 * @author NigelB
 */
public enum AtlasSensor {

    PH("PH"),
    DO("Dissolved Oxygen"),
    ORP("Oxidation Reduction Potential"),
    EC("Electrical Conductivity");

    private int id;
    private String longName;

    AtlasSensor(String longName) {
        this.longName = longName;
        id = Count.count++;

    }

    private static class Count {
        static int count = 0;
    }

    public int getId() {
        return id;
    }

    public String getLongName() {
        return longName;
    }

    public static AtlasSensor find(int id)
    {
        for (AtlasSensor atlasSensor : values()) {
            if(atlasSensor.getId() == id)
            {
                return atlasSensor;
            }
        }
        return null;
    }

    //    public static
    public static void main(String[] args) {
        AtlasSensor d = DO;
        switch(d)
        {
            case PH:
                System.out.println(PH);
                break;
            case DO:
                System.out.println(DO);
                break;
        }
    }
}
