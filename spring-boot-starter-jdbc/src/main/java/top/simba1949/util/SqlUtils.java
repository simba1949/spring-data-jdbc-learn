package top.simba1949.util;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * @author anthony
 * @date 2023/1/6
 */
@Slf4j
public class SqlUtils {

    private static final Set<String> DEFAULT_IGNORE_COLUMN_SET = new HashSet<>();

    static {
        DEFAULT_IGNORE_COLUMN_SET.add("serialVersionUID");
        DEFAULT_IGNORE_COLUMN_SET.add("id");
    }

    /**
     * 组装 INSERT SQL （默认忽略 serialVersionUID 和 id 字段）
     * @param aClass
     * @return
     */
    public static String getInsertSql(Class<?> aClass){
        return getInsertSql(aClass, DEFAULT_IGNORE_COLUMN_SET);
    }

    /**
     * 组装 INSERT SQL
     * <p>
     *      SQL 示例：
     *     INSERT INTO asset_base_bill (bill_name, parent_id, parent_name) VALUES('入库单', 0, '入库单');
     * </p>
     * @param aClass
     * @param ignoreColumns
     * @return
     */
    public static String getInsertSql(Class<?> aClass, Set<String> ignoreColumns){
        Objects.requireNonNull(aClass, "组装 insert SQL 异常");
        if (null == ignoreColumns){ // prevent NPE
            ignoreColumns = new HashSet<>();
        }

        // 获取表名
        String tableName = getTableName(aClass);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");

        StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append(") VALUES(");

        Field[] declaredFields = getAllFieldIncludeSuperExcludeIgnore(aClass, ignoreColumns);

        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);

            String filedColumnName = getColumnName(field);

            sqlBuilder.append(filedColumnName);
            valueBuilder.append("?"); // ? 半角问号占位符

