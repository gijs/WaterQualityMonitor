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

#ifndef CONSTANTS_H_
#define CONSTANTS_H_

static char colon = ':';
static char space = ' ';
static char slash = '/';
static char tab = '\t';
static char carrage_return = '\r';

#define DEBUG_STREAM Serial1
#define START_DEBUG_STREAM(baud_rate) DEBUG_STREAM.begin(baud_rate);
#define DEBUG_LN(val) DEBUG_STREAM.println(val)
#define DEBUG_LN_(val, fmt) DEBUG_STREAM.println(val, fmt)
#define DEBUG(val) DEBUG_STREAM.print(val)
#define DEBUG_(val, fmt) DEBUG_STREAM.print(val, fmt)

#define _DEBUG(val) DEBUG_STREAM.print(tab);DEBUG_STREAM.print(val)
#define _DEBUG_LN(val) DEBUG_STREAM.print(tab);DEBUG_STREAM.println(val)

#define FREE_MEM DEBUG("Free Ram: ");DEBUG_LN(FreeRam())


#endif /* CONSTANTS_H_ */
