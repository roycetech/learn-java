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

import ph.rye.building.facility.ElevatorController;

/**
 * @author royce
 *
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class BsaCondoMain {


    private BsaCondoMain() {}


    /**
     * @param args
     */
    public static void main(final String[] args) {


        final AbstractBuilding building = new Building10F4E();


        building.operate();

        final ElevatorController controller = building.getController();

        final Thread person1 = new Thread(
            new Person(
                "Mitchie",
                controller,
                Person.Type.Manager,
                building.getFloor("G"),
                building.getFloor("P"),
                3000));

        person1.start();

    }

}

