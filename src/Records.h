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

struct Record
{
	int8_t record_type;
	int8_t id;
	time_t time_stamp;
	int8_t characteristic;
	int8_t mantissa;

	void setVal(float ph);

	Record(): record_type(0), id(0){};
};

struct OneWireRecord
{
	int8_t record_type;
	int8_t id[8];
	time_t time_stamp;
	int8_t characteristic;
	int8_t mantissa;

	OneWireRecord(): record_type(1){};

	void setVal(float ph);
};

#endif /* RECORDS_H_ */
