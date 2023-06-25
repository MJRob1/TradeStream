package demos.pipelines;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;


// public class TradeListener implements EntryListener<String, Trade> {
public class TradeListener implements EntryAddedListener<String, GenericRecord>, EntryUpdatedListener<String, GenericRecord> {

    @Override
    public void entryAdded(EntryEvent<String, GenericRecord> event) {
// Print console message when map entry is added, listing stock symbol and price.
       System.out.println("TradeListener: New symbol " + event.getValue().getString("symbol") + ", price: " + event.getValue().getInt32("price"));
        //System.out.println("TradeListener: New symbol and price" );

    }

    @Override
    public void entryUpdated(EntryEvent<String, GenericRecord> event) {
// Print console message when map entry is updated, listing stock symbol and price.
        System.out.println("TradeListener: Symbol updated " + event.getValue().getString("symbol") + ", price: " + event.getValue().getInt32("price"));
        //System.out.println("TradeListener: Symbol updated with new price");

    }

   /* @Override
    public void entryEvicted(EntryEvent<String, Trade> event) {

    }

    @Override
    public void entryExpired(EntryEvent<String, Trade> event) {

    }

    @Override
    public void entryRemoved(EntryEvent<String, Trade> event) {

    }


    @Override
    public void mapCleared(MapEvent event) {

    }

    @Override
    public void mapEvicted(MapEvent event) {

    }  */
}
