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

#include "Records.h"

FloatRecord::FloatRecord()
{
	record_type = RECORD_TYPE_FLOAT_RECORD;
	id = 0;
	value1 = INFINITY;
	value2 = INFINITY;
}

OneWireRecord::OneWireRecord()
{
	record_type = RECORD_TYPE_ONE_WIRE_RECORD;
	for(int i = 0; i < 8; i++)
	{
		id[i] = 0;
	}
}

SalinityRecord::SalinityRecord() {
	record_type = SALINITY_RECORD;
	id = 0;
}
