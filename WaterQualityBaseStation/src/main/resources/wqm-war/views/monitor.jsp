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
<mytags:layout title="Monitoring ${station.getDisplayName()}" view="m" noMenu="true">

<jsp:attribute name="head">
    <script src="/js/seriesMap.js"></script>
    <script src="/js/monitor.js"></script>
</jsp:attribute>


    <jsp:body>
        <div id="monitor_content" >
            <H1>Monitoring ${station.getDisplayName()}</H1>
            <br/>
            <div id="monitor_graph"></div>
            <script>
                initMonitor("${station.getDisplayName()} Water Quality", "monitor_graph", "/wqm/d/${station.getCompactAddress()}")
            </script>
        </div>

    </jsp:body>

</mytags:layout>
