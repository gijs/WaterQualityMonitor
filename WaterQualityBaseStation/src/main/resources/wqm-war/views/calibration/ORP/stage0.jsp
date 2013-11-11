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
<mytags:layout title="Calibrate ${station.getCommonName()} ORP" view="calibrate" caching="false" logo="/images/orp-logo.jpg">

<jsp:attribute name="head">
    <script src="/js/calibrate_orp.js"></script>
</jsp:attribute>


    <jsp:body>
        <div id="calibrate_content" class="span5">
            <H3>${station.getCommonName()} ORP Calibration</H3>
            Have a look at you calibration solution and note its mV rating. Then use the + and - buttons to match th
            output of the sensor to the value of the calibration solution.
            <div id="orp_graph"></div>

            <div id="calibration_button_1">
                <table>
                    <tr>
                        <td>
                            <button onclick="callCommand('/wqm/c/${station.getCompactAddress()}/${sensor.getId()}/${phase}/up', this)" class="btn btn-default" type="button">+</button>
                        </td>
                        <td>
                            <button onclick="callCommand('/wqm/c/${station.getCompactAddress()}/${sensor.getId()}/${phase}/down', this)" class="btn btn-default" type="button">-</button>
                        </td>
                    </tr>


                    <tr>
                        <td colspan="2" style="padding-top: 50px">
                            <form action="/wqm/c/${station.getCompactAddress()}/${sensor.getId()}/0/quit">
                                <button class="btn btn-default" type="submit">Quit</button>
                            </form>
                        </td>
                    </tr>
                </table>
            </div>

            <script>
                initORP("ORP Calibration", "orp_graph", "/wqm/d/${station.getCompactAddress()}/${sensor.getId()}/${phase}")
            </script>
        </div>
    </jsp:body>
</mytags:layout>