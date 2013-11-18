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

function initORP(title, chart_element_id, url) {
    var calibration = {
        title: title,
        chart_element_id: chart_element_id,

        intervalID: -1,
        series: [
            {
                name: 'mV',
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
                    text: 'mV',
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