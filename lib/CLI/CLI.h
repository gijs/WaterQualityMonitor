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

#ifndef __CLI_H__
#define __CLI_H__

#include <Arduino.h>
#include "cli_util.h"


const prog_char CLI_PRESS_ANY_KEY[] PROGMEM  = {"Press any key to enter CLI. "};
const prog_char CLI_CONTINUING[] PROGMEM  = {"Continuing..."};
const prog_char CLI_ERROR_FINDING_COMMAND[] PROGMEM  = {"Error finding command: "};
const prog_char CLI_AVAILABLE_COMMANDS[] PROGMEM  = {"The following commands are available: "};
const prog_char CLI_HELP_NOT_FOUND[] PROGMEM  = {"Help not found for: "};

const prog_char CLI_NO_SETUP[] PROGMEM  = {"No setup method provided..."};

const prog_char CLI_DELETE_CHAR[] PROGMEM  = {"\b \b"};

const prog_char CLI_EXIT_COMMAND[] PROGMEM  = {"exit"};
const prog_char CLI_HELP_COMMAND[] PROGMEM  = {"help"};


const prog_char CLI_ENV_COMMAND[] PROGMEM  = {"env"};
const prog_char CLI_ENV_COMMAND_DESC[] PROGMEM  = {"Lists the environment variables."};

const prog_char CLI_STRING[] PROGMEM  = {"CLI"};
const prog_char TIMEOUT[] PROGMEM  = {"Timeout"};
const prog_char SECONDS[] PROGMEM  = {"seconds"};

const prog_char USAGE[] PROGMEM  = {"Usage:"};

static const char* exit_code = {"EXIT"};

template<typename T> struct list_node
{
	T node;
	list_node<T>* next;
	list_node():
		next(NULL){};
};

template<typename T, typename V> struct Pair
{
	T first;
	V second;
};

class Environment
{
private:
	list_node<Pair<char*,char*> >* env_list;
	void dealloc(list_node<Pair<char*,char*> >* current);
public:
	Environment();
	~Environment();
	char* get_env(char* key);
	void set_env(char* key, String* value);
	void set_env(char* key, char* value);
	void set_env(char* key, int value);
	void list_values();
	Stream *input;
};

struct Command
{
	const char* command;
	const char* brief_description;
	int (*command_callback)(char** argv, int argc, Environment* env);
	int (*help_callback)(char** argv, int argc, Environment* env);

	Command():
		command(NULL),
		brief_description(NULL),
		command_callback(NULL),
		help_callback(NULL){};

};

class CLI
{
private:

	bool process_input(String *value);
	int return_code;
	const char* prompt;
	Stream *input;
	list_node<Command>* commands;
	Environment *env;
    int noticePin;
    bool registered;
    unsigned long timeout;
    unsigned long lastTime;

    void dealloc(list_node<Command>* current);
    void register_cli_commands();

public:
	CLI(const char* prompt, Stream *input,  int noticePin, int timeout);
	~CLI();
	void wait_for_input(int seconds, void (*setup)(CLI* prompt));
	void run_cli();
	void register_command(const char* command, const char* brief_description, int (*command_callback)(char** argv, int argc, Environment* env), int (*help_callback)(char** argv, int argc, Environment* env));
	int help(char** argv, int argc);
	Environment* getEnvironment();
};

#endif


