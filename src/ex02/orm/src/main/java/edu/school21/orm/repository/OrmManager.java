package edu.school21.orm.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.school21.orm.annotations.OrmColumn;
import edu.school21.orm.annotations.OrmColumnId;
import edu.school21.orm.annotations.OrmEntity;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrmManager {
    private DataSource dataSource;
    private List<Class<?>> classes;

    public OrmManager() {
        dataSource = createDataSource();
        classes = getAnnotatedClasses();
        deleteTables();
        createTables();
    }

    private void deleteTables() {
        String query = "DROP TABLE IF EXISTS ";
        for (Class<?> clas : classes) {
            String table = clas.getAnnotation(OrmEntity.class).table();
            executeCreateUpdate(query + table + " cascade;");
        }
    }

    private void createTables() {
        for (Class<?> c : classes) {
            Field idField = Arrays.stream(c.getDeclaredFields())
                    .filter(filed -> filed.getAnnotation(OrmColumnId.class) != null)
                    .findFirst().get();
            List<Field> annotatedFields = Arrays.stream(c.getDeclaredFields())
                    .filter(filed -> filed.getAnnotation(OrmColumn.class) != null)
                    .collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            sb.append("Create table if not exists ")
                    .append(c.getAnnotation(OrmEntity.class).table())
                    .append("( ")
                    .append(idField.getName())
                    .append(" serial primary key, ");
            for (Field f : annotatedFields) {
                String name = f.getAnnotation(OrmColumn.class).name();
                String sqlType = getSqlType(f);
                sb.append(f.getAnnotation(OrmColumn.class).name())
                        .append(" ")
                        .append(sqlType);
                if (sqlType.equals("VARCHAR")) {
                    sb.append("(")
                            .append(f.getAnnotation(OrmColumn.class).length())
                            .append(")");
                }
                sb.append(", ");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append(");");
            executeCreateUpdate(sb.toString());
        }
    }

    private void executeCreateUpdate(String string) {
        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement()) {
            statement.executeUpdate(string);
            System.out.println(string);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSqlType(Field f) {
        String type = f.getType().getSimpleName();
        switch (type) {
            case "String":
                return "VARCHAR";
            case "Integer":
                return "INT";
            case "Long":
                return "BIGINT";
            case "Double":
                return "DOUBLE";
            case "Boolean":
                return "BOOLEAN";
            default:
                return null;
        }
    }

    private List<Class<?>> getAnnotatedClasses() {
        List<Class<?>> classes = new ArrayList<>();
        Reflections reflections = new Reflections("edu.school21.orm.entity", new SubTypesScanner(false));

        Set<Class<?>> allClasses =
                reflections.getSubTypesOf(Object.class);
        for (Class<?> c : allClasses) {
            if (c.getAnnotation(OrmEntity.class) != null) {
                classes.add(c);
            }
        }
        return classes;
    }

    private DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setUsername("postgres");
        config.setPassword("1qaz");
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres?useSSL=false");
        return new HikariDataSource(config);
    }

    public void save(Object entity) {
        String table = entity.getClass().getAnnotation(OrmEntity.class).table();
        List<Field> annotatedFields = Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(OrmColumn.class) != null).collect(Collectors.toList());
        annotatedFields.forEach(f -> f.setAccessible(true));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ")
                .append(table)
                .append("(");
        annotatedFields.forEach(f -> {
                stringBuilder.append(f.getAnnotation(OrmColumn.class).name());
                stringBuilder.append(", ");
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append(") values(");

        annotatedFields.forEach(f -> {
            try {
                boolean bracketsNeeded = false;
                if (getSqlType(f).equals("VARCHAR")) {
                    bracketsNeeded = true;
                }
                if (bracketsNeeded) {
                    stringBuilder.append("'");
                }
                stringBuilder.append(f.get(entity));
                if (bracketsNeeded) {
                    stringBuilder.append("'");
                }
                stringBuilder.append(", ");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append(");");
        executeCreateUpdate(stringBuilder.toString());
    }

    public void update(Object entity) {
        String table = entity.getClass().getAnnotation(OrmEntity.class).table();
        StringBuilder query = new StringBuilder();
        query.append("update ").append(table).append(" set ");
        List<Field> annotatedFields = Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(OrmColumn.class) != null).collect(Collectors.toList());
        annotatedFields.forEach(f -> f.setAccessible(true));

        annotatedFields.forEach(f -> {
            try {
                query.append(f.getAnnotation(OrmColumn.class).name()).append(" = ");
                boolean bracketsNeeded = false;
                if (getSqlType(f).equals("VARCHAR")) {
                    bracketsNeeded = true;
                }
                if (bracketsNeeded) {
                    query.append("'");
                }
                query.append(f.get(entity));
                if (bracketsNeeded) {
                    query.append("'");
                }
                query.append(", ");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        query.deleteCharAt(query.lastIndexOf(","));
        try {
            Field fieldId = Arrays.stream(entity.getClass().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(OrmColumnId.class))
                    .findFirst().get();
            fieldId.setAccessible(true);
            query.append("where id = ")
                    .append(fieldId.get(entity))
                    .append(";");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        executeCreateUpdate(query.toString());
    }

    public <T> T findById(Long id, Class<T> aClass) {
        String table = aClass.getAnnotation(OrmEntity.class).table();
        String query = "SELECT * FROM " + table + " WHERE id = " + id ;
        T o = null;
        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            System.out.println(query);
            while (rs.next()) {
                List<Field> fields = Arrays.stream(aClass.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(OrmColumn.class))
                        .collect(Collectors.toList());
                List<Object> param = new ArrayList<>();
                try {
                    Constructor<T> constructor = aClass.getConstructor();
                    o = constructor.newInstance();
                    Field idField = Arrays.stream(aClass.getDeclaredFields())
                            .filter(f -> f.isAnnotationPresent(OrmColumnId.class))
                            .findFirst().get();
                    idField.setAccessible(true);
                    idField.set(o, rs.getLong(1));
                    for (Field f : fields) {
                        f.setAccessible(true);
                        f.set(o, rs.getObject(f.getAnnotation(OrmColumn.class).name()));
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return o;
    }

}
