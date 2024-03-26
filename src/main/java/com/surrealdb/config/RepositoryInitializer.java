package com.surrealdb.config;

import com.surrealdb.driver.model.QueryResult;
import org.aopalliance.intercept.MethodInterceptor;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.surrealdb.config.SurrealDBConnection.getRepoDriver;

@Configuration
public class RepositoryInitializer implements BeanDefinitionRegistryPostProcessor {
    public Set<Class<? extends SurrealCrudRepository>> findSurrealCrudRepositories(String packageName) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packageName))
            .setScanners(new SubTypesScanner(false)));

        return reflections.getSubTypesOf(SurrealCrudRepository.class);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Set<Class<? extends SurrealCrudRepository>> allClasses = findSurrealCrudRepositories("");
        for (Class<? extends SurrealCrudRepository> repoClass : allClasses) {
            Type[] genericInterfaces = repoClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    if (parameterizedType.getRawType().equals(SurrealCrudRepository.class)) {
                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        if (typeArguments.length == 2) {
                            Type entityType = typeArguments[0];
                            Class<?> entityClass = null;
                            if (entityType instanceof Class<?>) {
                                entityClass = (Class<?>) entityType;
                            }
                            registerRepositoryBean(entityClass, registry, repoClass);
                        }
                    }
                }
            }
        }
    }

    private <T, ID, R extends SurrealCrudRepository<T, ID>> void registerRepositoryBean(Class<T> entityType, BeanDefinitionRegistry registry, Class<R> repositoryClass) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addInterface(repositoryClass);
        SurrealCrudRepositoryImpl<T, ID> target = new SurrealCrudRepositoryImpl<>(entityType);

        proxyFactory.addAdvice((MethodInterceptor) invocation -> {
            Method method = invocation.getMethod();
            if (method.isAnnotationPresent(SurrealQuery.class)) {
                String query = method.getAnnotation(SurrealQuery.class).value();
                Object[] args = invocation.getArguments();
                Pattern pattern = Pattern.compile("\\?(\\d+)");
                Matcher matcher = pattern.matcher(query);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    int index = Integer.parseInt(matcher.group(1)) - 1;
                    if (index < args.length) {
                        String replacement = args[index] instanceof String ? "'" + args[index] + "'" : args[index].toString();
                        matcher.appendReplacement(sb, replacement);
                    } else {
                        throw new SQLException("Argument index out of bounds: " + (index + 1));
                    }
                }
                matcher.appendTail(sb);
                String processedQuery = sb.toString();
                Type returnType = method.getGenericReturnType();
                if (returnType instanceof ParameterizedType) {
                    Type[] typeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                    if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> genericClass) {
                        return getRepoDriver().query(processedQuery, Collections.emptyMap(), genericClass);
                    }
                } else {
                    List<QueryResult<T>> results = getRepoDriver().query(processedQuery, Collections.emptyMap(), (Class<? extends T>) returnType);
                    var result = results.stream()
                        .flatMap(qr -> qr.getResult().stream())
                        .toList();
                    return result.get(0);
                }
                throw new SQLException("Unsupported return type for method: " + method.getName());
            } else {
                return invocation.getMethod().invoke(target, invocation.getArguments());
            }
        });

        R repositoryProxy = (R) proxyFactory.getProxy();
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
            .genericBeanDefinition(repositoryClass, () -> repositoryProxy)
            .getBeanDefinition();

        registry.registerBeanDefinition(repositoryClass.getSimpleName(), beanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

}
