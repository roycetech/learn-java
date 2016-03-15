/**
 *   Copyright 2016 Royce Remulla
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ph.rye.building;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ph.rye.building.facility.Elevator;
import ph.rye.building.facility.ElevatorController;
import ph.rye.building.facility.SharedObject;
import ph.rye.logging.OneLogger;

/**
 * @author royce
 */
public abstract class AbstractBuilding {


    private static final OneLogger LOGGER = OneLogger.getInstance();


    /** Maps floor description to Floor Object */
    private final transient Map<String, Floor> descFloorMap = new HashMap<>();


    private transient Floor[] floors;


    private final transient Set<Elevator> elevatorSet = new TreeSet<>(
        (elevator1, elevator2) -> elevator1
            .getType()
            .compareTo(elevator2.getType()));


    private final transient ElevatorController controller =
            new ElevatorController(this);


    protected abstract void initFloors();

    protected abstract void initElevators();


    public AbstractBuilding() {
        initFloors();
        assert floors != null && floors.length > 0;

        initElevators();
    }


    protected void addElevator(final Elevator elevator) {
        elevatorSet.add(elevator);
        SharedObject.getInstance().registerElevator(elevator);
    }

    public Floor getFloor(final int floorNumber) {
        return floors[floorNumber];
    }


    public Floor getFloor(final String floorNumber) {
        return descFloorMap.get(floorNumber);
    }

    void operate() {
        controller.start();

        for (final Elevator elevator : elevatorSet) {
            elevator.start();
        }

        LOGGER.info(
            String.format(
                "Building %s is now operating!",
                getClass().getSimpleName()));
    }


    /**
     * @return the controller
     */
    ElevatorController getController() {
        return controller;
    }


    /**
     * @return the elevatorSet
     */
    public Set<Elevator> getElevatorSet() {
        return elevatorSet;
    }

    protected void setFloors(final List<Floor> floorList) {

        assert floors == null;

        floors = floorList.toArray(new Floor[floorList.size()]);
        for (final Floor floor : floorList) {
            descFloorMap.put(floor.getDisplay(), floor);
        }

    }

}
