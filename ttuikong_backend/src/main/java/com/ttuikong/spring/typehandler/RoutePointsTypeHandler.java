package com.ttuikong.spring.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttuikong.spring.model.dto.RoutePoint;

public class RoutePointsTypeHandler extends BaseTypeHandler<List<RoutePoint>> {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<RoutePoint> parameter, JdbcType jdbcType) 
            throws SQLException {
        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("RoutePoints를 JSON으로 변환하는 중 오류 발생", e);
        }
    }

    @Override
    public List<RoutePoint> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<RoutePoint> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<RoutePoint> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }
    
    private List<RoutePoint> parseJson(String json) throws SQLException {
        if (json == null) {
            return null;
        }
        
        try {
            return MAPPER.readValue(json, new TypeReference<List<RoutePoint>>() {});
        } catch (Exception e) {
            throw new SQLException("JSON을 RoutePoints로 변환하는 중 오류 발생", e);
        }
    }
}