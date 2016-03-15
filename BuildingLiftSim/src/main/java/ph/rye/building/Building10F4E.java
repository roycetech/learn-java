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
class Building10F4E extends AbstractBuilding {


    private static final int TOP_FLOOR = 9;


    /* (non-Javadoc)
     * @see ph.rye.condo.AbstractBuilding#initElevators(java.util.Map)
     */
    @Override
    public void initElevators() {
        addElevator(
            new Elevator(
                this,
                1,
                Elevator.Type.Regular,
                new Range<Object>(0, TOP_FLOOR)));

        addElevator(
            new Elevator(
                this,
                2,
                Elevator.Type.Regular,
                new Range<Object>(0, TOP_FLOOR)));

        addElevator(
            new Elevator(
                this,
                3,
                Elevator.Type.Service,
                new Range<Object>(0, TOP_FLOOR)));

        addElevator(
            new Elevator(
                this,
                4,
                Elevator.Type.Special,
                new Range<Object>(0, TOP_FLOOR)));

    }

    /* (non-Javadoc)
     * @see ph.rye.condo.AbstractBuilding#initFloors()
     */
    @Override
    protected void initFloors() {
        final List<Floor> floorList = new ArrayList<>();
        floorList.add(new Floor(Floor.Type.Regular, 0, "B", Floor.BTN_UP));
        floorList.add(new Floor(Floor.Type.Common, 1, "G", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.Service, 2, "2", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.Regular, 3, "3", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.Regular, 4, "5", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.Common, 5, "6", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.Regular, 6, "7", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.Regular, 7, "8", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.Regular, 8, "9", Floor.BTN_BOTH));
        floorList.add(new Floor(Floor.Type.VIP, 9, "P", Floor.BTN_BOTH));
        setFloors(floorList);
    }

}
