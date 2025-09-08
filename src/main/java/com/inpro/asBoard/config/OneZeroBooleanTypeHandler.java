package com.inpro.asBoard.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes({Boolean.class, boolean.class})
@MappedJdbcTypes(value = {JdbcType.VARCHAR, JdbcType.CHAR}, includeNullJdbcType = true)
public class OneZeroBooleanTypeHandler extends BaseTypeHandler<Boolean> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, (parameter != null && parameter) ? "1" : "0");
    }

    private Boolean toBoolean(String s) {
        if (s == null) return null;
        s = s.trim();
        if ("1".equals(s) || "Y".equalsIgnoreCase(s) || "TRUE".equalsIgnoreCase(s))  return true;
        if ("0".equals(s) || "N".equalsIgnoreCase(s) || "FALSE".equalsIgnoreCase(s)) return false;
        return null;
    }

    @Override public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException { return toBoolean(rs.getString(columnName)); }
    @Override public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException { return toBoolean(rs.getString(columnIndex)); }
    @Override public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException { return toBoolean(cs.getString(columnIndex)); }
}

