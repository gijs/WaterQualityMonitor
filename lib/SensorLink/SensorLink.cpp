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

#include "SensorLink.h"

void SinkPacket::init()
{
	packet_type = SENSORLINK_SINK_PACKET;
	header_size = sizeof(SinkPacket);
	DH = 0x1F2F3F4F;
	DL = 0x1B2B3B4B;
	flags = 0;
}

void DataPacket::init()
{
	packet_type = SENSORLINK_DATA_PACKET;
	header_size = sizeof(DataPacket);
}

void CalibratePacket::init()
{
	packet_type = SENSORLINK_CALIBRATE_PACKET;
	header_size = sizeof(CalibratePacket);
	flags = 0;
	value1 = 0;
	exponent1 = 0;
	value2 = 0;
	exponent2 = 0;
	value2 = 0;
	exponent2 = 0;
}

void CalibratePacket::setVal1(float value, int16_t exponent)
{
	value1 = int64_t(double(value) * (pow(10, exponent)));
	this->exponent1 = -1 * exponent;
}

void CalibratePacket::setVal2(float value, int16_t exponent)
{
	value2 = int64_t(double(value) * (pow(10, exponent)));
	this->exponent2 = -1 * exponent;
}

void CalibratePacket::setVal3(float value, int16_t exponent)
{
	value3 = int64_t(double(value) * (pow(10, exponent)));
	this->exponent3 = -1 * exponent;
}

void StatusPacket::init()
{
	packet_type = SENSORLINK_STATUS_PACKET;
	header_size = sizeof(StatusPacket);
	flags = 0;
	codes = 0;
}
