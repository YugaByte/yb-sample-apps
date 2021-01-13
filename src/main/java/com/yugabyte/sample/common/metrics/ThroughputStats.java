// Copyright (c) YugaByte, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied.  See the License for the specific language governing permissions and limitations
// under the License.
//

package com.yugabyte.sample.common.metrics;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class ThroughputStats implements Serializable {
    private ThroughputObserver activeObserver;

    private SummaryStatistics throughput;

    private static final long NANOS_PER_THROUGHPUT_OBSERVATION = TimeUnit.SECONDS.toNanos(1);

    private ThroughputObserver getObserver(long startTsNanos) {
        return new ThroughputObserver(startTsNanos, startTsNanos + NANOS_PER_THROUGHPUT_OBSERVATION);
    }

    public ThroughputStats() {
        this.activeObserver = null;
        this.throughput = new SummaryStatistics();
    }

    public void observe(Observation o) {
        if (activeObserver == null) {
            activeObserver = getObserver(o.getStartTsNanos());
        }
        long observedSoFar = activeObserver.observe(o);
        while (observedSoFar < o.getCount() && activeObserver.getStartTsNanos() < o.getEndTsNanos()) {
            throughput.addValue(activeObserver.getOps());
            long oldEndTsNanos = activeObserver.getEndTsNanos();
            activeObserver = getObserver(oldEndTsNanos);
            observedSoFar += activeObserver.observe(o);
        }
        assert (observedSoFar == o.getCount());
    }

    public SummaryStatistics getStats() {
        return throughput;
    }
}
