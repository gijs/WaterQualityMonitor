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

var wakeupButton = -1;
var count = 5 * 60 * 1000;
function enableButton(do_accept_button, time_element) {
    function doEnable() {
        var minutes = Math.floor( count / (60 * 1000) );
        var seconds = Math.floor((count - (minutes * (60 * 1000)))/1000);
        var _sec = ("0" +seconds).slice (-2);
        console.log(minutes + ":" + _sec);
        time_element.innerHTML = (minutes + ":" + _sec);
        if (count > 0) {
            count -= 1000;

        } else {
            do_accept_button.disabled = false;
            clearInterval(wakeupButton);
        }
    }
    return doEnable;
}

function initDO(title, chart_element_id, url, do_accept_button, time_element) {
    var calibration = {
        title: title,
        chart_element_id: chart_element_id,

        intervalID: -1,
        series: [
            {
                name: 'mg/L',
                yAxis: 0,
                data: [1],
                color: '#294EA3',
                tooltip: {
                    valueDecimals: 3
                }
            }
        ],
        yAxis: [
            {
                title: {
                    text: 'mg/L',
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

    calibration.intervalID = setInterval(updateGraph(chart, url, calibration), 1000);
    wakeupButton = setInterval(enableButton(do_accept_button, time_element), 1000);
}