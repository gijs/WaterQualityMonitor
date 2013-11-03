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

package wqm.web.exceptions;

/**
 * Date: 11/3/13
 * Time: 6:44 PM
 *
 * @author NigelB
 */
public class RedirectException extends RuntimeException{
    private String redirectTo;

    public RedirectException(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public RedirectException(String message, String redirectTo) {
        super(message);
        this.redirectTo = redirectTo;
    }

    public RedirectException(String message, Throwable cause, String redirectTo) {
        super(message, cause);
        this.redirectTo = redirectTo;
    }

    public RedirectException(Throwable cause, String redirectTo) {
        super(cause);
        this.redirectTo = redirectTo;
    }

    public String getRedirectTo() {
        return redirectTo;
    }
}
