package tradegenerator;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * We use java.io.{@link java.io.Serializable} here for the sake of simplicity.
 * In production, Hazelcast Custom Serialization should be used.
 */
public class Trade implements Portable {

    private int tradeId;
    private long tradeTime;
    private String symbol;
    private int quantity;
    private int price; // in cents
    private String companyName;

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId=" + tradeId +
                ", tradeTime=" + tradeTime +
                ", symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", companyName='" + companyName + '\'' +
                '}';
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getTradeId() {
        return tradeId;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }
/* public Trade(long time, String ticker, int quantity, int price) {
        this.time = time;
        this.symbol = ticker;
        this.quantity = quantity;
        this.price = price;
    } */

    /**
     * Timestamp for the trade in UNIX time
     */
    public long getTime() {
        return tradeTime;
    }

    /**
     * The symbol
     */
    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * The price in cents
     */
    public int getPrice() {
        return price;
    }

    public void setTradeTime(long tradeTime) {
        this.tradeTime = tradeTime;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    private static final List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT");
    public static Trade fake(int tradeId){
        Trade result = new Trade();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // Generate random integer between 0 and 500 for customer ID
        // and random decimal < 100 for order price
        result.setTradeId(tradeId);
        result.setSymbol(symbols.get(rnd.nextInt(symbols.size())));
        result.setTradeTime(System.currentTimeMillis());
        result.setQuantity(100);
        result.setPrice(rnd.nextInt(5000));
        System.out.println(result);
        //LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5)); // sleep for 1 second
        return result;
    }
    public static int ID = 1;
    @Override
    public int getFactoryId() {
        return TradePortableFactory.ID;
    }

    @Override
    public int getClassId() {
        return ID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt( "tradeId", tradeId);
        portableWriter.writeLong("tradeTime", tradeTime);
        portableWriter.writeString("symbol", symbol);
        portableWriter.writeInt( "quantity", quantity);
        portableWriter.writeInt( "price", price);
        portableWriter.writeString("companyName", companyName);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.tradeId = portableReader.readInt("tradeId");
        this.tradeTime = portableReader.readLong("tradeTime");
        this.symbol = portableReader.readString("symbol");
        this.quantity = portableReader.readInt("quantity");
        this.price = portableReader.readInt("price");
        this.companyName = portableReader.readString("companyName");
    }
}