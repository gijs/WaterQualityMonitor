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

OneWire* Devices::bus;
DallasTemperature* Devices::ds18b20;
XBee* Devices::xbee;
RecordStorage* Devices::store;
Atlas* Devices::atlas;
XBeeSinkAddress* Devices::sink_address;
list_node<XBeeResponse>* Devices::packet_queue_head = NULL;
list_node<XBeeResponse>* Devices::packet_queue_tail = NULL;


uint32_t Devices::initilize_devices(int sd_cs_pin, uint8_t one_wire_bus_pin, Stream& xbee_bus, SensorPosition* sensorMap, uint8_t e_pin, uint8_t so_pin, uint8_t si_pin, Stream& atlas_bus, char* record_store_filename, int32_t max_record_size)
{
	uint32_t toRet = 0;
	pinMode(sd_cs_pin, OUTPUT);
	if (SD.begin(sd_cs_pin)) {
		toRet |= SD_FLAG;
		store = new RecordStorage(max_record_size, record_store_filename, &DEBUG_STREAM);
		DEBUG_LN("SD Initialized!");
	}

	Devices::bus = new OneWire(one_wire_bus_pin);
	Devices::ds18b20 = new DallasTemperature(bus);

	Devices::xbee = new XBee();
	Devices::xbee->begin(xbee_bus);

	Devices::sink_address = new XBeeSinkAddress();
	byte address[sizeof(XBeeSinkAddress)];

	uint16_t read_count = read_eeprom((byte*)&address, XBEE_SINK_ADDRESS_EEPROM_POSITION, sizeof(XBeeSinkAddress));
	DEBUG("EEPROM Read Count: ");
	DEBUG_LN(read_count);


	XBeeSinkAddress* addr = (XBeeSinkAddress*)(&address);
	if(addr->magic_number == XBBE_SINK_ADDRESS_MAGIC_NUMBER)
	{
		Devices::sink_address->DH = addr->DH;
		Devices::sink_address->DL = addr->DL;
	}else
	{
		DEBUG_LN("XBee sink address not set, searching...");
		findSink();
	}
	DEBUG("XBee sink address: 0x");
	DEBUG_(Devices::sink_address->DH, HEX);
	DEBUG(" 0x");
	DEBUG_LN_(Devices::sink_address->DL, HEX);

	RTC.set33kHzOutput(false);
	RTC.clearAlarmFlag(3);
	RTC.setSQIMode(sqiModeAlarm1);

	Devices::atlas = new Atlas(&atlas_bus, e_pin, so_pin, si_pin, sensorMap);
	if(!Devices::atlas->isSensorMapValid())
	{
		toRet |= ATLAS_FLAG;
	}
	return toRet;
}

bool Devices::findSink()
{
	XBeeSinkAddress address;
	XBeeAddress64 addr64 = XBeeAddress64(address.DH, address.DL);
	SinkPacket packet;
	ZBTxRequest request(addr64, (byte*)&packet, sizeof(SinkPacket));


	Devices::xbee->send(request);
	if(wait_for_packet_type(5000, ZB_TX_STATUS_RESPONSE))
	{

	}


}

bool Devices::wait_for_packet_type(int timeout, int api_id)
{

	long end = millis() + timeout;
	while(millis() < end)
	{
		if(Devices::xbee->readPacket(end - millis()) && Devices::xbee->getResponse().getApiId() == api_id)
		{
			return true;
		}else
		{
			queue_packet();
		}
	}
	return false;
}

void _queue_packet(XBeeResponse* packet)
{
	DEBUG("Queued packet with API id: ");
	DEBUG_LN(packet->getApiId());
	if(Devices::packet_queue_head == NULL &&  Devices::packet_queue_tail == NULL)
	{
		Devices::packet_queue_head = new list_node<XBeeResponse>();
		Devices::packet_queue_tail = Devices::packet_queue_head;
		Devices::packet_queue_head->node = packet;
	}else
	{
		Devices::packet_queue_tail->next = new list_node<XBeeResponse>();
		Devices::packet_queue_tail = Devices::packet_queue_tail->next;
		Devices::packet_queue_tail->node = packet;
	}
}

#define _QUEUE_PACKET(packet_type) {packet_type* packet = new packet_type();\
		Devices::xbee->getResponse(*packet);\
		_queue_packet(packet);}


bool Devices::queue_packet()
{

	switch (Devices::xbee->getResponse().getApiId()) {
//	case TX_64_REQUEST:
//		_QUEUE_PACKET(RemoteAtCommandResponse);
//		break;
//	case ZB_TX_REQUEST:
//		_QUEUE_PACKET(ZBTxRequest);
	case RX_64_RESPONSE:
		break;
	case RX_16_RESPONSE:
		break;
	case RX_64_IO_RESPONSE:
		break;
	case RX_16_IO_RESPONSE:
		break;
	case TX_STATUS_RESPONSE:
		break;
	case MODEM_STATUS_RESPONSE:
		break;
	case ZB_RX_RESPONSE:
		break;
	case ZB_EXPLICIT_RX_RESPONSE:
		break;
	case ZB_TX_STATUS_RESPONSE:
		break;
	case ZB_IO_SAMPLE_RESPONSE:
		break;
	case ZB_IO_NODE_IDENTIFIER_RESPONSE:
		break;
	case AT_COMMAND_RESPONSE:
		_QUEUE_PACKET(AtCommandResponse);
		return true;
		break;
	case REMOTE_AT_COMMAND_RESPONSE:
		_QUEUE_PACKET(RemoteAtCommandResponse);
		return true;
		break;
	}

}

