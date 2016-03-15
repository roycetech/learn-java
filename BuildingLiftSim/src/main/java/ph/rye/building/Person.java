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

import ph.rye.building.facility.Elevator;
import ph.rye.building.facility.ElevatorController;
import ph.rye.building.facility.SharedObject;
import ph.rye.building.util.ThreadUtil;
import ph.rye.common.lang.Ano;
import ph.rye.logging.OneLogger;

/**
 * @author royce
 *
 */
//public class Person extends Thread {
@SuppressWarnings("PMD.DoNotUseThreads")
public class Person implements Runnable {


    private static final OneLogger LOGGER = OneLogger.getInstance();


    enum Type {
        Worker, Manager, Resident, Vip
    }


    private final transient String name;
    private final transient Type type;
    private transient int capacity = 1;


    private final transient ElevatorController controller;


    private transient Floor currentFloor;
    private final transient Floor desiredFloor;
    private final transient int delayMs;


    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    @SuppressWarnings("PMD.UseNotifyAllInsteadOfNotify")
    public void run() {

        if (delayMs > 0) {
            ThreadUtil.sleep(delayMs);
        }

        currentFloor.addPersonWaiting(this);

        LOGGER.info(
            String.format(
                "[%s] arrived at %s elevator entrance and wants to go to %s",
                name,
                currentFloor,
                desiredFloor));

        final Ano<Elevator> elevator = new Ano<>();
        final Ano<Boolean> entered = new Ano<>(false);
        do {
            if (currentFloor.getOpenDoors().isEmpty()) {
                pickDesiredDirection();
                waitUntilLiftArriveAndOpen();
                /* Arrived and a door is open/about to open. */
            } else {
                LOGGER.info(
                    "[" + name + "] finds that lift is open/about to open...");
            }

            elevator.set(pickElevator(getDesiredDirection()));
            if (elevator.get() == null) {
                LOGGER.warn(
                    "[" + name
                            + "] did not like the open elevator, wait for another.");
                continue;
            } else {

                waitForElevatorToFullyOpen(elevator.get());

                //final boolean canFitInside =
                enterElevator(elevator.get(), getDesiredDirection());

                //if (canFitInside) {
                entered.set(true);
                //} else {
                //   LOGGER.warn("Did not fit!");
                // }
            }
        } while (!entered.get());

        waitForElevatorToReachDestination(elevator.get());

        LOGGER.info(String.format("%s arrived at destination, thanks!", name));
        ThreadUtil.sleep(1000);

        stepOutOfElevator(elevator.get());
    }


    /**
     *
     */
    private void waitForElevatorToFullyOpen(final Elevator elevator) {
        while (!elevator.isOpen()
                && !elevator.getCurrentFloor().equals(currentFloor)) {
            ThreadUtil.wait(elevator, null);
        }
    }


    private void pickDesiredDirection() {
        if (getDesiredDirection() == Elevator.Direction.UP) {
            if (!currentFloor.isPressedUp()) {
                LOGGER.info("[" + name + "] pressed the [UP] button");

                ThreadUtil.syncedAction(
                    SharedObject.LOCK_BUTTON,
                    () -> controller.pressUp(currentFloor));
            }
        } else {
            if (!currentFloor.isPressedDown()) {
                LOGGER.info(name + " pressed the [Down] button");

                ThreadUtil.syncedAction(
                    SharedObject.LOCK_BUTTON,
                    () -> controller.pressDown(currentFloor));
            }
        }
    }

    private Elevator.Direction getDesiredDirection() {
        return desiredFloor.getIndex() > currentFloor.getIndex()
                ? Elevator.Direction.UP : Elevator.Direction.DOWN;
    }

    @SuppressWarnings("PMD.UseNotifyAllInsteadOfNotify")
    private boolean enterElevator(final Elevator elevator,
                                  final Elevator.Direction desiredDirection) {

        final Ano<Boolean> retval = new Ano<>(false);

        synchronized (elevator) {

            if (elevator.canAccomodatePerson(this)) {
                currentFloor.removePersonWaiting(this);

                retval.set(true);

                ThreadUtil.longAction(() -> {
                    elevator.admitPerson(this);
                    LOGGER.info(
                        "[" + name + "] is now entering lift: E"
                                + elevator.getNumber());
                } , 2000);


                if (!elevator.isFloorPressed(desiredFloor)) {
                    LOGGER.info(
                        "[" + name + "] presses button for: " + desiredFloor);
                    elevator.pressFloor(desiredFloor, desiredDirection);
                }
            } else {
                retval.set(false);
            }

            elevator.notifyAll();
        }

        return retval.get();

    }

    private void stepOutOfElevator(final Elevator elevator) {
        synchronized (elevator) {
            ThreadUtil.longAction(() -> {
                elevator.admitPerson(this);
                LOGGER.info(
                    "[" + name + "] is now leaving lift: E"
                            + elevator.getNumber());
            } , 2000);
            elevator.notifyAll();
        }
    }


    private void waitForElevatorToReachDestination(final Elevator elevator) {
        while (!elevator.getCurrentFloor().equals(desiredFloor)) {
            ThreadUtil.wait(
                elevator,
                () -> LOGGER.debug(
                    "[" + name + "] is waiting to reach desired floor..."));
        }
    }

    /**
     * There must already be one or more open doors. A person may not be able to
     * enter because of space issues especially if a person occupies more than 1
     * slot.
     *
     * @param desiredDirection direction the person wants to go.
     */
    private Elevator pickElevator(final Elevator.Direction desiredDirection) {
        final Ano<Elevator> retval = new Ano<>();
        for (final Elevator elevator : currentFloor.getOpenDoors()) {
            if (elevator.getCurrentDirection() == desiredDirection
                    && elevator.canAccomodatePerson(this)) {
                retval.set(elevator);
                break;
            }
        }
        return retval.get();
    }

    private void waitUntilLiftArriveAndOpen() {
        while (currentFloor.getOpenDoors().isEmpty()) {
            ThreadUtil.wait(currentFloor, () -> {
                LOGGER.debug(
                    name + " at " + currentFloor
                            + " is waiting for lift to arrive and open.");
            });
        }
    }

    Person(final String name, final ElevatorController controller,
            final Type type, final Floor currentFloor, final Floor desiredFloor,
            final int delayMs) {

        this.name = name;
        this.controller = controller;

        this.type = type;

        this.currentFloor = currentFloor;
        this.desiredFloor = desiredFloor;

        this.delayMs = delayMs;

    }

    public Person initCapacity(final int capacity) {
        assert capacity > 0 && capacity <= Elevator.MAX_SPACE;
        this.capacity = capacity;
        return this;
    }

    /**
     * @return the type
     */
    Type getType() {
        return type;
    }

    /**
     * @return
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @param currentFloor2
     */
    public void setCurrentFloor(final Floor currentFloor) {
        this.currentFloor = currentFloor;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
