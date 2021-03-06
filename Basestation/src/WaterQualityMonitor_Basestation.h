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

#ifndef WaterQualityMonitorBasestation_H_
#define WaterQualityMonitorBasestation_H_

#include "Arduino.h"
#include "Devices.h"

#include <avr/sleep.h>
#include <avr/power.h>
#include <avr/power.h>

#define RADIO_RECEIVE_SECONDS 120




#ifdef __cplusplus
extern "C" {
#endif
void loop();
void setup();
#ifdef __cplusplus
} // extern "C"
#endif

time_t wakeup_at();
void handle();
void handle_rx_request(ZBRxResponse &request);
void handle_sink_request(ZBRxResponse &request);
void handle_data_request(ZBRxResponse &request);
void INT0_ISR();

#endif /* WaterQualityMonitorBasestation_H_ */
