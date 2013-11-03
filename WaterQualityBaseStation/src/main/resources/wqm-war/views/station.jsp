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
<mytags:layout title="Experiments" view="station">

<jsp:attribute name="head">
    <%--<script src="/js/experiments.js"></script>--%>
    <%--<link rel="stylesheet" href="/css/commands.css"/>--%>
</jsp:attribute>


    <jsp:body>
        <c:forEach var="entry" items="${stations}">
            Name:  ${entry} <br/>
            <c:choose>
                <c:when test="${entry.getCommonName().length() > 0}">
                Value: ${entry.getCommonName()} <br/>
                </c:when>
                <c:otherwise>
                    ${entry.get_Address()} <br/>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </jsp:body>
</mytags:layout>