            // 不是最后一个字段时，需要使用分割符
            if (i != declaredFields.length - 1){
                sqlBuilder.append(", ");
                valueBuilder.append(", ");
            }
        }

        valueBuilder.append(")");
        String sql = sqlBuilder.append(valueBuilder).toString();
        log.info("最终生成的SQL是：{}", sql);
        return sql;
    }

    /**
     * 获取对象字段值，并根据字段顺序封装在数组中
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Object[] getInsertVal(T data) {
        return getInsertVal(data, DEFAULT_IGNORE_COLUMN_SET);
    }

    /**
     * 获取对象字段值，并根据字段顺序封装在数组中
     * @param data
     * @param ignoreColumns
     * @param <T>
     * @return
     */
    public static <T> Object[] getInsertVal(T data, Set<String> ignoreColumns) {
        if (null == ignoreColumns){
            ignoreColumns = new HashSet<>(); // prevent NPE
        }

        Class<?> aClass = data.getClass();
        Field[] declaredFields = getAllFieldIncludeSuperExcludeIgnore(aClass, ignoreColumns);

        List list = new ArrayList<>();
        for (Field field : declaredFields) {
            field.setAccessible(true);

            try {
                list.add(field.get(data));
            }catch (Exception e){
                log.info("获取数据失败{}", e.getMessage(), e);
            }

        }

        return list.toArray();
    }


    /**
     * 预处理数据
     * @param preparedStatement
     * @param aClass
     * @param dataList
     * @throws Exception
     */
    public static void preparedStatementDealWith(PreparedStatement preparedStatement, Class<?> aClass, List<?> dataList) throws Exception {
        if (null == preparedStatement || null == aClass || null == dataList || dataList.size() == 0){
            return;
        }

        for (int i = 0; i < dataList.size(); i++) {
            Object record = dataList.get(i);

            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field field : declaredFields) {
                setValOrNull(i + 1, preparedStatement, field, record);
            }
            preparedStatement.addBatch();
        }
    }

    // =============================================
    // ================ private ====================
    // =============================================

    /**
     * 获取表名
     * @param aClass
     * @return
     */
    private static String getTableName(Class<?> aClass){
        // 判断 class 是否存在 @Table 注解，如果存在直接获取表名，如果不存在，默认Class类名
        boolean annotationPresent = aClass.isAnnotationPresent(Table.class);
        if (annotationPresent){
            Table annotation = aClass.getAnnotation(Table.class);
            return annotation.name();
        }

        return aClass.getSimpleName();
    }

    /**
     * 获取字段名称
     * @param field
     * @return
     */
    private static String getColumnName(Field field){
        // 如果存在Column注解，并且 Column.name() 不为空，获取 Column.name() 为字段名称
        // 获取不到默认为字段名称
        String filedColumnName = field.getName();

        if (field.isAnnotationPresent(Column.class) &&
                null != field.getAnnotation(Column.class) &&
                0 != field.getAnnotation(Column.class).name().length()){
            filedColumnName = field.getAnnotation(Column.class).name();
        }

        return filedColumnName;
    }

    /**
     * 获取所有字段（包含父类所有字段）
     * @param aClass
     * @param ignoreColumns
     * @return
     */
    private static Field[] getAllFieldIncludeSuperExcludeIgnore(Class<?> aClass, Set<String> ignoreColumns) {
        List<Field> fieldList = new ArrayList<>();

        while (null != aClass){
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (null != ignoreColumns && ignoreColumns.contains(fieldName)){
                    continue;
                }
                fieldList.add(field);
            }

            aClass = aClass.getSuperclass();
        }

        return fieldList.toArray(new Field[0]);
    }

    /**
     * 设置值
     * @param i
     * @param preparedStatement
     * @param field
     * @param record
     * @throws IllegalAccessException
     * @throws SQLException
     */
    private static void setValOrNull(int i, PreparedStatement preparedStatement, Field field, Object record) throws IllegalAccessException, SQLException {
        field.setAccessible(true);

        Class<?> fieldType = field.getType();

        // 如果 Column.columnDefinition() 设置值，则转换数据库中的 java.sql.Date 和 java.sql.Timestamp
        // 如果 Column.columnDefinition() 不设置值，则使用 preparedStatement.setObject() 设置，存在时区问题，需要业务判断时区问题
        if (Date.class.equals(fieldType) &&
                field.isAnnotationPresent(Column.class) &&
                null != field.getAnnotation(Column.class) &&
                0 != field.getAnnotation(Column.class).columnDefinition().length()){

            // Column.columnDefinition() 存在值，值为数据库类型，需要将对应的时间转换数据库对应类型时间
            // 否则等同于直接使用字段时间，这样可能会存在时区问题
            String columnDefinition = field.getAnnotation(Column.class).columnDefinition();

            if ("timestamp".equalsIgnoreCase(columnDefinition)){
                // 时间戳设置 timestamp
                preparedStatement.setTimestamp(i, getRealTimestampValOrNull(field, record));
            }else if ("datetime".equalsIgnoreCase(columnDefinition)){
                // 日期设置 datetime
                preparedStatement.setDate(i, getRealDateValOrNull(field, record));
            }else {
                // 如果设置其他等同于没有设置值
                preparedStatement.setObject(i, getRealValOrNull(field, record));
            }
            return;
        }

        preparedStatement.setObject(i, getRealValOrNull(field, record));
    }

    /**
     * 获取数据
     * @param field
     * @param record
     * @return
     * @throws IllegalAccessException
     */
    private static Object getRealValOrNull(Field field, Object record) throws IllegalAccessException {
        if (null == field || null == record){
            return null;
        }

        return field.get(record);
    }

    /**
     * 获取日期数据
     * 防止时区问题
     * @param field
     * @param record
     * @return
     * @throws IllegalAccessException
     */
    private static java.sql.Date getRealDateValOrNull(Field field, Object record) throws IllegalAccessException {
        if (null == field || null == record){
            return null;
        }

        Date val = (Date) field.get(record);
        return null == val ? null : new java.sql.Date(val.getTime());
    }

    /**
     * 获取时间戳数据
     * 防止时区问题
     * @param field
     * @param record
     * @return
     * @throws IllegalAccessException
     */
    private static java.sql.Timestamp getRealTimestampValOrNull(Field field, Object record) throws IllegalAccessException {
        if (null == field || null == record){
            return null;
        }

        Date val = (Date) field.get(record);
        return null == val ? null : new java.sql.Timestamp(val.getTime());
    }
}
