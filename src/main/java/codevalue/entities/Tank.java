package codevalue.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Tank implements Serializable {

    private int tankId;
    private double currentCapacity = 0;
    private LocalDateTime lastUpdated;

    //for serialization
    public Tank() {
    }

    public Tank(int tankId) {
        this.tankId = tankId;
    }

    public double getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(double currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getTankId() {
        return tankId;
    }

    @Override
    public String toString() {
        return "Tank{" +
                "tankId=" + tankId +
                ", currentCapacity=" + currentCapacity +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
