import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class GenreTopMoviesMapper extends Mapper<LongWritable, Text, Text, Text> {
    
    private Text genre = new Text();
    private Text movieInfo = new Text();
    
    @Override
    public void map(LongWritable key, Text value, Context context) 
            throws IOException, InterruptedException {
        
        // Skip header line
        if (key.get() == 0) {
            return;
        }
        
        String line = value.toString();
        
        // Check if this is from movies.csv (has title and genres)
        if (line.contains("\"") || line.split(",").length >= 3) {
            // This is movies.csv format: movieId,title,genres
            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            if (tokens.length >= 3) {
                String movieId = tokens[0];
                String title = tokens[1].replaceAll("\"", "");
                String genres = tokens[2];
                
                // Emit movie info for later joining
                context.write(new Text("MOVIE:" + movieId), new Text(title + "|" + genres));
            }
        } else {
            // This is ratings output format: movieId    avgRating,count
            String[] tokens = line.split("\t");
            if (tokens.length >= 2) {
                String movieId = tokens[0];
                String ratingInfo = tokens[1]; // format: avgRating,count
                
                // Emit rating info for later joining
                context.write(new Text("RATING:" + movieId), new Text(ratingInfo));
            }
        }
    }
}
