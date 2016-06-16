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

import ph.rye.util.function.Callback;

/**
 * @author royce
 *
 */
public final class ThreadUtil {


    private ThreadUtil() {}


    public static void syncedAction(final Object monitor,
                                    final Callback callback) {
        synchronized (monitor) {
            callback.call();
            monitor.notifyAll();
        }
    }

    public static void longAction(final Callback callback, final long millis) {
        assert millis > 0;

        if (callback != null) {
            callback.call();
        }

        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            throw new AppException(e);
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

    public static void wait(final Object monitor, final Callback callback) {
        wait(monitor, callback, 0);
    }

    public static void wait(final Object monitor, final Callback callback,
                            final long millis) {

        assert monitor != null;
        assert millis >= 0;

        synchronized (monitor) {

            if (callback != null) {
                callback.call();
            }

            try {
                monitor.wait(millis);
            } catch (final InterruptedException e) {
                throw new AppException(e);
            }
            //            monitor.notifyAll();
        }
    }


}
