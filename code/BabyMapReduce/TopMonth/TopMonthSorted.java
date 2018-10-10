
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class TopMonth {

    public static class TopMapper extends Mapper<Object, Text, Text, Text> {

        private Text mapDate = new Text();
        private Text mapMoney = new Text();

        private TreeMap<Double, Text> topMoney = new TreeMap<Double, Text>();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            String[] dateMoney = value.toString().split(",");
            String money = dateMoney[1];

            String[] fullDate = dateMoney[0].split("/");
            String monthYear = fullDate[1] + "/" + fullDate[2];

            topMoney.put(Double.parseDouble(money), new Text(monthYear));
            if (topMoney.size() > 10) {
                topMoney.remove(topMoney.firstKey());
            }

            // mapDate.set(monthYear);
            // mapMoney.set(money);
            // context.write(mapDate, mapMoney);
        }

        public void cleanup(Context context) throws IOException, InterruptedException {
            for (Map.Entry<Double, Text> entry : topMoney.entrySet()) {
                context.write(entry.getValue(), new Text(Double.toString(entry.getKey())));
            }
        }
    }

    public static class TopReducer extends Reducer<Text, Text, Text, Text> {

        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            Double money = 0d;
            // String tabulated = "";
            for (Text val : values) {
                money += Double.parseDouble(val.toString());
                // tabulated += "\t" + val.toString();
            }

            String stringMoney = Long.toString(money.longValue());
            // result.set(tabulated);
            result.set(stringMoney);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Top Month");
        job.setJarByClass(TopMonth.class);
        job.setMapperClass(TopMapper.class);
        job.setCombinerClass(TopReducer.class);
        job.setReducerClass(TopReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}