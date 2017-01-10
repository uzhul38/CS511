package org.icasa.temperature.regulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.icasa.temperature.api.TemperatureConfiguration;

import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

@Component
@Instantiate(name = "temperature.controller")
@SuppressWarnings("rawtypes")
@Provides(specifications = { PeriodicRunnable.class, TemperatureConfiguration.class })
public class TemperatureRegulatorImpl implements PeriodicRunnable, TemperatureConfiguration {

	/** Location Value ***/
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/** Constants values **/

	private static final int MESURES_DELAY = 1;

	private double ZERO_DEG = 273.15d;

	private double temperatureGoal_livingroom = 18.00d;

	private double temperatureGoal_bathroom = 23.00d;

	private double temperatureGoal_kitchen = 15.00d;

	private double temperatureGoal_bedroom = 20.00d;

	/** Service needed **/

	@Requires(id = "coolers", optional = true)
	private Cooler[] coolers;

	@Requires(id = "heaters", optional = true)
	private Heater[] heaters;

	@Requires(id = "thermometers", optional = true)
	private Thermometer[] thermometers;
	/***************** Life Cycle of Component **********************/

	@Invalidate
	public synchronized void stop() {
	}

	/** Component Lifecycle Method */
	@Validate
	public void start() {
		System.out.println("Component is Starting... " + getPeriod() +" unit " + getUnit());		
	}

	@Override
	public void run() {
		System.out.println("Delay" + MESURES_DELAY);
		settingTemperatureOnLocation(getThermometerfromLocation(thermometers));
	}

	@Override
	public long getPeriod() {
		return MESURES_DELAY;
	}

	@Override
	public TimeUnit getUnit() {
		return TimeUnit.SECONDS;
	}


	/********* Power management *************/
	public double convertTempToPower(String location, double current_value) {

		double goal = 0.0d;

		switch (location) {

		case "livingroom":
			goal = temperatureGoal_livingroom + ZERO_DEG;

			break;

		case "kitchen":
			goal = temperatureGoal_kitchen + ZERO_DEG;

			break;

		case "bedroom":
			goal = temperatureGoal_bedroom + ZERO_DEG;

			break;

		case "bathroom":
			goal = temperatureGoal_bathroom + ZERO_DEG;

			break;
		}

		return (goal - current_value) / 1000.0d;
	}

	/********** Define cooler and heater behavior ********/

	public void defineCoolerAndHeaterAction(String location, double power) {
		if (power > 0) {
			heaterBehavior(location, power);
			coolerBehavior(location, 0);
		} else if (power < 0) {
			heaterBehavior(location, 0);
			coolerBehavior(location, -power);
		} else {
			heaterBehavior(location, 0);
			coolerBehavior(location, 0);
		}

	}

	public void heaterBehavior(String location, double power) {

		List<Heater> sameLocationHeater = getHeaterFromLocation(location);

		for (Heater heater : sameLocationHeater) {
			heater.setPowerLevel(power);
		}
	}

	public void coolerBehavior(String location, double power) {

		List<Cooler> sameLocationCooler = getCoolerFromLocation(location);
		for (Cooler cooler : sameLocationCooler) {
			cooler.setPowerLevel(power);
		}
	}

	/**************** Get device from location **************/

	private synchronized List<Cooler> getCoolerFromLocation(String location) {
		List<Cooler> coolerLocation = new ArrayList<Cooler>();

		for (Cooler cooler : coolers) {
			if (cooler.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				coolerLocation.add(cooler);
			}
		}

		return coolerLocation;
	}

	private synchronized List<Heater> getHeaterFromLocation(String location) {

		List<Heater> heaterLocation = new ArrayList<Heater>();

		for (Heater heater : heaters) {
			if (heater.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
				heaterLocation.add(heater);
			}
		}

		return heaterLocation;

	}

	@Override
	public void setTargetedTemperature(String targetedRoom, float temperature) {

		switch (targetedRoom) {
		case "livingroom":
			setTemperatureGoal_livingroom(temperature);
			break;

		case "bathroom":
			setTemperatureGoal_bathroom(temperature);
			break;

		case "kitchen":
			setTemperatureGoal_kitchen(temperature);
			break;

		case "bedroom":
			setTemperatureGoal_bedroom(temperature);
			break;

		}
	}

	public HashMap<Thermometer, String> getThermometerfromLocation(Thermometer[] thermometers) {

		HashMap<Thermometer, String> myMap = new HashMap<Thermometer, String>();

		for (Thermometer therm : thermometers) {
			myMap.put(therm, (String) therm.getPropertyValue(LOCATION_PROPERTY_NAME));
		}
		return myMap;
	}

	public void settingTemperatureOnLocation(HashMap<Thermometer, String> mymap) {

		for (Map.Entry<Thermometer, String> mapelement : mymap.entrySet()) {
			Thermometer temperature = mapelement.getKey();
			String location = mapelement.getValue();

			System.out.println("Current temperature in: " + location + " is " + (temperature.getTemperature() - ZERO_DEG));
			defineCoolerAndHeaterAction(location, convertTempToPower(location,temperature.getTemperature()));
		}

	}

	@Override
	public float getTargetedTemperature(String room) {

		switch (room) {

		case "livingroom":
			return (float) getTemperatureGoal_livingroom();

		case "bathroom":
			return (float) getTemperatureGoal_bathroom();

		case "kitchen":
			return (float) getTemperatureGoal_kitchen();

		case "bedroom":
			return (float) getTemperatureGoal_bedroom();

		default:
			return (float) ZERO_DEG;
		}
	}

	public double getTemperatureGoal_livingroom() {
		return temperatureGoal_livingroom;
	}

	public void setTemperatureGoal_livingroom(double temperatureGoal_livingroom) {
		this.temperatureGoal_livingroom = temperatureGoal_livingroom;
	}

	public double getTemperatureGoal_bathroom() {
		return temperatureGoal_bathroom;
	}

	public void setTemperatureGoal_bathroom(double temperatureGoal_bathroom) {
		this.temperatureGoal_bathroom = temperatureGoal_bathroom;
	}

	public double getTemperatureGoal_kitchen() {
		return temperatureGoal_kitchen;
	}

	public void setTemperatureGoal_kitchen(double temperatureGoal_kitchen) {
		this.temperatureGoal_kitchen = temperatureGoal_kitchen;
	}

	public double getTemperatureGoal_bedroom() {
		return temperatureGoal_bedroom;
	}

	public void setTemperatureGoal_bedroom(double temperatureGoal_bedroom) {
		this.temperatureGoal_bedroom = temperatureGoal_bedroom;
	}

}
