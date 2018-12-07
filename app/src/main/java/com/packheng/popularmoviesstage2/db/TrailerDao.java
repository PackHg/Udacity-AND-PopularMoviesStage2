/*
 * Copyright (c) 2018 Pack Heng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.packheng.popularmoviesstage2.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TrailerDao {

    @Query("SELECT * FROM trailers WHERE id = :id")
    LiveData<TrailerEntry> loadTrailerWithId(int id);

    @Query("SELECT * FROM trailers WHERE movieId = :movieId")
    LiveData<List<TrailerEntry>> loadTrailersWithMovieId(int movieId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrailer(TrailerEntry trailerEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrailers(List<TrailerEntry> trailerEntries);

    @Delete
    void deleteTrailer(TrailerEntry trailerEntry);

    @Query("DELETE FROM trailers WHERE movieId = :movieId")
    void deleteTrailersWithMovieId(int movieId);

    @Query("DELETE FROM trailers")
    void deleteAllTrailers();

}
