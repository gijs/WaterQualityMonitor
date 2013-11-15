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

#ifndef CLI_UTIL_H_
#define CLI_UTIL_H_

#include <avr/pgmspace.h>
#include "Stream.h"
#include "Constants.h"


static const char* COMMAND_SET = "set";
static const char* COMMAND_GET = "get";

void flash_print(Stream* stream, const prog_char *ptr);

void flash_println(Stream* stream, const prog_char *ptr);


#define flash_copy_local(local_name, flash_name)\
		char local_name[strlen_P((char*)flash_name) + 1];\
		{\
			int32_t len = strlen_P((char*)flash_name);\
			/*strcpy_P(local_name, (char*)flash_name);*/\
			for(int32_t i = 0; i < len; i++){local_name[i] = (char)pgm_read_byte_near(flash_name + i);}\
			local_name[len] = 0;\
		}\

#endif /* CLI_UTIL_H_ */
