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


Atlas::Atlas(Stream* sensor_stream, uint8_t e_pin, uint8_t so_pin, uint8_t si_pin, SensorPosition* sensorMap)
{
	this->sensor_stream = sensor_stream;
	this->e_pin = e_pin;
	this->so_pin = so_pin;
	this->si_pin = si_pin;
	this->sensorMap = sensorMap;
	validSensorMap = -1;
	pinMode(e_pin, OUTPUT);
	disable();
	pinMode(so_pin, OUTPUT);
	pinMode(si_pin, OUTPUT);
}

double Atlas::getPH(double temperature)
{
	double toRet = NAN;
	if (isSensorMapValid() && select(PH)) {
		;
		if (!isnan(temperature)) {
			enable();
			sensor_stream->print(temperature, 2);
			sensor_stream->print(carrage_return);
//			sensor_stream->print('R');
//			sensor_stream->print(carrage_return);
			String d = sensor_stream->readStringUntil(carrage_return);
			disable();
			char ph[d.length() + 1];
			for(unsigned int i = 0; i < d.length(); i++)
			{
				ph[i] = d[i];
			}
			ph[d.length()] = '\0';
			toRet = atof(ph);

		}
	}
	return toRet;
}

void Atlas::getEC(double temperature, double &us, double &ppm, double &salinity)
{

}

double Atlas::getDO(double temperature, double conductivity)
{

}

double Atlas::getORP()
{

}

String Atlas::dumpPort(Sensor sensorToSelect)
{
	if (isSensorMapValid() && select(sensorToSelect)) {
		return sensor_stream->readStringUntil(carrage_return);
	}
	return NULL;
}

void Atlas::selectSensor(Sensor sensorToSelect)
{

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
	clean_sensor_port();
}

void Atlas::disable(){
	digitalWrite(e_pin, HIGH);
}

bool Atlas::select(Sensor sensor)
{
	for(int i = 0; i < ATLAS_MAX_SENSORS; i++)
	{
		if(sensorMap[i].sensor == sensor)
		{
			if((sensorMap[i].sensor & 1) > 0)
			{
				digitalWrite(so_pin, HIGH);
			}else
			{
				digitalWrite(so_pin, LOW);
			}
			if((sensorMap[i].sensor & 2) > 0)
			{
				digitalWrite(si_pin, HIGH);
			}else
			{
				digitalWrite(si_pin, LOW);
			}
			return true;
		}
	}
	return false;
}

bool Atlas::isSensorMapValid()
{
	if(validSensorMap == -1)
	{
		for(int i = 0; i < ATLAS_MAX_SENSORS; i++)
		{
			if(sensorMap[i].position ==-1 && sensorMap[i].sensor == SENSOR_TERMINATOR)
			{
				validSensorMap = true;
			}
		}
		if(validSensorMap == -1)
		{
			validSensorMap = false;
		}
	}
	return validSensorMap;
}
