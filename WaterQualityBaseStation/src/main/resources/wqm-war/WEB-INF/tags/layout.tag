<%@tag description="page layout" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@attribute name="head" fragment="true" %>
<%@attribute name="station_nav" fragment="true" %>
<%@attribute name="title" required="true" type="java.lang.String" %>
<%@attribute name="view" required="true" type="java.lang.String" %>
<%@attribute name="noMenu" required="false" type="java.lang.Boolean" %>
<%@attribute name="caching" required="false" type="java.lang.Boolean" %>
<%@attribute name="logo" required="false" type="java.lang.String" %>
<!DOCTYPE html>
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


<c:if test="${ not empty caching}">
    <c:if test="${caching}">
        <%
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setDateHeader("Expires", -1);
        %>
    </c:if>
</c:if>

<html>
<head>
    <title>${title}</title>
    <meta charset="utf-8"/>
    <link rel="stylesheet" href="/css/wqm.css"/>
    <link rel="stylesheet" href="/css/bootstrap.min.css"/>
    <script src="/js/jquery.js"></script>
    <script src="/js/highstock.js"></script>
    <jsp:invoke fragment="head"/>
</head>
<body>
<c:if test="${view.equals('c')}">
    <c:set var="calibrate_class" value="nav_selected"/>
</c:if>
<c:if test="${view.equals('m')}">
    <c:set var="monitor_class" value="nav_selected"/>
</c:if>
<div class="navbar">
    <div class="navbar-inner">
        <a class="brand" href="/">Water Quality Monitor</a>
        <ul class="nav">
            <li class="${monitor_class}"><a href="/wqm/m">Monitor</a></li>
            <li class="${calibrate_class}"><a href="/wqm/c">Calibrate</a></li>
            <li><a href="/WQMData">Data</a></li>
        </ul>
    </div>
</div>

<div class="container-fluid">
    <div class="row-fluid">
        <c:if test="${empty noMenu}">
            <div id="side-bar" class="span2">

                <c:if test="${not empty logo}">
                    <img id="logo" src="${logo}"/>
                </c:if>

                <table id="server_commands" class='table'>
                    <thead>
                    <tr>
                        <th>Stations</th>
                    </tr>

                    <c:forEach var="entry" items="${stations}">
                    <tr>
                        <td>
                            <a href="/wqm/${view}/${entry.getCompactAddress()}">${entry.getDisplayName()}</a>
                            <c:if test="${!(entry.getCommonName().length() > 0)}"><br/><a
                                    href="/wqm/r/${entry.getCompactAddress()}">Rename</a></c:if>
                        </td>

                        </c:forEach>
                    </tr>
                    </thead>
                </table>
            </div>
        </c:if>

        <div>
            <c:set var="view" scope="session" value="${view}"/>
            <div id="messages">
                <c:if test="${not empty success_message}">
                    <span id="success_message">Success: ${success_message}</span><BR/><BR/>
                </c:if>
                <c:if test="${not empty message}">
                    <span id="message">${message}</span><BR/><BR/>
                </c:if>
                <c:if test="${not empty warning_message}">
                    <span id="warning_message">Warning: ${warning_message}</span><BR/><BR/>
                </c:if>
                <c:if test="${not empty error_message}">
                    <span id="error_message">Error: ${error_message}</span><BR/><BR/>
                </c:if>
            </div>
            <jsp:doBody/>
        </div>
    </div>

    <script src="/js/wqm.js"></script>
</div>

</body>
</html>