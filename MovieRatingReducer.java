import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MovieRatingReducer extends Reducer<Text, Text, Text, Text> {
    
    private Text result = new Text();
    
    @Override
    public void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        
        double totalRating = 0.0;
        int totalCount = 0;
        
        // Sum up all ratings and counts for this movie
        for (Text value : values) {
            String[] parts = value.toString().split(",");
            if (parts.length == 2) {
                try {
                    double rating = Double.parseDouble(parts[0]);
                    int count = Integer.parseInt(parts[1]);
                    
                    totalRating += rating;
                    totalCount += count;
                } catch (NumberFormatException e) {
                    // Skip malformed values
                }
            }
        }
        
        if (totalCount > 0) {
            double avgRating = totalRating / totalCount;
            result.set(String.format("%.2f,%d", avgRating, totalCount));
            context.write(key, result);
        }
    }
}
