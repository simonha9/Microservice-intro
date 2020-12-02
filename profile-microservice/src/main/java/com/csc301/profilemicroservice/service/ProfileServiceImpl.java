package com.csc301.profilemicroservice.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.csc301.profilemicroservice.ProfileController;
import com.csc301.profilemicroservice.dao.ProfileDriver;
import com.csc301.profilemicroservice.dao.ProfilePlaylistRelationshipDriver;
import com.csc301.profilemicroservice.domain.DbQueryExecResult;
import com.csc301.profilemicroservice.domain.DbQueryStatus;
import com.csc301.profilemicroservice.domain.Profile;

@Service
public class ProfileServiceImpl implements ProfileService {

	@Autowired
	SongService songService;
	@Autowired
	ProfileDriver prDriver;
	@Autowired
	ProfilePlaylistRelationshipDriver ppDriver;

	@Override
	public DbQueryStatus createProfile(Profile profile) {
		Boolean ok = false;
		DbQueryStatus resultStatus = new DbQueryStatus();
		resultStatus.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		if (profile.getUserName() == null || profile.getUserName().isEmpty() || profile.getFullName() == null
				|| profile.getFullName().isEmpty() || profile.getPassword() == null
				|| profile.getPassword().isEmpty()) {

			resultStatus.setMessage("Missing information is required or empty, try again.");
			return resultStatus;
		}
		DbQueryStatus plStatus = songService.createPlaylist(profile.getUserName());
		if (okStatus(plStatus)) {
			DbQueryStatus prStatus = prDriver.createUserProfile(profile.getUserName(), profile.getFullName(),
					profile.getPassword());
			if (okStatus(prStatus)) {
				DbQueryStatus ppStatus = ppDriver.createRelationship(profile.getUserName());
				if (okStatus(ppStatus)) {
					resultStatus.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
					ok = true;
				}
			}
		}

		if (!ok) {
			resultStatus.setMessage("Could not create Userprofile, try again.");
		} else {
			resultStatus.setMessage("Successfully created userprofile with username: " + profile.getUserName() + ".");
		}
		return resultStatus;
	}

	private boolean okStatus(DbQueryStatus status) {
		return status.getdbQueryExecResult().name().equals(DbQueryExecResult.QUERY_OK.name());
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		DbQueryStatus status = new DbQueryStatus();

		if (!isFollowing(userName, frndUserName)) {
			return prDriver.followFriend(userName, frndUserName);
		} else {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
			status.setMessage(userName + " is already following " + frndUserName + ".");
		}
		return status;
	}

	@Override
	public Boolean isFollowing(String userName, String frndUserName) {
		return prDriver.isFollowing(userName, frndUserName);
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
		if (isFollowing(userName, frndUserName)) {
			status = prDriver.unfollowFriend(userName, frndUserName);
		}
		return status;
	}

	@Override
	public DbQueryStatus getAllFriendFavouriteSongTitles(String userName) {
		List<String> songTitles = new ArrayList<>();
		List<String> songIds = new ArrayList<>();
		DbQueryStatus status = new DbQueryStatus();
		status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
		List<String> friendUserNames = prDriver.getAllFriendsOf(userName);
		if (friendUserNames != null && !friendUserNames.isEmpty()) {
			for (String friendUserName : friendUserNames) {
				List<String> returnedSongIds = songService.getAllSongsFromUser(friendUserName);
				if (returnedSongIds != null) {
					for (String returnedSongId : returnedSongIds) {
						if (!songIds.contains(returnedSongId))
							songIds.add(returnedSongId);
					}
				}
			}
		}
		
		
		RestTemplate restTemplate = new RestTemplate();
		for (String songId : songIds) {
			// get song name
			String path = ProfileController.songBasePath + "getSongTitleById/" + songId;
			Map<String, Object> response = restTemplate.getForObject(path, Map.class);
			if (response != null) {
				songTitles.add(response.get("data").toString());
			}
		}
		status.setMessage("Found friends' favorite songs.");
		status.setData(songTitles);
		return status;
	}

	private static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		result.append("?");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			result.append("&");
		}

		String resultString = result.toString();
		return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
	}

}
