<%--
  ~ Water Quality Monitor Java Basestation
  ~ Copyright (C) 2013  nigelb
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License along
  ~ with this program; if not, write to the Free Software Foundation, Inc.,
  ~ 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  --%>

<%@taglib tagdir="/WEB-INF/tags" prefix="mytags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<mytags:layout title="Calibration" view="Locked">

<jsp:attribute name="head">
</jsp:attribute>


    <jsp:body>
        <div id="calibrate_content" class="span5">
            <H1>Calibration
                <c:choose>
                    <c:when test="${not empty param.station}"> : ${station.getCommonName()} <c:choose><c:when
                            test="${not empty param.sensor}">- ${sen.getLongName()}</c:when></c:choose></c:when>
                </c:choose>
            </H1>
            <br/>

            This station is locked for calibration by somebody else.
        </div>

    </jsp:body>

</mytags:layout>
