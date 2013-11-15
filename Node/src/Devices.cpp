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
OneWire* Devices::bus_air;
//DallasTemperature* Devices::ds18b20;
XBee* Devices::xbee;
RecordStorage* Devices::store;
Atlas* Devices::atlas;
XBeeSinkAddress* Devices::sink_address;
list_node_ptr<XBeeResponse>* Devices::packet_queue_head = NULL;
//list_node<XBeeResponse>* Devices::packet_queue_tail = NULL;

int32_t Devices::SH;
int32_t Devices::SL;

bool          doCalibrating;
unsigned long doStartTime;


uint32_t Devices::initilize_devices(
		int sd_cs_pin,
		uint8_t in_water_one_wire_bus_pin, //This is the bus that is use to find the temperature of the water the sensors are in.
		uint8_t other_one_wire_bus_pin, //This is the bus that is use to find the temperature of of the air and case etc.
		Stream& xbee_stream,
		uint8_t xbee_associate_pin,
		/*SensorPosition* sensorMap,*/
		uint8_t e_pin,
		uint8_t so_pin,
		uint8_t si_pin,
		Stream& atlas_bus,
		char* record_store_filename,
		int32_t max_record_size) {

	doCalibrating = false;
	uint32_t toRet = 0;
	pinMode(sd_cs_pin, OUTPUT);
	if(!setupXbee(xbee_stream, xbee_associate_pin))
	{
		toRet = toRet | ERROR_CODE_XBEE_FLAG;
	}
	if (SD.begin(sd_cs_pin)) {
		DEBUG_LN("SD Initialized!");

		store = new RecordStorage(max_record_size,record_store_filename, Devices::SH, Devices::SL, &DEBUG_STREAM);
		if(store->getErrorCode() == ERROR_NO_ERROR)
		{

		}else{
			uint64_t code = store->getErrorCode();
			if((code | ERROR_INVALID_MAGIC_NUMBER) > 0)
			{
				DEBUG_LN("The Record Storage had an invalid Magic Number.");
				toRet |= ERROR_CODE_SD_FLAG;
			}else if ((code | ERROR_WRITING_HEADER) > 0){
				DEBUG_LN("The Record Storage could not write the header.");
				toRet |= ERROR_CODE_SD_FLAG;
			}else if((code | ERROR_ROW_SIZE_MISMATCH) > 0)
			{
				DEBUG_LN("The Record Storage row size does not match.");
				toRet |= ERROR_CODE_SD_FLAG;
			}
		}
	}else
	{
		toRet |= ERROR_CODE_SD_FLAG;
	}

	Devices::bus = new OneWire(in_water_one_wire_bus_pin);
//	Devices::ds18b20 = new DallasTemperature(Devices::bus);
//
	Devices::bus_air = new OneWire(other_one_wire_bus_pin);
//	Devices::ds18b20 = new DallasTemperature(Devices::bus_air);


	RTC.set33kHzOutput(false);
	RTC.clearAlarmFlag(3);
	RTC.setSQIMode(sqiModeAlarm1);

	Devices::atlas = new Atlas(&atlas_bus, e_pin, so_pin, si_pin, true);
	if (Devices::atlas->getSensorCount() == 0) {
		toRet |= ERROR_CODE_ATLAS_FLAG;
	}
	return toRet;
}

bool Devices::setupXbee(Stream& xbee_stream, uint8_t xbee_associate_pin)
{
	//Initialize XBEE

		Devices::xbee = new XBee();
		Devices::xbee->begin(xbee_stream);

		XBeeUtil::getRadioAddress(Devices::xbee, Devices::SH, Devices::SL, &DEBUG_STREAM);
		DEBUG("Radio Address: 0x");
		DEBUG_(Devices::SH, HEX);
		DEBUG(" 0x");
		DEBUG_LN_(Devices::SL, HEX);

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
		return true;
}

bool Devices::matchSinkPacket()
{

}

