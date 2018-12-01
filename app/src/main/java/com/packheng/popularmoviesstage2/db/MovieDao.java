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
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movies WHERE id = :id")
    LiveData<MovieEntry> loadById(int id);

    @Query("SELECT * FROM movies")
    LiveData<List<MovieEntry>> loadAll();

//    @Query("SELECT * FROM movies ORDER BY popularity")
//    LiveData<List<MovieEntry>> loadAllByPopularity();

    @Query("SELECT * FROM movies ORDER BY userRating")
    LiveData<List<MovieEntry>> loadAllByUserRating();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MovieEntry movieEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(MovieEntry movieEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MovieEntry> movieEntries);

    @Delete
    void delete(MovieEntry movieEntry);

    /**
     * Deletes all rows from the movies table.
     */
    @Query("DELETE FROM movies")
    void deleteAll();
}
