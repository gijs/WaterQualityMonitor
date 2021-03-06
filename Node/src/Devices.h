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
#include "DS3232RTC.h"
#include "OneWire.h"
#include "DallasTemperature.h"
#include "RecordStorage.h"
#include "XBee.h"
#include "Atlas.h"
#include "SensorLink.h"
#include "Records.h"
#include "EEPROM.h"
#include "XBeeUtil.h"
#include "CLI.h"
#include "cli_rtc.h"



#define LEADING_ZERO(STREAM, value) if(value < 10){STREAM->print(0);}
#define XBBE_SINK_ADDRESS_MAGIC_NUMBER 0xDEADBEEF
#define XBEE_SINK_ADDRESS_EEPROM_POSITION 0
#define XBEE_ASSOCIATE_THRESHOLD 1000

#define ERROR_CODE_SD_FLAG    1
#define ERROR_CODE_ATLAS_FLAG (1 < 1)
#define ERROR_CODE_XBEE_FLAG  (1 < 2)
#define DS18B20_ERROR_TEMP 85

struct Sample
{
	double temperature;
	Sample() : temperature(NAN){};
};

struct XBeeSinkAddress
{
	uint32_t magic_number;
	uint32_t DH;
	uint32_t DL;
	XBeeSinkAddress(): magic_number(XBBE_SINK_ADDRESS_MAGIC_NUMBER), DH(0x0), DL(BROADCAST_ADDRESS){};
};

template<typename T> struct list_node_ptr
{
	T* node;
	list_node_ptr<T>* next;
	list_node_ptr():
		node(NULL), next(NULL){};
};

namespace Devices
{
	const prog_char FIND_SINK_COMMAND_NAME[] PROGMEM  = {"find_sink"};
	const prog_char FIND_SINK_DESCRIPTION[] PROGMEM  = {"Search for and set the sink node."};
	const prog_char CLEAR_SINK_COMMAND_NAME[] PROGMEM  = {"clear_sink"};
	const prog_char CLEAR_SINK_DESCRIPTION[] PROGMEM  = {"Erase the current sink node's address from the EEPROM."};

	extern OneWire* bus;
	extern OneWire* bus_air;
//	extern DallasTemperature* ds18b20;
	extern XBee* xbee;
	extern RecordStorage* store;
	extern Atlas* atlas;
	extern XBeeSinkAddress* sink_address;
	extern list_node_ptr<XBeeResponse> *packet_queue_head; //, *packet_queue_tail;

	extern int32_t SH;
	extern int32_t SL;

	uint32_t initilize_devices(
			int sd_cs_pin,
			uint8_t one_wire_bus_pin,
			uint8_t other_one_wire_bus_pin,
			Stream& xbee_bus,
			uint8_t xbee_associate_pin,
//			SensorPosition* sensorMap,
			uint8_t e_pin, uint8_t so_pin,
			uint8_t si_pin,
			Stream& atlas_bus,
			char* record_store_filename,
			int32_t max_record_size);


	bool setupXbee(Stream& xbee_stream, uint8_t xbee_associate_pin);

	void log_onewire(Sample &sample, OneWire* _bus);
	void log_ds18b20(uint8_t* address, time_t time, Sample &sample, DallasTemperature* _ds18b20);
	void displayDate(time_t time, Stream* displayOn);
	void displayDate(tmElements_t* toDisplay, Stream* displayOn);
	void displayOneWireAddress(uint8_t* address, uint8_t address_length, Stream* displayOn);

	void sample();

	bool associate();
	bool matchSinkPacket();
	bool findSink();
	void queue_packet();
//	bool wait_for_packet_type(int timeout, int api_id);

	void send_status(StatusPacket* status_packet);

	void calibrate(ZBRxResponse* calibrate_request, unsigned long timeout);

	bool search_and_retrieve_from_queue( uint32_t packet_type, XBeeResponse* &packet);


	uint16_t write_eeprom(byte* loc, uint16_t eeprom_position, uint16_t count);
	uint16_t read_eeprom(byte* loc, uint16_t eeprom_position, uint16_t count);



	int initialize_cli(CLI* prompt);
	int find_sink_callback(char** argv, int argc, Environment* env);
	int clear_sink_callback(char** argv, int argc, Environment* env);
}


#endif /* DEVICES_H_ */
