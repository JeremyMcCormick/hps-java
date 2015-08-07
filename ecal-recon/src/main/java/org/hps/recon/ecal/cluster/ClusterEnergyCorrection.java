package org.hps.recon.ecal.cluster;

import org.lcsim.event.Cluster;
import org.lcsim.event.base.BaseCluster;

/**
 * This is the cluster energy correction requiring the particle id 
 * uncorrected cluster energy. This is not accurate for edge crystals
 * and should be used after cluster-track matching and after position
 * corrections.
 * 
 * @author Holly Vance <hvanc001@odu.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public final class ClusterEnergyCorrection {
        
    // Variables for electron energy corrections.
    //Updated with recent monte carlo --7AUG15 HS. 
    //Old values from 2014-2015 commented out to right.
    static final double ELECTRON_ENERGY_A = 0.01004;//-0.0027;
    static final double ELECTRON_ENERGY_B = -0.122;//-0.06;
    static final double ELECTRON_ENERGY_C = 0.9646;//0.95;
    
    // Variables for positron energy corrections.
    static final double POSITRON_ENERGY_A = 0.00711;//-0.0096;
    static final double POSITRON_ENERGY_B = -0.1154;//-0.042;
    static final double POSITRON_ENERGY_C = 0.9614;//0.94;
    
    // Variables for photon energy corrections.
    static final double PHOTON_ENERGY_A = 0.007595;//0.0015;
    static final double PHOTON_ENERGY_B = -0.09766;//-0.047;
    static final double PHOTON_ENERGY_C = 0.9512;//0.94;
          
    /**
     * Calculate the corrected energy for the cluster.
     * @param cluster The input cluster.
     * @return The corrected energy.
     */
    public static double calculateCorrectedEnergy(Cluster cluster) {
        double rawE = cluster.getEnergy();
        return computeCorrectedEnergy(cluster.getParticleId(), rawE);
    }
    
    /**
     * Calculate the corrected energy and set on the cluster.
     * @param cluster The input cluster.
     */
    public static void setCorrectedEnergy(BaseCluster cluster) {
        double correctedEnergy = calculateCorrectedEnergy(cluster);
        cluster.setEnergy(correctedEnergy);
    }
                            
    /**
     * Calculates energy correction based on cluster raw energy and particle type as per 
     * <a href="https://misportal.jlab.org/mis/physics/hps_notes/index.cfm?note_year=2014">HPS Note 2014-001</a>
     * @param pdg Particle id as per PDG
     * @param rawEnergy Raw Energy of the cluster (sum of hits with shared hit distribution)
     * @return Corrected Energy
     */    
    private static double computeCorrectedEnergy(int pdg, double rawEnergy) {
        switch(pdg) {
            case 11: 
                // electron             
                return computeCorrectedEnergy(rawEnergy, ELECTRON_ENERGY_A, ELECTRON_ENERGY_B, ELECTRON_ENERGY_C);
            case -11: 
                // positron
                return computeCorrectedEnergy(rawEnergy, POSITRON_ENERGY_A, POSITRON_ENERGY_B, POSITRON_ENERGY_C);
            case 22: 
                // photon
                return computeCorrectedEnergy(rawEnergy, PHOTON_ENERGY_A, PHOTON_ENERGY_B, PHOTON_ENERGY_C);
            default: 
                // unknown 
                return rawEnergy;
        }
    }
    
    /**
     * Calculates the energy correction to a cluster given the variables from the fit as per
     * <a href="https://misportal.jlab.org/mis/physics/hps_notes/index.cfm?note_year=2014">HPS Note 2014-001</a>
     * Note that this is correct as there is a typo in the formula print in the note. 
     * @param rawEnergy Raw energy of the cluster
     * @param A,B,C from fitting in note
     * @return Corrected Energy
     */   
    private static double computeCorrectedEnergy(double rawEnergy, double varA, double varB, double varC){
        double corrEnergy = rawEnergy / (varA / rawEnergy + varB / (Math.sqrt(rawEnergy)) + varC);
        return corrEnergy;
    }                   
}