package org.hps.conditions.ecal;

import junit.framework.TestCase;

import org.hps.conditions.database.DatabaseConditionsManager;
import org.hps.conditions.database.TableConstants;
//import org.hps.conditions.config.DevReadOnlyConfiguration;
import org.hps.conditions.ecal.EcalLed.EcalLedCollection;
import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;

/**
 * A very basic test to make sure ECAL LED information is 
 * readable from the conditions dev database.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class EcalLedTest extends TestCase {
          

    DatabaseConditionsManager conditionsManager;
	  public void setUp() {
	        conditionsManager = DatabaseConditionsManager.getInstance();
	        try {
	            conditionsManager.setDetector("HPS-Proposal2014-v7-2pt2", 2000);
	        } catch (ConditionsNotFoundException e) {
	            throw new RuntimeException(e);
	        }
	    }
	
    
    
    public void testEcalLed() {
        DatabaseConditionsManager manager = DatabaseConditionsManager.getInstance();
        EcalLedCollection collection = manager.getConditionsData(EcalLedCollection.class, TableConstants.ECAL_LEDS);
        for (EcalLed led : collection) {
        	
        	System.out.println(led);
            //System.out.println("ECAL LED info ...");
            //System.out.println("ecal_channel_id: " + led.getEcalChannelId());
            //System.out.println("crate: " + led.getCrateNumber());
            //System.out.println("number: " + led.getLedNumber());
            //System.out.println("time_delay: " + led.getTimeDelay());
            //System.out.println("amplitude_low: " + led.getAmplitudeLow());
            //System.out.println("amplitude_high: " + led.getAmplitudeHigh());
            //System.out.println();
        }
    }
}
