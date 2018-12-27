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

import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Parent class used to extend {@link TrailerEntry} and {@link FavoriteTrailerEntry}.
 */
public class Trailer {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String trailerId;
    private int movieId;
    private String youtubeKey;
    private String site;
    private String type;

    @Ignore
    public Trailer(String trailerId, int movieId , String youtubeKey, String site, String type) {
        this.trailerId = trailerId;
        this.movieId = movieId;
        this.youtubeKey = youtubeKey;
        this.site = site;
        this.type = type;
    }

    public Trailer(int id, String trailerId, int movieId , String youtubeKey, String site, String type) {
        this.id = id;
        this.trailerId = trailerId;
        this.movieId = movieId;
        this.youtubeKey = youtubeKey;
        this.site = site;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getTrailerId() {
        return trailerId;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getYoutubeKey() {
        return youtubeKey;
    }

    public String getSite() {
        return site;
    }

    public String getType() {
        return type;
    }
}
