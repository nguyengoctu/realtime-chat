package com.chatapp.common.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Dynamic data source routing implementation that routes database connections
 * based on the current thread's data source type context.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    /**
     * Determines the current lookup key for data source routing.
     *
     * @return the current data source type from the thread context
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}