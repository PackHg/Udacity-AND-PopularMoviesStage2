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

package com.packheng.popularmoviesstage2;

/**
 * {@link Movie} class encapsulates data that are relevant to a movie.
 */
public class Movie {
    private String title;
    private String posterUrl;
    private String plotSynopsis;
    private double userRating;
    private String releaseDate;

    public Movie() {
    }

    public Movie(String title, String posterUrl, String plotSynopsis, double userRating, String releaseDate) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public Double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        final String NL = "\n";

        return "Title: " + getTitle() + NL +
                "Overview: " + getPlotSynopsis() + NL +
                "Poster url: " + getPosterUrl() + NL +
                "User rating: " + getUserRating() + NL +
                "Release date: "+ getReleaseDate() + NL;
    }
}
