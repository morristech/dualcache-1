package com.iagocanalejas.core.configurationsToTest;


import com.iagocanalejas.core.Builder;
import com.iagocanalejas.core.DualCacheTest;
import com.iagocanalejas.core.testobjects.AbstractVehicule;

/**
 * Created by Iago on 26/12/2016.
 */

public class VolatileRamCustomSerializerDiskCustomSerializer extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, new SerializerForTesting())
                .useSerializerInDisk(DISK_MAX_SIZE, true, new SerializerForTesting(), getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }

}