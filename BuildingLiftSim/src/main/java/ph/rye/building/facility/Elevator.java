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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ph.rye.building.AbstractBuilding;
import ph.rye.building.Direction;
import ph.rye.building.DoorState;
import ph.rye.building.Floor;
import ph.rye.building.Person;
import ph.rye.building.thread.Lock;
import ph.rye.building.util.ThreadUtil;
import ph.rye.common.lang.Ano;
import ph.rye.common.lang.Range;
import ph.rye.logging.OneLogger;

/**
 * @author royce
 */
@SuppressWarnings({
        "PMD.DoNotUseThreads",
        "PMD.TooManyMethods" })
public class Elevator extends Thread {


    private static final OneLogger LOGGER = OneLogger.getInstance();


    public static final int MAX_SPACE = 20;


    private final static long ASCEND_PER_FLR_MS = 2000;
    private final static long DSCEND_PER_FLR_MS = 2000;


    public final transient Object synchronizer = new Lock("Lift");


    public enum Type {
        Regular, Special, Service
    }


    /** */
    private final transient AbstractBuilding building;


    private transient Direction currentDirection;

    /** Used as monitor by people and the elevator. */
    public final transient Lock door = new Lock("Door");


    private final transient Type type;
    private final transient int number;

    private transient DoorState doorState = DoorState.CLOSED;
    private transient Floor currentFloor;
    private final transient Range<?> range;


    private final transient Set<Floor> pressedFloorSet = new HashSet<>();

    /**
     * These are floors registered by passengers and floor for pick up as chosen
     * by the controller.
     */
    private final transient Map<Floor, Direction> targetFloors = new TreeMap<>(
        (floor1, floor2) -> floor1.getIndex().compareTo(floor2.getIndex()));

    private final transient Set<Person> personInside = new HashSet<>();


    /**
     *
     * @param number
     * @param type
     * @param range
     * @param elevFloorMap
     */
    public Elevator(final AbstractBuilding building, final int number,
            final Type type, final Range<?> range) {

        this.building = building;

        currentFloor = building.getFloor("G");

        this.type = type;
        this.number = number;
        this.range = range;

        setPriority(Thread.NORM_PRIORITY);
        setName("E" + number);
    }

    public Elevator initStartFloor(final String floor) {
        currentFloor = building.getFloor(floor);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        LOGGER.info("E" + number + " is ready.");

        while (true) {

            if (targetFloors.isEmpty()) {

                ThreadUtil.wait(
                    synchronizer,
                    () -> LOGGER.debug(
                        "E" + number
                                + " has no request, so it'll wait to save power."));

            } else {

                final Floor closestFloor = findClosestFloorInQueue();
                if (closestFloor.equals(currentFloor)) {

                    LOGGER.debug(
                        "E" + getNumber() + " is at the requested floor.");

                    pressedFloorSet.remove(currentFloor);

                    /* If pressed by people outside, clear the UP/DOWN from panel. */
                    final Direction targetDirection =
                            targetFloors.get(closestFloor);
                    if (targetDirection == Direction.UP) {
                        currentFloor.setPressedUp(false);
                    } else {
                        currentFloor.setPressedDown(false);
                    }
                    currentDirection = targetDirection;


                    ThreadUtil.syncedAction(synchronizer, () -> {});

                    targetFloors.remove(closestFloor);
                    openDoor();

                    /* Allow people to enter/leave. */
                    ThreadUtil.longAction(
                        () -> LOGGER.info(
                            "Giving initial 3secs for people to enter/exit"),
                        null,
                        3000);

                    waitUntilNoMorePeopleEnteringOrLeaving();

                    /* People would lock while entering, will close door only
                     * after everyone within capacity is inside.*/
                    synchronized (synchronizer) {
                        closeDoor();
                    }

                } else {
                    move();
                }
            }
        }
    }

    void move() {
        ThreadUtil.sleep(1000);
        if (currentDirection == Direction.UP) {
            moveUp();
        } else {
            moveDown();
        }
        ThreadUtil.sleep(1000);
    }


    /** */
    private void waitUntilNoMorePeopleEnteringOrLeaving() {
        LOGGER.info("waiting for people to enter or leave...");
        while (!isFull() && getCurrentFloor().hasPeopleWaiting(currentDirection)
                || hasPeopleExiting()) {
            LOGGER.debugf(
                "Full: %s, people waiting: %s",
                isFull(),
                getCurrentFloor().hasPeopleWaiting(currentDirection));
            /* Wait but timeout after sometime and don't expect to be told to wake up. */
            ThreadUtil.wait(door, 3000);
        }
        LOGGER.info("With " + personInside + " now proceeding...");
    }

    /**
     * @return
     */
    private boolean hasPeopleExiting() {
        final Ano<Boolean> retval = new Ano<>(false);

        for (final Person person : personInside) {
            if (person.getDesiredFloor().equals(currentFloor)) {
                retval.set(true);
                break;
            }
        }

        return retval.get();
    }

