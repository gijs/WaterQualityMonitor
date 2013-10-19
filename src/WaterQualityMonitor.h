/*
    Arduino Water Quality Monitor
    Copyright (C) 2013  nigelb

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

#ifndef WaterQualityMonitor_H_
#define WaterQualityMonitor_H_
#include "Arduino.h"
//add your includes for the project WaterQualityMonitor here


#include "Devices.h"
#include "Records.h"

SensorPosition sensorMap[] = {
		{0, PH},
		{1, DO},
		{2, ORP},
		{3, EC},
		{-1, SENSOR_TERMINATOR}
};

//end of add your includes here
#ifdef __cplusplus
extern "C" {
#endif
void loop();
void setup();
void downtime(char* message);
void upload();
void sample();
bool do_upload();
time_t wakeup_at();
void INT0_ISR();
#ifdef __cplusplus
} // extern "C"
#endif

//add your function definitions for the project WaterQualityMonitor here




//Do not add code below this line
#endif /* WaterQualityMonitor_H_ */
