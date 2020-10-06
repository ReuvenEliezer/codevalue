package codevalue.services;

import codevalue.entities.Tank;
import codevalue.system.Configuration;
import codevalue.utils.LoggerWrapper;
import codevalue.utils.SystemUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class TankDataManagerImpl implements TankDataManager {
    private static LoggerWrapper logger = new LoggerWrapper(TankDataManagerImpl.class.getSimpleName());
    private static final Map<Integer, Object> tankToLockMap = new HashMap<>(Configuration.totalTanks);
    private HashMap<Integer, Tank> tanks = new HashMap<>(Configuration.totalTanks);

    @PostConstruct
    private void init() {
        String methodName = "init";
        int totalTanks = Configuration.totalTanks;
        for (int tankId = 0; tankId < totalTanks; tankId++) {
            tankToLockMap.put(tankId, new Object());
            Tank tank = new Tank(tankId);
            logger.debugFormat(methodName, "create tank %s", tank.toString());
            tanks.put(tankId, tank);
        }
    }

    @Override
    public Boolean addWater(int tankId, double waterQuantity) {
        String methodName = "addWater";
        if (!isExist(tankId))
            return null;
        synchronized (tankToLockMap.get(tankId)) {
            Double currentCapacity = getCurrentCapacity(tankId);
            double total = currentCapacity + waterQuantity;
            if (total > Configuration.maxTankCapacity) {
                logger.debugFormat(methodName, "currentCapacity of tank %s is %s, cannot be apply %s because of total %s is more more than %s", tankId, currentCapacity, waterQuantity, total, Configuration.maxTankCapacity);
                return false;
            }
            Tank tank = tanks.get(tankId);
            tank.setCurrentCapacity(total);
            tank.setLastUpdated(SystemUtils.getUtcNow());
            logger.debugFormat(methodName, "add %s water to tank %s. total capacity now is %s", waterQuantity, tankId, total);
            return true;
        }
    }

    private boolean isExist(int tankId) {
        String methodName = "isExist";
        Object lock = tankToLockMap.get(tankId);
        if (lock == null) {
            logger.debugFormat(methodName, "tankId %s not found", tankId);
            return false;
        }
        return true;
    }

    @Override
    public Double getCurrentCapacity(int tankId) {
        String methodName = "getCurrentCapacity";
        logger.debugFormat(methodName, "for tank %s", tankId);
        if (!isExist(tankId)) return null;

        synchronized (tankToLockMap.get(tankId)) {
            logger.debugFormat(methodName, "acquired lock for tank %s", tankId);

//       TODO remove it - only for sync tests sync_Test, syncDifferenceTanks_Test
//            SystemUtils.sleep(Duration.ofSeconds(10));

            Tank tank = tanks.get(tankId);
            LocalDateTime lastUpdated = tank.getLastUpdated();
            if (lastUpdated == null) {
                logger.debugFormat(methodName, "tank %s is empty", tankId);
                return tank.getCurrentCapacity();
            }
            LocalDateTime utcNow = SystemUtils.getUtcNow();
            Duration elapsedTime = Duration.between(lastUpdated, utcNow);
            logger.debugFormat(methodName, "CurrentCapacity of tank %s is %s. elapsedTime: %s", tankId, tank.getCurrentCapacity(), elapsedTime);

            double leakageAmount;
            if (elapsedTime.toMillis() > 0) {
                leakageAmount = (double) Configuration.leakageLiterAmountInMinute / 60000 * elapsedTime.toMillis();
                double currentCapacityIncludeLeak = tank.getCurrentCapacity() - leakageAmount;
                tank.setCurrentCapacity(currentCapacityIncludeLeak < 0d ? 0d : currentCapacityIncludeLeak);
                tank.setLastUpdated(utcNow);
                logger.debugFormat(methodName, "update CurrentCapacity of tank %s due to leakage to %s", tankId, tank.getCurrentCapacity());
            }

            return round(tank.getCurrentCapacity());
        }
    }

    private double round(double value) {
        return SystemUtils.round(value, Configuration.roundDecimalPlaces);
    }

    @Override
    public Integer getMaxCapacity(int tankId) {
        if (!isExist(tankId)) return null;
        return Configuration.maxTankCapacity;
    }

}
