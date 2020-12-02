package com.csc301.profilemicroservice.dao;

import com.csc301.profilemicroservice.domain.DbQueryStatus;

public interface ProfilePlaylistRelationshipDriver {
	DbQueryStatus createRelationship(String userName);
	Boolean existsByUsername(String userName);
}