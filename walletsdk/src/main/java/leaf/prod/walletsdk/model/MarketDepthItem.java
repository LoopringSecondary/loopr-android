package leaf.prod.walletsdk.model;

import lombok.Builder;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2019-04-01 2:44 PM
 * Cooperation: loopring.org 路印协议基金会
 */
@Data
@Builder
public class MarketDepthItem {

    private String[] depths;

    private double rate;
}
