package self.robin.tools.message.builder;

import javassist.*;
import org.apache.commons.lang3.StringUtils;
import self.robin.tools.JavassistHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 袋子工厂，生产袋子
 *
 * @author Li Yalei - Robin
 * @since 2021/4/22 17:57
 */
public class MessageProxyFactory {

    /**
     * 生产一个模板对象实例
     *
     * @return 消息实例
     * @throws Exception ...
     */
    public static <T extends Message> T create(Class<T> message) throws Exception {
        return (T) new DefaultMessageProxy(new Class[]{message}).newInstance();
    }

    public static <T extends Message> Class<?> getProxyClass(Class<T> message) throws Exception {
        return new DefaultMessageProxy(new Class[]{message}).getProxyClass();
    }


    interface MessageProxy<T extends Message> {

        /**
         * 构建方法体实现
         *
         * @return ...
         */
        String bodyBuild(String template, String[] varNames);

        /**
         * 获取代理的接口类型
         *
         * @return ...
         */
        Class<T>[] getProxyType();

        /**
         * 创建一个实例
         *
         * @return ...
         * @throws Exception ...
         */
        default T newInstance() throws Exception {
            Class<T> clazz = getProxyClass();
            return clazz.newInstance();
        }

        default Class<T> getProxyClass() throws Exception {
            ClassPool pool = JavassistHelper.getDefaultClassPool();
            CtClass abstractBag = pool.get(AbstractBuilder.class.getCanonicalName());
            CtClass ctClass = create(pool, abstractBag);
            //ctClass.writeFile("D:/javassist/");
            return (Class<T>) ctClass.toClass();
        }

        /**
         * 克隆消息接口的方法，后面将做实现
         *
         * @param pool
         * @param originClass
         * @param methodName
         * @param inputTypes
         * @param targetClass
         * @return
         * @throws NotFoundException
         * @throws CannotCompileException
         */
        default CtMethod cloneCtMethod(ClassPool pool, CtClass originClass, String methodName, Class[] inputTypes, CtClass targetClass)
                throws NotFoundException, CannotCompileException {
            List<CtClass> list = new ArrayList<>();
            for (Class clazz : inputTypes) {
                list.add(pool.getCtClass(clazz.getCanonicalName()));
            }
            CtMethod originCtMethod = originClass.getDeclaredMethod(methodName, list.toArray(new CtClass[0]));
            return CtNewMethod.copy(originCtMethod, targetClass, null);
        }

        /**
         * 添加方法实现
         *
         * @param pool
         * @param providerClass
         * @param targetClass
         * @param method
         * @param template
         * @throws NotFoundException
         * @throws CannotCompileException
         */
        default void methodAddAndImpl(ClassPool pool, CtClass providerClass, CtClass targetClass, Method method, Template template)
                throws NotFoundException, CannotCompileException {
            CtMethod ctMethod = cloneCtMethod(pool, providerClass, method.getName(), method.getParameterTypes(), targetClass);
            //获取所有变量名
            String[] varNames = Arrays.stream(method.getParameters()).map(p -> p.getName()).toArray(String[]::new);
            String body = bodyBuild(template.value(), varNames);
            ctMethod.setBody(body);
            targetClass.addMethod(ctMethod);
        }

        /**
         * 创建代理对象
         *
         * @param pool
         * @return
         * @throws Exception
         */
        default CtClass create(ClassPool pool, CtClass parentClass) throws CannotCompileException, NotFoundException,
                IllegalAccessException, InstantiationException {
            String[] proxyClasses = Arrays.stream(getProxyType()).map(Class::getCanonicalName).toArray(String[]::new);
            CtClass[] proxyCtClasses = pool.get(proxyClasses);
            String basePackage = MessageProxyFactory.class.getPackage().getName();
            //代理类
            CtClass realBagClass = pool.makeClass(basePackage + ".Proxy" + System.currentTimeMillis());
            //父类
            realBagClass.setSuperclass(parentClass);
            //模板接口
            realBagClass.setInterfaces(proxyCtClasses);

            //方法实现
            for (Class<T> aClass : getProxyType()) {
                CtClass ctClass = pool.getCtClass(aClass.getCanonicalName());
                for (Method method : aClass.getMethods()) {
                    Template template = method.getAnnotation(Template.class);
                    if (template == null) {
                        continue;
                    }
                    methodAddAndImpl(pool, ctClass, realBagClass, method, template);
                }
            }
            return realBagClass;
        }
    }

    /**
     * 构建热聊消息代理对象
     */
    static class DefaultMessageProxy<T extends Message> implements MessageProxy<T> {

        private Class<T>[] proxyTypes;

        public DefaultMessageProxy(Class<T>[] proxyTypes) {
            this.proxyTypes = proxyTypes;
        }

        private static String methodBody = "" +
                "Object[] args = new Object[]{${params}};" +
                "String[] varNames = new String[]{${varNames}};" +
                "String template = \"${template}\";" +
                "return buildMessage(template, varNames, args);";

        @Override
        public String bodyBuild(String template, String[] varNames) {
            List<String> params = new ArrayList<>();
            List<String> vars = new ArrayList<>();
            for (int i = 0; i < varNames.length; i++) {
                params.add("$" + (i + 1));
                vars.add("\"" + varNames[i] + "\"");
            }
            String paramsStr = params.stream().collect(Collectors.joining(","));
            String varNamesStr = vars.stream().collect(Collectors.joining(","));

            String[] bodyVars = new String[]{"${params}", "${varNames}", "${template}"};
            String[] bodyValues = new String[]{paramsStr, varNamesStr, template};
            return "{" + StringUtils.replaceEach(methodBody, bodyVars, bodyValues) + "}";
        }

        @Override
        public Class<T>[] getProxyType() {
            return this.proxyTypes;
        }
    }

}
