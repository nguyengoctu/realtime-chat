package com.chatapp.common.config;

/**
 * Thread-local context holder for managing data source routing.
 * Provides thread-safe access to data source type information.
 */
public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

    /**
     * Sets the data source type for the current thread.
     *
     * @param dataSourceType the data source type to set
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    /**
     * Gets the data source type for the current thread.
     *
     * @return the current data source type, or null if not set
     */
    public static DataSourceType getDataSourceType() {
        return contextHolder.get();
    }

    /**
     * Clears the data source type for the current thread.
     * Should be called after completing database operations to prevent memory leaks.
     */
    public static void clearDataSourceType() {
        contextHolder.remove();
    }
}