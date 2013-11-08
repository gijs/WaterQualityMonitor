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

#include "XBeeUtil.h"

Stream* XBeeUtil::debug_stream;

bool XBeeUtil::getRadioAddress(XBee* xbee, int32_t& msb, int32_t& lsb, Stream* debug_stream){

	bool toRet = true;
	XBeeUtil::debug_stream = debug_stream;

	AtCommandRequest atRequest = AtCommandRequest();
	AtCommandResponse atResponse = AtCommandResponse();

	uint8_t SH_cmd[] = { 'S', 'H' };
	uint8_t SL_cmd[] = { 'S', 'L' };


	atRequest.setCommand(SH_cmd);
	if (XBeeUtil::atCommand(xbee, atRequest, atResponse) && atResponse.getValueLength() == 4
			&& XBeeUtil::readAtResponseInt(xbee, atResponse, msb)) {
	} else {
		toRet = false;
	}

	atRequest.setCommand(SL_cmd);
	if (XBeeUtil::atCommand(xbee, atRequest, atResponse) && atResponse.getValueLength() == 4
			&& XBeeUtil::readAtResponseInt(xbee, atResponse, lsb)) {
	} else {
		toRet = false;
	}


	return toRet;
}

bool XBeeUtil::associate(XBee* xbee)
{
	uint8_t AI_cmd[] = { 'A', 'I' };
	AtCommandRequest atRequest = AtCommandRequest();
	AtCommandResponse atResponse = AtCommandResponse();
	atRequest.setCommand(AI_cmd);
	if (XBeeUtil::atCommand(xbee, atRequest, atResponse) && atResponse.getValueLength() == 1) {
		return true;
	} else {
		return false;;
	}
}

bool XBeeUtil::atCommand(XBee* xbee, AtCommandRequest& request,	AtCommandResponse& response) {
	xbee->send(request);

// wait up to 5 seconds for the status response
	if (xbee->readPacket(5000)) {
		// got a response!

		// should be an AT command response
		if (xbee->getResponse().getApiId() == AT_COMMAND_RESPONSE) {
			xbee->getResponse().getAtCommandResponse(response);

			if (response.isOk()) {
//				XBeeUtil::debug_stream->print("Command [");
//				XBeeUtil::debug_stream->print(response.getCommand()[0]);
//				XBeeUtil::debug_stream->print(response.getCommand()[1]);
//				XBeeUtil::debug_stream->println("] was successful!");

				if (response.getValueLength() > 0) {
//					XBeeUtil::debug_stream->print("Command value length is ");
//					XBeeUtil::debug_stream->println(response.getValueLength(), DEC);

//					XBeeUtil::debug_stream->print("Command value: ");

					for (int i = 0; i < response.getValueLength(); i++) {
//						XBeeUtil::debug_stream->print(response.getValue()[i], HEX);
//						XBeeUtil::debug_stream->print(" ");
					}

//					XBeeUtil::debug_stream->println("");
				}
				return true;
			} else {
//				XBeeUtil::debug_stream->print("Command return error code: ");
//				XBeeUtil::debug_stream->println(response.getStatus(), HEX);
			}
		} else {
//			XBeeUtil::debug_stream->print("Expected AT response but got ");
//			XBeeUtil::debug_stream->print(xbee->getResponse().getApiId(), HEX);
		}
	} else {
		// at command failed
		if (xbee->getResponse().isError()) {
//			XBeeUtil::debug_stream->print("Error reading packet.  Error code: ");
//			XBeeUtil::debug_stream->println(xbee->getResponse().getErrorCode());
		} else {
//			XBeeUtil::debug_stream->print("No response from radio");
		}
	}
	return false;
}

bool XBeeUtil::readAtResponseInt(XBee* xbee, AtCommandResponse& response, int32_t &value) {
	if (response.getValueLength() == 4) {
		int_packer val;
		for (int i = 0, j = 3; i < 4; i++, j--) {
			val.data[j] = response.getValue()[i];
		}
		value = val.value;
		return true;
	}
	return false;
}

bool XBeeUtil::wait_for_packet_type(XBee* xbee, int timeout, int api_id, bool (*match_packet)(), void (*queue_packet)())
{

	unsigned long end = millis() + timeout;
	while(millis() < end)
	{
		if(xbee->readPacket(end - millis()) && xbee->getResponse().getApiId() == api_id)
		{
			if(match_packet != NULL)
			{
				if(match_packet())
				{
					return true;
				}else
				{
					if (queue_packet != NULL) {
						queue_packet();
					}
				}

			}else
			{
				return true;
			}
		} else {
			if (queue_packet != NULL) {
				queue_packet();
			}
		}
	}
	return false;
}
