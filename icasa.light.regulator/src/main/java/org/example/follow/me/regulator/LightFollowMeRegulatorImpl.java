package org.example.follow.me.regulator;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;


import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;
import org.example.follow.me.api.FollowMeConfiguration;

@Component
@Instantiate(name = "light.follow.me.regulator")
@Provides(specifications={FollowMeConfiguration.class})
@SuppressWarnings("rawtypes")
/**
 * Created by aygalinc on 28/10/16.
 */
public class LightFollowMeRegulatorImpl implements DeviceListener, FollowMeConfiguration{

	/**
	 * The maximum number of lights to turn on when a user enters the room :
	 **/
	private int maxLightsToTurnOnPerRoom = 1;
	
	/**
	 * The maximum power of light to turn on when a user enters into a room
	 */
	
	private double maximumEnergyConsumptionAllowedInARoom = 100.0d;
	

	@Requires(id="presenceSensors", optional=true)
	/** Field for presenceSensors dependency */
	private PresenceSensor[] presenceSensors;
	@Requires(id="binaryLights", optional=true)
	/** Field for binaryLights dependency */
	private BinaryLight[] binaryLights;
	@Requires(id="dimmerLights", optional=true)
	/** Field for dimmerLights dependency */
	private DimmerLight[] dimmerLights;

	/**
	 * The name of the LOCATION property
	 */
	public static final String LOCATION_PROPERTY_NAME = "Location";

	/**
	 * The name of the location for unknown value
	 */
	public static final String LOCATION_UNKNOWN = "unknown";

