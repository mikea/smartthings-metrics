/**
 *  smartthings-metrics
 *
 *  Copyright 2017 Mike Aizatsky
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "smartthings-metrics",
    namespace: "mikea",
    author: "Mike Aizatsky",
    description: "smartthings-metrics",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
   section("Collect Metrics") {
        input "co2_devices", "capability.carbonDioxideMeasurement", required: false, title: "Carbon Dioxide", multiple: true
        input "i_devices", "capability.illuminanceMeasurement", required: false, title: "Illuminance", multiple: true
        input "ph_devices", "capability.phMeasurement", required: false, title: "pH", multiple: true
        input "rh_devices", "capability.relativeHumidityMeasurement", required: false, title: "Relative Humidity", multiple: true
        input "t_devices", "capability.temperatureMeasurement", required: false, title: "Temperature", multiple: true
        input "v_devices", "capability.voltageMeasurement", required: false, title: "Voltage", multiple: true
   }
   
   section("Publish Metrics") {
        input "datadrop_bin", type: "text", required: true, title: "Wolfram Data Drop Bin ID"
        input "period", type: "number", required: true, title: "Period, min", defaultValue: 15
   }
}

def installed() {
	log.debug "[metrics] installed settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "[metrics] updated settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "[metrics] initializing"
	log.debug "[metrics] unscheduling..."
	unschedule()
	log.debug "[metrics] scheduling..."
    runEvery15Minutes(publishMetrics)
    log.debug "[metrics] initialized"
}

def publishDevice(device, parameterName) {
    def params = [
        uri:  'https://datadrop.wolframcloud.com/api/v1.0/Add',
        query: [bin:datadrop_bin, 
                v: d.currentValue(parameterName),
                n: device.displayName]
    ]

    log.debug "[metrics] publishing ${params}"

    try {
        httpGet(params) { resp ->
            log.debug "[metrics] response status: ${resp.status}"
        }
    } catch (e) {
        log.error "[metrics] exception: $e"
    }
}

def publishMetrics() {
	log.debug "[metrics] publishMetrics binid: ${datadrop_bin} ${period}"
    
    if (datadrop_bin == "") {
    	log.error "[metrics] error: datadrop bin not set"
        return
    }
    
	co2_devices.each{ d -> publishDevice(d, "carbonDioxide") }
	i_devices.each{ d -> publishDevice(d, "illuminance") }
	ph_devices.each{ d -> publishDevice(d, "pH") }
	rh_devices.each{ d -> publishDevice(d, "humidity") }
	t_devices.each{ d -> publishDevice(d, "temperature") }
    v_devices.each{ d -> publishDevice(d, "voltage") }
}
