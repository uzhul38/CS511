package org.icasa.temperature.manager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.icasa.temperature.api.TemperatureConfiguration;
import org.icasa.temperature.api.TemperatureManagerAdministration;

@Component
@Instantiate(name = "temperature.manager")
@Provides(specifications = { TemperatureManagerAdministration.class })
public class TemperatureManagerImpl implements TemperatureManagerAdministration {
	
	
	@Requires
	private TemperatureConfiguration temperatureConfiguration;

	@Override
	public void temperatureIsTooHigh(String roomName) {
		
		temperatureConfiguration.setTargetedTemperature(roomName, temperatureConfiguration.getTargetedTemperature(roomName)-1);
	}

	@Override
	public void temperatureIsTooLow(String roomName) {
		
		temperatureConfiguration.setTargetedTemperature(roomName, temperatureConfiguration.getTargetedTemperature(roomName)+1);
	}
}
