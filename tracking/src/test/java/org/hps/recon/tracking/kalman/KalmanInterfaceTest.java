package org.hps.recon.tracking.kalman;

import java.io.File;

import junit.framework.TestCase;

import org.hps.conditions.database.DatabaseConditionsManager;
import org.hps.detector.svt.SvtDetectorSetup;
import org.hps.recon.tracking.RawTrackerHitFitterDriver;
import org.hps.recon.tracking.TrackingReconstructionPlots;
import org.lcsim.recon.tracking.digitization.sisim.config.RawTrackerHitSensorSetup;
import org.lcsim.recon.tracking.digitization.sisim.config.ReadoutCleanupDriver;
import org.lcsim.util.loop.LCIODriver;
import org.lcsim.util.loop.LCSimLoop;
import org.lcsim.util.test.TestUtil.TestOutputFile;

public class KalmanInterfaceTest extends TestCase {
    static final String testInput = "fullGBL.slcio";
    private final int nEvents = 1;
    static final String testOutput = "KalmanTest_" + testInput;
    static final String aidaOutput = "target/test-output/KalmanTestPlots.root";

    public void testKalman() throws Exception {
        File lcioInputFile = new File(testInput);
        File outputFile = new TestOutputFile(testOutput);

        final DatabaseConditionsManager manager = new DatabaseConditionsManager();
        manager.addConditionsListener(new SvtDetectorSetup());

        LCSimLoop loop2 = new LCSimLoop();
        loop2.setLCIORecordSource(lcioInputFile);

        RawTrackerHitSensorSetup rthss = new RawTrackerHitSensorSetup();
        String[] readoutColl = { "SVTRawTrackerHits" };
        rthss.setReadoutCollections(readoutColl);
        loop2.add(rthss);

        RawTrackerHitFitterDriver rthfd = new RawTrackerHitFitterDriver();
        rthfd.setFitAlgorithm("Pileup");
        rthfd.setUseTimestamps(false);
        rthfd.setUseTruthTime(false);
        rthfd.setSubtractTOF(true);
        rthfd.setSubtractTriggerTime(true);
        rthfd.setCorrectChanT0(true);
        rthfd.setCorrectTimeOffset(true);
        rthfd.setCorrectT0Shift(true);
        loop2.add(rthfd);

        org.hps.recon.tracking.DataTrackerHitDriver dthd = new org.hps.recon.tracking.DataTrackerHitDriver();
        dthd.setNeighborDeltaT(8.0);
        loop2.add(dthd);

        KalmanDriverHPS kdhps = new KalmanDriverHPS();
        kdhps.setOutputPlotsFilename(aidaOutput);
        loop2.add(kdhps);

        TrackingReconstructionPlots trp = new TrackingReconstructionPlots();
        trp.setOutputPlots(aidaOutput);
        trp.setTrackCollectionName(kdhps.getOutputSeedTrackCollectionName());
        loop2.add(trp);

        ReadoutCleanupDriver rcd = new ReadoutCleanupDriver();
        loop2.add(rcd);

        loop2.add(new LCIODriver(outputFile));

        loop2.loop(nEvents, null);
        loop2.dispose();
    }
}