package org.simpleframework.util;



import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ClassUtil {
    /**
     * 根据包名获取类的集合
     * @param packageName
     * @return
     */
    public static Set<Class<?>> extractPackageClass(String packageName){
//1.获取类加载器；2.通过类加载器获取到加载的资源信息；3.依据不同资源类型，采用不同方式获取资源的集合
        ClassLoader classLoader=getClassLoader();
        URL url=  classLoader.getResource(packageName.replace(".","/"));
        if(url==null){
            return null;
        }
        Set<Class<?>> classSet=null;
        if(url.getProtocol().equalsIgnoreCase("file")){
            classSet=new HashSet<Class<?>>();
            File packageDirectory=new File(url.getPath());
            extractClassFile(classSet,packageDirectory,packageName);
        }
        return classSet;
    }

    /**
     * 递归获取目标package里面所有的class文件（包括package文件夹里面的文件）
     * @param emptyClassSet
     * @param fileSource
     * @param packageName
     */
    private static void extractClassFile(Set<Class<?>> emptyClassSet, File fileSource, String packageName) {
        if(!fileSource.isDirectory()){
            return;
        }
        //如果是文件夹，则列出其中所有的文件
        File[] files=fileSource.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file.isDirectory()){
                    return true;
                }else{
                    //获取文件的绝对值路径
                    String absoluteFilePath=file.getAbsolutePath();
                    if(absoluteFilePath.endsWith(".class")){
                        //如果是clss文件直接加载
                        addToClassSet(absoluteFilePath);
                    }
                }
                return false;
            }

            private void addToClassSet(String absoluteFilePath) {
                //获取类的全路径
                absoluteFilePath.replace(File.separator,".");
                String className=absoluteFilePath.substring(absoluteFilePath.indexOf(packageName));
                className=className.substring(0,className.lastIndexOf("."));
                //通过反射获取类
                emptyClassSet.add(loadClass(className));
            }
        });
        if(files!=null){
            for (File file:files
                 ) {
                extractClassFile(emptyClassSet,file,packageName);
            }

        }

    }

    public static Class<?> loadClass(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static ClassLoader getClassLoader(){
       return Thread.currentThread().getContextClassLoader();
    }
}
