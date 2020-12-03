package com.csc301.songmicroservice;

import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;
	private final String COLLECTION_NAME = "songs";

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		DbQueryStatus status = new DbQueryStatus();
		if (songToAdd.getSongName() == null || songToAdd.getSongName().isEmpty()
				|| songToAdd.getSongArtistFullName() == null || songToAdd.getSongArtistFullName().isEmpty()
				|| songToAdd.getSongAlbum() == null || songToAdd.getSongAlbum().isEmpty()) {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
			status.setMessage("Request is missing required information.  Check your fields and try again.");
		} else {
			MongoCollection col = db.getCollection(COLLECTION_NAME);
			Document doc = new Document();
			doc.append("songName", songToAdd.getSongName());
			doc.append("songArtistFullName", songToAdd.getSongArtistFullName());
			doc.append("songAlbum", songToAdd.getSongAlbum());
			// ???
			doc.append("songAmountFavourites", (long)0);
			// ???
			col.insertOne(doc);
			songToAdd.setId(doc.getObjectId("_id"));
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			status.setData(songToAdd);
		}
		return status;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		DbQueryStatus status = new DbQueryStatus();
		if (songId == null || songId.isEmpty()) {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
			status.setData(null);
			status.setMessage("Id in request cannot be null.");
		} else {
			Song song = null;
			MongoCollection col = db.getCollection(COLLECTION_NAME);
			FindIterable rs = col.find(Filters.eq("_id", new ObjectId(songId)));
			Document doc = (Document) rs.first();
			song = buildSongFromDoc(doc);
			if (song != null) {
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
				status.setData(song);
			} else {
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				status.setData(null);
				status.setMessage("A song with that id does not exist.");
			}
		}
		return status;
	}

	private Song buildSongFromDoc(Document doc) {
		if (doc == null || doc.isEmpty()) {
			return null;
		}
		Song song = new Song();
		if (doc.containsKey("songAlbum"))
			song.setSongAlbum(doc.getString("songAlbum"));
		if (doc.containsKey("songAmountFavourites"))
			song.setSongAmountFavourites(doc.getInteger("songAmountFavourites"));
		if (doc.containsKey("songName"))
			song.setSongName(doc.getString("songName"));
		if (doc.containsKey("songArtistFullName"))
			song.setSongArtistFullName(doc.getString("songArtistFullName"));
		if (doc.containsKey("_id")) {
			ObjectId docId = doc.getObjectId("_id");
			song.setId(docId);
		}
		return song;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		DbQueryStatus status = findSongById(songId);
		DbQueryStatus updatedStatus = new DbQueryStatus();
		if (status.getData() != null) {
			Song returnedSong = (Song) status.getData();
			updatedStatus.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			updatedStatus.setData(returnedSong.getSongName());
			return updatedStatus;
		} else {
			return status;
		}
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		DbQueryStatus status = new DbQueryStatus();
		if (songId == null || songId.isEmpty()) {
			status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
			status.setData(null);
			status.setMessage("Id in request cannot be null.");
		} else {
			MongoCollection col = db.getCollection(COLLECTION_NAME);
			ObjectId docId = new ObjectId(songId);
			if (col.find(Filters.eq("_id", docId)).first() != null) {
				col.deleteOne(Filters.eq("_id", docId));
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
			} else {
				status.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				status.setMessage("A document with that id does not exist.");
			}
		}
		return status;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		DbQueryStatus updatedStatus = new DbQueryStatus();
		MongoCollection col = db.getCollection(COLLECTION_NAME);
		DbQueryStatus status = findSongById(songId);
		Song returnedSong = (Song) status.getData();
		long updatedCount = returnedSong.getSongAmountFavourites();
		if (updatedCount == 0 && shouldDecrement) {
			updatedStatus.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
			updatedStatus.setMessage("Favourite count for " + songId + "is already 0");
		} else {
			if (shouldDecrement)
				updatedCount--;
			else
				updatedCount++;
			col.findOneAndUpdate(Filters.eq("_id", new ObjectId(songId)),
					new Document("$set", new Document("songAmountFavourites", updatedCount)));
			
			updatedStatus.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
		}
		return updatedStatus;
	}
}