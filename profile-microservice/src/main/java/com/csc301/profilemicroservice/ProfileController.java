package com.csc301.profilemicroservice;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.domain.DbQueryStatus;
import com.csc301.profilemicroservice.domain.Profile;
import com.csc301.profilemicroservice.service.ProfileService;
import com.csc301.profilemicroservice.service.SongService;

import okhttp3.OkHttpClient;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";
	
	public static final String songBasePath = "http://localhost:3001/";

	@Autowired
	ProfileService profileService;
	@Autowired
	SongService songService;

	OkHttpClient client = new OkHttpClient();

//	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
//		this.profileDriver = profileDriver;
//		this.playlistDriver = playlistDriver;
//	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		Profile profile = buildProfileFromParams(params);
		DbQueryStatus status = profileService.createProfile(profile);
		
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		DbQueryStatus status = profileService.followFriend(userName, friendUserName);
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus status = profileService.getAllFriendFavouriteSongTitles(userName);
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus status = profileService.unfollowFriend(userName, friendUserName);
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus status = songService.likeSong(userName, songId);
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus status = songService.unlikeSong(userName, songId);
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		
		return null;
	}
	
	@RequestMapping(value = "/createSongNode", method = RequestMethod.POST)
	public Map<String, Object> createSongNode(@RequestParam Map<String, String> params,
			HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		String songId = params.get("songId");
		DbQueryStatus status = songService.createSong(songId);
		response.put("message", status.getMessage());
		response = Utils.setResponseStatus(response, status.getdbQueryExecResult(), status.getData());
		return response;
	}
	
	private Profile buildProfileFromParams(Map<String, String> params) {
		Profile profile = new Profile();
		if (params.containsKey(KEY_USER_NAME)) profile.setUserName(params.get(KEY_USER_NAME));
		if (params.containsKey(KEY_USER_FULLNAME)) profile.setFullName(params.get(KEY_USER_FULLNAME));
		if (params.containsKey(KEY_USER_PASSWORD)) profile.setPassword(params.get(KEY_USER_PASSWORD));
		return profile;
	}
}