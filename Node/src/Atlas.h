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

#define ATLAS_MAX_SENSORS 4

static char END_COMMAND = 'E';
static char IDENTIFY_COMMAND = 'I';
static char SINGLE_SAMPLE_COMMAND = 'R';
static char CONTINUOUS_COMMAND = 'C';

enum Sensor {
	PH, DO, ORP, EC
};

enum PHCalibration
{
	Four, Seven, Ten
};




struct SensorDescriptor
{
////	Sensor sensor;
//	bool   available;
	int8_t version_major;
	int8_t version_minor;
	int8_t month;
	int8_t year;
	int8_t port;
	SensorDescriptor(): version_major(-1), version_minor(-1), month(-1), year(-1), port(-1){};
};


class Atlas
{
private:
	Stream* sensor_stream;
	uint8_t e_pin, so_pin, si_pin;
	SensorDescriptor sensorMap[ATLAS_MAX_SENSORS];
	int8_t sensorCount;

	int32_t CMODE;

	void clean_sensor_port();
	bool select(Sensor sensor);
	bool select(int port);
	void probe_ports();
	void parse_version(String &version, int port);
	void enable();
	void disable();

	double toDouble(String &value);
	int split_string_count(char* toSplit, int length, char split_on[], int split_on_length);
	int split_string(char* toSplit, char* result[], int result_length);

public:
	Atlas(Stream* sensor_stream, uint8_t e_pin, uint8_t so_pin, uint8_t si_pin/*, SensorPosition* sensorMap*/);

	double getPH(double temperature);
	double getEC(double temperature, int32_t &us, int32_t &ppm, int32_t &salinity);
	double getDO(double temperature, int32_t us);
	double getORP();
	String dumpPort(Sensor sensorToSelect);
	int8_t getSensorCount();


	double continuousPH(double temperature);
	void   acceptPH(PHCalibration val);
	void endContinuous();
};


#endif /* ATLAS_H_ */
