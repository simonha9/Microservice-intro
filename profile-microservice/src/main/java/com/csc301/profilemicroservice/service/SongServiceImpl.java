package com.csc301.profilemicroservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csc301.profilemicroservice.dao.PlaylistDriver;
import com.csc301.profilemicroservice.domain.DbQueryStatus;

@Service
public class SongServiceImpl implements SongService {

	@Autowired
	PlaylistDriver plDriver;
	
	@Override
	public DbQueryStatus createSong(String songId) {
		return plDriver.createSong(songId);
	}

	@Override
	public Boolean songExistsById(String songId) {
		return plDriver.songExistsById(songId);
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		return plDriver.likeSong(userName, songId);
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		return plDriver.unlikeSong(userName, songId);
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		return plDriver.deleteSongFromDb(songId);
	}

	@Override
	public DbQueryStatus createPlaylist(String userName) {
		return plDriver.createPlaylist(userName);
	}

	@Override
	public Boolean existsByUsername(String userName) {
		return plDriver.existsByUsername(userName);
	}

	@Override
	public List<String> getAllSongsFromUser(String userName) {
		return plDriver.getAllSongsFromUser(userName);
	}

}
