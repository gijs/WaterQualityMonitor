
<%@taglib tagdir="/WEB-INF/tags" prefix="mytags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<mytags:layout title="Calibrate ${station.getCommonName()} PH" view="calibrate">

<jsp:attribute name="head">
    <%--<script src="/js/experiments.js"></script>--%>
    <%--<link rel="stylesheet" href="/css/commands.css"/>--%>
</jsp:attribute>


    <jsp:body>
        <div id="calibrate_content" class="span5">
            <H1>${station.getCommonName()} PH - Stage 0</H1>
<%--            <div id="ph_instructions" >
                <a href="/wqm/j/${view}.jsp?station=${param.station}&sensor=${sensor.getId()}&stage=0">Next</a>
            </div>
            <div id="ph_warning">
                This station is currently being calibrated by someone else.
            </div>

            <script>
                var status = $.ajax({
                    dataType: "json",
                    url: "/wqm/calibrate/${param.station}/${param.sensor}",
                    data: {},
                    success: function (){},
                    async:false
                }).responseJSON;
                console.log(status);
                console.log();
                if(status.acquired_lock)
                {
                    $("#ph_instructions").css("visibility", "visible");
                }else
                {
                    $("#ph_warning").css("visibility", "visible");
                }
            </script>--%>
        </div>
    </jsp:body>
</mytags:layout>