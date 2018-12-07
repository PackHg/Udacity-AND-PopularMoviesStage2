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

package com.packheng.popularmoviesstage2.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Defines Endpoints for querying movies data
 */
public interface TMDBEndpointInterface {
    final String API_KEY = "api_key";

    @GET("movie/popular")
    Call<TMDBMovies> popularMovies(@Query(API_KEY) String api_key);

    @GET("movie/top_rated")
    Call<TMDBMovies> topRatedMovies(@Query(API_KEY) String api_key);

    @GET("movie/{movieId}/reviews")
    Call<TMDBReviews> reviews(@Path("movieId") int movieId, @Query(API_KEY) String api_key);

    @GET("movie/{movieId}/videos")
    Call<TMDBTrailers> trailers(@Path("movieId") int movieId, @Query(API_KEY) String api_key);
}
