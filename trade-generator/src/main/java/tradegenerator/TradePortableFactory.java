package tradegenerator;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class TradePortableFactory implements PortableFactory {

    public static final int ID = 1;
    @Override
    public Portable create(int classId) {
        if (Trade.ID == classId) {
            return new Trade();
        } else
            return null;
    }
}
