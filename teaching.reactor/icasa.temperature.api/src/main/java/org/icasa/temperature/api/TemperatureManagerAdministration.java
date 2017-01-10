package org.icasa.temperature.api;

public interface TemperatureManagerAdministration {

    /**
     * This method is called every time a user think the temperature is too high
     * in a given room.
     * 
     * @param roomName
     *            the room where the temperature should be reconfigured
     */
    public void temperatureIsTooHigh(String roomName);
 
    /**
     * This method is called every time a user think the temperature is too high
     * in a given room.
     * 
     * @param roomName
     *            the room where the temperature should be reconfigured
     */
    public void temperatureIsTooLow(String roomName);
  
}
