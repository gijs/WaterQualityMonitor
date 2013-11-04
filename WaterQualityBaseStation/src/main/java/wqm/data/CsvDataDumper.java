/*
 * Water Quality Monitor Java Basestation
 * Copyright (C) 2013  nigelb
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package wqm.data;

import wqm.radio.RecordStorage.record.BaseRecord;

import java.io.File;
import java.io.IOException;

/**
 * Date: 11/4/13
 * Time: 5:30 PM
 *
 * @author NigelB
 */
public interface CsvDataDumper <T extends BaseRecord>{
    Integer getPacketType();
    void dumpData(File outputDir, String prefix, T record) throws IOException;
}
