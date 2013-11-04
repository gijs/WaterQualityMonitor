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
