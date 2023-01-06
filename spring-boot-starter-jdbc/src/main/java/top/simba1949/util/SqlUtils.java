package top.simba1949.util;

import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

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

    /**
     * 组装 INSERT SQL
     * @param aClass
     * @return
     */
    public static String getInsertSql(Class<?> aClass){
        return getInsertSql(aClass, Collections.emptySet());
    }

    /**
     * 组装 INSERT SQL
     * <p>
     *     SQL 示例：
     *     INSERT INTO asset_base_bill (bill_name, parent_id, parent_name) VALUES('入库单', 0, '入库单');
     * </p>
     *
     * @param aClass
     * @param ignoreColumns
     * @return
     */
    public static String getInsertSql(Class<?> aClass, Set<String> ignoreColumns){
        // 默认添加忽略序列化字段和id字段
        if (CollectionUtils.isEmpty(ignoreColumns)){
            ignoreColumns = new HashSet<>();
        }
        ignoreColumns.add("serialVersionUID");
        ignoreColumns.add("id");

        // 获取表名
        String tableName = getTableName(aClass);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");

        StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append(") VALUES(");

        Field[] declaredFields = aClass.getDeclaredFields();
        int index = 0;
        for (Field field : declaredFields) {
            field.setAccessible(true);

            String fieldName = field.getName();
            if (CollectionUtils.isNotEmpty(ignoreColumns) && ignoreColumns.contains(fieldName)){
                continue;
            }

            // 如果存在Column注解，并且 Column.name() 不为空，获取 Column.name() 为字段名称，获取不到默认为字段名称
            String filedColumnName = fieldName;
            if (field.isAnnotationPresent(Column.class) &&
                    null != field.getAnnotation(Column.class) &&
                    StringUtils.isNotBlank(field.getAnnotation(Column.class).name())){
                filedColumnName = field.getAnnotation(Column.class).name();
            }

            sqlBuilder.append(filedColumnName);
            valueBuilder.append("?"); // ? 半角问号占位符

            // 不是最后一个字段需要使用分割符
            int lastRealColumnIndex = declaredFields.length - ignoreColumns.size() - 1;
            if (index != lastRealColumnIndex){
                sqlBuilder.append(", ");
                valueBuilder.append(", ");
            }

            index++;
        }

        valueBuilder.append(")");
        String sql = sqlBuilder.append(valueBuilder).toString();
        log.info("最终生成的SQL是：{}", sql);
        return sql;
    }


    public static <T> Object[] getInsertVal(T data) {
        return getInsertVal(data, Collections.emptySet());
    }
    /**
     * 获取对应的值
     * @param data
     * @param ignoreColumns
     * @param <T>
     * @return
     */
    public static <T> Object[] getInsertVal(T data, Set<String> ignoreColumns) {
        if (CollectionUtils.isEmpty(ignoreColumns)){
            ignoreColumns = new HashSet<>();
        }
        ignoreColumns.add("id");
        ignoreColumns.add("serialVersionUID");

        Class<?> aClass = data.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();

        List list = new ArrayList<>();
        for (Field field : declaredFields) {
            field.setAccessible(true);

            String fieldName = field.getName();
            if (ignoreColumns.contains(fieldName)){
                continue;
            }

            try {
                list.add(field.get(data));
            }catch (Exception e){
                log.info("获取数据失败{}", e.getMessage(), e);
            }

        }

        log.info("获取的数据值为{}", JSONArray.toJSONString(list));

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
        if (null == preparedStatement || null == aClass || CollectionUtils.isEmpty(dataList)){
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
                StringUtils.isNotBlank(field.getAnnotation(Column.class).columnDefinition())){

            // Column.columnDefinition() 存在值，值为数据库类型，如果设置其他等同于没有设置值
            String columnDefinition = field.getAnnotation(Column.class).columnDefinition();

            if ("timestamp".equals(columnDefinition)){
                // 时间戳设置 timestamp
                preparedStatement.setTimestamp(i, getRealTimestampValOrNull(field, record));
            }else if ("datetime".equals(columnDefinition)){
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

    /**
     * 获取表名
     * @param aClass
     * @return
     */
    private static String getTableName(Class<?> aClass){
        Validate.notNull(aClass, "数据异常：无法获取表名");

        // 判断 class 是否存在 @Table 注解，如果存在直接获取表名，如果不存在，默认Class类名
        boolean annotationPresent = aClass.isAnnotationPresent(Table.class);
        if (annotationPresent){
            Table annotation = aClass.getAnnotation(Table.class);
            return annotation.name();
        }

        return aClass.getSimpleName();
    }
}
