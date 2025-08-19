/*
 * Copyright (C) 2025 _Novit_ (github.com/novitpw)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pw.novit.asnlookup;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author _Novit_ (novitpw)
 */
@UtilityClass
public class AsnLookupExecutors {

    public static @NotNull ExecutorService polled() {
        val systemThreads = Runtime.getRuntime().availableProcessors();
        return polled(Math.max(1, systemThreads / 2), systemThreads);
    }

    public static @NotNull ExecutorService polled(
            int poolSize,
            int maximumPoolSize
    ) {
        val threadPoolExecutor = new ThreadPoolExecutor(
                poolSize, maximumPoolSize, 30, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                AsnLookupExecutors.threadFactory());

        threadPoolExecutor.allowCoreThreadTimeOut(true);

        return threadPoolExecutor;
    }

    private static ThreadFactory threadFactory() {
        val counter = new AtomicInteger(1);

        return runnable -> {
            val thread = new Thread(runnable, "asn-blacklist-thread-" + counter.getAndIncrement());
            thread.setDaemon(true); // делает поток демоном
            return thread;
        };
    }

}
