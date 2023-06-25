package tradegenerator;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class IMapStores {

    private static final String TRADES_MAP = "trades-map" ;
    private static final String LOOKUP_TABLE = "lookup-table" ;

    private static final String TRADES_MAPPING_SQL =  "CREATE OR REPLACE MAPPING " + "\"" + TRADES_MAP + "\" " +
            "(tradeId INTEGER, " +
            "tradeTime BIGINT, " +
            "symbol VARCHAR, " +
            "quantity INTEGER, " +
            "price INTEGER, " +
            "companyName VARCHAR)" +
            "TYPE IMap OPTIONS (" +
            "'keyFormat' = 'java'," +
            "'keyJavaClass' = 'java.lang.Integer'," +
            "'valueFormat' = 'compact'," +
            "'valueCompactTypeName' = 'tradegenerator.Trade')";


    public void doSQLMappings(HazelcastInstance hzClient){
        hzClient.getSql().execute(TRADES_MAPPING_SQL);

        System.out.println("Initialized SQL Mappings");
    }

    // symbol -> company name
    public void loadRefData(HazelcastInstance hzClient) {
        IMap<String, String> lookupTable = hzClient.getMap(LOOKUP_TABLE);
        lookupTable.put("AAPL", "Apple Inc. - Common Stock");
        lookupTable.put("GOOGL", "Alphabet Inc.");
        lookupTable.put("MSFT", "Microsoft Corporation");
    }
}
