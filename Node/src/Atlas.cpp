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

#include "Atlas.h"


Atlas::Atlas(Stream* sensor_stream, uint8_t e_pin, uint8_t so_pin, uint8_t si_pin/*, SensorPosition* sensorMap*/)
{
	sensorCount = 0;
	this->sensor_stream = sensor_stream;
	this->e_pin = e_pin;
	this->so_pin = so_pin;
	this->si_pin = si_pin;
	CMODE = -1;

	pinMode(e_pin, OUTPUT);
	disable();
	pinMode(so_pin, OUTPUT);
	pinMode(si_pin, OUTPUT);
	probe_ports();
}

void Atlas::probe_ports() {
	for (int i = 0; i < ATLAS_MAX_SENSORS; i++) {
		select(i);
		enable();
		sensor_stream->print(END_COMMAND);
		sensor_stream->print(carrage_return);
		delay(100);
		sensor_stream->print(END_COMMAND);
		sensor_stream->print(carrage_return);
		clean_sensor_port();
		sensor_stream->print(IDENTIFY_COMMAND);
		sensor_stream->print(carrage_return);
		String version = sensor_stream->readStringUntil(carrage_return);
		_DEBUG(i);
		_DEBUG(" Version: ");
		_DEBUG_LN(version);

		if (version.length() > 0) {

			parse_version(version, i);

		}
//		sensor_stream->print('L1');
//		sensor_stream->print(carrage_return);
		disable();
	}
}

void Atlas::parse_version(String &version, int port) {
	SensorDescriptor* sensor;
	switch (version[0]) {
	case 'O':
	case 'o':
		sensor = &sensorMap[ORP];
		break;
	case 'P':
	case 'p':
		sensor = &sensorMap[PH];
		break;
	case 'E':
	case 'e':
		sensor = &sensorMap[EC];
		break;
	case 'D':
	case 'd':
		sensor = &sensorMap[DO];
		break;
	default:
		return;
	}

	int length = version.length();
	char buf[length + 1];
	for (int i = 0; i < length; i++) {
		buf[i] = version[i];
		if (buf[i] == ',' || buf[i] == '/' || buf[i] == '.') {
			buf[i] = '\0';
		}
	}
	buf[version.length()] = '\0';

	int section_count = 5;

	char* sections[section_count];
	int pos = 0;
	for (int i = 0; i < section_count; i++) {
		sections[i] = buf + pos;
		pos += strlen(sections[i]) + 1;
	}

	sensor->version_major = atoi(sections[1]+1);
	sensor->version_minor = atoi(sections[2]);
	sensor->month = atoi(sections[3]);
	sensor->year = atoi(sections[4]);
	sensor->port = port;
	sensorCount++;
	return;
}

double Atlas::getPH(double temperature)
{
	double toRet = NAN;
	if (select(PH)) {
		if (!isnan(temperature)) {
			enable();
			sensor_stream->print(temperature, 2);
			sensor_stream->print(carrage_return);
			sensor_stream->print(SINGLE_SAMPLE_COMMAND);
			sensor_stream->print(carrage_return);
			String d = sensor_stream->readStringUntil(carrage_return);
			disable();
			toRet = toDouble(d);

		}
	}
	return toRet;
}

double Atlas::getEC(double temperature, int32_t &us, int32_t &ppm, int32_t &salinity)
{
	if (select(EC)) {
		if (!isnan(temperature)) {
			enable();
			sensor_stream->print(temperature, 1);
			sensor_stream->print(carrage_return);
			sensor_stream->print(SINGLE_SAMPLE_COMMAND);
			sensor_stream->print(carrage_return);
			String result = sensor_stream->readStringUntil(carrage_return);

			char split_on[] = {',', '\0'};
			char parts_buf[result.length() + 1];
			memccpy(parts_buf, result.c_str(), sizeof(char), result.length());
			parts_buf[result.length()] = '\0';
			int parts_length = split_string_count(parts_buf, result.length(), split_on, strlen(split_on));


			char* parts[parts_length];
			split_string(parts_buf, parts, parts_length);
			if(parts_length == 3)
			{
				us       = atoi(parts[0]);
				ppm      = atoi(parts[1]);
				salinity = atoi(parts[2]);
			}else
			{
				DEBUG("Error parsing the EC Output: ");
				DEBUG_LN(result);
			}
			disable();
			return 0;
		}
	}
	return NAN;

}

