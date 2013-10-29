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

#include <avr/pgmspace.h>

//static char tab = '\t';

#define DEBUG_STREAM Devices::debug_stream
//#define START_DEBUG_STREAM(baud_rate) DEBUG_STREAM->begin(baud_rate);
#define DEBUG_LN(val) DEBUG_STREAM->println(val)
#define DEBUG_LN_(val, fmt) DEBUG_STREAM->println(val, fmt)
#define DEBUG(val) DEBUG_STREAM->print(val)
#define DEBUG_(val, fmt) DEBUG_STREAM->print(val, fmt)

#define _DEBUG(val) DEBUG_STREAM->print(tab);DEBUG_STREAM->print(val)
#define _DEBUG_LN(val) DEBUG_STREAM->print(tab);DEBUG_STREAM->println(val)
#define FREE_MEM DEBUG("Free Ram: ");DEBUG_LN(FreeRam())



//#define DEBUG_STREAM Devices::debug_stream
////#define START_DEBUG_STREAM(baud_rate) DEBUG_STREAM->begin(baud_rate);
//#define DEBUG_LN(val)
//#define DEBUG_LN_(val, fmt)
//#define DEBUG(val)
//#define DEBUG_(val, fmt)
//
//#define _DEBUG(val)
//#define _DEBUG_LN(val)
//#define FREE_MEM DEBUG("Free Ram: ");DEBUG_STREAM->println(FreeRam())



#define ___FDEBUG(val) Devices::flash_print(DEBUG_STREAM, val);
#define ___FDEBUG_LN(val) Devices::flash_println(DEBUG_STREAM, val);

namespace WQM_Strings{
	const prog_char STARTING[] PROGMEM  = {"Starting.."};
	const prog_char DEVICE_INITIALIZATION_ERROR[] PROGMEM  = {"Device initialization error: "};
	const prog_char RECIEVED_PACKET[] PROGMEM  = {"Received Packet: "};
	const prog_char UNKNOWN_API_TYPE[] PROGMEM  = {"Unknown API type"};
	const prog_char RECIEVED_PACKET_ERROR_CODE[] PROGMEM  = {"Error code: 0x"};
	const prog_char RECEIVED_PACKET_FROM[] PROGMEM  = {" from: 0x"};

	const prog_char ZEROX_HEX_PREFIX[] PROGMEM  = {"0x"};
	const prog_char UNKNOWN_PACKET_TYPE[] PROGMEM  = {"Unknown packet type: "};
	const prog_char RADIO_ADDRESS[] PROGMEM  = {"Radio Address: "};
	const prog_char DH[] PROGMEM  = {"DH: "};
	const prog_char DL[] PROGMEM  = {"DL: "};
	const prog_char SH[] PROGMEM  = {"SH: "};
	const prog_char SL[] PROGMEM  = {"SL: "};
	const prog_char API_TYPE[] PROGMEM  = {"API Type: "};
	const prog_char SENT[] PROGMEM  = {"SENT"};
	const prog_char ROWS_RECEIVED[] PROGMEM  = {"Rows received: "};
	const prog_char STORED_RECORDS[] PROGMEM  = {"Records in store: "};
	const prog_char INCORRECT_DATA_SZIE[] PROGMEM  = {"Incorrect data size for DataPacket"};
	const prog_char INITIALIZING_DEVICES[] PROGMEM  = {"Initializing devices."};
	const prog_char SD_INITIALIZED[] PROGMEM  = {"SD Initialized."};

	const prog_char DOT[] PROGMEM  = {"."};
	const prog_char SLASH[] PROGMEM  = {"/"};
	const prog_char SPACE[] PROGMEM  = {" "};
	const prog_char COLON[] PROGMEM  = {":"};
}







