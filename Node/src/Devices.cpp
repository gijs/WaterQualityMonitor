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


uint32_t Devices::initilize_devices(
		int sd_cs_pin,
		uint8_t one_wire_bus_pin,
		Stream& xbee_bus,
		uint8_t xbee_associate_pin,
		/*SensorPosition* sensorMap,*/
		uint8_t e_pin,
		uint8_t so_pin,
		uint8_t si_pin,
		Stream& atlas_bus,
		char* record_store_filename,
		int32_t max_record_size) {

	uint32_t toRet = 0;
	pinMode(sd_cs_pin, OUTPUT);
	if (SD.begin(sd_cs_pin)) {
		DEBUG_LN("SD Initialized!");
		toRet |= SD_FLAG;
		store = new RecordStorage(max_record_size, record_store_filename,
				&DEBUG_STREAM);
		if(store->getErrorCode() == ERROR_NO_ERROR)
		{

		}else{
			uint64_t code = store->getErrorCode();
			if((code | ERROR_INVALID_MAGIC_NUMBER) > 0)
			{
				DEBUG_LN("The Record Storage had an invalid Magic Number.");
			}else if ((code | ERROR_WRITING_HEADER) > 0){
				DEBUG_LN("The Record Storage could not write the header.");
			}else if((code | ERROR_ROW_SIZE_MISMATCH) > 0)
			{
				DEBUG_LN("The Record Storage row size does not match.");
			}
		}
	}

	Devices::bus = new OneWire(one_wire_bus_pin);
	Devices::ds18b20 = new DallasTemperature(bus);

	//Initialize XBEE
	{
		Devices::xbee = new XBee();
		Devices::xbee->begin(xbee_bus);

		Devices::sink_address = new XBeeSinkAddress();
		byte address[sizeof(XBeeSinkAddress)];

		XBeeSinkAddress* addr = (XBeeSinkAddress*) (&address);

		//Is the associate pin pulled high? If so search for the sink node
		pinMode(xbee_associate_pin, INPUT);
		uint16_t associate_pin_value = analogRead(xbee_associate_pin);
		DEBUG("Associate Pin value: ");
		DEBUG_LN(associate_pin_value);
		if (associate_pin_value > XBEE_ASSOCIATE_THRESHOLD) {
			addr->magic_number = 0;
			DEBUG_LN("Resetting Sink Address");
		} else {
			uint16_t read_count = read_eeprom((byte*) &address,
					XBEE_SINK_ADDRESS_EEPROM_POSITION, sizeof(XBeeSinkAddress));
			DEBUG("EEPROM Read Count: ");
			DEBUG_LN(read_count);
		}

		if (addr->magic_number == XBBE_SINK_ADDRESS_MAGIC_NUMBER) {
			Devices::sink_address->DH = addr->DH;
			Devices::sink_address->DL = addr->DL;
		} else {
			DEBUG_LN("XBee sink address not set, searching...");
			findSink();
		}
		DEBUG("XBee sink address: 0x");
		DEBUG_(Devices::sink_address->DH, HEX);
		DEBUG(" 0x");
		DEBUG_LN_(Devices::sink_address->DL, HEX);
	}

	RTC.set33kHzOutput(false);
	RTC.clearAlarmFlag(3);
	RTC.setSQIMode(sqiModeAlarm1);

	Devices::atlas = new Atlas(&atlas_bus, e_pin, so_pin,
			si_pin/*, sensorMap*/);
	if (Devices::atlas->getSensorCount() == 0) {
		toRet |= ATLAS_FLAG;
	}
	return toRet;
}

