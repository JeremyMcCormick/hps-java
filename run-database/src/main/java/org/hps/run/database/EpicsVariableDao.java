package org.hps.run.database;

import java.util.List;

/**
 * Database interface for EPICS variables.
 * 
 * @author Jeremy McCormick, SLAC
 */
interface EpicsVariableDao {

    /**
     * Get the full list of EPICs variables.
     * 
     * @return the full list of EPICS variables
     */
    List<EpicsVariable> getEpicsVariables();
    
    /**
     * Get a list of EPICs variables by type.
     * 
     * @param variableType the EPICS variable type
     * @return the list of variables
     */
    List<EpicsVariable> getEpicsVariables(EpicsType variableType);
}
