package demos.pipelines;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Util;
import com.hazelcast.jet.accumulator.LongAccumulator;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.datamodel.KeyedWindowResult;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;
// import eventlisteners.TradeListener;

import java.util.Map;


public class TradeStream {
    private static final String LATEST_TRADES_PER_SYMBOL = "trades" ;
    public static final String EVENT_MAP_NAME = "trades-map";
    public static final String TRADE_EVENT_MAP_NAME = "trade-events-map";
    private static final String LOOKUP_TABLE = "lookup-table" ;
    private static final String ENRICHED_TRADES_MAP = "enriched-trades-map" ;
    private static final long PRICE_DROP_TRESHOLD = 200;

    public static void main (String[] args) {

        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        Pipeline p = buildPipeline(hz);
        JetService jet = hz.getJet();

        hz.getJet().newJob(p).join();
    }

    private static Pipeline buildPipeline(HazelcastInstance hz) {

        IMap<String, String> lookupTable = hz.getMap(LOOKUP_TABLE);

        hz.getMap(TRADE_EVENT_MAP_NAME)
                        .addEntryListener(new TradeListener(), true);

        IMap<Integer, GenericRecord> tradeEventsMap = hz.getMap(TRADE_EVENT_MAP_NAME);
        IMap<Integer, GenericRecord> enrichedTradesMap = hz.getMap(ENRICHED_TRADES_MAP);

        Pipeline pipeline = Pipeline.create();

        // Read Trade events from source trades-map
        StreamStage<Map.Entry<Integer, GenericRecord>> tradeEvents = pipeline.readFrom(
                        Sources.<Integer, GenericRecord>mapJournal(
                                EVENT_MAP_NAME,
                                JournalInitialPosition.START_FROM_OLDEST))
                .withTimestamps(item -> item.getValue().getInt64("tradeTime"), 1000)
                .setName("trade events");

        // Pipeline 1: Write to console to show have got raw trade values
        tradeEvents.writeTo(Sinks.logger( event -> "\n\nNew Event key = " + event.getKey() +
                ", New tradeId = " + event.getValue().getInt32("tradeId") +
                ", New symbol = " + event.getValue().getString("symbol") +
                ", New quantity = " + event.getValue().getInt32("quantity") +
                ", New price = " + event.getValue().getInt32("price") +
                ", New tradeTime = " + event.getValue().getInt64("tradeTime")));

        // Also write to map to show map listeners still work
        tradeEvents.writeTo(Sinks.map(tradeEventsMap));

        // Pipeline 2: Enriching the trade data
        // For each trade (GenericRecord) - lookup in the lookupTable IMap for the map entry with that symbol
        // and the result of the lookup is merged with the item and logged to console and also written to a map */
        StreamStage<Tuple2<Integer, GenericRecord>> enrichedTrade =
                tradeEvents.mapUsingIMap(lookupTable,
                        trade->trade.getValue().getString("symbol"),
                        (trade, companyName) -> updatedTrade(trade, companyName))
                        .setName("enriched trades");

        enrichedTrade.writeTo(Sinks.logger( event -> "\n\nEnriched Trade: New Event key = " + event.getKey() +
                ", New tradeId = " + event.getValue().getInt32("tradeId") +
                ", New symbol = " + event.getValue().getString("symbol") +
                ", New quantity = " + event.getValue().getInt32("quantity") +
                ", New price = " + event.getValue().getInt32("price") +
                ", New tradeTime = " + event.getValue().getInt64("tradeTime") +
                ", New companyName = " + event.getValue().getString("companyName")));

        enrichedTrade.writeTo(Sinks.map(enrichedTradesMap));

        // Pipeline 3 Stateful processing - report if a price drops by more than 200
        // First parameter returns the object that holds the state.
        // Jet passes this object (new Long Accumulator instance previousPrice) along with each input item
        // (trade in this case) to second parameter functional mapFn, which can update the object's state.
        // Returns the difference if > 200 otherwise null
        StreamStage<Long> priceDifference = enrichedTrade.mapStateful(
                        LongAccumulator::new,
                        (previousPrice, currentTrade) -> {
                            Long difference = previousPrice.get() - currentTrade.f1().getInt32("price");
                            previousPrice.set(currentTrade.f1().getInt32("price"));

                            return (difference > PRICE_DROP_TRESHOLD) ? difference : null;
                        })
                        .setName("price difference");;

        priceDifference.writeTo(Sinks.logger(event -> "\n\nPrice difference = " + event));

        /*
         * Group the events by symbol. For each symbol, compute the average price over a 10s
         * tumbling window.
         *
         */



         StreamStage<KeyedWindowResult<String, Double>> averagePrices = enrichedTrade
                .groupingKey( entry -> entry.getValue().getString("symbol"))
                .window(WindowDefinition.tumbling(10000))
                .aggregate(AggregateOperations.averagingLong(item -> item.getValue().getInt32("price")))
                .setName("Average Price").peek();

        averagePrices.writeTo(Sinks.logger( event -> "\n\naverage price = " + event.toString()));

        return pipeline;
    }

    private static Tuple2<Integer, GenericRecord> updatedTrade(Map.Entry<Integer, GenericRecord> trade, String companyName) {

        GenericRecord modifiedGenericRecord = trade.getValue().newBuilderWithClone()
                .setString("companyName", companyName)
                .build();

        return Tuple2.tuple2(trade.getKey(), modifiedGenericRecord);
    }



}

