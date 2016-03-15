package ph.rye.building;
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

import ph.rye.logging.OneLogger;

/**
 *
 *
 * @author royce
 *
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class BuildingMain {


    private static final OneLogger LOGGER = OneLogger.getInstance();


    private BuildingMain() {}


    /**
     * Single Person. Elevator on same floor.
     */
    static void case1(final AbstractBuilding bldg) {

        final Thread person1 = new Thread(
            new Person(
                "Mitchie",
                bldg.getController(),
                Person.Type.Manager,
                bldg.getFloor("G"),
                bldg.getFloor("2"),
                2000));
        person1.setPriority(Thread.NORM_PRIORITY);

        person1.start();
    }

    /**
     * Single Person. Elevator on same floor.
     */
    static void case2(final AbstractBuilding bldg) {

        final Person person1 = new Person(
            "Mitchie",
            bldg.getController(),
            Person.Type.Manager,
            bldg.getFloor("G"),
            bldg.getFloor("2"),
            2000);
        person1.setPriority(Thread.NORM_PRIORITY);
        person1.setPersonName("Mitchie");

        final Person person2 = new Person(
            "Royce",
            bldg.getController(),
            Person.Type.Resident,
            bldg.getFloor("G"),
            bldg.getFloor("2"),
            4000);
        person2.setPriority(Thread.NORM_PRIORITY);
        person2.setPersonName("Royce");

        final Person person3 = new Person(
            "Rye",
            bldg.getController(),
            Person.Type.Resident,
            bldg.getFloor("G"),
            bldg.getFloor("2"),
            6000);
        person3.setPriority(Thread.NORM_PRIORITY);
        person3.setPersonName("Rye");

        final Person person4 = new Person(
            "Lucy",
            bldg.getController(),
            Person.Type.Resident,
            bldg.getFloor("G"),
            bldg.getFloor("2"),
            6000);
        person4.setPriority(Thread.NORM_PRIORITY);
        person4.setPersonName("Lucy");

        person1.start();
        person2.start();
        person3.start();
        person4.start();
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {

        LOGGER.info("Simulation started!");

        final AbstractBuilding bldg = new Building2F1E();
        bldg.operate();

        case2(bldg);
    }

}

