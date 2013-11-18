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

#include "cli_rtc.h"

DS3232RTC* CLI_RTC::RTC = NULL;


void CLI_RTC::displayDateTime(time_t _now, Stream* input)
{
	tmElements_t now;
	breakTime(_now, now);
	CLI_RTC::displayDateTime(now, input);

}
void CLI_RTC::displayDateTime(tmElements_t &now, Stream* input)
{

	input->print(tmYearToCalendar(now.Year));
	input->print(path_sep);

	if(now.Month < 10){input->print(0);}
	input->print(now.Month);
	input->print(path_sep);


	if(now.Day < 10){input->print(0);}
	input->print(now.Day);
	input->print(space);

	if(now.Hour < 10){input->print(0);}
	input->print(now.Hour);
	input->print(colen);

	if(now.Minute < 10){input->print(0);}
	input->print(now.Minute);
	input->print(colen);

	if(now.Second < 10){input->print(0);}
	input->print(now.Second);

	input->print(space);

//	input->print(CLI_RTC::getWeekDay(now.Wday));
	printWeekDay(input, now.Wday);

}

int rct_callback(char** argv, int argc, Environment* env)
{
	if(argc <= 1)
	{
		flash_print(env->input, CLI_RTC::RTC_EXPECTED_GET_OR_SET);
		return 2;
	}
	if(strcmp(argv[1], COMMAND_SET) == 0)
	{
		CLI_RTC::set_rtc(argv, argc, env);
	}else if(strcmp(argv[1], COMMAND_GET) == 0)
	{
		CLI_RTC::displayDateTime(CLI_RTC::RTC->get(), env->input);
		env->input->println();
		return 0;
	}else
	{
		flash_print(env->input, CLI_RTC::RTC_UNKNOWN_OPTION);
		env->input->println(argv[1]);
		return 1;
	}
	return 5;
}

int rtc_help_callback(char** argv, int argc, Environment* env)
{
	if (argc == 2)
	{
		flash_println(env->input, CLI_RTC::RTC_HELP_LINE_1);
		env->input->println();
		flash_println(env->input, CLI_RTC::RTC_HELP_LINE_2);
		flash_println(env->input, CLI_RTC::RTC_HELP_LINE_3);
		env->input->println();
		flash_println(env->input, CLI_RTC::RTC_HELP_LINE_4);
		return 0;
	}else if(argc == 3 && strcmp(argv[2], COMMAND_SET) == 0)
	{
			flash_println(env->input, CLI_RTC::RTC_SET_HELP_LINE_1);

			env->input->println();
			flash_println(env->input, USAGE);

			env->input->println();
			flash_println(env->input, CLI_RTC::RTC_SET_HELP_LINE_3);

			env->input->println();
			flash_println(env->input, CLI_RTC::RTC_SET_HELP_LINE_4);

			env->input->println();
			flash_print(env->input, CLI_RTC::RTC_SET_HELP_LINE_5);

			time_t _now = CLI_RTC::RTC->get();
			tmElements_t now;
			breakTime(_now, now);
			CLI_RTC::displayDateTime(now, env->input);
			env->input->println();
			env->input->println();
			flash_println(env->input, CLI_RTC::RTC_SET_HELP_LINE_6);

			env->input->println();
			flash_print(env->input, CLI_RTC::RTC_SET_HELP_LINE_7);

			env->input->print(tmYearToCalendar(now.Year));
			env->input->print(space);

			env->input->print(now.Month);
			env->input->print(space);


			env->input->print(now.Day);
			env->input->print(space);

			env->input->print(now.Hour);
			env->input->print(space);

			env->input->print(now.Minute);
			env->input->print(space);

			env->input->print(now.Second);
			env->input->print(space);

			env->input->println(now.Wday);
			env->input->println();
			flash_println(env->input, CLI_RTC::RTC_SET_HELP_LINE_9);




			env->input->println();
			for(int i = 1; i < 8; i++)
			{
				env->input->print(tab);
				env->input->print(tab);
				env->input->print(i, DEC);
				env->input->print(": ");
				CLI_RTC::printWeekDay(env->input, i);
				env->input->println();
			}
	}else if(argc == 3 && strcmp(argv[2], COMMAND_GET) == 0)
	{
		env->input->println();
		flash_println(env->input, USAGE);
		env->input->println();
		flash_println(env->input, CLI_RTC::RTC_SET_HELP_LINE_10);
	}
	return 0;
}

void CLI_RTC::printWeekDay(Stream* output, int weekday)
{
	switch(weekday)
	{
	case 1:
		flash_print(output, RTC_WEEK_DAY_SUN);
		break;
	case 2:
		flash_print(output, RTC_WEEK_DAY_MON);
		break;
	case 3:
		flash_print(output, RTC_WEEK_DAY_TUE);
		break;
	case 4:
		flash_print(output, RTC_WEEK_DAY_WED);
		break;
	case 5:
		flash_print(output, RTC_WEEK_DAY_THU);
		break;
	case 6:
		flash_print(output, RTC_WEEK_DAY_FRI);
		break;
	case 7:
		flash_print(output, RTC_WEEK_DAY_SAT);
		break;
	default:
		flash_print(output, RTC_WEEK_DAY_INV);
		break;
	}
}

int CLI_RTC::initialize(CLI* prompt, DS3232RTC* rtc)
{
	CLI_RTC::RTC = rtc;
	flash_copy_local(rtc_command_name, RTC_COMMAND_NAME);
	flash_copy_local(rtc_command_desc, RTC_COMMAND_DESCRIPTION);
	prompt->register_command(rtc_command_name, rtc_command_desc, &rct_callback, &rtc_help_callback);
	return 0;
}

int CLI_RTC::set_rtc(char** argv, int argc, Environment* env)
{
	if(argc != 9)
	{
		flash_print(env->input, CLI_RTC::RTC_SET_ERROR_LINE_1);
		env->input->print(9);

		flash_print(env->input, CLI_RTC::RTC_SET_ERROR_LINE_2);
		env->input->print(argc);
		return 1;
	}
	tmElements_t now;
	int count = 2;
	now.Year = CalendarYrToTm(atoi(argv[count++]));
	now.Month = atoi(argv[count++]);
	now.Day = atoi(argv[count++]);
	now.Hour = atoi(argv[count++]);
	now.Minute = atoi(argv[count++]);
	now.Second = atoi(argv[count++]);
	now.Wday = atoi(argv[count++]);
	CLI_RTC::RTC->set(makeTime(now));
	env->input->print("Time set to: ");
	CLI_RTC::displayDateTime(CLI_RTC::RTC->get(), env->input);
	env->input->println();
	return makeTime(now);
}



