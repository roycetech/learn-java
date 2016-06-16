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
package ph.rye.building.facility;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ph.rye.building.AbstractBuilding;
import ph.rye.building.Floor;

/**
 * Application specific shared object.
 *
 * @author royce
 */
public final class SharedObject {


    private static final SharedObject INSTANCE = new SharedObject();


    private transient long maxWaitTime;


    //    private final transient ConcurrentMap<Person.Type, Floor> elevPersonMap =
    //            new ConcurrentHashMap<>();

    private final transient ConcurrentMap<Elevator, Floor> elevFloorMap =
            new ConcurrentHashMap<>();

    private final transient ConcurrentMap<Elevator.Type, Set<Elevator>> elevTypeMap =
            new ConcurrentHashMap<>();

    private final transient ConcurrentMap<Elevator, Elevator.Direction> directionMap =
            new ConcurrentHashMap<>();


    /** Lock for registering people to elevator. */
    public static final Object LOCK_PERSON_REG = new Object();

    /** Lock for registering presses on the outside of the elevator. */
    static final Object LOCK_FLR_REG = new Object();


    /**
     * Lock for registering presses on the outside of the elevator. Controller
     * waits on this. Person notifies by pressing button from floor or by
     * choosing floor inside the elevator.
     */
    public static final Object LOCK_BUTTON = new Object();


    /** Lock for finding an elevator. Notify on door close. */
    static final Object LOCK_FIND_ELEV = new Object();


    private SharedObject() {}


    public Floor getFloor(final AbstractBuilding building,
                          final Elevator elevator) {
        return elevFloorMap.getOrDefault(elevator, building.getFloor(0));
    }

    public void setFloor(final Elevator elevator, final Floor floor) {
        elevFloorMap.put(elevator, floor);
    }

    public void setDirection(final Elevator elevator,
                             final Elevator.Direction direction) {
        directionMap.put(elevator, direction);
    }

    /**
     * @return the maxWaitTime
     */
    long getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * @param maxWaitTime the maxWaitTime to set
     */
    void setMaxWaitTime(final long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public void registerElevator(final Elevator elevator) {
        elevTypeMap.putIfAbsent(elevator.getType(), new HashSet<Elevator>());
        final Set<Elevator> set = elevTypeMap.get(elevator.getType());
        set.add(elevator);
    }


    public static SharedObject getInstance() {
        return INSTANCE;
    }


    /**
     * @return
     */
    public Set<Elevator> getRegularElevators() {
        return elevTypeMap.get(Elevator.Type.Regular);
    }


}
