package org.simpleframework.inject.anotation;

import org.simpleframework.core.BeanContainer;
import org.simpleframework.util.ClassUtil;
import org.simpleframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 依赖注入类，注入成员变量的实例
 */
public class DependencyInjector {
    /**
     * Bean容器
     */
    private BeanContainer beanContainer;

    public DependencyInjector() {
        beanContainer = BeanContainer.getInstance();
    }

    public void doIoc() {
        if (CollectionUtils.isEmpty(beanContainer.getClasses())) {
            return;
        }
        for (Class<?> clazz : beanContainer.getClasses()
        ) {
            Field[] fields = clazz.getDeclaredFields();
            if (fields.length == 0) {
                return;
            }
            for (Field field : fields
            ) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String autowiredValue = autowired.value();
                    Class<?> fieldClass = field.getType();
                    Object fieldValue = getFieldInstance(fieldClass, autowiredValue);
                    Object targetBean = beanContainer.getBean(clazz);
                    ClassUtil.setField(field, targetBean, fieldValue, true);
                }
            }
        }

    }

    /**
     * 根据class在beanContainer里获取其实例或者发现类
     *
     * @param fieldClass
     * @return
     */
    private Object getFieldInstance(Class<?> fieldClass, String autowiredVaule) {
        Object fieldValue = beanContainer.getBean(fieldClass);
        if (fieldValue != null) {
            return fieldValue;
        } else {
            Class<?> implementClass = getImplementClass(fieldClass, autowiredVaule);
            if (implementClass != null) {
                return beanContainer.getBean(implementClass);
            } else {
                return null;
            }
        }
    }

    /**
     * 这里如果是接口的话，获取其实现类
     * @param fieldClass
     * @param autowiredValue
     * @return
     */
    private Class<?> getImplementClass(Class<?> fieldClass, String autowiredValue) {
        Set<Class<?>> classSet = beanContainer.getClassBySuper(fieldClass);
        if (CollectionUtils.isEmpty(classSet)) {
            return null;
        }
        if (autowiredValue != null) {
            if (classSet.size() == 1) {
                return classSet.iterator().next();
            } else {
                throw new RuntimeException(fieldClass.getName());
            }
        } else {
            for (Class<?> clazz : classSet
            ) {
                if (autowiredValue.equals(clazz.getSimpleName())) {
                    return clazz;
                }
            }
        }
        return null;
    }

}
