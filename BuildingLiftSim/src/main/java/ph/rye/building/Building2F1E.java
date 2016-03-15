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
class Building2F1E extends AbstractBuilding {


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
                new Range<Object>(0, 1)).initStartFloor("G"));

    }

    /* (non-Javadoc)
     * @see ph.rye.condo.AbstractBuilding#initFloors()
     */
    @Override
    protected void initFloors() {
        final List<Floor> floorList = new ArrayList<>();
        floorList.add(new Floor(Floor.Type.Regular, 0, "G", Floor.BTN_UP));
        floorList.add(new Floor(Floor.Type.Regular, 1, "2", Floor.BTN_DOWN));
        setFloors(floorList);
    }

}
