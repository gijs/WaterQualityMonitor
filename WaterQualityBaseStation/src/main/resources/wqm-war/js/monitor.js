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

var intervalID = -1;
var offset = 0;

function doGraph(title, chart_element_id) {
    var chart = new Highcharts.StockChart({
        chart: {
            renderTo: chart_element_id,
            zoomType: 'x'
        },

        plotOptions: {
            line: {
                animation: false
            }
        },

        title: {
            text: title
        },
        series: Monitor.series,
        rangeSelector: {
            selected: 1,
            buttons: [
                {
                    type: 'minute',
                    count: 60,
                    text: '1h'
                },
                {
                    type: 'minute',
                    count: 720,
                    text: '12h'
                },
                {
                    type: 'day',
                    count: 1,
                    text: '1d'
                },
                {
                    type: 'week',
                    count: 1,
                    text: '1w'
                },
                {
                    type: 'month',
                    count: 1,
                    text: '1m'
                },
                {
                    type: 'ytd',
                    text: 'YTD'
                },
                {
                    type: 'year',
                    count: 1,
                    text: '1y'
                },
                {
                    type: 'all',
                    text: 'All'
                }
            ]
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {// don't display the dummy year
                month: '%e. %b',
                year: '%b'
            },
            ordinal: false,
            events: {
                // afterSetExtremes : loadCsv
            },
            minRange: 3600 // one minute
        },
        yAxis: Monitor.yAxis
    });
    return chart;
}

function updateData(chart) {
    function _updateData(data) {
        var keys = Object.keys(data);
        for (var i = 0; i < keys.length; i++) {
            var series = keys[i];
            var id = Monitor.seriesMap[series];
            var dataSeries = data[series];
            var _series = Monitor.series[id];
            if (_series.first) {
                chart.series[id].setData(dataSeries, true);
                _series.first = false;
                console.log("FIRST");

            } else {

                for (var j = 0; j < dataSeries.length; j++) {
//                    console.log(offset+" "+dataSeries[j][0]);
                    if (dataSeries[j][0] > offset) {
                        offset = dataSeries[j][0];
//                        console.log("Update offset");
                    }
                    chart.series[id].addPoint(dataSeries[j], true, true);
//                    console.log("Added: "+series);

                }
            }

        }
    }

    return _updateData;
}

function updateAxis(chart, url) {
    jQuery.ajax({
        url: url + "?after=" + offset,
        dataType: 'json',
        success: updateData(chart),
        error: function () {
            if (intervalID != -1) {
                clearInterval(intervalID);
            }
        }
    });
}


function updateGraph(chart, url, sensors) {

    return function _updateGraph() {
        updateAxis(chart, url);
    }

}
var chart;
function initMonitor(title, chart_element_id, url) {
    chart = doGraph(title, chart_element_id);
    var fun = updateGraph(chart, url);
    intervalID = setInterval(fun, 60000);
    fun();
}