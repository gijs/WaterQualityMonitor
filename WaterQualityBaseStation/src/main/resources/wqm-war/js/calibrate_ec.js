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

//function doGraph(calibration) {
//    var chart = new Highcharts.StockChart({
//        chart: {
//            renderTo: calibration.chart_element_id,
//            zoomType: 'x'
//        },
//
//        plotOptions: {
//            line: {
//                animation: false
//            }
//        },
//
//        title: {
//            text: calibration.title
//        },
//        series: calibration.series,
//        rangeSelector: {
//            selected: 1,
//            buttons: [
//                {
//                    type: 'minute',
//                    count: 60,
//                    text: '1h'
//                },
//                {
//                    type: 'minute',
//                    count: 720,
//                    text: '12h'
//                },
//                {
//                    type: 'day',
//                    count: 1,
//                    text: '1d'
//                },
//                {
//                    type: 'week',
//                    count: 1,
//                    text: '1w'
//                },
//                {
//                    type: 'month',
//                    count: 1,
//                    text: '1m'
//                },
//                {
//                    type: 'ytd',
//                    text: 'YTD'
//                },
//                {
//                    type: 'year',
//                    count: 1,
//                    text: '1y'
//                },
//                {
//                    type: 'all',
//                    text: 'All'
//                }
//            ]
//        },
//        xAxis: {
//            type: 'datetime',
//            dateTimeLabelFormats: {// don't display the dummy year
//                month: '%e. %b',
//                year: '%b'
//            },
//            ordinal: false,
//            events: {
//                // afterSetExtremes : loadCsv
//            },
//            minRange: 3600 // one minute
//        },
//        yAxis: calibration.yAxis
//    });
//    return chart;
//}
//
//
//function updateGraph(chart, url) {
//    var offset = 0;
//    var first = true;
//    return function _updateGraph() {
//        jQuery.ajax({
//            url: url + "?offset=" + offset,
//            dataType: 'json',
//            success: function (data) {
//                if(first)
//                {
//                    first = false;
//                    chart.series[0].setData(data, true);
//                    offset = data.length;
//                    chart.xAxis[0].setExtremes(dataSeries[dataSeries.length - 1][0] - (1000 * 60 * 60), dataSeries[dataSeries.length - 1][0] + 1000);
//                }else
//                {
//                    for (var i = 0; i < data.length; i++) {
//                        console.log(data[i]);
//                        chart.series[0].addPoint(data[i], true, false);
//                        offset++;
//                    }
//                }
//            },
//            error: function(){
//                if(intervalID != -1)
//                {
//                    clearInterval(intervalID);
//                }
//            }
//        });
//    }
//}

function initEC(title, chart_element_id, url) {
    var calibration = {
        title: title,
        chart_element_id: chart_element_id,

        intervalID: -1,
        series: [
            {
                name: 'µs/cm',
                yAxis: 0,
                data: [1],
                color: '#294EA3',
                tooltip: {
                    valueDecimals: 3
                }
            }
        ],
        yAxis:[
            {
                title: {
                    text: 'µs/cm',
                    style: {
                        color: '#294EA3'
                    }
                }, labels: {
                style: {
                    color: '#294EA3'
                },
                max: 14,
                min: 0,
                minRange: 14,
                autoScale: false
            }
            }
        ]

    };
    var chart = doGraph(calibration);
    console.log(chart);

    calibration.intervalID = setInterval(updateGraph(chart, url), 1000);
}