void Devices::sample()
{
	DEBUG_LN("Sample: ");
	Sample sample;
	Devices::log_onewire(sample);
	if(!isnan(sample.temperature)){

		_DEBUG("Temperature: ");
		DEBUG_LN(sample.temperature);

		Record rec;

		double orp = Devices::atlas->getORP();
		rec.time_stamp = RTC.get();
		if(!isnan(orp))
		{
			_DEBUG("ORP: ");
			DEBUG_LN(orp);

			rec.id = ORP;
			rec.setVal(orp);
			store->storeRecord((byte*)&rec, sizeof(Record));
		}


		double ph = Devices::atlas->getPH(sample.temperature);
		rec.time_stamp = RTC.get();
		if(!isnan(ph))
		{
			_DEBUG("PH: ");
			DEBUG_LN(ph);

			rec.id = PH;
			rec.setVal(ph);
			store->storeRecord((byte*)&rec, sizeof(Record));
		}

		double us = NAN, ppm = NAN, salinity = NAN;
		Devices::atlas->getEC(sample.temperature, us, ppm, salinity);

		double _do = Devices::atlas->getDO(sample.temperature, 0);
		rec.time_stamp = RTC.get();
		if(!isnan(_do))
		{
			_DEBUG("DO: ");
			DEBUG_LN(_do);

			rec.id = DO;
			rec.setVal(_do);
			store->storeRecord((byte*)&rec, sizeof(Record));
		}
	}
}


void Devices::log_onewire(Sample &sample)
{
	tmElements_t time;

	Devices::bus->reset_search();
	uint8_t address[8];
	boolean found = false;
	Devices::ds18b20->begin();
	Devices::ds18b20->requestTemperatures();
	RTC.read(time);
	while (Devices::bus->search(address)) {
		if (OneWire::crc8(address, 7) == address[7]) {
			switch (address[0]) {
			case 0x28:
			case 0x10:
				found = true;
				Devices::log_ds18b20(address, &time, sample);
				break;
			default:
				break;
			}
		}else
		{
			DEBUG("The CRC of the address: ");
			displayOneWireAddress(address, 8, &DEBUG_STREAM);
			DEBUG_LN(" is incorrect.");
			DEBUG_LN_(OneWire::crc8(address, 7), HEX);
			DEBUG_LN_(address[7], HEX);
		}
		if(!found)
		{
			DEBUG("Unknown OneWire device with address address: ");
			displayOneWireAddress(address, 8, &DEBUG_STREAM);
			DEBUG_LN();
		}
	}
}

void Devices::log_ds18b20(uint8_t* address, tmElements_t* time, Sample &sample)
{
	OneWireRecord rec;
	for(int i = 0; i < 8; i++)
	{
		rec.id[i] = address[i];
	}

	double temp = Devices::ds18b20->getTempC(address);
	if(!isnan(temp) && temp < DS18B20_ERROR_TEMP)
	{
		rec.setVal(temp);
		store->storeRecord((byte*)&rec, sizeof(OneWireRecord));
		if(isnan(sample.temperature))
		{
			sample.temperature = temp;
		}
	}
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
    displayOn->print(slash);
    LEADING_ZERO(displayOn, time->Month);
    displayOn->print(time->Month, DEC);
    displayOn->print(slash);
    LEADING_ZERO(displayOn, time->Day);
    displayOn->print(time->Day, DEC);
    displayOn->print(space);
    LEADING_ZERO(displayOn, time->Hour);
    displayOn->print(time->Hour, DEC);
    displayOn->print(colon);
    LEADING_ZERO(displayOn, time->Minute);
    displayOn->print(time->Minute, DEC);
    displayOn->print(colon);
    LEADING_ZERO(displayOn, time->Second);
    displayOn->print(time->Second, DEC);
    displayOn->println();
}

void Devices::displayOneWireAddress(uint8_t* address, uint8_t address_length, Stream* displayOn)
{
	for(int i = 0; i < address_length; i++)
	{
		displayOn->print(address[i], HEX);
		if(i == 0)
		{
			displayOn->print('x');
		}
	}
}

bool Devices::associate()
{
	uint8_t assocCmd[] = {'A','I'};
	AtCommandRequest atRequest = AtCommandRequest();
	AtCommandResponse atResponse = AtCommandResponse();
	atRequest.setCommand(assocCmd);
	xbee->send(atRequest);
	if (xbee->readPacket(5000)) {
		// got a response!

		// should be an AT command response
		if (xbee->getResponse().getApiId() == AT_COMMAND_RESPONSE) {
			xbee->getResponse().getAtCommandResponse(atResponse);
			return true;
		}
	}
	return false;
}

uint16_t Devices::write_eeprom(byte* loc, uint16_t eeprom_position, uint16_t count)
{

	uint16_t i = 0;
	for(; i < count; i++)
	{
		if(EEPROM.read(eeprom_position + i) != loc[i])
		{
			EEPROM.write(eeprom_position + i, loc[i]);
		}
	}
	return i;
}

uint16_t Devices::read_eeprom(byte* loc, uint16_t eeprom_position, uint16_t count)
{

	uint16_t i = 0;
	for(; i < count; i++)
	{
		loc[i] = EEPROM.read(eeprom_position + i);
	}
	return i;
}
