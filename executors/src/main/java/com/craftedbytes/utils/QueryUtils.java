package com.craftedbytes.utils;

import com.craftedbytes.domain.QuickSearch;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class QueryUtils {

    private static Logger logger = Logger.getLogger(QueryUtils.class);

    public static double collectTaskWithSQL(IMap<String, QuickSearch> qs_map, Predicate predicate)
            throws InterruptedException, ExecutionException {

        StopWatch clock = new StopWatch();
        clock.reset();
        clock.start();

        Set<String> values = qs_map.keySet(predicate);

//        logger.info("resultset size = " + values.size());

        clock.stop();
        return clock.getTime();

    }
}

