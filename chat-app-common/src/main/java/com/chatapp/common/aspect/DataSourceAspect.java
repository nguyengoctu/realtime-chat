package com.chatapp.common.aspect;

import com.chatapp.common.annotation.ReadOnlyRepository;
import com.chatapp.common.annotation.WriteRepository;
import com.chatapp.common.config.DataSourceContextHolder;
import com.chatapp.common.config.DataSourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aspect for intercepting methods annotated with data source routing annotations
 * and setting the appropriate data source context for read/write operations.
 */
@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    /**
     * Intercepts methods annotated with @ReadOnlyRepository and sets the read data source context.
     *
     * @param joinPoint the method execution join point
     * @param readOnlyRepository the read-only repository annotation
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("@annotation(readOnlyRepository)")
    public Object setReadDataSourceType(ProceedingJoinPoint joinPoint, ReadOnlyRepository readOnlyRepository) throws Throwable {
        try {
            DataSourceContextHolder.setDataSourceType(DataSourceType.READ);
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * Intercepts methods annotated with @WriteRepository and sets the write data source context.
     *
     * @param joinPoint the method execution join point
     * @param writeRepository the write repository annotation
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("@annotation(writeRepository)")
    public Object setWriteDataSourceType(ProceedingJoinPoint joinPoint, WriteRepository writeRepository) throws Throwable {
        try {
            DataSourceContextHolder.setDataSourceType(DataSourceType.WRITE);
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * Intercepts methods in classes annotated with @ReadOnlyRepository and sets the read data source context.
     *
     * @param joinPoint the method execution join point
     * @param readOnlyRepository the read-only repository annotation on the class
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("@within(readOnlyRepository)")
    public Object setReadDataSourceTypeClass(ProceedingJoinPoint joinPoint, ReadOnlyRepository readOnlyRepository) throws Throwable {
        try {
            DataSourceContextHolder.setDataSourceType(DataSourceType.READ);
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * Intercepts methods in classes annotated with @WriteRepository and sets the write data source context.
     *
     * @param joinPoint the method execution join point
     * @param writeRepository the write repository annotation on the class
     * @return the result of the method execution
     * @throws Throwable if the method execution throws an exception
     */
    @Around("@within(writeRepository)")
    public Object setWriteDataSourceTypeClass(ProceedingJoinPoint joinPoint, WriteRepository writeRepository) throws Throwable {
        try {
            DataSourceContextHolder.setDataSourceType(DataSourceType.WRITE);
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}