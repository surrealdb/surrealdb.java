package com.surrealdb.config;

import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.refactor.exception.MissingSurrealTableAnnotationException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.surrealdb.config.SurrealDBConnection.getRepoDriver;

public class SurrealCrudRepositoryImpl<T, ID> implements SurrealCrudRepository<T, ID> {

    private final Class<T> entityType;
    SyncSurrealDriver driver = getRepoDriver();
    private final String tableName;

    public SurrealCrudRepositoryImpl(Class<T> entityType) {
        this.entityType = entityType;
        this.tableName = resolveTableName(entityType);
    }

    private static String resolveTableName(Class<?> entityType) {
        if (!entityType.isAnnotationPresent(SurrealTable.class)) {
            throw new MissingSurrealTableAnnotationException(entityType);
        }
        return entityType.getAnnotation(SurrealTable.class).value();
    }

    @Override
    public Optional<T> findById(ID id) {
        String query = String.format("select * from %s where id = '%s';", tableName, id.toString());
        List<QueryResult<T>> response = driver.query(query, Collections.emptyMap(), entityType);
        if (response == null || response.isEmpty()) {
            return Optional.empty();
        }
        List<T> res = response.stream()
                .flatMap(qr -> qr.getResult().stream())
                .toList();
        if (!res.isEmpty()) {
            return Optional.ofNullable(res.get(0));
        }
        return Optional.empty();
    }

    @Override
    public List<T> findAll() {
        String query = String.format("select * from %s;", tableName);
        List<QueryResult<T>> response = driver.query(query, Collections.emptyMap(), entityType);
        if (response == null || response.isEmpty()) {
            return Collections.emptyList();
        }
        return response.stream()
                .flatMap(qr -> qr.getResult().stream())
                .collect(Collectors.toList());
    }

    @Override
    public T save(T entity) {
        processSequenceIds(entity);
        return driver.create(tableName, entity);
    }

    private void processSequenceIds(T entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SurrealSequenceId.class)) {
                field.setAccessible(true);
                try {
                    Object currentValue = field.get(entity);
                    if (currentValue == null) {
                        String sequenceName = field.getAnnotation(SurrealSequenceId.class).sequenceName();
                        Long nextIdValue = generateRouteId(sequenceName);
                        if (field.getType().equals(String.class)) {
                            field.set(entity, String.valueOf(nextIdValue));
                        } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                            field.set(entity, nextIdValue);
                        } else if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
                            field.set(entity, nextIdValue.intValue());
                        } else {
                            throw new IllegalArgumentException("Unsupported field type for @SurrealSequenceId: " + field.getType());
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access or set value for @SurrealSequenceId field", e);
                }
            }
        }
    }

    private Long generateRouteId(String sequenceName) {
        String sequenceKey = "sequence:" + tableName + sequenceName;
        List<QueryResult<SequenceEntity>> response = driver.query(String.format("select seq_value from %s;", sequenceKey), Collections.emptyMap(), SequenceEntity.class);
        List<SequenceEntity> res = response.stream()
                .flatMap(qr -> qr.getResult().stream())
                .toList();
        if (res.isEmpty()) {
            return createNewSequence(sequenceKey);
        }
        Long currentValue = res.get(0).getSeq_value() + 1;
        driver.query(String.format("update %s set seq_value = %d;", sequenceKey, currentValue), Collections.emptyMap(), Object.class);
        return currentValue;

    }

    private Long createNewSequence(String sequenceKey) {
        driver.query(String.format("create %s SET seq_value = 1", sequenceKey), Collections.emptyMap(), Object.class);
        return 1L;
    }

    @Override
    public void delete(T entity) {
        driver.delete(getIdFromEntity(entity));
    }

    @Override
    public T update(T entity) {
        return driver.update(getIdFromEntity(entity), entity).get(0);
    }

    private String getIdFromEntity(T entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SurrealId.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (value != null) {
                        return value.toString();
                    } else {
                        return null;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access @SurrealId field value", e);
                }
            }
        }
        return null;
    }
}

