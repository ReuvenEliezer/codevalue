package codevalue.utils;

import codevalue.services.TankDataManagerImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class SystemUtils {

    private static LoggerWrapper logger = new LoggerWrapper(TankDataManagerImpl.class.getSimpleName());


    public static LocalDateTime getUtcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static double round(double value, int places) {

        String methodName = "round";

        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd;
        try {
            bd = new BigDecimal(value);
        } catch (Exception ex) {
            logger.errorFormat(methodName, "cause exception: %s", ex);
            return 0.0;
        }

        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
