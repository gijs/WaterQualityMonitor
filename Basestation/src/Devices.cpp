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

#include "Devices.h"

Stream* Devices::xbee_stream;
Stream* Devices::debug_stream;
RecordStorage* Devices::store;
XBee*   Devices::xbee;
int32_t Devices::SH;
int32_t Devices::SL;

uint64_t Devices::initilize_devices(int sd_cs_pin, char* store_files_name, int32_t max_record_size, Stream* xbee_stream, Stream* debug_stream)
{
	uint64_t ERROR_CODE = 0;
	Devices::debug_stream = debug_stream;
	___FDEBUG_LN(WQM_Strings::INITIALIZING_DEVICES);
	Devices::xbee_stream = xbee_stream;

	RTC.set33kHzOutput(false);
	RTC.clearAlarmFlag(3);
	RTC.setSQIMode(sqiModeAlarm1);

	Devices::xbee = new XBee();
	Devices::xbee->begin(*xbee_stream);
	int32_t a,b;
	if(!XBeeUtil::getRadioAddress(Devices::xbee, Devices::SH, Devices::SL, DEBUG_STREAM))
	{
		ERROR_CODE = ERROR_CODE | DEVICES_RADIO_ERROR;
	}

	if(ERROR_CODE == 0 )
	{
		if(SD.begin(sd_cs_pin))
		{
			___FDEBUG_LN(WQM_Strings::SD_INITIALIZED);
			store = new RecordStorage(max_record_size, store_files_name, Devices::SH, Devices::SL, DEBUG_STREAM);
		}else
		{
			ERROR_CODE = ERROR_CODE | DEVICES_SD_ERROR;
		}
	}

	return ERROR_CODE;
}

void Devices::flash_print(Stream* stream, const prog_char *ptr)
{
	int32_t len = strlen_P((char*)ptr);
//	char buf[len];
//	strcpy_P(buf, (char*)ptr);
	for(int i = 0; i < len; i++)
	{
		stream->print((char)pgm_read_byte_near(ptr + i));
	}

}

void Devices::flash_println(Stream* stream, const prog_char *ptr)
{
	int32_t len = strlen_P((char*)ptr);
//	char buf[len];
//	strcpy_P(buf, (char*)ptr);
//	stream->println(buf);
	Devices::flash_print(stream, ptr);
	stream->println();
}

void Devices::displayDate(time_t time, Stream* displayOn)
{
	tmElements_t _t;
	breakTime(time, _t);
	displayDate(&_t, displayOn);
}

void Devices::displayDate(tmElements_t* time, Stream* displayOn)
{
    displayOn->print(tmYearToCalendar(time->Year), DEC);
    flash_print(displayOn, WQM_Strings::SLASH);
    LEADING_ZERO(displayOn, time->Month);
    displayOn->print(time->Month, DEC);
    flash_print(displayOn, WQM_Strings::SLASH);
    LEADING_ZERO(displayOn, time->Day);
    displayOn->print(time->Day, DEC);
    flash_print(displayOn, WQM_Strings::SPACE);
    LEADING_ZERO(displayOn, time->Hour);
    displayOn->print(time->Hour, DEC);
    flash_print(displayOn, WQM_Strings::COLON);
    LEADING_ZERO(displayOn, time->Minute);
    displayOn->print(time->Minute, DEC);
    flash_print(displayOn, WQM_Strings::COLON);
    LEADING_ZERO(displayOn, time->Second);
    displayOn->print(time->Second, DEC);
    displayOn->println();
}