bool Devices::findSink()
{
	XBeeSinkAddress address;
	XBeeAddress64 addr64 = XBeeAddress64(address.DH, address.DL);
	SinkPacket packet;
	packet.init();
	ZBTxRequest request(addr64, (byte*)&packet, sizeof(SinkPacket));


	Devices::xbee->send(request);

	if(XBeeUtil::wait_for_packet_type(Devices::xbee, 5000, ZB_TX_STATUS_RESPONSE, NULL, Devices::queue_packet))
	{
		ZBTxStatusResponse response;
		Devices::xbee->getResponse(response);

		XBeeResponse* packet;
		bool in_queue = Devices::search_and_retrieve_from_queue(SENSORLINK_SINK_PACKET, packet);
		if(in_queue | XBeeUtil::wait_for_packet_type(Devices::xbee, 5000, ZB_RX_RESPONSE, Devices::matchSinkPacket, Devices::queue_packet))
		{
			ZBRxResponse* request;
			if(in_queue)
			{
				DEBUG_LN("Found in queue");
				request = static_cast<ZBRxResponse*>(packet);
			}else
			{
				ZBRxResponse _request;
				Devices::xbee->getResponse(_request);
				request = &_request;
			}

			byte* data = request->getData();
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

bool populate_sensor_calibration(CalibratePacket* calibrate_packet, CalibratePacket* outGoing)
{
	DEBUG("Populating: ");
	switch (calibrate_packet->sensor) {
	case PH:
	{
		DEBUG("PH: ");
		outGoing->sensor = calibrate_packet->sensor;
		double ph = Devices::atlas->continuousPH(NAN);
		DEBUG_LN(ph);
		if (!isnan(ph)) {
			outGoing->value1 = ph;
			return true;
		}
	}
		break;
	case DO:
		if(!doCalibrating)
		{
			doStartTime = millis();
			doCalibrating = true;
		}
		DEBUG("DO: ");
		outGoing->sensor = calibrate_packet->sensor;
		double percentage, _do;
		Devices::atlas->continuousDO(NAN, 0, percentage, _do);
		DEBUG(_do);
		DEBUG(" %: ");
		DEBUG_LN(percentage);
		if (!isnan(_do)) {
			outGoing->value1 = _do;
			outGoing->value2 = percentage;
			outGoing->value3 = (millis() - doStartTime);
			return true;
		}
		break;
	case ORP:
	{
		DEBUG("ORP: ");
		outGoing->sensor = calibrate_packet->sensor;
		double orp = Devices::atlas->continuousORP();
		DEBUG_LN(orp);
		if (!isnan(orp)) {
			outGoing->value1 = orp;
			return true;
		}
	}
		break;
	case EC:
		DEBUG("EC, uS: ");
		int32_t us, ppm, salinity;
		double _ok = Devices::atlas->continuousEC(NAN, us, ppm, salinity);
		DEBUG(us);DEBUG(", ppm: ");DEBUG(ppm);DEBUG(", salinity: ");DEBUG_LN(salinity);
		if(!isnan(_ok))
		{
			outGoing->value1 = us;
			outGoing->value2 = ppm;
			outGoing->value3 = salinity;
			return true;
		}
		break;
	}
	return false;
}

void accept_sensor_calibration(CalibratePacket* calibrate_packet)
{
	DEBUG_LN("ACCEPT");
	_DEBUG("Sensor: ");
	DEBUG_LN(calibrate_packet->sensor);
	_DEBUG("Flags: ");
	DEBUG_LN(calibrate_packet->flags);
	switch (calibrate_packet->sensor) {
	case PH: {
		bool accept = false;
		PHCalibration val;
		switch (calibrate_packet->flags
				& SENSORLINK_CALIBRATION_FLAG_ACCEPT_CALIBRATION) {
		case SENSORLINK_CALIBRATION_FLAG_ACCEPT_0:
			val = Seven;
			accept = true;
			break;
		case SENSORLINK_CALIBRATION_FLAG_ACCEPT_1:
			val = Four;
			accept = true;
			break;
		case SENSORLINK_CALIBRATION_FLAG_ACCEPT_2:
			val = Ten;
			accept = true;
			break;

		}
		if (accept) {
			Devices::atlas->calibratePH(val);
		}
	}
		break;
	case DO:
		doCalibrating = false;
		Devices::atlas->calibrateDO();
		break;
	case ORP: {

		if (calibrate_packet->value1 == SENSORLINK_CALIBRATION_ORP_PLUS) {
			DEBUG_LN("ORP PLUS");
			Devices::atlas->calibrateORP(Plus);
		} else if (calibrate_packet->value1 == SENSORLINK_CALIBRATION_ORP_MINUS) {
			DEBUG_LN("ORP MINUS");
			Devices::atlas->calibrateORP(Minus);
		} else {
			DEBUG("Unknown calibration value: ");
			DEBUG_LN(calibrate_packet->value1);
		}
		break;
	}
	case EC: {
		DEBUG_LN("ACCEPT EC");
		switch (calibrate_packet->flags	& SENSORLINK_CALIBRATION_FLAG_ACCEPT_CALIBRATION) {

			case SENSORLINK_CALIBRATION_FLAG_ACCEPT_0: {

				DEBUG("EC Sensor Type: ");
				DEBUG_LN(calibrate_packet->value1);
				switch (int(calibrate_packet->value1)) {
					case SENSORLINK_CALIBRATION_EC_K_0_1:
						Devices::atlas->setECType(K0_1);
						break;
					case SENSORLINK_CALIBRATION_EC_K_1_0:
						Devices::atlas->setECType(K1_0);
						break;
					case SENSORLINK_CALIBRATION_EC_K_10_0:
						Devices::atlas->setECType(K10_0);
						break;
					default:
						DEBUG_LN("Invalid EC Sensor type selected: ");
						break;
				}
			}
			break;
			case SENSORLINK_CALIBRATION_FLAG_ACCEPT_1: //Dry Phase
				DEBUG_LN("Accept Dry Phase");
				Devices::atlas->calibrateEC(Dry);
				break;
			case SENSORLINK_CALIBRATION_FLAG_ACCEPT_2: //High Phase
				DEBUG_LN("Accept High Phase");
				Devices::atlas->calibrateEC(High);
				break;
			case SENSORLINK_CALIBRATION_FLAG_ACCEPT_3: //Low Phase
				DEBUG_LN("Accept Low Phase");
				Devices::atlas->calibrateEC(Low);
				break;


		}
		break;
	}
	}

}

void Devices::calibrate(ZBRxResponse* calibrate_request, unsigned long timeout)
{
	DEBUG_LN("CALIBRATE");
	XBeeAddress64 addr64 = XBeeAddress64(Devices::sink_address->DH, Devices::sink_address->DL);
	CalibratePacket outGoing;
	outGoing.init();

	CalibratePacket* queued;
	ZBRxResponse* _queued;
	ZBTxRequest request(addr64, (byte*)&outGoing, sizeof(CalibratePacket));
	XBeeResponse* packet;

	unsigned long start = millis();
	CalibratePacket* calibrate_packet = (CalibratePacket*)calibrate_request->getData();
	_DEBUG("Flags: ");
	_DEBUG_LN(calibrate_packet->flags);
	_DEBUG_LN(millis());

	if((calibrate_packet->flags & SENSORLINK_CALIBRATION_FLAG_START_CALIBRATION) > 0)
	{
		_DEBUG("Calibrating sensor:");
		DEBUG_LN(calibrate_packet->sensor);

		//The EC sensor includes an accept parameter in the start calibration packet
		if((calibrate_packet->flags & SENSORLINK_CALIBRATION_FLAG_ACCEPT_CALIBRATION) > 0)
		{
			DEBUG_LN("Received start/accept calibration.");
			accept_sensor_calibration(calibrate_packet);
			start = millis();
			DEBUG_LN();
		}
		while (millis() - start < timeout) {
			outGoing.init();
			if (populate_sensor_calibration(calibrate_packet, &outGoing)) {
				Devices::xbee->send(request);
				XBeeUtil::wait_for_packet_type(Devices::xbee, 5000, ZB_TX_STATUS_RESPONSE, NULL, Devices::queue_packet);
			}else
			{
				DEBUG_LN("Something went wrong.");
//				Devices::atlas->align();
				delay(10000);
				//Error
//				XBeeUtil::wait_for_packet_type(Devices::xbee, 50, ZB_TX_STATUS_RESPONSE, NULL, Devices::queue_packet);
			}
			if (Devices::search_and_retrieve_from_queue(SENSORLINK_CALIBRATE_PACKET, packet)) {
				_queued = static_cast<ZBRxResponse*>(packet);
				queued = (CalibratePacket*)_queued->getData();
				DEBUG_LN();
				_DEBUG("Found Calibrate Packet: ");
				DEBUG_LN(queued->flags);
				if((queued->flags & SENSORLINK_CALIBRATION_FLAG_STOP_CALIBRATION) > 0){
					start = start - (timeout * 2);
					Devices::atlas->endContinuous();
					DEBUG_LN("Received quit calibration packet.");
				}else if((queued->flags & SENSORLINK_CALIBRATION_FLAG_ACCEPT_CALIBRATION) > 0)
				{
					DEBUG_LN("Received accept calibration.");
					accept_sensor_calibration(queued);
					start = millis();
				}
				free(_queued->getData());
				delete packet;
			}
		}
		DEBUG_LN("Finished Calibrating...");
		_DEBUG_LN(millis());
		Devices::atlas->endContinuous();
	}else
	{
		DEBUG_LN("Stop Calibration request received whilst not in calibration mode.");
	}
}

bool Devices::search_and_retrieve_from_queue(uint32_t packet_type, XBeeResponse* &packet)
{
	if(Devices::packet_queue_head == NULL)
	{
		return false;
	}
//	_DEBUG_LN("Searching for packet.");
	list_node_ptr<XBeeResponse>* prev = Devices::packet_queue_head;
	list_node_ptr<XBeeResponse>* current = Devices::packet_queue_head;
	while(current != NULL)
	{
		if(current->node->getApiId() == ZB_RX_RESPONSE)
		{
			ZBRxResponse* res = static_cast<ZBRxResponse*>(current->node);
			if(res->getData(0) == packet_type)
			{
				packet = current->node;
				prev->next = current->next;
				if(current == Devices::packet_queue_head)
				{
					Devices::packet_queue_head = current->next;
				}
				delete current;
				return true;

			}
		}
		prev = current;
		current = current->next;
	}

	return false;

}

void _queue_packet(XBeeResponse* packet)
{
	DEBUG("Queued packet with API id: 0x");
	DEBUG_LN_(packet->getApiId(), HEX);
	if(Devices::packet_queue_head == NULL)
	{
		Devices::packet_queue_head = new list_node_ptr<XBeeResponse>();
		Devices::packet_queue_head->node = packet;
	}else
	{
		list_node_ptr<XBeeResponse>* tmp = new list_node_ptr<XBeeResponse>();
		list_node_ptr<XBeeResponse>* current = NULL;
		tmp->node = packet;
		tmp->next = NULL;
		current = Devices::packet_queue_head;

		//Iterate to the end of the queue
		while(current->next != NULL)
		{
			current = current->next;
		}
		current->next = tmp;

	}
}

#define _QUEUE_PACKET(packet_type) {packet_type* packet = new packet_type();\
		Devices::xbee->getResponse(*packet);\
		packet->setFrameLength(Devices::xbee->getResponse().getFrameDataLength());\
		uint8_t* data = (uint8_t*)malloc(packet->getFrameDataLength());\
		memcpy(data, packet->getFrameData(), packet->getFrameDataLength());\
		packet->setFrameData(data);\
		_queue_packet(packet);}


void Devices::queue_packet()
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
//		return true;
		break;
	case REMOTE_AT_COMMAND_RESPONSE:
		_QUEUE_PACKET(RemoteAtCommandResponse);
//		return true;
		break;
	}

//	return false;

}



