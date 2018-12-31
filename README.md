# Popular Movies (Stage 2)  Project
3rd project of the Udacity Android Developer Nanodegree.

## Project Overview
The **Popular Movies (Stage 2)** app shows a grid view of either most popular, top rated of favorite movies.

The user can
* see the details of a movie by clicking on its poster;
* refresh the movies data by swiping down or selecting "Refresh" in the options menu when the main view shows either most popular or top rated movies;
* change the sort type to either Most Popular Movies, Top Rated Movies or Favorite Movies in the settings.

In the detailed view of a selected movie, the user can:
* favorite or unfavorite a movie;
* also see the movie's trailers and reviews if they exist;
* launch a trailer's Youtube video when clicking on this trailer's thumbnail;
* share the movie's title and its first trailer's Youtube URL.

<img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen00.png" width="300"> <img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen01.png" width="300">
<img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen02.png" width="300"> <img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen03.png" width="300">
<img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen04.png" width="300">
<img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen05land.png" width="600">
<img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen06.png" width="300">
<img src="https://raw.githubusercontent.com/PackHg/Udacity-AND-PopularMoviesStage2/master/screencopies/screen07land.png" width="600">

This app is for learning purpose.
It uses the database of [The Movie DB](https://www.themoviedb.org), it is not condoned by The Movie DB.

## API Key
The API Key can be requested at [The Movie DB](https://www.themoviedb.org/account/signup).

It should be defined as the following:

  MyTheMovieDbOrg_ApiKey="Your api key"

in gradle.properties file in your home directory under .gradle directory.
Please refer to [Hiding API keys from your Android repository](https://medium.com/code-better/hiding-api-keys-from-your-android-repository-b23f5598b906) for the instructions.

## Third party libraries used
* [Picasso](https://square.github.io/picasso/)
* [Retrofit](https://square.github.io/retrofit/)

## License
Copyright (C) 2018 Pack Heng

Licensed under the Apache License, Version 2.0 (the "License");
you may not use these files except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
