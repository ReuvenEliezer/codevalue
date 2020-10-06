package codevalue.services;

public interface TankDataManager {

    Boolean addWater(int tankId, double waterQuantity);

    Double getCurrentCapacity(int tankId);

    Integer getMaxCapacity(int tankId);

}
