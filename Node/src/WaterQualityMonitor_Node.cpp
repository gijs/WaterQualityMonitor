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

//int sample_interval = 900;
//int sample_count = -1;
//int sample_upload_at = -1;
//
//int32_t upload_interval = 86400; // 24 * 60 * 60
//int32_t upload_offset   = 43200; // 12 * 60 * 60

int sample_interval = 60;
int sample_count = 0;
int sample_upload_at = 1;

int32_t upload_interval = 86400; // 24 * 60 * 60
int32_t upload_offset   = 43200; // 12 * 60 * 60



int max_transmit_count = 5;

int32_t max_record_size = max_record_size(sizeof(FloatRecord), sizeof(OneWireRecord), sizeof(SalinityRecord));

volatile bool is_downtime = true;

void dumpHEX(double value)
{
	DEBUG(value);
	DEBUG(": ");
	byte* d = (byte*)&value;
	for(int i = 0; i < sizeof(double); i++)
	{
		DEBUG_(d[i], HEX);
		DEBUG(" ");
	}
	DEBUG_LN();
}

void setup()
{
	Serial.begin(57600);
	START_DEBUG_STREAM(115200);
	Serial2.begin(38400);
	DEBUG_LN("Starting...");
	DEBUG("Max Record Size: ");
	DEBUG_LN(max_record_size);
	FREE_MEM;
	DEBUG("Serial Buffer Size: ");
	DEBUG_LN(SERIAL_BUFFER_SIZE); // If you define SERIAL_BUFFER_SIZE to be the size of the
								  // maximum radio packet size then a whole packet can sit
								  // in the buffer. Otherwise it will be  acknowledged by
								  // the radio but can be invalid by the time the arduino
								  // parses it, as the packet can be overwritten in the
								  // ring buffer.
	delay(2000);

	uint32_t device_status = Devices::initilize_devices(
			4, 				//SD CS pin
			32, 			//In Water OneWire Bus
			34,				//In Air one Wire Bus
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
	StatusPacket status;
	status.init();
	if(device_status > 0)
	{
		DEBUG("Error initializing device: ");
		DEBUG_LN(device_status);
		while(true){};
	}
	set_sample_count();
	status.flags = SENSORLINK_STATUS_FLAG_OK;
	status.codes = 0;
	Devices::send_status(&status);
	if(analogRead(A1) <= XBEE_ASSOCIATE_THRESHOLD)
	{
		downtime("Aligning to boundary");
	}
}

void loop()
{
	DEBUG("Upload at: ");
	DEBUG(sample_upload_at);
	DEBUG(" current counter: ");
	DEBUG_LN(sample_count);

	FREE_MEM;
	Devices::sample();
	sample_count++;
	if (do_upload()) {
		upload();
		sample_count = 0;
	}
	handle_queue();
	downtime("Sleeping...");
}

void downtime(const char* message)
{
	unsigned long start = millis();
	attachInterrupt(0, INT0_ISR, FALLING);
	DEBUG(message);
	DEBUG(" at: ");

	tmElements_t _t;
	breakTime(wakeup_at(), _t);
	RTC.writeAlarm(1, alarmModeDateMatch, _t);

	Devices::displayDate(&_t, &DEBUG_STREAM);

	is_downtime = true;
	while(is_downtime){
		XBeeUtil::wait_for_packet_type(Devices::xbee, 50, 0xFFFFF, NULL, Devices::queue_packet);
		handle_queue();
		if((millis() - start) >  (sample_interval * 2000))
		{
			is_downtime = false;
			DEBUG_LN("Two periods spent in downtime, waking up anyway.");
		}
	}
	detachInterrupt(0);
	RTC.clearAlarmFlag(3);
}

void handle_queue()
{
	list_node<XBeeResponse>* tmp;
	while(Devices::packet_queue_head != NULL)
	{
		tmp = Devices::packet_queue_head;
		Devices::packet_queue_head = Devices::packet_queue_head->next;
		handle_queued_packet(tmp->node);
		free(tmp->node->getFrameData());
		delete tmp->node;
		delete tmp;
	}
//	Devices::packet_queue_tail = NULL;
}

void handle_queued_packet(XBeeResponse* packet)
{
	switch(packet->getApiId())
	{
	case ZB_RX_RESPONSE:
		ZBRxResponse* request = static_cast<ZBRxResponse*>(packet);
		DEBUG("Packet ID: ");
		DEBUG_LN(request->getData(0));
		switch(request->getData(0))
		{
		case SENSORLINK_CALIBRATE_PACKET:
			Devices::calibrate(request, 600000);
			break;
		}

		break;
	}
}

bool do_upload()
{
	return sample_count >= sample_upload_at;
}

void set_sample_count()
{
	if(upload_interval % sample_interval != 0)
	{
		DEBUG_LN("Warning, sample_interval is not a factor of upload_interval.");
		delay(10000);
	}
	if(sample_count == -1 && sample_upload_at == -1)
	{
		time_t time = RTC.get();
		sample_upload_at = upload_interval / sample_interval;
		sample_count = (((time % upload_interval) + upload_offset) / sample_interval) % sample_upload_at;
	}

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

		if(XBeeUtil::wait_for_packet_type(Devices::xbee, 5000, ZB_TX_STATUS_RESPONSE, NULL, Devices::queue_packet))
//		if(Devices::xbee->readPacket(5000) && Devices::xbee->getResponse().getApiId() == ZB_TX_STATUS_RESPONSE)
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
				//Add a match packet above and have the basestation send an acc packet and we can avoid this delay
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
			Devices::store->readHeader();
			DEBUG_LN();
			_DEBUG_LN("Failed to transmit packet.");
			fail_count++;
		}

		if(fail_count >= max_transmit_count)
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



