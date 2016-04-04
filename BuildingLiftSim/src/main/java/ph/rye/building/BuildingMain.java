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
@SuppressWarnings({
        "PMD.DoNotUseThreads",
        "PMD.ShortVariable" /* For person names only.*/ })
public final class BuildingMain {


    private static final OneLogger LOGGER = OneLogger.getInstance();


    private BuildingMain() {}


    /**
     * @param args
     */
    public static void main(final String[] args) {

        LOGGER.info("Simulation started!");

        final Building2F1E bldg = new Building2F1E();
        bldg.operate();

        bldg.simCase4();

        bldg.verifyLiftsAndFloorsAreEmpty(60);

    }

}

