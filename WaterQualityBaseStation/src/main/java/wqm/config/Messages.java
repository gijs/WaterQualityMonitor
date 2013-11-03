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

package wqm.config;

/**
 * Date: 11/3/13
 * Time: 7:05 PM
 *
 * @author NigelB
 */
public interface Messages {
    public static final String ERROR_MESSAGE = "error_message";
    public static final String MESSAGE = "message";
    public static final String SUCCESS_MESSAGE = "success_message";
    public static final String WARNING_MESSAGE = "warning_message";

    public static final String[] SESSION_FIELDS = new String[]{ERROR_MESSAGE, MESSAGE, SUCCESS_MESSAGE};
}