void Devices::send_status(StatusPacket* status_packet)
{
	XBeeAddress64 addr64 = XBeeAddress64(Devices::sink_address->DH, Devices::sink_address->DL);
	ZBTxRequest request(addr64, (byte*)status_packet, sizeof(StatusPacket));
	Devices::xbee->send(request);
	DEBUG_LN("Sent Status packet.");
}

void Devices::sample()
{
	DEBUG_LN("Sample: ");
	Sample sample;
	Devices::log_onewire(sample, Devices::bus);
	if(!isnan(sample.temperature)){

		_DEBUG("Temperature: ");
		DEBUG_LN(sample.temperature);

		FloatRecord rec;
		double orp = Devices::atlas->getORP();
		rec.time_stamp = RTC.get();
		if(!isnan(orp))
		{
			_DEBUG("ORP: ");
			DEBUG_LN(orp);

			rec.id = ORP;
			rec.value1 = orp;
			store->storeRecord((byte*)&rec, sizeof(FloatRecord));
		}

		double ph = Devices::atlas->getPH(sample.temperature);
		rec.time_stamp = RTC.get();
		if(!isnan(ph))
		{
			_DEBUG("PH: ");
			DEBUG_LN(ph);

			rec.id = PH;
			rec.value1 = ph;
			store->storeRecord((byte*)&rec, sizeof(FloatRecord));
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

		double _precent, _do;
		double result = Devices::atlas->getDO(sample.temperature, ec_rec.salinity, _precent, _do);
		rec.time_stamp = RTC.get();
		if(!isnan(result))
		{
			_DEBUG("DO: ");
			DEBUG(_do);
			DEBUG(", %: ");
			DEBUG_LN(_precent);

			rec.id = DO;
			rec.value1 = _precent;
			rec.value2 = _do;
			store->storeRecord((byte*)&rec, sizeof(FloatRecord));
		}
	}
	Devices::log_onewire(sample, Devices::bus_air);
}


void Devices::log_onewire(Sample &sample, OneWire* _bus)
{
//	tmElements_t time;

	_bus->reset_search();
	DallasTemperature _ds18b20(_bus);
	uint8_t address[8];
	boolean found = false;
	_ds18b20.begin();
	_ds18b20.requestTemperatures();
//	RTC.read(time);
	time_t time = RTC.get();
	while (_bus->search(address)) {
		if (OneWire::crc8(address, 7) == address[7]) {
			switch (address[0]) {
			case 0x28:
			case 0x10:
				found = true;
				Devices::log_ds18b20(address, time, sample, &_ds18b20);
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

void Devices::log_ds18b20(uint8_t* address, time_t time, Sample &sample, DallasTemperature* _ds18b20)
{
	OneWireRecord rec;
	for(int i = 0; i < 8; i++)
	{
		rec.id[i] = address[i];
	}
	if(_ds18b20->getResolution(address) != TEMP_12_BIT)
	{
		_ds18b20->setResolution(address, TEMP_12_BIT);
	}
	double temp = _ds18b20->getTempC(address);
	if(!isnan(temp) && temp < DS18B20_ERROR_TEMP)
	{
		rec.value = temp;
		rec.time_stamp = time;
		store->storeRecord((byte*)&rec, sizeof(OneWireRecord));
		if(isnan(sample.temperature))
		{
			sample.temperature = temp;
		}
		_DEBUG("\tTemperature ");
		displayOneWireAddress(address, 8, &DEBUG_STREAM);
		DEBUG(": ");
		DEBUG_LN(temp);
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

int Devices::initialize_cli(CLI* prompt)
{
	flash_copy_local(find_command_name, FIND_SINK_COMMAND_NAME);
	flash_copy_local(find_command_desc, FIND_SINK_DESCRIPTION);
	flash_copy_local(clear_command_name, CLEAR_SINK_COMMAND_NAME);
	flash_copy_local(clear_command_desc, CLEAR_SINK_DESCRIPTION);
	prompt->register_command(find_command_name, find_command_desc, &Devices::find_sink_callback, &Devices::find_sink_help_callback);
	prompt->register_command(clear_command_name, clear_command_desc, &Devices::clear_sink_callback, &Devices::clear_sink_help_callback);
	return 0;
}
int Devices::find_sink_callback(char** argv, int argc, Environment* env) {
	Devices::findSink();
	return 0;
}
int Devices::find_sink_help_callback(char** argv, int argc, Environment* env) {
	flash_println(env->input, FIND_SINK_DESCRIPTION);
	env->input->println();
	flash_println(env->input, CLI_RTC::RTC_SET_HELP_LINE_2);
	env->input->println();
	flash_println(env->input, FIND_SINK_COMMAND_NAME);
	return 0;
}
int Devices::clear_sink_callback(char** argv, int argc, Environment* env) {
	EEPROM.write(XBEE_SINK_ADDRESS_EEPROM_POSITION, 0);
	return 0;
}
int Devices::clear_sink_help_callback(char** argv, int argc, Environment* env) {
	return 0;
}

