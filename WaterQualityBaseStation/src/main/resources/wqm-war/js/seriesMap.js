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

var Monitor = {
    seriesMap: {
        Temperature: 0,
        PH: 1,
        DO: 2,
        Percent: 3,
        ORP: 4,
        PPM: 5,
        uS: 6,
        Salinity: 7
    },
    series: [],
    yAxis: []
};


Monitor.series[Monitor.seriesMap.Temperature] = {
    name: "Temperature",
    yAxis: Monitor.seriesMap.Temperature,
    data: [0],
    first: true,
    color: '#294EA3',
    tooltip: {
        valueDecimals: 3
    }
};

Monitor.yAxis[Monitor.seriesMap.Temperature] = {
    title: {
        text: 'Temperature_',
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
};

Monitor.series[Monitor.seriesMap.PH] = {
    name: "PH",
    yAxis: Monitor.seriesMap.PH,
    data: [0],
    first: true,
    color: '#de6868',
    tooltip: {
        valueDecimals: 3
    }
};

Monitor.yAxis[Monitor.seriesMap.PH] = {
    title: {
        text: 'PH',
        style: {
            color: '#de6868'
        }
    }, labels: {
        style: {
            color: '#de6868'
        },
        max: 14,
        min: 0,
        minRange: 14,
        autoScale: false
    }
};

Monitor.series[Monitor.seriesMap.DO] = {
    name: "DO",
    yAxis: Monitor.seriesMap.DO,
    data: [0],
    first: true,
    color: '#c1de68',
    tooltip: {
        valueDecimals: 3
    }
};

Monitor.yAxis[Monitor.seriesMap.DO] = {
    title: {
        text: 'DO',
        style: {
            color: '#c1de68'
        }
    }, labels: {
        style: {
            color: '#c1de68'
        },
        max: 14,
        min: 0,
        minRange: 14,
        autoScale: false
    }
};

Monitor.series[Monitor.seriesMap.Percent] = {
    name: "Percent Saturation",
    yAxis: Monitor.seriesMap.Percent,
    data: [0],
    first: true,
    color: '#658012',
    tooltip: {
        valueDecimals: 3
    }
};
Monitor.yAxis[Monitor.seriesMap.Percent] = {
    title: {
        text: 'Percent Saturation',
        style: {
            color: '#658012'
        }
    }, labels: {
        style: {
            color: '#658012'
        },
        max: 14,
        min: 0,
        minRange: 14,
        autoScale: false
    }
};
Monitor.series[Monitor.seriesMap.ORP] = {
    name: "ORP",
    yAxis: Monitor.seriesMap.ORP,
    data: [0],
    first: true,
    color: '#3e63cb',
    tooltip: {
        valueDecimals: 3
    }
};
Monitor.yAxis[Monitor.seriesMap.ORP] = {
    title: {
        text: 'ORP',
        style: {
            color: '#3e63cb'
        }
    }, labels: {
        style: {
            color: '#3e63cb'
        },
        max: 14,
        min: 0,
        minRange: 14,
        autoScale: false
    }
};
Monitor.series[Monitor.seriesMap.PPM] = {
    name: "PPM",
    yAxis: Monitor.seriesMap.PPM,
    data: [0],
    first: true,
    color: '#8e8716',
    tooltip: {
        valueDecimals: 3
    }
};

Monitor.yAxis[Monitor.seriesMap.PPM] = {
    title: {
        text: 'PPM',
        style: {
            color: '#8e8716'
        }
    }, labels: {
        style: {
            color: '#8e8716'
        },
        max: 14,
        min: 0,
        minRange: 14,
        autoScale: false
    }
};

Monitor.series[Monitor.seriesMap.uS] = {
    name: "uS",
    yAxis: Monitor.seriesMap.uS,
    data: [0],
    first: true,
    color: '#c8c260',
    tooltip: {
        valueDecimals: 3
    }
};

Monitor.yAxis[Monitor.seriesMap.uS] = {
    title: {
        text: 'uS',
        style: {
            color: '#c8c260'
        }
    }, labels: {
        style: {
            color: '#c8c260'
        },
        max: 14,
        min: 0,
        minRange: 14,
        autoScale: false
    }
};

Monitor.series[Monitor.seriesMap.Salinity] = {
    name: "Salinity",
    yAxis: Monitor.seriesMap.Salinity,
    data: [0],
    first: true,
    color: '#ede9a1',
    tooltip: {
        valueDecimals: 3
    }
};

Monitor.yAxis[Monitor.seriesMap.Salinity] = {
    title: {
        text: 'Salinity',
        style: {
            color: '#ede9a1'
        }
    }, labels: {
        style: {
            color: '#ede9a1'
        },
        max: 14,
        min: 0,
        minRange: 14,
        autoScale: false
    }
};

