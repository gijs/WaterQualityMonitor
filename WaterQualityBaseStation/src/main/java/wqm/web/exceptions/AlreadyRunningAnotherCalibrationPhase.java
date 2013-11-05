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

package wqm.web.exceptions;

import wqm.config.Station;

/**
 * Date: 11/4/13
 * Time: 7:31 AM
 *
 * @author NigelB
 */
public class AlreadyRunningAnotherCalibrationPhase extends Exception{
    private int phaseID;

    public AlreadyRunningAnotherCalibrationPhase(int phaseID) {
        this.phaseID = phaseID;
    }

    public AlreadyRunningAnotherCalibrationPhase(String message, int phaseID) {
        super(message);
        this.phaseID = phaseID;
    }

    public AlreadyRunningAnotherCalibrationPhase(String message, Throwable cause, int phaseID) {
        super(message, cause);
        this.phaseID = phaseID;
    }

    public AlreadyRunningAnotherCalibrationPhase(Throwable cause, int phaseID) {
        super(cause);
        this.phaseID = phaseID;
    }

    public int getPhaseID() {
        return phaseID;
    }
}
