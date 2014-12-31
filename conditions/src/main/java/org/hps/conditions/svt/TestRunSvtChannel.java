package org.hps.conditions.svt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hps.conditions.database.Converter;
import org.hps.conditions.database.Field;
import org.hps.conditions.database.MultipleCollectionsAction;
import org.hps.conditions.database.Table;
import org.hps.util.Pair;

@Table(names = {"test_run_svt_channels"})
@Converter(multipleCollectionsAction = MultipleCollectionsAction.ERROR)
public final class TestRunSvtChannel extends AbstractSvtChannel {

    public static class TestRunSvtChannelCollection extends AbstractSvtChannel.AbstractSvtChannelCollection<TestRunSvtChannel> {

        @Override
        public Collection<TestRunSvtChannel> find(Pair<Integer, Integer> pair) {
            List<TestRunSvtChannel> channels = new ArrayList<TestRunSvtChannel>();
            int fpga = pair.getFirstElement();
            int hybrid = pair.getSecondElement();
            for (TestRunSvtChannel channel : this) {
                if (channel.getFpgaID() == fpga && channel.getHybridID() == hybrid) {
                    channels.add(channel);
                }
            }
            return channels;
        }
    }

    /**
     * Get the FPGA ID.
     * 
     * @return The FPGA ID
     */
    @Field(names = {"fpga"})
    public int getFpgaID() {
        return getFieldValue("fpga");
    }

    /**
     * Get the hybrid ID.
     * 
     * @return The hybrid ID.
     */
    @Field(names = {"hybrid"})
    public int getHybridID() {
        return getFieldValue("hybrid");
    }

    /**
     * Implementation of equals.
     * @return True if the object equals this one; false if not.
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof TestRunSvtChannel))
            return false;
        if (o == this)
            return true;
        TestRunSvtChannel channel = (TestRunSvtChannel) o;
        return getChannelID() == channel.getChannelID() && getFpgaID() == channel.getFpgaID() && getHybridID() == channel.getHybridID() && getChannel() == channel.getChannel();
    }
}
