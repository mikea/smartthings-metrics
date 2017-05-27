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
   section("Monitor These Devices") {
        input "t_devices", "capability.temperatureMeasurement", required: true, title: "Temperature", multiple: true
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

def publishMetrics() {
	log.debug "[metrics] publishMetrics"
    
    t_devices.each{ d ->
            def params = [
                uri:  'https://datadrop.wolframcloud.com/api/v1.0/Add',
                query: [bin:'lYr1BSA8', 
                        t: d.currentValue("temperature"),
                        n: d.displayName]
                ]

            log.debug "[metrics] publishing ${params}"

        try {
            httpGet(params) { resp ->
                resp.headers.each {
                   log.debug "${it.name} : ${it.value}"
                }
                log.debug "[metrics] response status: ${resp.status}"
            }
        } catch (e) {
            log.error "[metrics] exception: $e"
        }
	}
}
