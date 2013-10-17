WaterQualityMonitor
===================

Arduino Water Quality Monitor

The following libraries are required:

* [OneWire](http://playground.arduino.cc/Learning/OneWire) and its [zip](http://www.pjrc.com/teensy/arduino_libraries/OneWire.zip)
* [Time](http://playground.arduino.cc/Code/Time) and its [zip](http://www.pjrc.com/teensy/arduino_libraries/Time.zip)
* [XBee](http://code.google.com/p/xbee-arduino/) and its [zip](http://xbee-arduino.googlecode.com/files/xbee-arduino-0.4-softwareserial-beta.zip)

Download and unzip them in your Arduino libraries folder. If you are using [ino](http://inotool.org) to
build this project then they can go into the lib folder. 

	
The following libraries are pulled in as git submodules:

* [DallasTemperature](https://github.com/milesburton/Arduino-Temperature-Control-Library)
* [DS3232RTC](https://github.com/Tecsmith/DS3232RTC)

Hardware
========

* (Arduino Mega2560)[http://arduino.cc/en/Main/arduinoBoardMega2560]
* (Arduino Wireless SD Shield)[http://arduino.cc/en/Main/ArduinoWirelessShield]
* (Freetronics DS3232 RealTime Clock)[http://www.freetronics.com/products/real-time-clock-rtc-module]
* (Atlas Scientific Serial Port Connector)[https://www.atlas-scientific.com/product_pages/embedded/serial-port-connector.html]

Sensors
=======

* (Atlas Scientific PH Sensor)[https://www.atlas-scientific.com/product_pages/kits/ph-kit.html]
* (Atlas Scientific Dissolved Oxygen Sensor)[https://www.atlas-scientific.com/product_pages/kits/do-kit.html]
* (Atlas Scientific Oxygen-Reduction Potential Sensor)[https://www.atlas-scientific.com/product_pages/kits/orp-kit.html]
* (Atlas Scientific Conductivity Sensor)(https://www.atlas-scientific.com/product_pages/kits/ec-kit.html)
* (Maxim DS18B20 Temperature Sensor)[http://www.maximintegrated.com/datasheet/index.mvp/id/2812]