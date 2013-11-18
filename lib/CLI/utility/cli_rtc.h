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

#ifndef CLI_RTC_H_
#define CLI_RTC_H_

#include <Wire.h>
#include "DS3232RTC.h"
#include "CLI.h"
#include "cli_util.h"

namespace CLI_RTC
{
	extern DS3232RTC* RTC;
	int initialize(CLI* prompt, DS3232RTC* rtc);
	int set_rtc(char** argv, int argc, Environment* env);
	void get_rtc();
	void displayDateTime(time_t now, Stream* input);
	void displayDateTime(tmElements_t &now, Stream* input);

	void printWeekDay(Stream* output, int weekday);


	const prog_char RTC_COMMAND_NAME[] PROGMEM  = {"rtc"};
	const prog_char RTC_COMMAND_DESCRIPTION[] PROGMEM  = {"Get or Set the DS3231 Real Time Clock."};

	const prog_char RTC_EXPECTED_GET_OR_SET[] PROGMEM  = {"Error: expected get or set"};
	const prog_char RTC_UNKNOWN_OPTION[] PROGMEM  = {"Unknown option: "};

	const prog_char RTC_SET_HELP_LINE_1[] PROGMEM  = {"Welcome to the RTC Time Setting Utility."};
//	const prog_char RTC_SET_HELP_LINE_2[] PROGMEM  = {"Usage:"};
	const prog_char RTC_SET_HELP_LINE_3[] PROGMEM  = {"\trtc set <YEAR> <MONTH> <DAY> <HOUR24> <MINUTE> <SECOND> <DAY_OF_WEEK>"};
	const prog_char RTC_SET_HELP_LINE_4[] PROGMEM  = {"\tExample:"};
	const prog_char RTC_SET_HELP_LINE_5[] PROGMEM  = {"\t\tCurrent RTC Time: "};
	const prog_char RTC_SET_HELP_LINE_6[] PROGMEM  = {"\tExample Set Command:"};
	const prog_char RTC_SET_HELP_LINE_7[] PROGMEM  = {"\t\trtc set "};
	const prog_char RTC_SET_HELP_LINE_9[] PROGMEM  = {"\tDays of Week:"};

	const prog_char RTC_SET_HELP_LINE_10[] PROGMEM  = {"\trtc get"};


	const prog_char RTC_SET_ERROR_LINE_1[] PROGMEM  = {"Expected "};
	const prog_char RTC_SET_ERROR_LINE_2[] PROGMEM  = {" parameters, got "};

	const prog_char RTC_SET_MESSAGE[] PROGMEM  = {"Setting date to: "};

	const prog_char RTC_WEEK_DAY_SUN[] PROGMEM  = {"Sun"};
	const prog_char RTC_WEEK_DAY_MON[] PROGMEM  = {"Mon"};
	const prog_char RTC_WEEK_DAY_TUE[] PROGMEM  = {"Tue"};
	const prog_char RTC_WEEK_DAY_WED[] PROGMEM  = {"Wed"};
	const prog_char RTC_WEEK_DAY_THU[] PROGMEM  = {"Thu"};
	const prog_char RTC_WEEK_DAY_FRI[] PROGMEM  = {"Fri"};
	const prog_char RTC_WEEK_DAY_SAT[] PROGMEM  = {"Sat"};
	const prog_char RTC_WEEK_DAY_INV[] PROGMEM  = {"INV"};


	const prog_char RTC_HELP_LINE_1[] PROGMEM  = {"rtc command supports the following sub-commands:"};
	const prog_char RTC_HELP_LINE_2[] PROGMEM  = {"\tset - Set the time on the RTC"};
	const prog_char RTC_HELP_LINE_3[] PROGMEM  = {"\tget - Retrieve the time from the RTC"};
	const prog_char RTC_HELP_LINE_4[] PROGMEM  = {"See \"help rtc set\" for more information."};

}


#endif /* CLI_RTC_H_ */
