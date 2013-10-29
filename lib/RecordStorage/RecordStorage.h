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

#ifndef __RECORD_STORAGE_H_
#define __RECORD_STORAGE_H_

#include <SD.h>
#define HEADER_MAGIC_NUMBER 0xDEAFBEEF

#define ERROR_NO_ERROR 0
#define ERROR_INVALID_MAGIC_NUMBER   1
#define ERROR_WRITING_HEADER         (1 << 1)
#define ERROR_ROW_SIZE_MISMATCH      (1 << 2)

#define RECORD_STORAGE_VA_NARGS_IMPL(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15, _16, _17, _18, _19, _20, N, ...) N
#define RECORD_STORAGE_VA_NARGS(...) RECORD_STORAGE_VA_NARGS_IMPL(__VA_ARGS__, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)

#define max_record_size(...) RECORD_STORAGE_MAX(RECORD_STORAGE_VA_NARGS(__VA_ARGS__), __VA_ARGS__)

int32_t RECORD_STORAGE_MAX(int32_t count, ...);

struct RecordHeader
{
	uint64_t magicNumber;
	int32_t maxRecordSize;
	int32_t uploaded;
	int32_t id_msb;
	int32_t id_lsb;

};

class RecordStorage
{
private:
//	uint64_t magic;
	uint64_t error_code;
	char* file_name;
	RecordHeader* header;
	File store;
	boolean closed;

    Stream* debug;

	void open();


public:
	RecordStorage(int32_t maxRecordSize, char* fileName, int32_t id_msb, int32_t id_lsb, Stream* debugStream = NULL);
	~RecordStorage();
	boolean goodToGo();
	boolean storeRecord(byte* record, int32_t recordSize);
	int32_t retrieveRecord(byte* record, int32_t recordID);
	int32_t getRecordCount();
	uint64_t getErrorCode();

	int32_t getUploadedCount();
	void setUploadedCount(int32_t count);

	int32_t getRecordSize();

	void readHeader();
	boolean writeHeader();
	void close();

	friend class RecordStorageUtils;

};

//class RecordStorageUtils
//{
//private:
//	void printDirectory(Stream* outstream);
//	RecordStorage* _storage;
//public:
//	RecordStorageUtils(RecordStorage* storage);
//	boolean list_directory(char* dir, Stream* outstream);
//
//};

#endif
