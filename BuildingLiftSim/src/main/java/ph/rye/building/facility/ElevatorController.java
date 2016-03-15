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

import java.util.Set;
import java.util.TreeSet;

import ph.rye.building.AbstractBuilding;
import ph.rye.building.Direction;
import ph.rye.building.Floor;
import ph.rye.building.util.ThreadUtil;
import ph.rye.common.lang.Ano;
import ph.rye.logging.OneLogger;

/**
 * There are many types of queuing.
 *
 *
 * <ol>
 * <li>First Come First Serve. (Default).
 * <li>Closest Proximity.
 * <li>Timed Closest Proximity. When a request from floor reaches a wait time
 * threshold, it is prioritized.
 * </ol>
 *
 * Rules:
 * <ul>
 * <li>Service Floor will only fetch Service Elevator. Going back, the service
 * personnel will no longer be given priority and have to queue just like
 * everybody from non service floors.
 * <li>When a person enters an elevator, elevator can now determine where to go
 * based on the input from user. Controller may add destination floor only for
 * person requesting outside.
 * <li>When a person cannot enter a lift, he waits until another elevator going
 * in the desired direction arrives.
 * </ul>
 *
 * @author royce
 *
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public class ElevatorController extends Thread {


    private static final OneLogger LOGGER = OneLogger.getInstance();


    private final transient Set<Floor> pressUpSet = new TreeSet<>(
        (floor1, floor2) -> floor1.getIndex().compareTo(floor2.getIndex()));

    private final transient Set<Floor> pressDownSet = new TreeSet<>(
        (floor1, floor2) -> floor2.getIndex().compareTo(floor1.getIndex()));


    private final transient AbstractBuilding building;


    public ElevatorController(final AbstractBuilding building) {
        this.building = building;
        setPriority(MAX_PRIORITY);
        setName("Elevator Controller");
    }


    public void pressUp(final Floor floor) {
        floor.setPressedUp(true);
        pressUpSet.add(floor);

        ThreadUtil.syncedAction(SharedObject.LOCK_BUTTON, null);
    }

    public void pressDown(final Floor floor) {
        floor.setPressedDown(true);
        pressDownSet.add(floor);

        ThreadUtil.syncedAction(SharedObject.LOCK_BUTTON, null);
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        LOGGER.info("Controller Ready!");

        while (true) {

            if (pressUpSet.isEmpty() && pressDownSet.isEmpty()) {

                ThreadUtil.wait(
                    SharedObject.LOCK_BUTTON,
                    () -> LOGGER.info("Waiting for people wanting a lift..."));

            } else {
                LOGGER.info(
                    "Received lift request, looking for available elevator...");

                Floor floor;
                Direction direction;
                if (pressUpSet.isEmpty()) {
                    floor = pressDownSet.iterator().next();
                    direction = Direction.DOWN;
                } else {
                    floor = pressUpSet.iterator().next();
                    direction = Direction.UP;
                }

                final Elevator elevator = findElevator(floor, direction);
                if (elevator == null) {

                    ThreadUtil.wait(
                        SharedObject.LOCK_FIND_ELEV,
                        () -> LOGGER.info(
                            "No elevator available, will wait if an elevator frees up..."));

                } else {

                    ThreadUtil.syncedAction(elevator.synchronizer, () -> {
                        LOGGER.info(
                            "Found elevator " + elevator.getNumber()
                                    + " and registered.");

                        elevator.pressFloor(floor, direction);
                        elevator.setCurrentDirection(direction);

                        if (direction == Direction.UP) {
                            pressUpSet.remove(floor);
                        } else {
                            pressDownSet.remove(floor);
                        }
                    });

                }
            }
        }

    }


    /**
     * Implementation is dependent on the specific goal.
     *
     * @param floor requesting floor.
     * @param direction requested direction to go.
     */
    private Elevator findElevator(final Floor floor,
                                  final Direction direction) {

        final Ano<Elevator> retval = new Ano<>();
        for (final Elevator elevator : building.getElevatorSet()) {

            if (floor.getType() == Floor.Type.Service
                    && elevator.getType() != Elevator.Type.Service) {
                continue;
            } else {
                if (!elevator.isFull() && !elevator
                    .isReserved() && (elevator.getCurrentDirection() == null
                            || elevator.getCurrentDirection() == direction)) {
                    retval.set(elevator);
                    break;
                }
            }
        }
        return retval.get();
    }

}
