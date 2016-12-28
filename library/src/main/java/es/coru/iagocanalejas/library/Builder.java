package es.coru.iagocanalejas.library;

import android.content.Context;



import java.io.File;

import es.coru.iagocanalejas.library.interfaces.CacheSerializer;
import es.coru.iagocanalejas.library.interfaces.SizeOf;
import es.coru.iagocanalejas.library.modes.DualCacheDiskMode;
import es.coru.iagocanalejas.library.modes.DualCacheRamMode;
import es.coru.iagocanalejas.library.modes.DualCacheVolatileMode;

/**
 * Class used to build a cache.
 *
 * @param <T> is the class of object to store in cache.
 */
public class Builder<T> {

    /**
     * Defined the sub folder from {@link Context#getCacheDir()} used to store all
     * the data generated from the use of this library.
     */
    private static final String CACHE_FILE_PREFIX = "dualcache";

    // Basic conf
    private String mCacheId;
    private int mAppVersion;
    private boolean mLogEnabled;

    // Ram conf
    private int mMaxRamSizeBytes;
    private DualCacheRamMode mRamMode;
    private CacheSerializer<T> mRamSerializer;
    private SizeOf<T> mSizeOf;

    // Disk conf
    private int mMaxDiskSizeBytes;
    private DualCacheDiskMode mDiskMode;
    private CacheSerializer<T> mDiskSerializer;
    private File mDiskFolder;

    // Persistence conf
    private DualCacheVolatileMode volatileMode;
    private Long mPersistenceTime;

    /**
     * Start the building of the cache.
     *
     * @param cacheId    is the mCacheId of the cache (should be unique).
     * @param appVersion is the app version of the app. If data are already stored in disk cache
     *                   with previous app version, it will be invalidate.
     */
    public Builder(String cacheId, int appVersion) {
        this.mCacheId = cacheId;
        this.mAppVersion = appVersion;
        this.mRamMode = null;
        this.mDiskMode = null;
        this.mPersistenceTime = null;
        this.volatileMode = DualCacheVolatileMode.PERSISTENCE; // By default all entries are persistent
        this.mLogEnabled = false;
    }

    /**
     * Enabling log from the cache. By default disable.
     *
     * @return the builder.
     */
    public Builder<T> enableLog() {
        this.mLogEnabled = true;
        return this;
    }

    /**
     * Builder the cache. Exception will be thrown if it can not be created.
     *
     * @return the cache instance.
     */
    public DualCache<T> build() {
        if (mRamMode == null) {
            throw new IllegalStateException("No ram mode set");
        }
        if (mDiskMode == null) {
            throw new IllegalStateException("No disk mode set");
        }

        DualCache<T> cache = new DualCache<>(mAppVersion, new Logger(mLogEnabled), mRamMode, mRamSerializer,
                mMaxRamSizeBytes, mSizeOf, mDiskMode, mDiskSerializer, mMaxDiskSizeBytes, mDiskFolder,
                volatileMode, mPersistenceTime
        );

        boolean isRamDisable = cache.getRAMMode().equals(DualCacheRamMode.DISABLE);
        boolean isDiskDisable = cache.getDiskMode().equals(DualCacheDiskMode.DISABLE);

        if (isRamDisable && isDiskDisable) {
            throw new IllegalStateException(
                    "The ram cache layer and the disk cache layer are "
                            + "disable. You have to use at least one of those "
                            + "layers.");
        }

        return cache;
    }

    /**
     * Use Json serialization/deserialization to store and retrieve object from ram cache.
     *
     * @param maxRamSizeBytes is the max amount of ram in bytes which can be used by the ram cache.
     * @param serializer      is the cache interface which provide serialization/deserialization
     *                        methods
     *                        for the ram cache layer.
     * @return the builder.
     */
    public Builder<T> useSerializerInRam(
            int maxRamSizeBytes, CacheSerializer<T> serializer
    ) {
        this.mRamMode = DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER;
        this.mMaxRamSizeBytes = maxRamSizeBytes;
        this.mRamSerializer = serializer;
        return this;
    }

