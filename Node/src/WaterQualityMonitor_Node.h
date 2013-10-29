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

#ifndef WaterQualityMonitor_H_
#define WaterQualityMonitor_H_

#include "Arduino.h"

#include "Devices.h"
#include "Records.h"


#ifdef __cplusplus
extern "C" {
#endif
void loop();
void setup();
#ifdef __cplusplus
}
#endif


void downtime(const char* message);
void upload();
void sample();
bool do_upload();
bool isDataHandled(ZBTxStatusResponse &response);
time_t wakeup_at();
void INT0_ISR();





#endif /* WaterQualityMonitor_H_ */
