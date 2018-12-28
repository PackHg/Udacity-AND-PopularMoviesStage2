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

package com.packheng.popularmoviesstage2.data.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import java.util.Date;

@Entity(tableName = "movies")
public class MovieEntry extends Movie {

    @Ignore
    public MovieEntry(int movieId, String title, String posterUrl, String plotSynopsis,
                      double userRating, Date releaseDate) {
        super(movieId, title, posterUrl, plotSynopsis, userRating, releaseDate);
    }

    public MovieEntry(int id, int movieId, String title, String posterUrl, String plotSynopsis,
                 double userRating, Date releaseDate) {
        super(id, movieId, title, posterUrl, plotSynopsis, userRating, releaseDate);
    }
}

