package org.icasa.temperature.command;

import java.rmi.UnexpectedException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.icasa.temperature.api.TemperatureManagerAdministration;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;

@Component
@Instantiate(name = "temperature.administration.command")
@CommandProvider(namespace = "temperature.provider")
public class TemperatureCommandImpl {

	@Requires
	private TemperatureManagerAdministration temperatureManagerAdministration;

	/**
	 * Command implementation to express that the temperature is too high in the
	 * given room
	 *
	 * @param room
	 *            the given room
	 */

	@Command
	public void tempTooHigh(String room) {
		
		 try
	      {
		    	if(room.equals("bathroom") | room.equals("bedroom") | room.equals("livingroom") | room.equals("kitchen"))
		    	   temperatureManagerAdministration.temperatureIsTooHigh(room);
		    		
		    	else{
		    		throw new UnexpectedException("Room UNKWNOWN");
		    		}
	      }
	      catch(UnexpectedException e){
	      	e.printStackTrace();
	      }
	}


	@Command
	public void tempTooLow(String room) {
		 try
	      {
		    	if(room.equals("bathroom") | room.equals("bedroom") | room.equals("livingroom") | room.equals("kitchen"))
		    	   temperatureManagerAdministration.temperatureIsTooLow(room);
		    		
		    	else{
		    		throw new UnexpectedException("Room UNKWNOWN");
		    		}
	      }
	      catch(UnexpectedException e){
	      	e.printStackTrace();
	      }
		
	}
}
