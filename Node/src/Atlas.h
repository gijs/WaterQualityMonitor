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
static char ORP_CALIBRATE_PLUS_COMMAND = '+';
static char ORP_CALIBRATE_MINUS_COMMAND = '-';
static char DO_CALIBRATE_COMMAND = 'M';
static char EC_SENSOR_TYPE_COMMAND = 'P';
static char EC_CALIBRATION_COMMAND = 'P';

enum Sensor {
	PH, DO, ORP, EC
};

enum PHCalibration
{
	Four, Seven, Ten
};

enum ECSensorType {
	SInvalid = 0, K0_1 = 1, K1_0 = 2, K10_0 = 3
};

enum ECCalibration {
	CInvalid = 0, Dry = 1, High = 2, Low = 3
};

enum ORPCalibration
{
	Plus, Minus
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
	boolean enableDOPS;
	ECSensorType ecType;

	int32_t CMODE;

	void clean_sensor_port();
	bool select(Sensor sensor);
	bool select(int port);
	void probe_ports();
	bool parse_version(String &version, int port);
	void enable();
	void disable();

	void initilizeDO(int port);

	double toDouble(String &value);
	int split_string_count(char* toSplit, int length, char split_on[], int split_on_length);
	int split_string(char* toSplit, char* result[], int result_length);

	bool startContinuous(Sensor sensor);
	void parseDO(String value, double &percentSaturation, double &_DO);
	bool parseEC(String value, int32_t &us, int32_t &ppm, int32_t &salinity);

public:
	Atlas(Stream* sensor_stream, uint8_t e_pin, uint8_t so_pin, uint8_t si_pin, bool enableDOSaturation);

	double getPH(double temperature);
	double getEC(double temperature, int32_t &us, int32_t &ppm, int32_t &salinity);
	double getDO(double temperature, int32_t us, double &percentSaturation, double &DO);
	double getORP();
	String dumpPort(Sensor sensorToSelect);
	int8_t getSensorCount();


	double continuousPH(double temperature);
	double continuousORP();
	double continuousDO(double temperature, int32_t us, double &percentSaturation, double &DO);
	double continuousEC(double temperature, int32_t &us, int32_t &ppm, int32_t &salinity);
	void   endContinuous();

	void calibratePH(PHCalibration val);
	void calibrateORP(ORPCalibration val);
	void calibrateDO();
	bool calibrateEC(ECCalibration val);
	void setECType(ECSensorType type);
};


#endif /* ATLAS_H_ */
