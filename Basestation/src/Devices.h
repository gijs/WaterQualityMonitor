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

#ifndef DEVICES_H_
#define DEVICES_H_

#include "Constants.h"
#include "SoftwareSerial.h"
#include "DS3232RTC.h"
#include "XBee.h"
#include "SensorLink.h"
#include "XBeeUtil.h"
#include "SD.h"
#include "RecordStorage.h"
#include "Records.h"

#define RGB_RED_PIN A0
#define RGB_GREEN_PIN A1
#define RGB_BLUE_PIN A2

#define RADIO_POWER_PIN 5

#define DEVICES_SD_ERROR 1
#define DEVICES_RTC_ERROR 2
#define DEVICES_RADIO_ERROR 4
#define DEVICES_SD_ERROR 16

#define LEADING_ZERO(STREAM, value) if(value < 10){STREAM->print(0);}

namespace Devices
{
	extern Stream* xbee_stream;
	extern Stream* debug_stream;
	extern RecordStorage* store;
	extern XBee*   xbee;
	extern int32_t SH;
	extern int32_t SL;

	uint64_t initilize_devices(int sd_cs_pin, char* store_files_name, int32_t max_record_size, Stream* xbee_stream, Stream* debug_stream);

	void flash_print(Stream* stream, const prog_char *ptr);
	void flash_println(Stream* stream, const prog_char *ptr);

	void displayDate(time_t time, Stream* displayOn);
	void displayDate(tmElements_t* toDisplay, Stream* displayOn);

}


#endif /* DEVICES_H_ */
