import java.io.IOException;
import java.util.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class GenreTopMoviesReducer extends Reducer<Text, Text, Text, Text> {
    
    private Map<String, String> movieGenres = new HashMap<>();
    private Map<String, String> movieRatings = new HashMap<>();
    private Text result = new Text();
    
    @Override
    public void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        
        String keyStr = key.toString();
        
        if (keyStr.startsWith("MOVIE:")) {
            // Store movie genre information
            String movieId = keyStr.substring(6);
            for (Text value : values) {
                movieGenres.put(movieId, value.toString());
            }
        } else if (keyStr.startsWith("RATING:")) {
            // Store movie rating information
            String movieId = keyStr.substring(7);
            for (Text value : values) {
                movieRatings.put(movieId, value.toString());
            }
        }
    }
    
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        // Process all movies and create genre-based rankings
        Map<String, List<MovieInfo>> genreMovies = new HashMap<>();
        
        for (String movieId : movieGenres.keySet()) {
            String genreInfo = movieGenres.get(movieId);
            String ratingInfo = movieRatings.get(movieId);
            
            if (genreInfo != null && ratingInfo != null) {
                String[] genreInfoParts = genreInfo.split("\\|");
                String title = genreInfoParts[0];
                String genres = genreInfoParts[1];
                
                String[] ratingParts = ratingInfo.split(",");
                if (ratingParts.length == 2) {
                    try {
                        double avgRating = Double.parseDouble(ratingParts[0]);
                        int count = Integer.parseInt(ratingParts[1]);
                        
                        // Only consider movies with at least 50 ratings
                        if (count >= 50) {
                            // Split genres and add to each genre list
                            String[] genreList = genres.split("\\|");
                            for (String genreName : genreList) {
                                if (!genreName.equals("(no genres listed)")) {
                                    genreName = genreName.trim();
                                    
                                    if (!genreMovies.containsKey(genreName)) {
                                        genreMovies.put(genreName, new ArrayList<MovieInfo>());
                                    }
                                    
                                    genreMovies.get(genreName).add(
                                        new MovieInfo(movieId, title, avgRating, count)
                                    );
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Skip malformed data
                    }
                }
            }
        }
        
        // Output top 10 movies for each genre
        for (String genre : genreMovies.keySet()) {
            List<MovieInfo> movies = genreMovies.get(genre);
            
            // Sort movies by average rating (descending)
            Collections.sort(movies, new Comparator<MovieInfo>() {
                @Override
                public int compare(MovieInfo m1, MovieInfo m2) {
                    return Double.compare(m2.avgRating, m1.avgRating);
                }
            });
            
            // Output top 10 movies for this genre
            int count = 0;
            for (MovieInfo movie : movies) {
                if (count >= 10) break;
                
                result.set(String.format("Rank %d: %s (ID:%s) - Rating:%.2f (%d ratings)", 
                    count + 1, movie.title, movie.movieId, movie.avgRating, movie.count));
                context.write(new Text(genre), result);
                count++;
            }
        }
    }
    
    // Helper class to store movie information
    private static class MovieInfo {
        String movieId;
        String title;
        double avgRating;
        int count;
        
        public MovieInfo(String movieId, String title, double avgRating, int count) {
            this.movieId = movieId;
            this.title = title;
            this.avgRating = avgRating;
            this.count = count;
        }
    }
}
