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

#ifndef SENSORLINK_H_
#define SENSORLINK_H_

#include "Arduino.h"


//#define MAX_PACKET_SIZE 200
#define MAX_PACKET_SIZE (MAX_FRAME_DATA_SIZE - 10)

#define SENSORLINK_SINK_PACKET 1
#define SENSORLINK_DATA_PACKET 2

#define SENSORLINK_DATA_FLAG_NO_ERROR 0
#define SENSORLINK_DATA_FLAG_ERROR_HANDLING_DATA 1


struct SensorLinkPacket
{
	uint8_t  packet_type;
	uint8_t  header_size;
};

struct SinkPacket: SensorLinkPacket
{
	uint32_t flags;
	uint32_t DH;
	uint32_t DL;
	void init();
};

struct DataPacket: SensorLinkPacket
{

	uint32_t flags;
	uint32_t senquence;
	uint8_t  row_size;
	uint8_t  row_count;

	void init();
};


#endif /* SENSORLINK_H_ */
