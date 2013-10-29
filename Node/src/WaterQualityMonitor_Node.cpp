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

#include "WaterQualityMonitor_Node.h"

int sample_interval = 60;
int sample_count = 11;
int sample_upload_at = 12;

int max_transmitt_count = 5;

int32_t max_record_size = max_record_size(sizeof(DoubleRecord), sizeof(OneWireRecord), sizeof(SalinityRecord));

volatile bool is_downtime = true;

void setup()
{
	Serial.begin(57600);
	START_DEBUG_STREAM(115200);
	Serial2.begin(38400);
	DEBUG_LN("Starting...");
	uint32_t device_status = Devices::initilize_devices(
			4, 				//SD CS pin
			32, 			//OneWire Bus
			Serial, 		//XBee Serial Port
			A0,
//			sensorMap,  	//Atlas Sensor Map
			33, 			//Atlas E Pin
			35, 			//Atlas SO Pin
			37, 			//Atlas SI Pin
			Serial2,		//Atlas Serial Port
			(char*)"WQM.DAT",		//RecordStorage filename
			max_record_size //Maximum Record Size

	);
	if(device_status > 0)
	{
		DEBUG("Error initializing device: ");
		DEBUG_LN(device_status);
		while(true){};
	}

	downtime("Aligning to boundary");

}

void loop()
{
	FREE_MEM;
	Devices::sample();
	sample_count++;
	if (do_upload()) {
		upload();
	}
	downtime("Sleeping...");
}

void downtime(const char* message)
{
	attachInterrupt(0, INT0_ISR, FALLING);
	DEBUG(message);
	DEBUG(" at: ");

	tmElements_t _t;
	breakTime(wakeup_at(), _t);
	RTC.writeAlarm(1, alarmModeDateMatch, _t);

	Devices::displayDate(&_t, &DEBUG_STREAM);

	is_downtime = true;
	while(is_downtime){	}
	detachInterrupt(0);
	RTC.clearAlarmFlag(3);
}

bool do_upload()
{
	return sample_count >= sample_upload_at;
}

time_t wakeup_at()
{
	time_t time = RTC.get();
	uint32_t intervals_so_far_today = elapsedSecsToday(time) / sample_interval;
	time_t next_interval_at = ((intervals_so_far_today + 1) * sample_interval) + previousMidnight(time);
	return next_interval_at;
}

void upload()
{
	DEBUG_LN("Upload: ");
	Devices::associate();
//	XBeeAddress64 addr64 = XBeeAddress64(0x0, BROADCAST_ADDRESS);
	//XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x40a53d1e);
	XBeeAddress64 addr64 = XBeeAddress64(Devices::sink_address->DH, Devices::sink_address->DL);
	if(!Devices::associate())
	{
		DEBUG_LN("Failed to Associate");
		delay(1000);
		return;
	}
	int32_t records_to_upload = Devices::store->getRecordCount() - Devices::store->getUploadedCount();
	int32_t header_size = sizeof(DataPacket);
	int32_t record_size = Devices::store->getRecordSize();
	int32_t records_per_packet = (MAX_PACKET_SIZE - header_size) / record_size;
	int32_t data_size = (records_per_packet * Devices::store->getRecordSize());
	int32_t packet_size =  data_size + header_size;

	_DEBUG("Header Size: ");
	DEBUG_LN(header_size);

	_DEBUG("Data Size: ");
	DEBUG_LN(data_size);

	_DEBUG("Packet Size: ");
	DEBUG_LN(packet_size);

	_DEBUG("Records Per Packet: ");
	DEBUG_LN(records_per_packet);

	_DEBUG("Records Size: ");
	DEBUG_LN(Devices::store->getRecordSize());


	byte packet[packet_size];
	ZBTxRequest request(addr64, (byte*)&packet, (uint8_t)packet_size);
	ZBTxStatusResponse response;

	DataPacket* header = (DataPacket*)&packet;
	header->init();

	_DEBUG("Packet Address: ");
	DEBUG_LN((int)(&packet));

	byte* data = ((byte*)(&packet)) + sizeof(DataPacket);

	_DEBUG("Data Address: ");
	DEBUG_LN((int)data);

	header->flags = 0;

	header->row_size = record_size;
	header->senquence = 0;

	_DEBUG("");FREE_MEM;DEBUG_LN();

	_DEBUG("Records to upload: ");
	DEBUG_LN(records_to_upload);

	int fail_count = 0;

	while(records_to_upload > 0)
	{
		int32_t pos = Devices::store->getUploadedCount();

		_DEBUG("Current Record Position: ");
		DEBUG_LN(pos);

		int32_t to_upload = records_to_upload > records_per_packet ? records_per_packet : records_to_upload;
		header->row_count = to_upload;
		_DEBUG("Records to upload in this packet: ");
		DEBUG_LN(to_upload);

		for(int32_t i = 0; i < to_upload; i++, pos++)
		{
//			_DEBUG("Packet Offset: ");
//			DEBUG_LN((int)(data + (i * max_record_size)));
			Devices::store->retrieveRecord(data + (i * max_record_size), pos);
		}

		Devices::store->setUploadedCount(pos);
		request.setPayloadLength(header_size + (to_upload * record_size));

		Devices::xbee->send(request);
		if(Devices::xbee->readPacket(5000) && Devices::xbee->getResponse().getApiId() == ZB_TX_STATUS_RESPONSE)
		{

			Devices::xbee->getResponse(response);
			DEBUG("Delivery status: ");
			DEBUG_LN(response.getDeliveryStatus());
			if(response.isSuccess())
			{
				DEBUG_LN();
				_DEBUG_LN("Successfully Transmitted packet.");
				Devices::store->writeHeader();

				header->senquence += 1;
				records_to_upload = Devices::store->getRecordCount() - Devices::store->getUploadedCount();
				fail_count = 0;
				delay(500);
			}else
			{
				DEBUG_LN();
				_DEBUG_LN("Failed to transmit packet.");
				Devices::store->readHeader();
				fail_count++;
			}
		}
		else
		{
			DEBUG_LN();
			_DEBUG_LN("Failed to transmit packet.");
			fail_count++;
		}

		if(fail_count >= max_transmitt_count)
		{
			DEBUG_LN();
			_DEBUG_LN("MAX retry limit exceeded.");
			break;
		}
	}

}


void INT0_ISR()
{
	DEBUG_LN("Woke UP");
	is_downtime = false;
}



