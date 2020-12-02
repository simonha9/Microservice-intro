package com.csc301.profilemicroservice.dao;

import java.util.List;

import com.csc301.profilemicroservice.domain.DbQueryStatus;

public interface ProfileDriver {
	DbQueryStatus createUserProfile(String userName, String fullName, String password);
	DbQueryStatus followFriend(String userName, String frndUserName);
	DbQueryStatus unfollowFriend(String userName, String frndUserName );
	DbQueryStatus getAllSongFriendsLike(String userName);
	Boolean existsByUsername(String userName);
	Boolean isFollowing(String userName, String frndUserName);
	List<String> getAllFriendsOf(String userName);
}