	/*------------------------------BINDING METHODS--------------------------*/
	@Bind(id="binaryLights")
	/** Bind Method for binaryLights dependency */
	public synchronized void bindBinaryLight(BinaryLight binaryLight, Map properties) {
		System.out.println("bind binary light " + binaryLight.getSerialNumber());
		binaryLight.addListener(this);
	}
	@Unbind(id="binaryLights")
	/** Unbind Method for binaryLights dependency */
	public synchronized void unbindBinaryLight(BinaryLight binaryLight, Map properties) {
		System.out.println("unbind binary light " + binaryLight.getSerialNumber());
		binaryLight.removeListener(this);
	}
	@Bind(id="presenceSensors")
	/** Bind Method for presenceSensors dependency */
	public synchronized void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		System.out.println("bind presence sensor " + presenceSensor.getSerialNumber());
		presenceSensor.addListener(this);
	}
	@Unbind(id="presenceSensors")
	/** Unbind Method for presenceSensors dependency */
	public synchronized void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
		System.out.println("Unbind presence sensor " + presenceSensor.getSerialNumber());
		presenceSensor.removeListener(this);
	}
	@Bind(id="dimmerLights")
	/** Bind Method for dimmerLights dependency */
	public void bindDimmerLight(DimmerLight dimmerLight, Map properties) {
		System.out.println("bind dimmer light" + dimmerLight.getSerialNumber());
		dimmerLight.addListener(this);
	}
	@Unbind(id="dimmerLights")
	/** Unbind Method for dimmerLights dependency */
	public void unbindDimmerLight(DimmerLight dimmerLight, Map properties) {
		System.out.println("Unbind dimmer light " + dimmerLight.getSerialNumber());
		dimmerLight.removeListener(this);
	}

	/*--------------------------------------COMPONENT LIFECYCLE--------------*/
	/** Component Lifecycle Method */
	@Invalidate
	public synchronized void stop() {
		System.out.println("Component is stopping...");
		for (PresenceSensor sensor : presenceSensors) {
			sensor.removeListener(this);
		}
		for (BinaryLight binaryLight : binaryLights) {
			binaryLight.removeListener(this);
		}
	}

	/** Component Lifecycle Method */
	@Validate
	public void start() {
		System.out.println("Component is starting...");
	}

	@Override
	public void deviceAdded(GenericDevice arg0) {
	}

	@Override
	public void deviceEvent(GenericDevice arg0, Object arg1) {
	}

	@Override
	public void devicePropertyAdded(GenericDevice arg0, String arg1) {

	}

	@Override
	public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
	}
	
	@Override
	public void deviceRemoved(GenericDevice arg0) {
	}

	/**
	 * PRESENCE This method is part of the DeviceListener interface and is
	 * called when a subscribed device property is modified.
	 * 
	 * @param device
	 *            is the device whose property has been modified.
	 * @param propertyName
	 *            is the name of the modified property.
	 * @param oldValue
	 *            is the old value of the property
	 * @param newValue
	 *            is the new value of the property
	 */
	public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object newValue) {

		
		//Activation of the light power
		//energyToLightConverter();
		
		/*
		 * If the device is a presence sensor
		 */
		if (device instanceof PresenceSensor) {

			PresenceSensor changingSensor = (PresenceSensor) device;

			/*
			 * Sensivity property of the presence sensor
			 */
			if (propertyName.equals(PresenceSensor.PRESENCE_SENSOR_SENSED_PRESENCE)) {

				if (!changingSensor.getPropertyValue(PresenceSensor.LOCATION_PROPERTY_NAME)
						.equals(PresenceSensor.LOCATION_UNKNOWN)) {
					if (changingSensor.getSensedPresence())
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME),
								maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME), 0);
				}
			}

			/*
			 * Location property of the presence sensor
			 */
			else if (propertyName.equals(PresenceSensor.LOCATION_PROPERTY_NAME)) {

				/* if the location is known */
				if (!changingSensor.getPropertyValue(PresenceSensor.LOCATION_PROPERTY_NAME)
						.equals(PresenceSensor.LOCATION_UNKNOWN)) {
					/* if a presence is sensed */
					if (changingSensor.getSensedPresence())
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME),
								maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) changingSensor.getPropertyValue(LOCATION_PROPERTY_NAME), 0);
				}
				/*
				 * turn off lights in the previous location is there is no
				 * presence sensor
				 */
				
				if (getPresenceSensorFromLocation((String) oldValue).isEmpty())
				
					if (getPresenceSensorFromLocation((String) oldValue).isEmpty()){
					setStateLights((String) oldValue, 0);
				}
			}

		}

		/*
		 * If the device is a Binary Light
		 */
		else if (device instanceof BinaryLight) {

			BinaryLight lightBulb = (BinaryLight) device;

			/* check if the change is related to the light location */
			if (propertyName.equals(BinaryLight.LOCATION_PROPERTY_NAME)) {

				/* if the new location is unknown */
				if (newValue.equals(BinaryLight.LOCATION_UNKNOWN)) {

					lightBulb.turnOff();

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getPresenceSensorFromLocation((String) oldValue)))
						setStateLights((String) oldValue, maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) oldValue, 0);
				}

				else {

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getPresenceSensorFromLocation((String) oldValue)))
						setStateLights((String) oldValue, maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) oldValue, 0);

					/* Refresh in the new location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
									getPresenceSensorFromLocation((String) newValue)))
						setStateLights((String) newValue, maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) newValue, 0);

				}

			}
		} else if (device instanceof DimmerLight) {

			DimmerLight lightBulb = (DimmerLight) device;

			/* check if the change is related to the light location */
			if (propertyName.equals(DimmerLight.LOCATION_PROPERTY_NAME)) {

				/* if the new location is unknown */
				if (newValue.equals(DimmerLight.LOCATION_UNKNOWN)) {

					lightBulb.setPowerLevel(0.0);

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getPresenceSensorFromLocation((String) oldValue)))
						setStateLights((String) oldValue, maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) oldValue, 0);
				}

				else {

					/* Refresh in the old location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getPresenceSensorFromLocation((String) oldValue)))
						setStateLights((String) oldValue, maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) oldValue, 0);

					/* Refresh in the new location */
					/* If there is a presence sensed */
					if (isThereAPresenceSensed(
							getPresenceSensorFromLocation((String) newValue)))
						setStateLights((String) newValue, maximumEnergyConsumptionAllowedInARoom);
					else
						setStateLights((String) newValue, 0);

				}

			}
		}

	}

	private boolean isThereAPresenceSensed(List<PresenceSensor> presenceSensors) {
		for (PresenceSensor p : presenceSensors) {
			if (p.getSensedPresence())
				return true;
		}
		return false;
	}

	/*----------------------------------LAMPS MANAGER ---------------------------*/
	private void setStateLights(String location, double maxPowerOfLightedLamps) {

		double restPower;
		restPower = setStateBinaryLightFromLocation(location, maxPowerOfLightedLamps);
		System.out.println("Check" + restPower);
		setStateDimmerLightFromLocation(location, maxPowerOfLightedLamps - restPower);

	}

	private double setStateBinaryLightFromLocation(String location, Double maxPowerOfLightedLamps) {

		/* list of binary lights in the location */
		List<BinaryLight> sameLocationLigths = getBinaryLightFromLocation(location);
		int i;

		
	    double[] result = greadySubSetClosestSum(maxPowerOfLightedLamps, powerBinaryLightInArray(sameLocationLigths));
	    double restPower = sum(result);

		for (BinaryLight binaryLight : sameLocationLigths) {		
			
			boolean isOn = false;
			
			for(i=0;i<result.length;i++){
				
				if(binaryLight.getMaxPowerLevel() == result[i]){
					if(!isOn)
					{
					binaryLight.turnOn();
					result[i] = result[i]-binaryLight.getMaxPowerLevel();
					isOn = true;
					}
				}
				else{
					if(!isOn)
						binaryLight.turnOff();
					else{}
				}
				
			}
			
			/*
			 * switch them on/off depending on the number of lighted lamps
			 * and their power status
			 */
			/*if (numberOfLightedLamps < maxNumberOfLightedLamps) {
				if (!binaryLight.getPowerStatus()) {
					binaryLight.turnOn();
					numberOfLightedLamps++;
				}
			} else if (numberOfLightedLamps > maxNumberOfLightedLamps) {
				if (binaryLight.getPowerStatus()) {
					binaryLight.turnOff();
					numberOfLightedLamps--;
				}
			}*/
		}
		
		System.out.println("Check" + sum(result));

		return restPower;
	}

	private double setStateDimmerLightFromLocation(String location, double powerRest) {

		/* list of binary lights in the location */
		List<DimmerLight> sameLocationLigths = getDimmerLightFromLocation(location);

		
		for (DimmerLight dimmerLight : sameLocationLigths) {
			
			if(powerRest >= 1.0)
			{
				while(powerRest >= 1){
					dimmerLight.setPowerLevel(1.0);
					powerRest--;
				}
			}
			else if(powerRest > 0){
				dimmerLight.setPowerLevel(powerRest);
			}
			else
				dimmerLight.setPowerLevel(0.0);
			/*
			 * switch them on/off depending on the number of lighted lamps
			 * and their power status
			 */
			/*if (numberOfLightedDimmerLamps < maxNumberOfLightedLamps) {
				if (dimmerLight.getPowerLevel() == 0.0) {
					dimmerLight.setPowerLevel(1.0);
					numberOfLightedDimmerLamps++;
				}
			} else if (numberOfLightedDimmerLamps > maxNumberOfLightedLamps) {
				if (dimmerLight.getPowerLevel() > 0.0) {
					dimmerLight.setPowerLevel(0.0);
					numberOfLightedDimmerLamps--;
				}
			}*/
		}
		return powerRest;
	}

	/*private synchronized int getNumberOfLightedLamps(List<BinaryLight> listOfBinaryLights) {

		int counter = 0;

		for (BinaryLight binaryLight : listOfBinaryLights) {
			if (binaryLight.getPowerStatus())
				counter++;
		}

		return counter;
	}*/

	/*private synchronized int getNumberOfLightedDimmerLamps(List<DimmerLight> listOfDimmerLights) {

		int counter = 0;

		for (DimmerLight dimmerLight : listOfDimmerLights) {
			if (dimmerLight.getPowerLevel() > 0)
				counter++;
		}

		return counter;
	}*/
	
	/*----------------Energy manager -------------------------*/
	
	private void energyToLightConverter()
	{
		int nboflight;
		 nboflight = (int) maximumEnergyConsumptionAllowedInARoom/100;
		 setMaximumNumberOfLightsToTurnOn(nboflight);
	}
	
	/*-----------------Get the max power of all binary light in the room -------------*/ 
	
	private double[] powerBinaryLightInArray(List<BinaryLight> sameLocationLigths)
	{
		double[] powerlightcontents = new double[15];
		int i=0;
		
		for(BinaryLight binarylight : sameLocationLigths){
			powerlightcontents[i] = binarylight.getMaxPowerLevel();
			i++;
		}
		return powerlightcontents;
	}
	

	/*--- Getting the location of a device  ---*/
	
	private synchronized List<BinaryLight> getBinaryLightFromLocation(
		    String location) {
		  List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
		  for (BinaryLight binLight : binaryLights) {
		    if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(
		        location)) {
		      binaryLightsLocation.add(binLight);
		    }
		  }
		  return binaryLightsLocation;
		}
	
	private synchronized List<DimmerLight> getDimmerLightFromLocation(String location){
		List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();
		
		for(DimmerLight dimlight : dimmerLights)
		{
			if(dimlight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)){
				dimmerLightsLocation.add(dimlight);
			}
		}
		
		return dimmerLightsLocation;
	}
	
	private synchronized List<PresenceSensor> getPresenceSensorFromLocation(String location){
		List<PresenceSensor> presenceSensorsLocation = new ArrayList<PresenceSensor>();
		
		for(PresenceSensor presence : presenceSensors)
		{
			if(presence.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)){
				presenceSensorsLocation.add(presence);
			}
		}
		
		return presenceSensorsLocation;
		
	}
	
	/*---------------------------------Perfect Algo -------------------------*/
	
    public static double[] greadySubSetClosestSum(final double maximalSum, final double[] items) {
        
        // the current best results :
        double bestSum = 0.0d;
        double[] bestCombination = new double[0];
 
        /*
         * Generate all the possible combinations. There are 2^N possibilities
         * that can therefore be represented by a bitset.
         * The use of bitset is done to reduce the number of line of codes.
         * The solution is thus far from being optimized.
         */
        for (int i = 0; i < Math.pow(2, items.length); i++) {
            // Get the current combination 
            double[] currentCombination = multiplyByBitset(convertToBitSet(i), items);
            double currentSum = sum(currentCombination);
 
            // if we have the best result possible
            if (currentSum == maximalSum) {
                // return it
                return currentCombination;
            }
 
            // if the current result is better than the previous best result
            if ((currentSum <= maximalSum) && (currentSum > bestSum)) {
                // store it
                bestSum = currentSum;
                bestCombination = currentCombination;
            }
        }
 
        return bestCombination;
    }
 
    /**
     * Sum of the given variables or array.
     * 
     * @param variables
     *            the variables to be summed.
     * @return the sum of the variables.
     */
    private static double sum(double[] variables) {
        double sum = 0;
        for (int i=0;i<variables.length;i++) {
            sum += variables[i];
        }
        return sum;
    }
 
    /**
     * Convert a number into BitSet.
     * This could be obtained directly in JAVA7 (iCASA is not compatible)
     * 
     * @param number
     *            the number to convert
     * @return the resulting bit set
     */
    private static BitSet convertToBitSet(long number) {
        BitSet bits = new BitSet();
        int index = 0;
        while (number != 0L) {
            if ((number % 2L) != 0) {
                bits.set(index);
            }
            ++index;
            number = number >>> 1;
        }
        return bits;
    }
 
    /**
     * Multiply an array by a bitset
     * 
     * @param bitset
     *            the BitSet
     * @param array
     *            the array
     * @return the resulting array
     */
    private static double[] multiplyByBitset(BitSet bitset, double[] array) {
        assert (bitset.length() == array.length) : "array and bitset must have the same size";
 
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = bitset.get(i) ? array[i] : 0;
        }
        return result;
    }
    
	
	/*----------------------------------FollowMe Configuration methods ------*/

	/**
	 * Gets the maximum number of lights to turn on each time an user is
	 * entering a room.
	 * 
	 * @return the maximum number of lights to turn on
	 */
	public int getMaximumNumberOfLightsToTurnOn() {
		
		return this.maxLightsToTurnOnPerRoom;
	}

	/**
	 * Sets the maximum number of lights to turn on each time an user is
	 * entering a room.
	 * 
	 * @param maximumNumberOfLightsToTurnOn
	 *            the new maximum number of lights to turn on
	 */
	public void setMaximumNumberOfLightsToTurnOn(int maximumNumberOfLightsToTurnOn) {
		this.maxLightsToTurnOnPerRoom = maximumNumberOfLightsToTurnOn;
	}
	

	/***
	 * Get the power AllowedEnergyInARoom	
	 * @return
	 */
	
	public double getMaximumAllowedEnergyInRoom() {

		return this.maximumEnergyConsumptionAllowedInARoom; 
	}
	
	
	/**
	 * Set the power AllowedEnergyInARoom
	 * 
	 * 	
	 */
	public void setMaximumAllowedEnergyInRoom(double maximumEnergy) {
		this.maximumEnergyConsumptionAllowedInARoom = maximumEnergy;
	}

}
