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
#include "RecordStorage.h"


int32_t RECORD_STORAGE_MAX(int32_t count, ...)
{
	va_list vl;
	va_start(vl, count);
	int32_t max = 0, tmp = 0;
	for (int i = 0; i < count; i++)
	{
		tmp=va_arg(vl, int);
		if(tmp > max)
		{
			max = tmp;
		}
	}
	va_end(vl);
	return max;
}

RecordStorage::RecordStorage(int32_t maxRecordSize, char* fileName, int32_t id_msb, int32_t id_lsb, Stream* debugStream) {
	closed = false;
	debug = debugStream;
//	if(debug != NULL)
//	{
//		debug->print("Record Storage: ");
//		debug->println(fileName);
//		debug->print("Record Size: ");
//		debug->println(maxRecordSize);
//	}
	file_name = fileName;
	error_code = 0;
	header = NULL;

//	magic = HEADER_MAGIC_NUMBER;

	if (SD.exists(fileName) ) {
//		if(debug != NULL)
//		{
//			debug->print("Record File Size: ");
//			debug->println(store.size());
//		}
		readHeader();
		if(header->maxRecordSize != maxRecordSize)
		{
			error_code = error_code | ERROR_ROW_SIZE_MISMATCH;
		}
	} else {
		header = (RecordHeader*) calloc(sizeof(RecordHeader), 1);
		header->magicNumber = HEADER_MAGIC_NUMBER;
		header->maxRecordSize = maxRecordSize;
		header->uploaded = 0;
		header->id_lsb = id_lsb;
		header->id_msb = id_msb;
		writeHeader();
	}
}

RecordStorage::~RecordStorage() {
	if (header != NULL) {
		free(header);
		header = NULL;
	}
	close();
}

void RecordStorage::readHeader() {
	open();
	if (store) {
		store.seek(0);
		if(header == NULL)
		{
			header = (RecordHeader*) calloc(sizeof(RecordHeader), 1);
		}

		store.read(header, sizeof(RecordHeader));
		if (header->magicNumber != (HEADER_MAGIC_NUMBER)) {
			free(header);
			header = NULL;
			error_code = error_code | ERROR_INVALID_MAGIC_NUMBER;
		}else
		{
//			if(debug != NULL)
//			{
//				debug->println("Read Header.");
//			}
		}
	}
}

boolean RecordStorage::writeHeader() {
//	if(debug != NULL)
//	{
//		debug->println("Write Header.");
//	}
	open();
	if (goodToGo()) {
		store.seek(0);
		store.write((uint8_t*) header, sizeof(RecordHeader));
		store.flush();

//		if(debug != NULL)
//		{
//			debug->println(store.getWriteError());
//			debug->println("Wrote Header.");
//		}
		return true;
	}else
	{
//		if (debug != NULL) {
//			debug->println("Error writing Header.");
//		}
		error_code = error_code | ERROR_WRITING_HEADER;
	}

	return false;
}

void RecordStorage::open() {
	if (!store) {
		store = SD.open(file_name, FILE_WRITE);
//		if(debug != NULL)
//		{
//			debug->println(store.name());
//		}
	}
}

void RecordStorage::close() {
//	if(debug != NULL)
//	{
//		debug->print("Closing ");
//	}
	if (store) {
		store.flush();
		store.close();

		closed = true;
//		if(debug != NULL)
//		{
//			debug->println("Done");
//		}
	}
}

boolean RecordStorage::goodToGo() {
	if(closed)
	{
		open();
	}
	return header != NULL && header->magicNumber == HEADER_MAGIC_NUMBER
			&& error_code == ERROR_NO_ERROR && store.operator bool();
}

boolean RecordStorage::storeRecord(byte* record, int32_t recordSize) {
//	if(debug != NULL)
//	{
//		debug->print("Record Size: ");
//		debug->println(recordSize);
//		debug->print("Max Size (header): ");
//		debug->println(header->maxRecordSize);
//	}

	if (goodToGo() && store.seek(store.size())) {
		byte area[header->maxRecordSize];
		for (int32_t i = 0; i < header->maxRecordSize; i++) {
			if (i < recordSize) {
				area[i] = record[i];
			} else {
				area[i] = 0;
			}
		}

		size_t wrote = store.write(area, header->maxRecordSize);
		store.flush();
//		if(debug != NULL)
//		{
//			debug->print("Recorded ");
//			debug->print(wrote);
//			debug->println(" bytes.");
//		}
		return wrote == header->maxRecordSize;
	}
	return false;
}

int32_t RecordStorage::retrieveRecord(byte* record, int32_t recordID) {
	if(goodToGo())
	{
//		if(debug != NULL)
//		{
//			debug->print("Record Position: ");
//			debug->println(sizeof(RecordHeader) + (header->maxRecordSize * recordID));
//		}

		store.seek(sizeof(RecordHeader) + (header->maxRecordSize * recordID));
		return store.read(record, header->maxRecordSize);
	}else if(debug != NULL)
	{
//		debug->println("Not Good To Go!");
	}
	return -1;
}

int32_t RecordStorage::getRecordCount() {
	if (goodToGo()) {
		return (store.size() - sizeof(RecordHeader)) / header->maxRecordSize;
	}
	return -1;
}

int32_t RecordStorage::getRecordSize() {
	if (goodToGo()) {
		return header->maxRecordSize;
	}
	return -1;
}

uint64_t RecordStorage::getErrorCode() {
	return error_code;
}

int32_t RecordStorage::getUploadedCount() {
	if (goodToGo()) {
		return header->uploaded;
	}
	return 0;
}

void RecordStorage::setUploadedCount(int32_t count) {
	if (goodToGo()) {
		header->uploaded = count;
	}
}





