package com.craftedbytes.client;

import com.craftedbytes.domain.QuickSearch;
import com.craftedbytes.utils.QueryUtils;
import com.craftedbytes.utils.QuickSearchFactory;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class Client {
    private static IMap<String, QuickSearch> qs_map;

    public static void main(String[] args)
            throws IOException, ExecutionException, InterruptedException {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        qs_map = hz.getMap("quicksearch");

        System.out.println("Number of indexes: "+hz.getConfig().getMapConfig("quicksearch").getMapIndexConfigs().size());

        int i = args.length > 0 ? Integer.parseInt(args[0]) : 150000;
        populateWithFactory(i);

        EntryObject eo = new PredicateBuilder().getEntryObject();

        runIMapSQLBasedQuery(10,
                eo.isNot("disabled_for_upload")
                        .and(eo.get("media_id").notEqual(-1)), 2);


        runIMapSQLBasedQuery(10, eo.isNot("disabled_for_upload")
                .and(eo.get("media_id").notEqual(-1))
                .and(eo.isNot("inactive"))
                .and(eo.get("quality_check_state").equal(12)), 4);


        PredicateBuilder p8 = eo.isNot("disabled_for_upload")
                .and(eo.get("media_id").notEqual(-1))
                .and(eo.isNot("inactive"))
                .and(eo.get("quality_check_state").equal(12))
                .and(eo.get("proxy_creation_state").equal(14))
                .and(eo.get("content_approval_media_state").notEqual(18))
                .and(eo.get("clock_approval_media_state").notEqual(42))
                .and(eo.get("class_approval_media_state").notEqual(72))
                .and(eo.get("num_instruction_rejected").equal(0));

        runIMapSQLBasedQuery(10, p8, 8);

        runIMapSQLBasedQuery(10, eo.isNot("disabled_for_upload")
                .and(eo.get("media_id").notEqual(-1))
                .and(eo.isNot("inactive"))
                .and(eo.get("quality_check_state").equal(12))
                .and(eo.get("proxy_creation_state").equal(14))
                .and(eo.get("content_approval_media_state").notEqual(18))
                .and(eo.get("clock_approval_media_state").notEqual(42))
                .and(eo.get("class_approval_media_state").notEqual(72))
                .and(eo.get("num_instruction_rejected").equal(0))
                .and(eo.get("qc_approval_media_state").notEqual(22))
                .and(eo.get("approval_advert_state").notEqual(11))
                .and(eo.isNot("aired"))
                .and(eo.get("approval_advert_state").in(9, 10)), 13);

        PredicateBuilder p17 = eo.isNot("disabled_for_upload")
                .and(
                        eo.get("approval_advert_state").equal(11)
                                .or(
                                        eo.isNot("inactive")
                                                .and(
                                                        eo.get("content_approval_media_state").equal(18)
                                                                .or(eo.get("clock_approval_media_state").equal(42))
                                                                .or(eo.get("qc_approval_media_state").equal(22))
                                                                .or(eo.get("num_instruction_rejected").greaterThan(0))
                                                )
                                )
                )
                .and(eo.get("first_air_date").notEqual(0)
                        .and(eo.get("delivery_deadline_ms_algo").greaterThan(1403560800000l)
                                .or(eo.get("first_air_date").equal(0))
                                .or(eo.get("delivery_deadline_ms_algo").lessThan(0).and(eo.get("first_air_date").greaterThan(1403560800000l)))));

        runIMapSQLBasedQuery(10, p17, 17);

        runIMapSQLBasedQuery(10, p17.or(p8), 25);
    }


    private static void runIMapSQLBasedQuery(int queries, Predicate p, int count) throws InterruptedException, ExecutionException {
        double[] total = new double[10];
        for (int i = 0; i < queries; i++) {
            double res = QueryUtils.collectTaskWithSQL(qs_map, p);
            total[i] = res;
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = 0; i < total.length; i++) {
            stats.addValue(total[i]);
        }
        System.out.println("Indexed predicate count: " + count);
        System.out.println("Query avg time of " + queries + " queries was : " + stats.getMean() + " ms");
        System.out.println("Max time of " + queries + " queries was : " + stats.getMax() + " ms");
        System.out.println("Query variance of " + queries + " queries was : " + stats.getVariance() + "");
        System.out.println("Query STD of " + queries + " queries was: " + stats.getStandardDeviation() + "\n");
    }

    private static void populateWithFactory(int limit) {
        QuickSearchFactory qs = new QuickSearchFactory();
        Collection<QuickSearch> q = qs.createQuickSearchList(limit);
        int count = 0;
        for (QuickSearch o : q) {
            if (count % 1000 == 0) {
                System.out.println(count);
            }
            qs_map.put(o.getQuickSearchKey(), o);
            count++;
        }
        System.out.println("Map has " + qs_map.size());
    }
}
