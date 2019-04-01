//package leaf.prod.app;
//
//import android.content.Context;
//import android.support.test.InstrumentationRegistry;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import leaf.prod.app.manager.GasDataManager;
//
//import static org.junit.Assert.assertNotNull;
//
//public class GasDataManagerTest {
//
//    Context appContext;
//
//    GasDataManager gasDataManager;
//
//    @Before
//    public void setup() {
//        appContext = InstrumentationRegistry.getTargetContext();
//        gasDataManager = GasDataManager.getInstance(appContext);
//    }
//
//    @Test
//    public void testGetRecommendGasPriceInGWei() {
//        assertNotNull(gasDataManager.getRecommendGasPriceInGWei());
//    }
//
//    @Test
//    public void testETHTransfer() {
//        assertEquals(gasDataManager.getGasLimitByType("eth_transfer")
//                .toString(), "21273");
//    }
//
//    @Test
//    public void testTokenTransfer() {
//        assertEquals(gasDataManager.getGasLimitByType("token_transfer")
//                .toString(), "100273");
//    }
//
//    @Test
//    public void testApprove() {
//        assertEquals(gasDataManager.getGasLimitByType("approve")
//                .toString(), "100273");
//    }
//
//    @Test
//    public void testWithdraw() {
//        assertEquals(gasDataManager.getGasLimitByType("withdraw")
//                .toString(), "100273");
//    }
//
//    @Test
//    public void testDeposit() {
//        assertEquals(gasDataManager.getGasLimitByType("deposit")
//                .toString(), "100273");
//    }
//
//    @Test
//    public void testCancelOrder() {
//        assertEquals(gasDataManager.getGasLimitByType("cancelOrder")
//                .toString(), "150273");
//    }
//
//    @Test
//    public void testCancelAllOrders() {
//        assertEquals(gasDataManager.getGasLimitByType("cancelAllOrders")
//                .toString(), "100273");
//    }
//
//    @Test
//    public void testCancelOrderByTokenPair() {
//        assertEquals(gasDataManager.getGasLimitByType("cancelOrderByTokenPair")
//                .toString(), "100273");
//    }
//
//    @Test
//    public void testSubmitRing() {
//        assertEquals(gasDataManager.getGasLimitByType("submitRing")
//                .toString(), "400000");
//    }
//
//    @Test
//    public void testLrcFee() {
//        assertEquals(gasDataManager.getGasLimitByType("lrcFee")
//                .toString(), "400000");
//    }
//}
