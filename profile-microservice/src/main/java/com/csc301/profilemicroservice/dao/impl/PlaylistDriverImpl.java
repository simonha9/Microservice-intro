package com.csc301.profilemicroservice.dao.impl;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;

import com.csc301.profilemicroservice.ProfileMicroserviceApplication;
import com.csc301.profilemicroservice.dao.PlaylistDriver;
import com.csc301.profilemicroservice.domain.DbQueryExecResult;
import com.csc301.profilemicroservice.domain.DbQueryStatus;

import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		String plName = userName + "-favorites";
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (existsByUsername(userName)) {
			if (!playlistIncludes(userName, songId)) {
				try (Session session = driver.session()) {
					session.writeTransaction(tx -> tx.run(
							"MATCH (pl:playlist),(s:song)" + " WHERE pl.plName = $plName AND s.songId = $songId"
									+ " CREATE (pl)-[r:includes]->(s)",
							parameters("plName", plName, "songId", songId)));
					status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
				}
			}
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			status.setMessage("Successfully liked song");
		} else {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			status.setMessage("The playlist does not exist in the database, please try again.");
		}
		return status;
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (existsByUsername(userName)) {
			if (playlistIncludes(userName, songId)) {
				String plName = userName + "-favorites";
				try (Session session = driver.session()) {
					session.writeTransaction(tx -> tx.run(
							"MATCH (pl:playlist)-[r:includes]-(s:song)" + " WHERE pl.plName = $plName AND s.songId = $songId"
									+ " DELETE r",
							parameters("plName", plName, "songId", songId)));
					status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
				}
			}
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			status.setMessage("Successfully unliked song");
		} else {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			status.setMessage("The playlist does not exist in the database, please try again.");
		}
		return status;
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {

		return null;
	}

	@Override
	public DbQueryStatus createPlaylist(String userName) {
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (!existsByUsername(userName)) {
			String plName = userName + "-favorites";
			try (Session session = driver.session()) {
				session.writeTransaction(
						tx -> tx.run("MERGE (p:playlist {plName: $plName})", parameters("plName", plName)));
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			}
		}
		return status;
	}

	@Override
	public Boolean existsByUsername(String userName) {
		String plName = userName + "-favorites";
		try (Session session = driver.session()) {
			String query = "MATCH (pl:playlist) WHERE pl.plName = $plName RETURN pl ";
			StatementResult res = session.run(query, parameters("plName", plName));
			return res != null && res.hasNext();
		}
	}

	@Override
	public Boolean songExistsById(String songId) {
		try (Session session = driver.session()) {
			String query = "MATCH (s:song) WHERE s.songId = $songId RETURN s ";
			StatementResult res = session.run(query, parameters("songId", songId));
			return res != null && res.hasNext();
		}
	}

	@Override
	public DbQueryStatus createSong(String songId) {
		DbQueryStatus status = new DbQueryStatus();
		try (Session session = driver.session()) {
			String query = "CREATE (s:song {songId: $songId})";
			StatementResult res = session.run(query, parameters("songId", songId));
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
		}
		return status;
	}

	@Override
	public Boolean playlistIncludes(String userName, String songId) {
		String plName = userName + "-favorites";
		try (Session session = driver.session()) {
			String query = "MATCH (pl:playlist)-[r:includes]-(s:song)" + " WHERE pl.plName = $plName AND s.songId = $songId"
					+ " return r";
			StatementResult res = session.run(query, parameters("plName", plName, "songId", songId));
			return res != null && res.hasNext();
		}
	}

	@Override
	public List<String> getAllSongsFromUser(String userName) {
		List<String> songs = null;
		String plName = userName + "-favorites";
		if (existsByUsername(userName)) {
			try (Session session = driver.session()) {
				String query = "MATCH (pl:playlist {plName: $plName})-[r:includes]-(s:song) return s.songId as songId";
				StatementResult res = session.run(query, parameters("plName", plName));
				if (res.hasNext()) songs = new ArrayList<>();
				while(res.hasNext()) {
					Map<String, Object> fieldMap = res.next().asMap();
					songs.add(fieldMap.get("songId").toString());
				}
			}
		}
		return songs;
	}
}
