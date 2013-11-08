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

#ifndef XBEE_UTIL_H_
#define XBEE_UTIL_H_

#include "Arduino.h"
#include "XBee.h"

union int_packer{
	int32_t value;
	byte data[4];
};

namespace XBeeUtil {
	extern Stream* debug_stream;

	bool getRadioAddress(XBee* xbee, int32_t& msb, int32_t& lsb, Stream* debug_stream);

	bool atCommand(XBee* xbee, AtCommandRequest& request, AtCommandResponse& response);
	bool readAtResponseInt(XBee* xbee, AtCommandResponse& response, int32_t &value);
	bool wait_for_packet_type(XBee* xbee, int timeout, int api_id, bool (*match_packet)() = NULL, void (*queue_packet)() = NULL);
	bool associate(XBee* xbee);
}

#endif /* XBEE_UTIL_H_ */
