package com.iagocanalejas.jacksonserializer.configurations;


import com.iagocanalejas.dualcache.Builder;
import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;
import com.iagocanalejas.jacksonserializer.DualCacheJacksonTest;

public class VolatileRamDefaultSerializerNoDisk extends DualCacheJacksonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<String, AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, mDefaultSerializer)
                .noDisk()
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
