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
package ph.rye.building.util;

import ph.rye.building.facility.SharedObject;
import ph.rye.common.lang.Action;
import ph.rye.common.lang.Maker;
import ph.rye.logging.OneLogger;

/**
 * @author royce
 *
 */
public final class ThreadUtil {


    private static final OneLogger LOGGER = OneLogger.getInstance();


    private ThreadUtil() {}


    /**
     *
     * @param maker
     * @param monitor
     * @param action before and after only.
     */
    public static void safeWait(final Maker<Boolean> maker,
                                final Object monitor, final Action... action) {

        safeWait(maker, monitor, 0, action);
    }

    /**
     *
     * @param checker
     * @param monitor
     * @param action before and after only.
     */
    public static void safeWait(final Maker<Boolean> maker,
                                final Object monitor, final long millis,
                                final Action... action) {

        assert action == null || action.length < 4;
        assert millis >= 0;

        Action[] waitAction;
        if (action.length == 3) {

            if (action[0] != null) {
                action[0].execute();
            }

            waitAction = new Action[2];
            System.arraycopy(action, 1, waitAction, 0, 2);
        } else {
            waitAction = new Action[action.length];
            System.arraycopy(action, 0, waitAction, 0, action.length);
        }

        while (maker.make()) {
            wait(monitor, millis, waitAction);
        }
    }

    public static void syncedAction(final Object monitor, final Action action) {

        LOGGER
            .info(Thread.currentThread() + " is synchronizing on: " + monitor);

        synchronized (monitor) {
            if (action != null) {
                action.execute();
            }
            monitor.notifyAll();
        }
        LOGGER.info(Thread.currentThread() + " notify on: " + monitor);
    }

    public static void longAction(final Action before, final Action after,
                                  final long millis) {
        assert millis > 0;

        if (before != null) {
            before.execute();
        }

        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            throw new AppException(e);
        }

        if (after != null) {
            after.execute();
        }

    }

    public static void sleep(final long millis) {
        assert millis > 0;

        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            throw new AppException(e);
        }
    }

    public static void wait(final Object monitor, final Action... action) {
        wait(monitor, 0, action);
    }

    public static void wait(final Object monitor, final long millis,
                            final Action... action) {

        assert monitor != null;
        assert millis >= 0;
        assert action.length <= 2;

        LOGGER.info(Thread.currentThread() + " waiting on monitor: " + monitor);

        synchronized (monitor) {

            if (action.length > 0 && action[0] != null) {
                action[0].execute();
            }

            try {
                SharedObject.getInstance().waitSet
                    .put(Thread.currentThread(), monitor);

                monitor.wait(millis);
                SharedObject.getInstance().waitSet
                    .remove(Thread.currentThread());


                LOGGER.info(
                    Thread.currentThread() + " [WAKES UP] on monitor: "
                            + monitor);

                if (action.length > 1 && action[1] != null) {
                    action[1].execute();
                }

            } catch (final InterruptedException e) {
                throw new AppException(e);
            }
        }
    }


}
