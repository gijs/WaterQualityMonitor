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

#ifndef ATLAS_H_
#define ATLAS_H_

#include "Arduino.h"
#include "Constants.h"

#define ATLAS_MAX_SENSORS 5

enum Sensor {
	PH, DO, ORP, EC, SENSOR_TERMINATOR
};

struct SensorPosition
{
	int8_t position;
	Sensor sensor;
};


class Atlas
{
private:
	Stream* sensor_stream;
	uint8_t e_pin, so_pin, si_pin;
	SensorPosition* sensorMap;
	int8_t validSensorMap;
	void selectSensor(Sensor sensorToSelect);
	void clean_sensor_port();
	bool select(Sensor sensor);
	void enable();
	void disable();
public:
	Atlas(Stream* sensor_stream, uint8_t e_pin, uint8_t so_pin, uint8_t si_pin, SensorPosition* sensorMap);
	bool isSensorMapValid();
	double getPH(double temperature);
	void getEC(double temperature, double &us, double &ppm, double &salinity);
	double getDO(double temperature, double conductivity);
	double getORP();
	String dumpPort(Sensor sensorToSelect);
};


#endif /* ATLAS_H_ */
