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

function callCommand(url, element)
{
    element.disable = false;
    jQuery.ajax({
        async: false,
        url: url,
        dataType: 'json',
        success: function(){
            element.disabled = false;
        },
        error: function(){
            console.log("Call failed: "+url);
            element.disabled = false;
        }
    });
}

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
        series: [
            {
                name: 'mV',
                yAxis: 0,
                data: [0],
                color: '#294EA3',
                tooltip: {
                    valueDecimals: 3
                }
            }
        ],
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
        yAxis: [
            {
                title: {
                    text: 'mV',
                    style: {
                        color: '#294EA3'
                    }
                }, labels: {
                style: {
                    color: '#294EA3'
                }
            }
            }
        ]
    });
    return chart;
}
var intervalID = -1;

function updateGraph(chart, url) {
    var all_data = [];
    return function _updateGraph() {
        jQuery.ajax({
            url: url + "?offset=" + all_data.length,
            dataType: 'json',
            success: function (data) {
                for (var i = 0; i < data.length; i++) {
                    all_data.push(data[i]);
                }
                chart.series[0].setData(all_data, true);
            },
            error: function(){
                if(intervalID != -1)
                {
                    clearInterval(intervalID);
                }
            }
        });
    }

}

function initORP(title, chart_element_id, url) {
    var chart = doGraph(title, chart_element_id);

    intervalID = setInterval(updateGraph(chart, url), 1000);
}