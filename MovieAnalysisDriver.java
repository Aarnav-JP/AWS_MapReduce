import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MovieAnalysisDriver {
    
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: MovieAnalysisDriver <input_path> <output1_path> <output2_path>");
            System.exit(-1);
        }
        
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        
        // Job 1: Calculate average ratings
        Job job1 = Job.getInstance(conf, "movie average rating");
        job1.setJarByClass(MovieAnalysisDriver.class);
        job1.setMapperClass(MovieRatingMapper.class);
        job1.setReducerClass(MovieRatingReducer.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);
        
        FileInputFormat.addInputPath(job1, new Path(args[0] + "/ratings.csv"));
        Path output1 = new Path(args[1]);
        
        // Delete output directory if exists
        if (fs.exists(output1)) {
            fs.delete(output1, true);
        }
        
        FileOutputFormat.setOutputPath(job1, output1);
        
        boolean job1Success = job1.waitForCompletion(true);
        if (!job1Success) {
            System.err.println("Job 1 failed!");
            System.exit(1);
        }
        
        System.out.println("Job 1 completed successfully!");
        
        // Job 2: Top 10 movies per genre
        Configuration conf2 = new Configuration();
        
        Job job2 = Job.getInstance(conf2, "genre top movies");
        job2.setJarByClass(MovieAnalysisDriver.class);
        job2.setMapperClass(GenreTopMoviesMapper.class);
        job2.setReducerClass(GenreTopMoviesReducer.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
        job2.setNumReduceTasks(1);
        
        // Add both movies.csv and job1 output as input
        FileInputFormat.addInputPath(job2, new Path(args[0] + "/movies.csv"));
        FileInputFormat.addInputPath(job2, output1);
        
        Path output2 = new Path(args[2]);
        
        // Delete output directory if exists
        if (fs.exists(output2)) {
            fs.delete(output2, true);
        }
        
        FileOutputFormat.setOutputPath(job2, output2);
        
        boolean job2Success = job2.waitForCompletion(true);
        if (!job2Success) {
            System.err.println("Job 2 failed!");
            System.exit(1);
        }
        
        System.out.println("Job 2 completed successfully!");
        System.exit(0);
    }
}
