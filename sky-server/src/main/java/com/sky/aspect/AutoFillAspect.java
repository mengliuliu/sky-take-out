package com.sky.aspect;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;

import lombok.extern.slf4j.Slf4j;

/**
 * 自动填充切面
 */
@Component
@Aspect
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点 自动填充创建时间和更新时间
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void pointcut() {
    }

    /**
     * 前置通知
     */
    @Before("pointcut()")
    public void autoFillCreateTimeAndUpdateTime(JoinPoint joinPoint) {
        log.info("自动填充创建时间和更新时间");

        // 获取当前被拦截的方法上的数据库操作类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        // 获取方法参数列表的实体对象
        Object[] args = joinPoint.getArgs();
        Object entity = args[0];

        // 获取当前时间、当前人
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 使用工具类反射设置创建时间和更新时间
        if (operationType == OperationType.INSERT) {
            try {
                Method setCreateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTimeMethod.invoke(entity, now);
                setUpdateTimeMethod.invoke(entity, now);
                setCreateUserMethod.invoke(entity, currentId);
                setUpdateUserMethod.invoke(entity, currentId);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTimeMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUserMethod = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                
                setUpdateTimeMethod.invoke(entity, now);
                setUpdateUserMethod.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