    /**
     * Store directly objects in ram (without serialization/deserialization).
     * You have to provide a way to compute the size of an object in
     * ram to be able to used the LRU capacity of the ram cache.
     *
     * @param maxRamSizeBytes is the max amount of ram which can be used by the ram cache.
     * @param handlerSizeOf   computes the size of object stored in ram.
     * @return the builder.
     */
    public Builder<T> useReferenceInRam(
            int maxRamSizeBytes, SizeOf<T> handlerSizeOf
    ) {
        this.mRamMode = DualCacheRamMode.ENABLE_WITH_REFERENCE;
        this.mMaxRamSizeBytes = maxRamSizeBytes;
        this.mSizeOf = handlerSizeOf;
        return this;
    }

    /**
     * The ram cache will not be used, meaning that only the disk cache will be used.
     *
     * @return the builder for the disk cache layer.
     */
    public Builder<T> noRam() {
        this.mRamMode = DualCacheRamMode.DISABLE;
        return this;
    }

    /**
     * Use custom serialization/deserialization to store and retrieve objects from disk cache.
     *
     * @param maxDiskSizeBytes is the max size of disk in bytes which an be used by the disk cache
     *                         layer.
     * @param usePrivateFiles  is true if you want to use {@link Context#MODE_PRIVATE} with the
     *                         default disk cache folder.
     * @param serializer       provides serialization/deserialization methods for the disk cache
     *                         layer.
     * @param context          is used to access file system.
     * @return the builder.
     */
    public Builder<T> useSerializerInDisk(
            int maxDiskSizeBytes,
            boolean usePrivateFiles,
            CacheSerializer<T> serializer,
            Context context
    ) {
        File folder = getDefaultDiskCacheFolder(usePrivateFiles, context);
        return useSerializerInDisk(maxDiskSizeBytes, folder, serializer);
    }

    /**
     * Use custom serialization/deserialization to store and retrieve object from disk cache.
     *
     * @param maxDiskSizeBytes is the max size of disk in bytes which an be used by the disk cache
     *                         layer.
     * @param diskCacheFolder  is the folder where the disk cache will be stored.
     * @param serializer       provides serialization/deserialization methods for the disk cache
     *                         layer.
     * @return the builder.
     */
    public Builder<T> useSerializerInDisk(
            int maxDiskSizeBytes, File diskCacheFolder, CacheSerializer<T> serializer
    ) {
        this.mDiskFolder = diskCacheFolder;
        this.mDiskMode = DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER;
        this.mMaxDiskSizeBytes = maxDiskSizeBytes;
        this.mDiskSerializer = serializer;
        return this;
    }

    /**
     * Set a persistence time for all cache entries
     *
     * @param timeInMillis time a cache entry can persist
     * @return the builder
     */
    public Builder<T> useVolatileCache(long timeInMillis) {
        if (volatileMode.equals(DualCacheVolatileMode.VOLATILE_ENTRY)) {
            throw new IllegalStateException("Incompatible cache modes VOLATILE_CACHE and VOLATILE_ENTRY");
        }
        this.mPersistenceTime = timeInMillis;
        this.volatileMode = DualCacheVolatileMode.VOLATILE_CACHE;
        return this;
    }

    /**
     * Allow user to set a different persistence time for each entry
     *
     * @param defaultTime default time for entries
     * @return the builder
     */
    public Builder<T> useVolatileEntry(long defaultTime) {
        if (volatileMode.equals(DualCacheVolatileMode.VOLATILE_CACHE)) {
            throw new IllegalStateException("Incompatible cache modes VOLATILE_ENTRY and VOLATILE_CACHE");
        }
        this.mPersistenceTime = defaultTime;
        this.volatileMode = DualCacheVolatileMode.VOLATILE_ENTRY;
        return this;
    }

    private File getDefaultDiskCacheFolder(boolean usePrivateFiles, Context context) {
        File folder;
        if (usePrivateFiles) {
            folder = context.getDir(CACHE_FILE_PREFIX + this.mCacheId, Context.MODE_PRIVATE);
        } else {
            folder = new File(context.getCacheDir().getPath()
                    + "/" + CACHE_FILE_PREFIX
                    + "/" + this.mCacheId
            );
        }
        return folder;
    }

    /**
     * Use this if you do not want use the disk cache layer, meaning that only the ram cache layer
     * will be used.
     *
     * @return the builder.
     */
    public Builder<T> noDisk() {
        this.mDiskMode = DualCacheDiskMode.DISABLE;
        return this;
    }
}
