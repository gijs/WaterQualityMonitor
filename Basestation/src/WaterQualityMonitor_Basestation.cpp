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

#include "WaterQualityMonitor_Basestation.h"

int32_t max_record_size = max_record_size(sizeof(FloatRecord), sizeof(OneWireRecord), sizeof(SalinityRecord));

void setup()
{
	Serial.begin(57600);
	SoftwareSerial* debug = new SoftwareSerial(8,9);
	debug->begin(9600);
	debug->println("Starting...");
	Devices::flash_println(debug, WQM_Strings::STARTING);
	uint64_t result = Devices::initilize_devices(10, "data1.wqm", max_record_size, &Serial, debug);
	if(result > 0)
	{
		___FDEBUG(WQM_Strings::DEVICE_INITIALIZATION_ERROR);
		DEBUG_LN((long)result);
		digitalWrite(RGB_RED_PIN, HIGH);delay(300);
		digitalWrite(RGB_RED_PIN, LOW);delay(300);
		digitalWrite(RGB_RED_PIN, HIGH);delay(300);
		digitalWrite(RGB_RED_PIN, LOW);delay(300);
		digitalWrite(RGB_RED_PIN, HIGH);delay(300);
		digitalWrite(RGB_RED_PIN, LOW);
		while(true){}

	}

	___FDEBUG(WQM_Strings::RADIO_ADDRESS);
	___FDEBUG(WQM_Strings::SH);
	___FDEBUG(WQM_Strings::SPACE);___FDEBUG(WQM_Strings::ZEROX_HEX_PREFIX);
	DEBUG_(Devices::SH, HEX);___FDEBUG(WQM_Strings::SPACE)

	___FDEBUG(WQM_Strings::SL);
	___FDEBUG(WQM_Strings::SPACE);___FDEBUG(WQM_Strings::ZEROX_HEX_PREFIX);
	DEBUG_LN_(Devices::SL, HEX);

	___FDEBUG(WQM_Strings::CURRENT_TIME);
	Devices::displayDate(RTC.get(), DEBUG_STREAM);



	pinMode(RADIO_POWER_PIN, OUTPUT);
    PORTD |= 0x04;
    DDRD &=~ 0x04;

	digitalWrite(RGB_GREEN_PIN, HIGH);delay(300);
	digitalWrite(RGB_GREEN_PIN, LOW);delay(300);
	digitalWrite(RGB_GREEN_PIN, HIGH);delay(300);
	digitalWrite(RGB_GREEN_PIN, LOW);delay(300);
	digitalWrite(RGB_GREEN_PIN, HIGH);delay(300);
	digitalWrite(RGB_GREEN_PIN, LOW);delay(300);
}

void loop()
{
	detachInterrupt(0);
	digitalWrite(RGB_BLUE_PIN, HIGH); //LED Warning Light
	handle();
	digitalWrite(RGB_BLUE_PIN, LOW);


	//////////////////////////////////////////////////////////////////////////////
	___FDEBUG(WQM_Strings::CURRENT_TIME);
	Devices::displayDate(RTC.get(), DEBUG_STREAM);
	tmElements_t alarm;
	time_t _wakeup_at = wakeup_at();
	___FDEBUG(WQM_Strings::WAKE_UP_AT);
	Devices::displayDate(_wakeup_at, DEBUG_STREAM);
	breakTime(_wakeup_at, alarm);

	RTC.clearAlarmFlag(3);
	attachInterrupt(0, INT0_ISR, FALLING);

    RTC.writeAlarm(1, alarmModeDateMatch, alarm);

	//Power Down routines
    cli();
    sleep_enable();      // Set sleep enable bit
    sleep_bod_disable(); // Disable brown out detection during sleep. Saves more power
    sei();

    ___FDEBUG_LN(WQM_Strings::SLEEPING);
    delay(30); //This delay is required to allow print to complete
    //Shut down all peripherals like ADC before sleep. Refer Atmega328 manual
    power_all_disable(); //This shuts down ADC, TWI, SPI, Timers and USART
    sleep_cpu();         // Sleep the CPU as per the mode set earlier(power down)
    sleep_disable();     // Wakes up sleep and clears enable bit. Before this ISR would have executed
    power_all_enable();  //This shuts enables ADC, TWI, SPI, Timers and USART
    delay(10); //This delay is required to allow CPU to stabilize
    ___FDEBUG_LN(WQM_Strings::WOKEN);
    Devices::displayDate(RTC.get(), DEBUG_STREAM);


}

time_t wakeup_at()
{
	return nextMidnight(RTC.get()) + (SECS_PER_HOUR * 12UL) - (RADIO_RECEIVE_SECONDS / 2 );
}

