package com.csc301.profilemicroservice.service;

import com.csc301.profilemicroservice.domain.DbQueryStatus;
import com.csc301.profilemicroservice.domain.Profile;

public interface ProfileService {

	public DbQueryStatus createProfile(Profile profile);
	public DbQueryStatus followFriend(String userName, String frndUserName);
	public Boolean isFollowing(String userName, String frndUserName);
	public DbQueryStatus unfollowFriend(String userName, String frndUserName);
	public DbQueryStatus getAllFriendFavouriteSongTitles(String userName);
}
