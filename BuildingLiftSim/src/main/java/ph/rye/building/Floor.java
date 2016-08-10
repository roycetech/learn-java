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

import java.util.HashSet;
import java.util.Set;

import ph.rye.building.facility.Elevator;
import ph.rye.common.lang.Ano;

/**
 * This serves as Monitor for Person. A floor alerts people when it opens on
 * that floor.
 *
 * @author royce
 */
public class Floor {


    //    private static final OneLogger LOG1 = OneLogger.getInstance();


    public enum Type {
        Common, Regular, Service, VIP
    }


    static final byte BTN_UP = 1;
    static final byte BTN_DOWN = 2;
    static final byte BTN_BOTH = 3;


    private final transient Type type;

    private transient boolean pressedUp;
    private transient boolean pressedDown;


    private final transient Set<Person> peopleWaitingSet = new HashSet<>();

    private final transient Set<Elevator> openLiftDoors = new HashSet<>();


    private final transient byte buttonAvailable;


    private final transient int index;
    private final transient String number;


    Floor(final Type type, final int index, final String number,
            final byte buttonAvailable) {
        this.type = type;

        this.index = index;
        this.number = number;

        this.buttonAvailable = buttonAvailable;
    }

    //    public boolean isSpecial() {
    //        return number == MAX_VALUE || number == MAX_VALUE - 1;
    //    }
    //
    //    public boolean isService() {
    //        return number == 2;
    //    }
    //
    //    public boolean isCommon() {
    //        return number == 5 || number == 2;
    //    }
    //
    //
    //    public boolean isRegular() {
    //        return !isSpecial() && !isService() && !isCommon();
    //    }

    /**
     * @return the number
     */
    public Integer getIndex() {
        return index;
    }

    public String getDisplay() {
        return number;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 31 * 1 + index;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        final Ano<Boolean> retval = new Ano<>(false);
        if (this == obj) {
            retval.set(true);
        } else if (obj != null && getClass() == obj.getClass()) {
            final Floor other = (Floor) obj;
            retval.set(index == other.index);
        }
        return retval.get();
    }


    /**
     * @return the pressedDown
     */
    public boolean isPressedDown() {
        return pressedDown;
    }

    /**
     * @param pressedDown the pressedDown to set
     */
    public void setPressedDown(final boolean pressedDown) {
        this.pressedDown = pressedDown;
    }

    /**
     * @return the pressedUp
     */
    public boolean isPressedUp() {
        return pressedUp;
    }

    /**
     * @param pressedUp the pressedUp to set
     */
    public void setPressedUp(final boolean pressedUp) {
        this.pressedUp = pressedUp;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getDisplay();
    }

    /**
     * @param elevator
     */
    public void markDoorAsOpen(final Elevator elevator) {
        openLiftDoors.add(elevator);

        synchronized (elevator) {
            elevator.notifyAll();
        }
    }

    /**
     * @param elevator
     */
    public void markDoorAsClosed(final Elevator elevator) {
        openLiftDoors.remove(elevator);
    }

    /**
     * @return
     */
    public Set<Elevator> getOpenDoors() {
        return openLiftDoors;
    }

    void addPersonWaiting(final Person person) {
        peopleWaitingSet.add(person);
    }

    void removePersonWaiting(final Person person) {
        peopleWaitingSet.remove(person);
    }

    public boolean hasPeopleWaiting() {
        return !peopleWaitingSet.isEmpty();
    }

}