void handle()
{
	digitalWrite(RADIO_POWER_PIN, HIGH);
	XBeeUtil::associate(Devices::xbee);
	time_t start = RTC.get();
	while(RTC.get() - start < RADIO_RECEIVE_SECONDS){
	if(Devices::xbee->readPacket(5000))
	{
		DEBUG_LN();
		___FDEBUG(WQM_Strings::RECIEVED_PACKET);
		DEBUG_LN(Devices::xbee->getResponse().getApiId());
		switch(Devices::xbee->getResponse().getApiId())
		{
		case 0x90:
		{
			ZBRxResponse request;
			Devices::xbee->getResponse().getRx64Response(request);
			handle_rx_request(request);
			start = RTC.get();
		}
			break;
		default:
			___FDEBUG_LN(WQM_Strings::UNKNOWN_API_TYPE);
			break;
		}
	}else
	{
		if(Devices::xbee->getResponse().isError())
		{
			___FDEBUG(WQM_Strings::RECIEVED_PACKET_ERROR_CODE);
			DEBUG_LN_(Devices::xbee->getResponse().getErrorCode(), HEX);
		}else
		{

		}
		___FDEBUG(WQM_Strings::DOT);
	}
	}
	digitalWrite(RADIO_POWER_PIN, LOW);
}


void handle_rx_request(ZBRxResponse &request)
{
	___FDEBUG(WQM_Strings::RECIEVED_PACKET);
	DEBUG(request.getData(0));
	___FDEBUG(WQM_Strings::RECEIVED_PACKET_FROM);
	DEBUG_(request.getRemoteAddress64().getMsb(), HEX);
	___FDEBUG(WQM_Strings::SPACE);
	___FDEBUG(WQM_Strings::ZEROX_HEX_PREFIX);
	DEBUG_LN_(request.getRemoteAddress64().getLsb(), HEX);

	switch(request.getData(0))
	{
	case SENSORLINK_SINK_PACKET:
		handle_sink_request(request);
		break;
	case SENSORLINK_DATA_PACKET:
		handle_data_request(request);
		break;
	default:
		___FDEBUG_LN(WQM_Strings::UNKNOWN_PACKET_TYPE);
		//Unknown packet type: "
		DEBUG_LN(request.getData(0));
		break;
	}
	FREE_MEM;
}

void handle_sink_request(ZBRxResponse &request)
{
	SinkPacket* snk;
	snk = ((SinkPacket*)request.getData());


	___FDEBUG(WQM_Strings::DH);
	DEBUG_LN_(snk->DH, HEX);
	___FDEBUG(WQM_Strings::DL);
	DEBUG_LN_(snk->DL, HEX);

	SinkPacket response;
	response.init();
	response.DH = Devices::SH;
	response.DL = Devices::SL;

	XBeeAddress64 addr64 = XBeeAddress64(request.getRemoteAddress64().getMsb(), request.getRemoteAddress64().getLsb());
	ZBTxRequest zxResponse(addr64, (byte*)&response, sizeof(SinkPacket));
	delay(500);
	Devices::xbee->send(zxResponse);
	long failAt = millis() + 5000;
	bool received = false;
	while(millis() < failAt && !received)
	{
		Devices::xbee->readPacket(100);
		if(Devices::xbee->getResponse().isAvailable())
		{
//			DEBUG("API Type: ");
			___FDEBUG(WQM_Strings::API_TYPE);
			DEBUG_LN(Devices::xbee->getResponse().getApiId());
			if(Devices::xbee->getResponse().getApiId() == ZB_TX_STATUS_RESPONSE)
			{
//				DEBUG("SENT");
				___FDEBUG_LN(WQM_Strings::SENT);
			}
		}
	}

}
void handle_data_request(ZBRxResponse &request)
{
	DataPacket* data;
	if(request.getDataLength() >= sizeof(DataPacket))
	{
		data = (DataPacket*)request.getData();
		byte* _data = request.getData() +  sizeof(DataPacket);
		___FDEBUG(WQM_Strings::ROWS_RECEIVED);
		DEBUG_LN(data->row_count);
		bool ok = true;
		for(int i = 0; i < data->row_count; i++)
		{
			ok = ok & Devices::store->storeRecord((_data + (i * data->row_size)), data->row_size);
		}
		if(ok)
		{
			___FDEBUG(WQM_Strings::STORED_RECORDS);
			DEBUG_LN(Devices::store->getRecordCount());
		}


	}else
	{
//		DEBUG_LN("Incorrect data size for DataPacket");
		___FDEBUG_LN(WQM_Strings::INCORRECT_DATA_SZIE);
	}
}

void INT0_ISR()
{
	DEBUG_STREAM->println("INT0");


}
