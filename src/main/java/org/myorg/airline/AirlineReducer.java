package org.myorg.airline;

import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AirlineReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {

    private DoubleWritable unreliabilityScore = new DoubleWritable();

    private static final int DELAY_EVENT_CODE = 1;
    private static final int CANCEL_EVENT_CODE = 2;
    private static final int FLIGHT_COUNT_CODE = 3;

    private static final double WEIGHT_DELAY = 1.0;
    private static final double WEIGHT_CANCEL = 1.5; // higher weight for cancellations

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        
        int totalDelays = 0;
        int totalCancellations = 0;
        int totalFlights = 0;

        for (IntWritable val : values) {
            int eventCode = val.get();
            if (eventCode == DELAY_EVENT_CODE) {
                totalDelays++;
            } else if (eventCode == CANCEL_EVENT_CODE) {
                totalCancellations++;
            } else if (eventCode == FLIGHT_COUNT_CODE) {
                totalFlights++;
            }
        }

        double score = 0.0;
        if (totalFlights > 0) {
            // Calculate unreliability score
            double numerator = (totalDelays * WEIGHT_DELAY) + (totalCancellations * WEIGHT_CANCEL);
            score = numerator / totalFlights;
        }
        
        unreliabilityScore.set(score);
        context.write(key, unreliabilityScore); // Emit (AirlineCode, UnreliabilityScore)
    }
}
