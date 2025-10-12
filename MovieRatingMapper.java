import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MovieRatingMapper extends Mapper<LongWritable, Text, Text, Text> {
    
    private Text movieId = new Text();
    private Text ratingInfo = new Text();
    
    @Override
    public void map(LongWritable key, Text value, Context context) 
            throws IOException, InterruptedException {
        
        // Skip header line
        if (key.get() == 0) {
            return;
        }
        
        String line = value.toString();
        String[] tokens = line.split(",");
        
        // Expected format: userId,movieId,rating,timestamp
        if (tokens.length >= 3) {
            try {
                String movieIdStr = tokens[1];
                double rating = Double.parseDouble(tokens[2]);
                
                movieId.set(movieIdStr);
                ratingInfo.set(rating + ",1"); // rating,count
                
                context.write(movieId, ratingInfo);
            } catch (NumberFormatException e) {
                // Skip malformed lines
            }
        }
    }
}