bool Devices::findSink()
{
	XBeeSinkAddress address;
	XBeeAddress64 addr64 = XBeeAddress64(address.DH, address.DL);
	SinkPacket packet;
	packet.init();
	ZBTxRequest request(addr64, (byte*)&packet, sizeof(SinkPacket));


	Devices::xbee->send(request);

	if(wait_for_packet_type(5000, ZB_TX_STATUS_RESPONSE))
	{
		ZBTxStatusResponse response;
		Devices::xbee->getResponse(response);
		if(wait_for_packet_type(5000, ZB_RX_RESPONSE))
		{
			ZBRxResponse request;
			Devices::xbee->getResponse(request);
			byte* data = request.getData();
			SinkPacket* packet = (SinkPacket*)data;

			Devices::sink_address->magic_number = XBBE_SINK_ADDRESS_MAGIC_NUMBER;
			Devices::sink_address->DH = packet->DH;
			Devices::sink_address->DL = packet->DL;

			uint16_t read_count = write_eeprom((byte*)Devices::sink_address, XBEE_SINK_ADDRESS_EEPROM_POSITION, sizeof(XBeeSinkAddress));
			if(read_count == sizeof(XBeeSinkAddress))
			{
				DEBUG("Wrote XBee Sink address to EEPROM, DH: 0x");
				DEBUG_(Devices::sink_address->DH, HEX);
				DEBUG(", DL: 0x");
				DEBUG_LN_(Devices::sink_address->DL, HEX);
				return true;
			}else
			{
				DEBUG("Failed to write XBee Sink address to EEPROM, killing magic number.");
				EEPROM.write(XBEE_SINK_ADDRESS_EEPROM_POSITION, 0);
			}
		}

	}
	return false;

}

bool Devices::wait_for_packet_type(int timeout, int api_id)
{

	unsigned long end = millis() + timeout;
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
		_QUEUE_PACKET(ZBRxResponse);
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

	return false;

}

void Devices::sample()
{
	DEBUG_LN("Sample: ");
	Sample sample;
	Devices::log_onewire(sample);
	if(!isnan(sample.temperature)){

		_DEBUG("Temperature: ");
		DEBUG_LN(sample.temperature);

		DoubleRecord rec;
		double orp = Devices::atlas->getORP();
		rec.time_stamp = RTC.get();
		if(!isnan(orp))
		{
			_DEBUG("ORP: ");
			DEBUG_LN(orp);

			rec.id = ORP;
			rec.setVal(orp, 2);
			store->storeRecord((byte*)&rec, sizeof(DoubleRecord));
		}

		double ph = Devices::atlas->getPH(sample.temperature);
		rec.time_stamp = RTC.get();
		if(!isnan(ph))
		{
			_DEBUG("PH: ");
			DEBUG_LN(ph);

			rec.id = PH;
			rec.setVal(ph, 2);
			store->storeRecord((byte*)&rec, sizeof(DoubleRecord));
		}

		int32_t us = -1, ppm = -1, salinity = -1;
		Devices::atlas->getEC(sample.temperature, us, ppm, salinity);
		SalinityRecord ec_rec;
		ec_rec.time_stamp = RTC.get();
		if(us >= 0 && ppm >=0  && salinity >= 0)
		{
			_DEBUG("uS: ");
			DEBUG(us);
			DEBUG(", PPM: ");
			DEBUG(ppm);
			DEBUG(", Salinity: ");
			DEBUG_LN(salinity);
			ec_rec.id = EC;
			ec_rec.us = us;
			ec_rec.ppm = ppm;
			ec_rec.salinity = salinity;
			store->storeRecord((byte*)&ec_rec, sizeof(SalinityRecord));
		}

		double _do = Devices::atlas->getDO(sample.temperature, ec_rec.salinity);
		rec.time_stamp = RTC.get();
		if(!isnan(_do))
		{
			_DEBUG("DO: ");
			DEBUG_LN(_do);

			rec.id = DO;
			rec.setVal(_do, 2);
			store->storeRecord((byte*)&rec, sizeof(DoubleRecord));
		}
	}
}


void Devices::log_onewire(Sample &sample)
{
//	tmElements_t time;

	Devices::bus->reset_search();
	uint8_t address[8];
	boolean found = false;
	Devices::ds18b20->begin();
	Devices::ds18b20->requestTemperatures();
//	RTC.read(time);
	time_t time = RTC.get();
	while (Devices::bus->search(address)) {
		if (OneWire::crc8(address, 7) == address[7]) {
			switch (address[0]) {
			case 0x28:
			case 0x10:
				found = true;
				Devices::log_ds18b20(address, time, sample);
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

void Devices::log_ds18b20(uint8_t* address, time_t time, Sample &sample)
{
	OneWireRecord rec;
	for(int i = 0; i < 8; i++)
	{
		rec.id[i] = address[i];
	}

	double temp = Devices::ds18b20->getTempC(address);
	if(!isnan(temp) && temp < DS18B20_ERROR_TEMP)
	{
		rec.setVal(temp, 2);
		rec.time_stamp = time;
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
