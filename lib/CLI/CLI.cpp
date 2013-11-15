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

#include "CLI.h"


Environment::Environment()
{
	env_list = NULL;
}

Environment::~Environment()
{
	if(env_list != NULL)
	{
		dealloc(env_list);
	}
}

void Environment::dealloc(list_node<Pair<char*,char*> >* current)
{
	if(current->next != NULL)
	{
		dealloc(current->next);
	}
	delete current->node.first;
	delete current->node.second;
	delete current;
}

void Environment::set_env(char* key, char* value)
{
	if(env_list == NULL)
	{
		env_list = new list_node<Pair<char*,char*> >();
		env_list->node.first = strdup(key);
		env_list->node.second = strdup(value);
	}else
	{
		list_node<Pair<char*,char*> > *previous, *current = env_list;
		do{
			if(strcmp(current->node.first, key) == 0)
			{
				delete current->node.second;
				current->node.second = strdup(value);
				return;
			}

			previous = current;
			current = current->next;

		}while(current != NULL);
		current = previous;
		current->next = new list_node<Pair<char*,char*> >();
		current = current->next;

		current->node.first = strdup(key);
		current->node.second = strdup(value);
	}
}

void Environment::set_env(char* key, int value)
{
	String f(value);
	int length = f.length();
	char data[length + 1];
	for(int i = 0; i < length; i++)
	{
		data[i] = f[i];
	}
	data[length] = 0;
	set_env(key, data);
}
void Environment::set_env(char* key, String* value)
{
	int32_t len = value->length();
	char new_value[len + 1];
	for(int32_t i = 0; i < len; i++)
	{
		new_value[i] = (*value)[i];
	}
	new_value[len] = 0;
	set_env(key, new_value);
}

char* Environment::get_env(char* key)
{
	if(env_list == NULL)
	{
		return NULL;
	}
	list_node<Pair<char*,char*> >* current = env_list;
	while(current != NULL)
	{
		if(strcmp(current->node.first, key) == 0)
		{
			return current->node.second;
		}
		current = current->next;
	}
	return NULL;
}

void Environment::list_values()
{
	if(env_list != NULL)
	{
		list_node<Pair<char*,char*> >* current = env_list;
		while(current != NULL)
		{
			input->print(current->node.first);;
			input->print(comma);
			input->print(space);
			input->println(current->node.second);
			current = current->next;
		}
	}
}

CLI::CLI(const char* prompt, Stream *input, int noticePin, int timeout)
{
	this->prompt = prompt;
	return_code = -1;
	this->input = input;
	commands = NULL;
	env = new Environment();
	env->input = input;
	this->noticePin = noticePin;
	registered = false;
	this->timeout = (((unsigned long)timeout) * 1000);
}

void CLI::dealloc(list_node<Command>* current)
{
	if(current->next != NULL)
	{
		dealloc(current->next);
	}
	delete current->node.command;
	delete current->node.brief_description;
	delete current;

}
CLI::~CLI()
{
	if(commands != NULL)
	{
		dealloc(commands);
	}
	delete env;
}

void CLI::wait_for_input(int seconds, void (*setup)(CLI* prompt))
{
	flash_print(input, CLI_PRESS_ANY_KEY);

	unsigned long start = millis();
	for(int i = seconds; i >= 0; i--)
	{
		input->print(i);
		while(millis() - start < 1000 && !input->available()){}
		start = millis();
		if (noticePin > 0) {
			digitalWrite(noticePin, HIGH);
			delay(20);
			digitalWrite(noticePin, LOW);
		}
		if(input->available())
		{
			i = 0;
			if(setup != NULL)
			{
				setup(this);
			}else
			{
				flash_print(input, CLI_NO_SETUP);
			}
			run_cli();
			return;
		}
		input->print(backspace);
	}
	input->println();
	flash_println(input, CLI_CONTINUING);
}

int env_callback(char** argv, int argc, Environment* env)
{
	if(argc == 1)
	{
		env->list_values();
	}
	return 0;
}

void CLI::register_cli_commands()
{
	if(!registered)
	{
		flash_copy_local(env_command, CLI_ENV_COMMAND);
		flash_copy_local(env_command_desc, CLI_ENV_COMMAND_DESC);
		register_command(env_command, env_command_desc, &env_callback, NULL);
		registered = true;
	}
}

