package tradegenerator;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class TradeGenerator {

    private static final String TRADES_MAP = "trades-map";

    public static void main(String[] args) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName("dev");
        clientConfig.getNetworkConfig().addAddress("127.0.0.1");

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IMapStores tradeMapStore = new IMapStores();
        tradeMapStore.doSQLMappings(client);
        tradeMapStore.loadRefData(client);

        IMap<Integer, Trade> tradesMap = client.getMap(TRADES_MAP);

        System.out.println("In TradeGenerator");

        //for (int i = 1; i < 1000; i++) {
        int i = 1;
        while (true) {

            Trade trade = Trade.fake(i++);

            tradesMap.put(trade.getTradeId(), trade);
            Thread.sleep(1000);
           // sleep(5000);
        }

       // HazelcastClient.shutdownAll();
    }
}
