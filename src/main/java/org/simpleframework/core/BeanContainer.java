package org.simpleframework.core;

import lombok.NoArgsConstructor;
import org.simpleframework.core.annotation.Component;
import org.simpleframework.core.annotation.Controller;
import org.simpleframework.core.annotation.Reponsitory;
import org.simpleframework.core.annotation.Service;
import org.simpleframework.util.ClassUtil;
import org.simpleframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建容器来管理Bean
 */
@NoArgsConstructor
public class BeanContainer {
    /**
     * 存放所有被配置标记的目标对象的Map
     */
    public final Map<Class<?>, Object> beanMap = new ConcurrentHashMap();

    /**
     * 查询加载状态
     */
    private boolean loaded = false;
    /**
     * 加载类的注解列表
     */
    private static final List<Class<? extends Annotation>> BEAN_ANOTATION = Arrays.asList(Component.class, Controller.class, Reponsitory.class, Service.class);

    public static BeanContainer getInstance() {
        return ContainerHolder.HOLDER.instance;
    }

    public enum ContainerHolder {
        HOLDER;
        private BeanContainer instance;

        ContainerHolder() {
            instance = new BeanContainer();
        }
    }

    /**
     * 根据包名获取包下面有注解的类
     *
     * @param packageName
     */
    public synchronized void loadBeans(String packageName) {
        //判断容器是否已经被加载过
        if (isLoaded()) {
            return;
        }
        Set<Class<?>> classSet = ClassUtil.extractPackageClass(packageName);
        if (CollectionUtils.isEmpty(classSet)) {
            return;
        }
        for (Class<?> clazz : classSet
        ) {
            for (Class<? extends Annotation> anotation : BEAN_ANOTATION
            ) {
                if (clazz.isAnnotationPresent(anotation)) {
                    beanMap.put(clazz, ClassUtil.newInstance(clazz, true));
                }
            }
        }
        loaded = true;
    }

    /**
     * 查看容器是否已经被加载
     *
     * @return
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 查看BEAN中的实例数量
     *
     * @return
     */
    public int size() {
        return beanMap.size();
    }

    /**
     * 添加一个class对象及其实例
     *
     * @param clazz Class对象
     * @param bean  Bean实例
     * @return 原有的Bean实例，没有则返回null
     */
    public Object addBean(Class<?> clazz, Object bean) {
        return beanMap.put(clazz, bean);
    }

    /**
     * 删除一个对象实例
     *
     * @param clazz Class对象
     * @return 返回被删除的对象的值
     */
    public Object removeBean(Class<?> clazz) {
        return beanMap.remove(clazz);
    }

    /**
     * 获取某个对象的实例
     *
     * @param clazz Class对象
     * @return 对象实例
     */
    public Object getBean(Class<?> clazz) {
        return beanMap.get(clazz);
    }

    /**
     * 获取所有对象的集合
     *
     * @return Class集合
     */
    public Set<Class<?>> getClasses() {
        return beanMap.keySet();
    }

    /**
     * 获取所有对象实例集合
     *
     * @return 对象实例集合
     */
    public Set<Object> getBeans() {
        return new HashSet<>(beanMap.values());
    }

    /**
     * 根据注解提取Class对象
     *
     * @param annotation 注解类型
     * @return 该注解对应的Class对象集合
     */
    public Set<Class<?>> getClassByAnnotation(Class<? extends Annotation> annotation) {
        Set<Class<?>> keySet = getClasses();
        Set<Class<?>> classSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(keySet)) {
            for (Class<?> clazz : keySet
            ) {
                if (clazz.isAnnotationPresent(annotation)) {
                    classSet.add(clazz);
                }
            }
            return classSet.size() > 0 ? classSet : null;
        }
        return null;
    }

    /**
     * 通过接口或父类获取实现类或子类的class集合，不包括其本身
     *
     * @param interfaceOrClass 注解类型
     * @return 该注解对应的Class对象集合
     */
    public Set<Class<?>> getClassBySuper(Class<?> interfaceOrClass) {
        Set<Class<?>> keySet = getClasses();
        Set<Class<?>> classSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(keySet)) {
            for (Class<?> clazz : keySet
            ) {
                if (interfaceOrClass.isAssignableFrom(clazz)) {
                    classSet.add(clazz);
                }
            }
            return classSet.size() > 0 ? classSet : null;
        }
        return null;
    }

}
