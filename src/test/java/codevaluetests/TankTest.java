package codevaluetests;

import codevalue.app.CodeValueApp;
import codevalue.services.TankDataManager;
import codevalue.system.Configuration;
import codevalue.utils.LoggerWrapper;
import codevalue.utils.SystemUtils;
import codevalue.utils.WsAddressConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = CodeValueApp.class)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TankTest {

    private final static LoggerWrapper logger = new LoggerWrapper(TankTest.class.getSimpleName());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TankDataManager tankDataManager;


    @Test
    public void sync_Test() throws InterruptedException {
        //unmark the sleep in sync method before testing it
        String testName = "sync_Test";
        LocalDateTime startTime = LocalDateTime.now(ZoneOffset.UTC);
//        AtomicReference<LocalDateTime> t1StartTime = new AtomicReference();
        Thread t1 = new Thread(() -> {
            logger.debugFormat(testName, "Thread 1: start get getCurrentCapacity/1");
            Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
//            t1StartTime.set(LocalDateTime.now(ZoneOffset.UTC));
            logger.debugFormat(testName, "Thread 1: done get getCurrentCapacity/1. result: %s", currentCapacity);
        });
//        AtomicReference<LocalDateTime> t2StartTime = new AtomicReference();
        Thread t2 = new Thread(() -> {
            logger.debugFormat(testName, "Thread 2: start get getCurrentCapacity/1");
            Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
//            t2StartTime.set(LocalDateTime.now(ZoneOffset.UTC));
            logger.debugFormat(testName, "Thread 2: done get getCurrentCapacity/1. result: %s", currentCapacity);
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC);
        Duration totalRunTime = Duration.between(startTime, endTime);
        //for each tank requesting we waiting 10 sec;
        Assert.assertTrue(totalRunTime.getSeconds() >= Duration.ofSeconds(10).multipliedBy(2).getSeconds());
    }


    @Test
    public void syncDifferenceTanks_Test() throws InterruptedException {
        //unmark the sleep in sync method before testing it
        String testName = "sync_Test";
        LocalDateTime startTime = LocalDateTime.now(ZoneOffset.UTC);
        Thread t1 = new Thread(() -> {
            logger.debugFormat(testName, "Thread 1: start get getCurrentCapacity/1");
            Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
            logger.debugFormat(testName, "Thread 1: done get getCurrentCapacity/1. result: %s", currentCapacity);
        });
        Thread t2 = new Thread(() -> {
            logger.debugFormat(testName, "Thread 2: start get getCurrentCapacity/2");
            Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/2", Double.class);
            logger.debugFormat(testName, "Thread 2: done get getCurrentCapacity/2. result: %s", currentCapacity);
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC);
        Duration totalRunTime = Duration.between(startTime, endTime);
        //for each tank requesting we waiting 10 sec;
        Assert.assertTrue(totalRunTime.getSeconds() <= Duration.ofSeconds(11).getSeconds());
    }

    @Test
    public void getMaxCapacity_Test() {
        Integer maxCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getMaxCapacity/1", Integer.class);
        Assert.assertEquals("maxTankCapacity not as expected", Configuration.maxTankCapacity, maxCapacity);
    }

    @Test
    public void getCurrentCapacityInitialize_Test() {
        Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
        Assert.assertEquals("currentCapacity not as expected", 0d, currentCapacity.doubleValue(), 0);
    }

    @Test
    public void addWater_Test() {
        Double waterToAdd = Configuration.maxTankCapacity - 1d;
        Boolean result = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "addWater/1/" + waterToAdd, Boolean.class);
        Assert.assertTrue("unable to add water to tank ", result);
        Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
        Assert.assertEquals("currentCapacity not as expected", waterToAdd, currentCapacity, 0.2);
    }

    @Test
    public void tryToAddWaterMoreThanCapacity_Test() {
        Double waterToAdd = Configuration.maxTankCapacity + 1d;
        Boolean result = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "addWater/1/" + waterToAdd, Boolean.class);
        Assert.assertFalse("tank added more than capacity", result);
        Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
        Assert.assertEquals("currentCapacity not as expected", 0, currentCapacity, 0);
    }


    @Test
    public void getCurrentCapacityAfterLeak_Test() {
        Configuration.leakageLiterAmountInMinute = Configuration.leakageLiterAmountInMinute * 60;
        Configuration.maxTankCapacity = 50;

        Double waterToAdd = 20d;
        restTemplate.getForObject(WsAddressConstants.tankFullUrl + "addWater/1/" + waterToAdd, Boolean.class);
        Duration sleepDuration = Duration.ofSeconds(5);

        for (int i = 1; i < 3; i++) {
            SystemUtils.sleep(sleepDuration);
            Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
            double leakageAmount = (double) Configuration.leakageLiterAmountInMinute / 60000 * sleepDuration.toMillis();
            Assert.assertEquals("currentCapacity not as expected", waterToAdd - leakageAmount * i, currentCapacity, 0.2);
        }
    }

    @Test
    public void getCurrentCapacityAfterLeak_Test1() {
        Double waterToAdd = 20d;
        restTemplate.getForObject(WsAddressConstants.tankFullUrl + "addWater/1/" + waterToAdd, Boolean.class);
        Duration sleepDuration = Duration.ofMinutes(1);
        SystemUtils.sleep(sleepDuration);
        Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
        double leakageAmount = (double) Configuration.leakageLiterAmountInMinute / 60000 * sleepDuration.toMillis();
        Assert.assertEquals("currentCapacity not as expected", waterToAdd - leakageAmount, currentCapacity, 0.2);
    }

    @Test
    public void getCurrentCapacityAfterAllTankAlreadyLeaked_Test() {
        Configuration.leakageLiterAmountInMinute = Configuration.leakageLiterAmountInMinute * 60;
        Configuration.maxTankCapacity = 50;

        Double waterToAdd = 10d;
        restTemplate.getForObject(WsAddressConstants.tankFullUrl + "addWater/1/" + waterToAdd, Boolean.class);
        SystemUtils.sleep(Duration.ofSeconds(15));
        Double currentCapacity = restTemplate.getForObject(WsAddressConstants.tankFullUrl + "getCurrentCapacity/1", Double.class);
        Assert.assertFalse("water quantity is negative", currentCapacity < 0);
    }

}
