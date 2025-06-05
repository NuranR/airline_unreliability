package org.myorg.airline;

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class AirlineMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private Text airlineCode = new Text();
    
    private static final int DELAY_EVENT_CODE = 1; // Represents a significant delay
    private static final int CANCEL_EVENT_CODE = 2; // Represents a cancellation
    private static final int FLIGHT_COUNT_CODE = 3; // Represents valid flight

    // Column indices
    private final int OP_CARRIER_IDX = 1;
    private final int DEP_DELAY_IDX = 7;
    private final int CANCELLED_IDX = 15;

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();
        // Skip header row
        if (key.get() == 0 && line.startsWith("FL_DATE")) {
            return;
        }
        String[] fields = line.split(","); // comma-separated

        // avoid ArrayIndexOutOfBoundsException
        if (fields.length <= Math.max(OP_CARRIER_IDX, Math.max(DEP_DELAY_IDX, CANCELLED_IDX))) {
            return;
        }

        String carrier = fields[OP_CARRIER_IDX].trim();

        // skip if carrier code is missing
        if (carrier.isEmpty()) {
            return;
        }
        airlineCode.set(carrier);

        context.write(airlineCode, new IntWritable(FLIGHT_COUNT_CODE));

        // Check for departure delay
        try {
            // Remove quotes if present
            String depDelayStr = fields[DEP_DELAY_IDX].replace("\"", "").trim();
            if (!depDelayStr.isEmpty() && !depDelayStr.equalsIgnoreCase("NA")) {
                double departureDelayMinutes = Double.parseDouble(depDelayStr);
                if (departureDelayMinutes > 15.0) {
                    context.write(airlineCode, new IntWritable(DELAY_EVENT_CODE));
                }
            }
        } catch (NumberFormatException e) {
        }

        // Check for cancellation
        try {
            String cancelledStr = fields[CANCELLED_IDX].replace("\"", "").trim();
             if (!cancelledStr.isEmpty() && !cancelledStr.equalsIgnoreCase("NA")) {
                double cancelledStatus = Double.parseDouble(cancelledStr);
                if (cancelledStatus == 1.0) {
                    context.write(airlineCode, new IntWritable(CANCEL_EVENT_CODE));
                }
            }
        } catch (NumberFormatException e) {
        }
    }
}
