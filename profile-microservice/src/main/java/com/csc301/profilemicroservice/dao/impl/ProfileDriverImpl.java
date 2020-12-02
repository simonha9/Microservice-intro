package com.csc301.profilemicroservice.dao.impl;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.csc301.profilemicroservice.ProfileMicroserviceApplication;
import com.csc301.profilemicroservice.dao.PlaylistDriver;
import com.csc301.profilemicroservice.dao.ProfileDriver;
import com.csc301.profilemicroservice.dao.ProfilePlaylistRelationshipDriver;
import com.csc301.profilemicroservice.domain.DbQueryExecResult;
import com.csc301.profilemicroservice.domain.DbQueryStatus;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	@Autowired
	PlaylistDriver plDriver;
	@Autowired
	ProfilePlaylistRelationshipDriver ppDriver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (!existsByUsername(userName)) {
			try (Session session = driver.session()) {
				session.writeTransaction(tx -> tx.run(
						"MERGE (p:profile {userName: $userName, fullName: $fullName, password: $password})",
						parameters("userName", userName, "fullName", fullName, "password", password)));
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
				status.setMessage("Inserted node");
			}
		}
		return status;
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (existsByUsername(userName) && existsByUsername(frndUserName)) {
			try (Session session = driver.session()) {
				session.writeTransaction(tx -> tx.run(
						"MATCH (pr_1:profile),(pr_2:profile)" + " WHERE pr_1.userName = $userName AND pr_2.userName = $frndUserName"
								+ " CREATE (pr_1)-[r:follows]->(pr_2)",
						parameters("userName", userName, "frndUserName", frndUserName)));
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			status.setMessage("Successfully followed: " + frndUserName);
		} else {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			status.setMessage("Either the user or friend does not exist in the database, please try again.");
		}
		return status;
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (existsByUsername(userName) && existsByUsername(frndUserName)) {
			try (Session session = driver.session()) {
				session.writeTransaction(tx -> tx.run(
						"MATCH (pr_1:profile)-[r:follows]-(pr_2:profile)" + " WHERE pr_1.userName = $userName AND pr_2.userName = $frndUserName"
								+ " DELETE r",
						parameters("userName", userName, "frndUserName", frndUserName)));
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			status.setMessage("Successfully unfollowed: " + frndUserName);
		} else {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			status.setMessage("Either the user or friend does not exist in the database, please try again.");
		}
		return status;
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {

		return null;
	}

	@Override
	public Boolean existsByUsername(String userName) {
		try (Session session = driver.session()) {
			String query = "MATCH (pr:profile) WHERE pr.userName = $userName RETURN pr ";
			StatementResult res = session.run(query, parameters("userName", userName));
			return res != null && res.hasNext();
		}
	}

	@Override
	public Boolean isFollowing(String userName, String frndUserName) {
		Boolean isFollowing = false;
		if (existsByUsername(userName) && existsByUsername(frndUserName)) {
			try (Session session = driver.session()) {
				String query = "MATCH (pr_1:profile)-[r:follows]-(pr_2:profile)" + " WHERE pr_1.userName = $userName AND pr_2.userName = $frndUserName"
						+ " return r";
				StatementResult res = session.run(query, parameters("userName", userName, "frndUserName", frndUserName));
				isFollowing = res != null && res.hasNext();
			}
		} 
		return isFollowing;
	}

	@Override
	public List<String> getAllFriendsOf(String userName) {
		List<String> friendUserNames = null;
		if (existsByUsername(userName)) {
			try (Session session = driver.session()) {
				String query = "MATCH (pr_1:profile {userName: $userName})-[r:follows]-(pr_2:profile) return pr_2.userName as userName";
				StatementResult res = session.run(query, parameters("userName", userName));
				if (res.hasNext()) friendUserNames = new ArrayList<>();
				while(res.hasNext()) {
					Map<String, Object> fieldMap = res.next().asMap();
					friendUserNames.add(fieldMap.get("userName").toString());
				}
			}
		}
		return friendUserNames;
	}
}