double Atlas::getDO(double temperature, int32_t us)
{
	if (select(DO)) {
		if (!isnan(temperature)) {
			enable();
			sensor_stream->print(temperature, 2);
			sensor_stream->print(comma);
			sensor_stream->print(us);
			sensor_stream->print(carrage_return);
			sensor_stream->print(SINGLE_SAMPLE_COMMAND);
			sensor_stream->print(carrage_return);
			String value = sensor_stream->readStringUntil(carrage_return);
			disable();
			return toDouble(value);
		}
	}
	return NAN;
}

double Atlas::getORP()
{
	if (select(ORP)) {
		enable();
		sensor_stream->print(SINGLE_SAMPLE_COMMAND);
		sensor_stream->print(carrage_return);
		String value = sensor_stream->readStringUntil(carrage_return);
		disable();
		return toDouble(value);
	}
	return NAN;
}

String Atlas::dumpPort(Sensor sensorToSelect)
{
	if (select(sensorToSelect)) {
		return sensor_stream->readStringUntil(carrage_return);
	}
	return NULL;
}

void Atlas::clean_sensor_port()
{
	while(sensor_stream->available())
	{
		sensor_stream->read();
	}
}

void Atlas::enable()
{
	digitalWrite(e_pin, LOW);
	sensor_stream->print(carrage_return);
	sensor_stream->print(carrage_return);
	delay(30);
	clean_sensor_port();

}

void Atlas::disable(){
	digitalWrite(e_pin, HIGH);
	delay(30);
	clean_sensor_port();
}

double Atlas::toDouble(String &value)
{
	char ph[value.length() + 1];
	for(unsigned int i = 0; i < value.length(); i++)
	{
		ph[i] = value[i];
	}
	ph[value.length()] = '\0';
	return atof(ph);
}

bool Atlas::select(Sensor sensor)
{
	if(sensorMap[sensor].port >= 0)
	{
		return select(sensorMap[sensor].port);
	}
	return false;
}

bool Atlas::select(int port)
{
	if((port & 1) > 0)
	{
		digitalWrite(so_pin, HIGH);
	}else
	{
		digitalWrite(so_pin, LOW);
	}
	if((port & 2) > 0)
	{
		digitalWrite(si_pin, HIGH);
	}else
	{
		digitalWrite(si_pin, LOW);
	}
	return true;
}

int8_t Atlas::getSensorCount()
{
	return sensorCount;
}

double Atlas::continuousPH(double temperature)
{
	if(!isnan(temperature) && CMODE == -1)
	{
		getPH(temperature);
	}

	if (CMODE == -1) {
		if (select(PH)) {
			enable();
			CMODE = PH;
			sensor_stream->print(carrage_return);
			sensor_stream->print(CONTINUOUS_COMMAND);
			sensor_stream->print(carrage_return);

		}
	}
	if(CMODE == PH)
	{
		String d = sensor_stream->readStringUntil(carrage_return);
		return toDouble(d);
	}

	return NAN;
}

void Atlas::acceptPH(PHCalibration val)
{
	DEBUG("Accept ");
	DEBUG_LN(val);
}

void Atlas::endContinuous()
{
	if (CMODE != -1) {
	sensor_stream->print(END_COMMAND);
	sensor_stream->print(carrage_return);
	delay(100);
	sensor_stream->print(END_COMMAND);
	sensor_stream->print(carrage_return);
	clean_sensor_port();
	disable();
	CMODE = -1;
	}

}


int Atlas::split_string_count(char* toSplit, int length, char split_on[], int split_on_length)
{
	int count = 1;
	for(int i = 0; i < length; i++)
	{
		for(int j = 0; j < split_on_length; j++)
		{
			if(toSplit[i] == split_on[j])
			{
				count++;
				toSplit[i] = '\0';
			}
		}
	}
	return count;
}

int Atlas::split_string(char* toSplit, char* result[], int result_length)
{
	int pos = 0;
	int count = 0;
	for (int i = 0; i < result_length; i++) {
		result[i] = toSplit + pos;
		pos += strlen(&toSplit[i]) + 1;
		count++;
	}
	return count;
}


