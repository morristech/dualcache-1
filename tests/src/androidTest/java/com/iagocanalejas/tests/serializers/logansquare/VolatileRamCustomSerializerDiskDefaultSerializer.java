package com.iagocanalejas.tests.serializers.logansquare;


import com.iagocanalejas.core.Builder;
import com.iagocanalejas.tests.serializers.DualCacheLoganTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicule;

public class VolatileRamCustomSerializerDiskDefaultSerializer extends DualCacheLoganTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, new SerializerForTesting())
                .useSerializerInDisk(DISK_MAX_SIZE, true, defaultCacheSerializer, getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