void CLI::register_command(const char* command, const char* brief_description, int (*command_callback)(char** argv, int argc, Environment* env), int (*help_callback)(char** argv, int argc, Environment* env))
{
	list_node<Command>* c;
	Command cmd;
	if(commands == NULL)
	{
		commands = new list_node<Command>();
		commands->next = 0;
		c = commands;

	}else
	{
		c = commands;
		while (c->next != NULL)
		{
			c = c->next;
		}
		c->next = new list_node<Command>();
		c = c->next;
		c->next = NULL;

	}

	c->node.command = strdup(command);
	c->node.brief_description = strdup(brief_description);
	c->node.command_callback = command_callback;
	c->node.help_callback = help_callback;

}

void CLI::run_cli()
{
	register_cli_commands();

	input->println();
	input->println();
	flash_print(input, CLI_STRING);
	input->print(space);
	flash_print(input, TIMEOUT);
	input->print(colon);
	input->print(space);
	input->print(timeout/1000);
	input->print(space);
	flash_println(input, SECONDS);

	while(input->available()){input->read();}
	input->println();
	input->print(prompt);
	bool running = true;
	String command;
	flash_copy_local(del_char, CLI_DELETE_CHAR);
	lastTime = millis();
	while (running)
	{
		if(input->available())
		{
			char value = (char)(input->read());
			if(value != 10 && value != 13)
			{
				lastTime = millis();
				if(value == 127)
				{
					if(command.length() > 0)
					{
						command = command.substring(0, command.length()-1);
						input->print(del_char);
					}
				}else{
					command.concat(value);
					input->print(value);
				}
			}
			else{

				running = process_input(&command);
				command = String();
				input->println();
				input->print(prompt);
			}
		}else
		{
			if((millis() - lastTime) > timeout)
			{
				input->println();
				flash_print(input, CLI_STRING);
				input->print(space);
				flash_print(input, TIMEOUT);
				input->println(dot);
				running = false;
			}
		}
	}
}

bool CLI::process_input(String *value)
{

	bool found = false;
	flash_copy_local(exit_command, CLI_EXIT_COMMAND);
	flash_copy_local(help_command, CLI_HELP_COMMAND);
	if(value->startsWith(exit_command))
	{
		return false;
	}

	value->trim();
	unsigned int length = value->length();
	char command[length + 1];
	int argc = 1;
	for(unsigned int i = 0; i < length; i++)
	{
		command[i] = (*value)[i];
		if(command[i] == ' ')
		{
			argc++;
			command[i] = 0;
		}
	}

	command[length] = 0;
	char* argv[argc];
	int pos = 0;
	for (int i = 0; i < argc; i++)
	{
		argv[i] = command + pos;
		pos += strlen(argv[i]) + 1;
	}
	input->println();
	input->println();
	if(strcmp(argv[0], help_command) == 0)
	{
		this->help(argv, argc);
		found = true;
	}
	list_node<Command>* tmp = commands;

	while(tmp != NULL){
		if(strcmp(argv[0], tmp->node.command) == 0)
		{
			found = true;
			env->set_env((char*)exit_code, tmp->node.command_callback(argv, argc, env));
			return true;
		}
		tmp = tmp->next;
	}

	if(!found)
	{
		flash_print(env->input, CLI_ERROR_FINDING_COMMAND);
		env->input->println(argv[0]);
	}
	return true;
}

int CLI::help(char** argv, int argc)
{

	list_node<Command>* tmp = commands;
	if(argc == 1)
	{
		flash_println(input, CLI_AVAILABLE_COMMANDS);
		input->println();
		while(tmp != NULL){
			input->print(tab);
			input->print(tmp->node.command);
			input->print(tab);
			input->println(tmp->node.brief_description);
			tmp = tmp->next;
		}
	}
	else{
		while(tmp != NULL && strcmp(tmp->node.command, argv[1]) != 0){
			tmp = tmp->next;
		}
		if(tmp != NULL && tmp->node.help_callback != NULL)
		{
			tmp->node.help_callback(argv, argc, env);
		}else
		{
			flash_print(input, CLI_HELP_NOT_FOUND);
			input->print(argv[1]);
		}
	}
	input->println();
	return 0;
}

Environment* CLI::getEnvironment()
{
	return env;
}
