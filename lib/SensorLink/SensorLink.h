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
#define SENSORLINK_CALIBRATE_PACKET 4
#define SENSORLINK_STATUS_PACKET 8

#define SENSORLINK_DATA_FLAG_NO_ERROR 0
#define SENSORLINK_DATA_FLAG_ERROR_HANDLING_DATA 1

#define SENSORLINK_STATUS_FLAG_OK 1
#define SENSORLINK_STATUS_FLAG_DEVICE_ERROR 2

#define SENSORLINK_CALIBRATION_FLAG_STOP_CALIBRATION 1
#define SENSORLINK_CALIBRATION_FLAG_START_CALIBRATION 2
#define SENSORLINK_CALIBRATION_FLAG_ACCEPT_0 4
#define SENSORLINK_CALIBRATION_FLAG_ACCEPT_1 16
#define SENSORLINK_CALIBRATION_FLAG_ACCEPT_2 32
#define SENSORLINK_CALIBRATION_FLAG_ACCEPT_3 64
#define SENSORLINK_CALIBRATION_FLAG_ACCEPT_4 128
#define SENSORLINK_CALIBRATION_FLAG_ACCEPT_5 256
#define SENSORLINK_CALIBRATION_FLAG_ACCEPT_CALIBRATION ( SENSORLINK_CALIBRATION_FLAG_ACCEPT_0 | SENSORLINK_CALIBRATION_FLAG_ACCEPT_1 | SENSORLINK_CALIBRATION_FLAG_ACCEPT_2 | SENSORLINK_CALIBRATION_FLAG_ACCEPT_3 | SENSORLINK_CALIBRATION_FLAG_ACCEPT_4 | SENSORLINK_CALIBRATION_FLAG_ACCEPT_5)

#define SENSORLINK_CALIBRATION_ORP_PLUS 2.0
#define SENSORLINK_CALIBRATION_ORP_MINUS 4.0

#define SENSORLINK_CALIBRATION_EC_K_0_1 2
#define SENSORLINK_CALIBRATION_EC_K_1_0 3
#define SENSORLINK_CALIBRATION_EC_K_10_0 4

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

struct CalibratePacket: SensorLinkPacket
{
	int32_t sensor;
	int32_t flags;

	float value1;
	float value2;
	float value3;

	void init();
};

struct StatusPacket: SensorLinkPacket
{
	int64_t flags;
	int64_t codes;
	void init();
};


#endif /* SENSORLINK_H_ */
