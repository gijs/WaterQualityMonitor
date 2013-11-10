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
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<mytags:layout title="Calibrate ${station.getCommonName()} PH" view="calibrate">


<jsp:attribute name="head">
</jsp:attribute>


    <jsp:body>
        <div id="calibrate_content" class="span5">
            <H3>Electrical Conductivity (EC)</H3>

            <p>The calibration of the EC sensor is carried out by following this procedure:
            <ol>
                <li>Select the type of sensor (this step on on this page)</li>
                <li>Carry out the "Dry Calibration step"</li>
                <li>Carry out the calibration for the "High end"</li>
                <li>Carry out the calibration for the "Low end"</li>
            </ol>
            </p>
            <br/>
            <div id="ec_sensor_type">
                <h4>Select the type of EC probe that is attached to the sensor:</h4>
                <form id="ec_select_sensor_type" action="/wqm/c/${station.getCompactAddress()}/${sensor.getId()}/0">
                    <input checked="checked" type="radio" name="ec_sensor_type" value="${k1}"> K0.1 Pure water and drinking water, 11µs/cm to 3,000μs/cm<BR/>
                    <input type="radio" name="ec_sensor_type" value="${k2}"> K1.0 Fresh water to brackish water, 1,300 µs/cm to 40,000µs/cm<BR/>
                    <input type="radio" name="ec_sensor_type" value="${k3}"> K10.0 Salt water, 36,000 µs/cm to 92,000µs/cm<BR/>
                </form>
            </div>
            <form id="quit_form" action="/wqm/c/${station.getCompactAddress()}/${sensor.getId()}/0/quit"></form>
            <div id="calibration_button_1">
                <table>
                    <tr>
                        <td>
                            <button form="quit_form" class="btn btn-default" type="submit">Quit</button>

                        </td>
                        <td>
                            <button form="ec_select_sensor_type" class="btn btn-default" type="submit">Next</button>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </jsp:body>
</mytags:layout>