    /**
     * @return
     */
    private Floor findClosestFloorInQueue() {
        final Ano<Floor> closest = new Ano<>();
        for (final Floor floor : targetFloors.keySet()) {
            if (closest.get() == null) {
                closest.set(floor);
            } else
                if (Math.abs(currentFloor.getIndex() - floor.getIndex()) < Math
                    .abs(closest.get().getIndex() - floor.getIndex())) {
                closest.set(floor);
            }
        }

        assert closest.get() != null;
        return closest.get();
    }

    public void pressFloor(final Floor floor, final Direction direction) {
        LOGGER.debug("Stop requested at " + floor + ", to go " + direction);
        targetFloors.put(floor, direction);
        pressedFloorSet.add(floor);
    }

    public boolean isFloorPressed(final Floor floor) {
        return pressedFloorSet.contains(floor);
    }


    private void moveUp() {


        if (currentFloor.getIndex() < range.getEnd()) {

            ThreadUtil.longAction(
                () -> LOGGER
                    .info(String.format("E%d is going up", getNumber())),
                null,
                ASCEND_PER_FLR_MS);

            currentFloor = building.getFloor(currentFloor.getIndex() + 1);

            LOGGER.info(
                String.format("E%d is now at %S", getNumber(), currentFloor));

            for (final Person person : personInside) {
                person.setCurrentFloor(currentFloor);
            }

            ThreadUtil.syncedAction(synchronizer, () -> LOGGER.info("DING!"));


            synchronized (SharedObject.LOCK_FLR_REG) {
                SharedObject.getInstance().setFloor(this, currentFloor);
            }


        } else {
            currentDirection = null;
        }
    }

    private void moveDown() {
        if (currentFloor.getIndex() > range.getStart()) {

            ThreadUtil.longAction(
                () -> LOGGER
                    .info(String.format("E%d is going down", getNumber())),
                null,
                DSCEND_PER_FLR_MS);

            currentFloor = building.getFloor(currentFloor.getIndex() - 1);

            LOGGER.info(
                String.format("E%d is now at %S", getNumber(), currentFloor));

            ThreadUtil.syncedAction(synchronizer, () -> LOGGER.info("DING!"));

            synchronized (SharedObject.LOCK_FLR_REG) {
                SharedObject.getInstance().setFloor(this, currentFloor);
            }

        } else {
            currentDirection = null;
        }
    }

    public void admitPerson(final Person person) {
        personInside.add(person);
    }

    public void dischargePerson(final Person person) {
        personInside.remove(person);
    }

    private void openDoor() {

        ThreadUtil.syncedAction(door, () -> {

            ThreadUtil.longAction(() -> {
                currentFloor.arriveElevator(this);
                LOGGER.info("E" + number + " is opening door...");
                doorState = DoorState.OPENING;
            } , null, 3000);

            ThreadUtil.syncedAction(
                currentFloor,
                () -> ThreadUtil.syncedAction(synchronizer, () -> {
                doorState = DoorState.OPEN;
                LOGGER.info("E" + number + " is now open at " + currentFloor);
            }));

        });
    }

    private void closeDoor() {
        ThreadUtil.syncedAction(door, () -> ThreadUtil.longAction(() -> {
            LOGGER.info("E" + number + " is closing door...");
            doorState = DoorState.CLOSING;
        } , () -> {
            doorState = DoorState.CLOSED;
            LOGGER.info("E" + number + " is closed!");
            currentFloor.markDoorAsClosed(this);
        } , 3000));

        synchronized (SharedObject.LOCK_FIND_ELEV) {
            SharedObject.LOCK_FIND_ELEV.notifyAll();
        }
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @return
     */
    public boolean isFull() {
        int totalOccupied = 0;
        for (final Person person : personInside) {
            totalOccupied += person.getCapacity();
        }
        return totalOccupied >= MAX_SPACE;
    }

    int getAvailableSpace() {
        int totalOccupied = 0;
        for (final Person person : personInside) {
            totalOccupied += person.getCapacity();
        }
        return MAX_SPACE - totalOccupied;
    }

    /**
     * @return the currentDirection
     */
    public Direction getCurrentDirection() {
        return currentDirection;
    }

    /**
     * @param currentDirection the currentDirection to set
     */
    void setCurrentDirection(final Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    /**
     * @return
     */
    public boolean isReserved() {
        return false;
    }

    /**
     * @return the currentFloor
     */
    public Floor getCurrentFloor() {
        return currentFloor;
    }

    /**
     * @param currentFloor the currentFloor to set
     */
    public void setCurrentFloor(final Floor currentFloor) {
        this.currentFloor = currentFloor;
    }

    /**
     * @param person
     * @return
     */
    public boolean canAccomodatePerson(final Person person) {
        return person.getCapacity() < getAvailableSpace();
    }

    /**
     * @return
     */
    public boolean isOpen() {
        return doorState == DoorState.OPEN;
    }


    @Override
    public String toString() {
        //        return "MyLock";
        return super.toString() + " " + getClass().getSimpleName() + "@"
                + Integer.toHexString(hashCode());
    }

}
