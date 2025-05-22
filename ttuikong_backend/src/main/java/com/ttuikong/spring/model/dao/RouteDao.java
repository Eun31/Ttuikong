package com.ttuikong.spring.model.dao;

import java.util.List;

import com.ttuikong.spring.model.dto.Route;

public interface RouteDao {

	void insertRoute(Route route);

	Route selectRoute(int routeId);

	List<Route> selectUserRoutes(int userId);

	int deleteRoute(int routeId);

	int updateRouteName(int routeId, String name);

	Integer selectLatestRouteId(int userId);
}
