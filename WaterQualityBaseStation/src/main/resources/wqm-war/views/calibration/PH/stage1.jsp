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
<mytags:layout title="Calibrate ${station.getCommonName()} PH" view="calibrate">

<jsp:attribute name="head">
    <script src="/js/calibrate_ph.js"></script>
</jsp:attribute>


    <jsp:body>
        <div id="calibrate_content" class="span5">
            <H1>${station.getCommonName()} PH Calibration - <span class="ph4">PH 4</span></H1>
            Rinse off pH sensor, dry with paper towel.
            Place you pH Sensor in the red <span class="ph4">pH 4</span> calibration solution.
            Wait one to two minutes and when the graph has stabilized press the accept button below, or you can press quit to exit calibration mode.
            <div id="ph_graph"></div>
            <form action="/wqm/c/${station.getCompactAddress()}/${sensor.getId()}/${phase}/accept"><button class="btn btn-default" type="submit">Accept</button></form>
            <form action="/wqm/c/${station.getCompactAddress()}/${sensor.getId()}/${phase}/quit"><button class="btn btn-default" type="submit">Quit</button></form>
            <script>
                initPH("PH Calibration", "ph_graph", "/wqm/d/${station.getCompactAddress()}/${sensor.getId()}/${phase}")
            </script>
        </div>
    </jsp:body>
</mytags:layout>