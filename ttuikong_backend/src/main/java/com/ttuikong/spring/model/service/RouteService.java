package com.ttuikong.spring.model.service;

import java.util.List;

import com.ttuikong.spring.model.dto.Route;

public interface RouteService {

	int saveRoute(Route route);

	List<Route> getUserRoutes(int id);

	Route getRoute(int routeId);

	boolean deleteRoute(int routeId);

	boolean updateRouteName(int routeId, String newName);

}
