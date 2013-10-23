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

#ifndef RECORDS_H_
#define RECORDS_H_

#include "Arduino.h"
#include "Time.h"


#define RECORD_TYPE_DOUBLE_RECORD 1
#define RECORD_TYPE_ONE_WIRE_RECORD 2
#define SALINITY_RECORD 3

struct BaseRecord
{
	int8_t record_type;
};

struct R: BaseRecord
{
	int8_t id;
	time_t time_stamp;
};

struct DoubleRecord: R
{
	int64_t characteristic;
	int16_t exponent;

	void setVal(float value, int16_t exponent);

	DoubleRecord();//: record_type(RECORD_TYPE_DOUBLE_RECORD), id(0){};
};

struct OneWireRecord: BaseRecord
{
	int8_t id[8];
	time_t time_stamp;
	int64_t characteristic;
	int16_t exponent;

	OneWireRecord();//: record_type(RECORD_TYPE_ONE_WIRE_RECORD){};

	void setVal(float value, int16_t exponent);
};

struct SalinityRecord: R
{
	int32_t us;
	int32_t ppm;
	int32_t salinity;

	SalinityRecord();
};

#endif /* RECORDS_H_ */
