package codevalue.controllers;

import codevalue.services.TankDataManager;
import codevalue.utils.WsAddressConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(WsAddressConstants.tankLogicUrl)
public class TankController {

    @Autowired
    private TankDataManager tankDataManager;


    @RequestMapping(method = RequestMethod.GET, value = "getMaxCapacity/{tankId}")
    public Integer getMaxCapacity(@PathVariable int tankId) {
        return tankDataManager.getMaxCapacity(tankId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "getCurrentCapacity/{tankId}")
    public Double getCurrentCapacity(@PathVariable int tankId) {
        return tankDataManager.getCurrentCapacity(tankId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "addWater/{tankId}/{waterAmount}")
    public Boolean getCurrentCapacity(@PathVariable int tankId, @PathVariable double waterAmount) {
        return tankDataManager.addWater(tankId,waterAmount);
    }


}
