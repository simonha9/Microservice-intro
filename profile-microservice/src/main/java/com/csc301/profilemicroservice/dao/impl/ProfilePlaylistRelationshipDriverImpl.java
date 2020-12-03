package com.csc301.profilemicroservice.dao.impl;

import static org.neo4j.driver.v1.Values.parameters;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;

import com.csc301.profilemicroservice.ProfileMicroserviceApplication;
import com.csc301.profilemicroservice.dao.ProfilePlaylistRelationshipDriver;
import com.csc301.profilemicroservice.domain.DbQueryExecResult;
import com.csc301.profilemicroservice.domain.DbQueryStatus;

@Repository
public class ProfilePlaylistRelationshipDriverImpl implements ProfilePlaylistRelationshipDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	@Override
	public DbQueryStatus createRelationship(String userName) {
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (!existsByUsername(userName)) {
			String plName = userName + "-favorites";
			try (Session session = driver.session()) {
				session.writeTransaction(tx -> tx.run(
						"MATCH (pr:profile),(pl:playlist)" + " WHERE pr.userName = $userName AND pl.plName = $plName"
								+ " CREATE (pr)-[r:created]->(pl)",
						parameters("userName", userName, "plName", plName)));
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
		}
		return status;
	}

	@Override
	public Boolean existsByUsername(String userName) {
		String plName = userName + "-favorites";
		try (Session session = driver.session()) {
			String query = "MATCH (pr:profile {userName: $userName})-[r:created]-(pl:playlist {plName: $plName}) RETURN r ";
			StatementResult res = session.run(query, parameters("userName", userName, "plName", plName));
			return res != null && res.hasNext();
		}
	}

}
