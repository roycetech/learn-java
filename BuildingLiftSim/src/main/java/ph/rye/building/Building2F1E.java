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

import java.util.ArrayList;
import java.util.List;

import ph.rye.building.facility.Elevator;
import ph.rye.common.lang.Range;

/**
 * @author royce
 *
 */
@SuppressWarnings({
        "PMD.ShortVariable" /* For person names only.*/ })
class Building2F1E extends AbstractBuilding {


    public static final String FLR_G = "G";
    public static final String FLR_2 = "2";


    static final String P1 = "Royce";
    static final String P2 = "Mitchie";
    static final String P3 = "Rye";
    static final String P4 = "Lucy";


    /* (non-Javadoc)
     * @see ph.rye.condo.AbstractBuilding#initElevators(java.util.Map)
     */
    @Override
    protected void initElevators() {
        addElevator(
            new Elevator(
                this,
                1,
                Elevator.Type.Regular,
                new Range<Object>(0, 1)).initStartFloor(FLR_G));

    }

    /* (non-Javadoc)
     * @see ph.rye.condo.AbstractBuilding#initFloors()
     */
    @Override
    protected void initFloors() {
        final List<Floor> floorList = new ArrayList<>();
        floorList.add(new Floor(Floor.Type.Regular, 0, FLR_G, Floor.BTN_UP));
        floorList.add(new Floor(Floor.Type.Regular, 1, FLR_2, Floor.BTN_DOWN));
        setFloors(floorList);
    }

    /**
     * Single Person. Elevator on same floor.
     */
    void simCase1() {

        final Person person1 = new Person(
            P1,
            getController(),
            Person.Type.Manager,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            2000);

        person1.start();
    }


    /**
     * Four Person all from G going to 2. Elevator on same floor.
     */
    void simCase2() {

        setStartingFloor(1, Building2F1E.FLR_G);

        final Person person1 = new Person(
            P1,
            getController(),
            Person.Type.Manager,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            2000);

        final Person person2 = new Person(
            P2,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            4000);

        final Person person3 = new Person(
            P3,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            6000);

        final Person person4 = new Person(
            P4,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            6000);

        person1.start();
        person2.start();
        person3.start();
        person4.start();
    }

    /**
     * Four Person all from G going to 2. Elevator from second floor.
     */
    void simCase3() {

        setStartingFloor(1, Building2F1E.FLR_2);

        final Person person1 = new Person(
            P1,
            getController(),
            Person.Type.Manager,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            2000);

        final Person person2 = new Person(
            P2,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            4000);

        final Person person3 = new Person(
            P3,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            6000);

        final Person person4 = new Person(
            P4,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            6000);

        person1.start();
        person2.start();
        person3.start();
        person4.start();
    }

    /**
     * Two person from G going up to 2, Two Person from 2 going down to G.
     * Elevator starts at ground floor.
     */
    void simCase4() {

        setStartingFloor(1, Building2F1E.FLR_2);

        final Person person1 = new Person(
            P1,
            getController(),
            Person.Type.Manager,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            2000);

        final Person person2 = new Person(
            P2,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_G),
            getFloor(Building2F1E.FLR_2),
            4000);

        final Person person3 = new Person(
            P3,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_2),
            getFloor(Building2F1E.FLR_G),
            6000);

        final Person person4 = new Person(
            P4,
            getController(),
            Person.Type.Resident,
            getFloor(Building2F1E.FLR_2),
            getFloor(Building2F1E.FLR_G),
            6000);

        person1.start();
        person2.start();
        person3.start();
        person4.start();
    }


}
