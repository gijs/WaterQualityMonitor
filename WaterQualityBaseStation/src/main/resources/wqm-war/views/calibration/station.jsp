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
<mytags:layout title="Calibration" view="c">


<jsp:attribute name="head">
    <%--<script src="/js/experiments.js"></script>--%>
    <%--<link rel="stylesheet" href="/css/commands.css"/>--%>
</jsp:attribute>

    <jsp:body>
        <div id="calibrate_content" class="span5">
            <H1>Select Station to Calibrate</H1>
            <br/>

            Please select the sensor you would like to calibrate: <br/>
            <ul>
                <c:forEach var="station" items="${stations}">
                    <li><a href="/wqm/${view}/${station.getCompactAddress()}">${station.getDisplayName()}</a></li>
                </c:forEach>
            </ul>

        </div>

    </jsp:body>

</mytags:layout>
