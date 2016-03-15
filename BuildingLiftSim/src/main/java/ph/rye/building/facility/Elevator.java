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
import ph.rye.building.Floor;
import ph.rye.building.Person;
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


    public static final float MAX_SPACE = 20;


    private final static long ASCEND_PER_FLR_MS = 2000;
    private final static long DSCEND_PER_FLR_MS = 2000;


    public enum Direction {
        UP, DOWN;
    }

    public enum Type {
        Regular, Special, Service
    }


    /** */
    private final transient AbstractBuilding building;


    private transient Direction currentDirection;
    private transient int currentOccupant;

    private final transient Type type;
    private final transient int number;
    private transient boolean open;

    private transient Floor currentFloor;
    private final transient Range<?> range;


    private final transient Set<Floor> pressedFloorSet = new HashSet<>();

    /**
     * These are floors registered by passengers and floor for pick up as chosen
     * by the controller.
     */
    private final transient Map<Floor, Elevator.Direction> targetFloors =
            new TreeMap<>(
                (floor1, floor2) -> floor1
                    .getIndex()
                    .compareTo(floor2.getIndex()));

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
                    this,
                    () -> LOGGER.debug(
                        "E" + number
                                + " has no request, so it'll wait to save power."));

            } else {
                final Floor closestFloor = getClosestFloor();
                if (closestFloor.equals(currentFloor)) {

                    LOGGER.debug(
                        "E" + getNumber() + " is at the requested floor.");
                    pressedFloorSet.remove(currentFloor);

                    final Direction targetDirection =
                            targetFloors.get(closestFloor);

                    if (targetDirection == Elevator.Direction.UP) {
                        currentFloor.setPressedUp(false);
                    } else {
                        currentFloor.setPressedDown(false);
                    }
                    currentDirection = targetDirection;

                    ThreadUtil.syncedAction(this, () -> {
                        targetFloors.remove(closestFloor);
                        openDoor();
                    });

                    /* Allow people to enter/leave. */
                    ThreadUtil.longAction(null, 3000);

                    waitForPeopleToComeInside();

                    /* People would lock while entering, will close door only
                     * after everyone within capacity is inside.*/
                    synchronized (this) {
                        closeDoor();
                    }

                } else {

                    ThreadUtil.sleep(1000);
                    if (currentDirection == Direction.UP) {
                        moveUp();
                    } else {
                        moveDown();
                    }
                    ThreadUtil.sleep(1000);
                }
            }
        }
    }


    /**
     *
     */
    private void waitForPeopleToComeInside() {

        while (!isFull() && getCurrentFloor().hasPeopleWaiting()) {

            LOGGER.debug("Waiting for people to come inside.");
            LOGGER.debug(
                String.format(
                    "Full: %s, people waiting: %s",
                    isFull(),
                    getCurrentFloor().hasPeopleWaiting()));

            ThreadUtil.wait(this, null, 3000);
        }

    }

    /**
     * @return
     */
    private Floor getClosestFloor() {
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

    public void pressFloor(final Floor floor,
                           final Elevator.Direction direction) {
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
                ASCEND_PER_FLR_MS);

            currentFloor = building.getFloor(currentFloor.getIndex() + 1);

            LOGGER.info(
                String.format("E%d is now at %S", getNumber(), currentFloor));

            ThreadUtil.syncedAction(this, () -> LOGGER.info("DING!"));

            for (final Person person : personInside) {
                person.setCurrentFloor(currentFloor);
            }

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
                DSCEND_PER_FLR_MS);

            currentFloor = building.getFloor(currentFloor.getIndex() - 1);

            LOGGER.info(
                String.format("E%d is now at %S", getNumber(), currentFloor));

            ThreadUtil.syncedAction(this, () -> LOGGER.info("DING!"));

            synchronized (SharedObject.LOCK_FLR_REG) {
                SharedObject.getInstance().setFloor(this, currentFloor);
            }

        } else {
            currentDirection = null;
        }
    }

    public void admitPerson(final Person person) {
        personInside.add(person);
        currentOccupant += person.getCapacity();
    }

    private void openDoor() {
        LOGGER.info("E" + number + " is opening door...");

        ThreadUtil.longAction(() -> currentFloor.markDoorAsOpen(this), 3000);
        ThreadUtil.syncedAction(
            currentFloor,
            () -> ThreadUtil.syncedAction(this, () -> {
                open = true;
                LOGGER.info("E" + number + " is now open at " + currentFloor);
            }));

    }

    private void closeDoor() {
        ThreadUtil.longAction(
            () -> LOGGER.info("E" + number + " is closing door..."),
            3000);

        open = false;
        LOGGER.info("E" + number + " is closed!");
        currentFloor.markDoorAsClosed(this);

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
        return currentOccupant >= MAX_SPACE;
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
     * @return the currentOccupant
     */
    public int getCurrentOccupant() {
        return currentOccupant;
    }

    /**
     * @param currentOccupant the currentOccupant to set
     */
    public void setCurrentOccupant(final int currentOccupant) {
        this.currentOccupant = currentOccupant;
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
        return person.getCapacity() + currentOccupant < MAX_SPACE;
    }

    /**
     * @return
     */
    public boolean isOpen() {
        return open;
    }


    @Override
    public String toString() {
        return super.toString() + " " + getClass().getName() + "@"
                + Integer.toHexString(hashCode());
    }

}
