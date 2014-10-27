package org.hps.conditions.svt;

import org.lcsim.conditions.ConditionsConverter;
import org.lcsim.conditions.ConditionsManager;
import org.hps.conditions.DatabaseConditionsManager;
import org.hps.conditions.TableMetaData;
import org.hps.conditions.svt.SvtBadChannel.SvtBadChannelCollection;
import org.hps.conditions.svt.SvtCalibration.SvtCalibrationCollection;
import org.hps.conditions.svt.SvtGain.SvtGainCollection;
import org.hps.conditions.svt.SvtShapeFitParameters.SvtShapeFitParametersCollection;
import org.hps.conditions.svt.SvtT0Shift.SvtT0ShiftCollection;


/**
 * Abstract class providing some of the common methods used in creating
 * SVT conditions objects from the database.
 * 
 * @author Omar Moreno <omoreno1@ucsc.edu>
 *
 * @param <T extends AbstractSvtConditions>  SVT conditions object type
 */
public abstract class AbstractSvtConditionsConverter<T extends AbstractSvtConditions> implements ConditionsConverter<T> {
	
	protected T conditions;
	
    /**
     * Create and return the SVT conditions object.
     * @param manager The current conditions manager.
     * @param name The conditions key, which is ignored for now.
     */
	public T getData(ConditionsManager manager, String name){
	
    	DatabaseConditionsManager dbConditionsManager = (DatabaseConditionsManager) manager;
    
    	// Get the SVT calibrations (baseline, noise) from the conditions database
        SvtCalibrationCollection calibrations = this.getCollection(SvtCalibrationCollection.class, dbConditionsManager);
        for (SvtCalibration calibration : calibrations.getObjects()) {
            AbstractSvtChannel channel = conditions.getChannelMap().findChannel(calibration.getChannelID());
            conditions.getChannelConstants(channel).setCalibration(calibration);
        }

        // Get the Channel pulse fit parameters from the conditions database
        SvtShapeFitParametersCollection shapeFitParametersCollection 
        	= this.getCollection(SvtShapeFitParametersCollection.class, dbConditionsManager);
        for (SvtShapeFitParameters shapeFitParameters : shapeFitParametersCollection.getObjects()) {
            AbstractSvtChannel channel = conditions.getChannelMap().findChannel(shapeFitParameters.getChannelID());
            conditions.getChannelConstants(channel).setShapeFitParameters(shapeFitParameters);
        }
	
        // Get the bad channels from the conditions database.  If there aren't any bad channels, 
        // notify the user and move on.
        try { 
        	SvtBadChannelCollection badChannels = this.getCollection(SvtBadChannelCollection.class, dbConditionsManager);
        	for (SvtBadChannel badChannel : badChannels.getObjects()) {
        		AbstractSvtChannel channel = conditions.getChannelMap().findChannel(badChannel.getChannelId());
        		conditions.getChannelConstants(channel).setBadChannel(true);
        	}
        } catch (RuntimeException e) { 
        	System.out.println("[ " + conditions.getClass().getSimpleName() + "]: A set of bad channels were not found!");
        }

        // Get the gains and offsets from the conditions database
        SvtGainCollection channelGains = this.getCollection(SvtGainCollection.class, dbConditionsManager);
        for (SvtGain channelGain : channelGains.getObjects()) {
            int channelId = channelGain.getChannelID();
            AbstractSvtChannel channel = conditions.getChannelMap().findChannel(channelId);
            conditions.getChannelConstants(channel).setGain(channelGain);
        }

        // Get the collection of T0 shifts from the conditions database
        SvtT0ShiftCollection t0Shifts = this.getCollection(SvtT0ShiftCollection.class, dbConditionsManager);
        conditions.setT0Shifts(t0Shifts);

        return conditions;
	}

	/**
	 * Get a given collection of the given type from the conditions database.
	 * 
	 * @param type Class type
	 * @param dbConditionsManager The database conditions manager
	 * @return A collection of objects of the given type from the conditions database
	 */
	protected <U> U getCollection(Class<U> type, DatabaseConditionsManager dbConditionsManager){
		
		// Get the table name from the database configuration
		TableMetaData metaData = dbConditionsManager.findTableMetaData(type);
		if(metaData == null) 
			throw new RuntimeException("Table name data for condition of type " + type.getSimpleName() + " was not found.");
		String tableName = metaData.getTableName();

		// FIXME: This should be changed to catch a conditions record not found exception instead of 
		// 		  a runtime exception.
		U conditionsCollection = dbConditionsManager.getCachedConditions(type, tableName).getCachedData(); 
		return conditionsCollection; 
	}
}