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

@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    @Around("@annotation(readOnlyRepository)")
    public Object setReadDataSourceType(ProceedingJoinPoint joinPoint, ReadOnlyRepository readOnlyRepository) throws Throwable {
        try {
            DataSourceContextHolder.setDataSourceType(DataSourceType.READ);
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    @Around("@annotation(writeRepository)")
    public Object setWriteDataSourceType(ProceedingJoinPoint joinPoint, WriteRepository writeRepository) throws Throwable {
        try {
            DataSourceContextHolder.setDataSourceType(DataSourceType.WRITE);
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    @Around("@within(readOnlyRepository)")
    public Object setReadDataSourceTypeClass(ProceedingJoinPoint joinPoint, ReadOnlyRepository readOnlyRepository) throws Throwable {
        try {
            DataSourceContextHolder.setDataSourceType(DataSourceType.READ);
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

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