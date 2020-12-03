package com.csc301.profilemicroservice.service;

import java.util.List;

import com.csc301.profilemicroservice.domain.DbQueryStatus;

public interface SongService {

	DbQueryStatus likeSong(String userName, String songId);
	DbQueryStatus unlikeSong(String userName, String songId);
	DbQueryStatus deleteSongFromDb(String songId);
	DbQueryStatus createPlaylist(String userName);
	Boolean existsByUsername(String userName);
	Boolean songExistsById(String songId);
	DbQueryStatus createSong(String songId);
	List<String> getAllSongsFromUser(String userName);
}
