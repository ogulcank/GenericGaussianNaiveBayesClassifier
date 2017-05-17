import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by anappp on 5/17/17.
 */
public class NaiveBayesClassifierExample
{
    public static void main(String args[]) throws IOException {
        String filename;
        filename = "t_pima-indians-diabetes.csv";
        boolean isLast = true;
        NaiveBayesClassifier nbc = new NaiveBayesClassifier(filename, isLast, ",", false);
        nbc.run();
        InputStream inStream;
        BufferedInputStream bis;
        String testFile = "pima-indians-diabetes.csv";
        // open input stream test.txt for reading purpose.
        inStream = new FileInputStream(testFile);
        // input stream is converted to buffered input stream
        bis = new BufferedInputStream(inStream);
        int correct = 0;
        int actual = 0;
        for (int i = 0; i < i + 1; i++) {
            String line[] = myGetLine(bis, ",");
            if (line == null) break;

            double inputVector[] = new double[line.length - 1];
            if (isLast) {
                String c = line[line.length - 1];
                line[line.length - 1] = line[0];
                line[0] = c;
            }
            for (int j = 0; j < inputVector.length; j++) {
                inputVector[j] = Double.parseDouble(line[j + 1]);
            }
            String prediction = nbc.predict(inputVector);
            if (prediction.equals(line[0])) correct++;
            actual++;
        }
        System.out.println("Accuracy is: " + (100.0 * correct / actual) + "%");

    }

    public static String[] myGetLine(BufferedInputStream bis, String delimitter) throws IOException {
        String s = "";
        try {
            if (bis.available() <= 0) return null;
            char c = (char) bis.read();
            while (c != '\n') {
                s += c;
                c = (char) bis.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s.split(delimitter);
    }
}
