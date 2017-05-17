import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

//Dependent variable is in the first column
//Assumming Gaussian Distribution of predictor variables

public class NaiveBayesClassifier {
    String filename; // name of the file
    InputStream inStream; // io
    BufferedInputStream bis; // io
    int rows; //#rows of data
    int cols; //number of predictor variables
    int numCategories; //number of unique values the dependent variable takes.
    Hashtable<Integer, String[]> table; // used to accept data
    String[][] dataMatrix; // stores the ^ data for easier access
    Vector<String> categories; // stores the names of the unique categories
    MeanVariance[][] classifier; // stores the mean/variance pairs of each predictor variable
    Hashtable<String, Double> priorProbabilities; // stores prior probabilities of all the predictor variables
    Hashtable<String, Integer> checker;// used to compute # categories
    boolean isLast; // whether the training data contains the actual value of dependent variable in the first column or last.
    String delimitter; // delimitter of the file. Usually "," for csv.
    boolean hasIndexCol;// whether the data has an index column

    NaiveBayesClassifier(String filename, boolean isLast, String delimitter, boolean hasIndexCol) {
        this.filename = filename;
        rows = 0;
        cols = 0;
        table = new Hashtable<Integer, String[]>();
        categories = new Vector<>();
        priorProbabilities = new Hashtable<>();
        this.isLast = isLast;
        this.delimitter = delimitter;
        this.hasIndexCol = hasIndexCol;
    }

    NaiveBayesClassifier(String filename, boolean isLast) {
        this(filename, isLast, ",", false);
    }

    private void readFile() {
        try {
            // open input stream test.txt for reading purpose.
            inStream = new FileInputStream(filename);
            // input stream is converted to buffered input stream
            bis = new BufferedInputStream(inStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] line = myGetLine();
        cols = line.length;
        rows = 0;
        while (line != null) {
            if (isLast) {
                String c = line[line.length - 1];
                line[line.length - 1] = line[0];
                line[0] = c;
            }
            table.put(rows++, line);
            line = myGetLine();
        }
    }

    private void initMatrix() {
        dataMatrix = new String[rows][cols];
    }

    private void fillMatrix() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String[] s = table.get(i);
                dataMatrix[i][j] = s[j];
            }
        }
    }


    private String[] myGetLine() {
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
        if (hasIndexCol)
            return s.substring(s.indexOf(delimitter) + 1).split(delimitter);
        else
            return s.split(delimitter);

    }

    private void computeNumCategories() {
        checker = new Hashtable<>();
        for (int i = 0; i < rows; i++) {
            if (checker.get(dataMatrix[i][0]) == null) {
                numCategories++;
                checker.put(dataMatrix[i][0], 0);
                categories.add(dataMatrix[i][0]);
            } else {
                checker.put(dataMatrix[i][0], checker.get(dataMatrix[i][0]) + 1);
            }
        }
    }

    private void computePriorProbabilities() {
        for (String s : categories) {
            int total = checker.get(s);
            priorProbabilities.put(s, ((total + 1.0) / rows));
        }
    }

    private void initClassifier() {
        classifier = new MeanVariance[numCategories][cols];
    }

    private void fillClassifier() {
        for (int i = 0; i < numCategories; i++) {
            for (int j = 1; j < cols; j++) {
                classifier[i][j] = computeMeanAndSdOfColByCategory(j, categories.get(i));
            }
        }
        int x = 1;
    }

    private int getIndexOfCategory(String category) {
        int index = 0;
        for (String s : categories) {
            if (s.equals(category)) return index;
            index++;
        }
        return -1;
    }

    private MeanVariance computeMeanAndSdOfColByCategory(int col, String category) {
        double mean = 0;
        double sd = 0;
        int n = 0;
        for (int i = 0; i < rows; i++) {
            if (dataMatrix[i][0].equals(category)) {
                n++;
                try {

                    mean = mean + Double.parseDouble(dataMatrix[i][col]);

                } catch (Exception e) {
                    System.out.println("Corrupt data. Value at (" + i + "," + col + ") is not double: " + dataMatrix[i][col]);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        mean = mean / n;
        for (int i = 0; i < rows; i++) {
            if (dataMatrix[i][0].equals(category)) {
                sd = sd + Math.pow(mean - Double.parseDouble(dataMatrix[i][col]), 2);
            }
        }
        sd = sd / n;
        sd = Math.sqrt(sd);
        return new MeanVariance(mean, sd);
    }

    private double getProbabilityGaussian(double x, double mean, double sd) {
        return (1 / Math.sqrt(2 * Math.PI * sd * sd)) * Math.exp(-1 * Math.pow(x - mean, 2) / (2 * sd * sd));
    }

    private double getProbabilityByColumnAndCategory(double x, String category, int col) {
        col += 1;
        int index = getIndexOfCategory(category);
        try {
            MeanVariance mv = classifier[index][col];
            double probability = getProbabilityGaussian(x, mv.getMean(), mv.getVariance());
            return probability;

        } catch (Exception e) {
            System.out.println("The category was not found!");
            e.printStackTrace();
            System.exit(1);
        }
        return -1;
    }

    private double getProbabilityOfRowGivenCategory(double[] inputVector, String category) {
        int index = getIndexOfCategory(category);
        double probability = 1;
        for (int i = 0; i < inputVector.length; i++) {
            probability += Math.log(getProbabilityByColumnAndCategory(inputVector[i], category, i));
        }
        return probability;
    }

    String predict(double[] inputVector) {
        double max = Integer.MIN_VALUE;
        String prediction = "Inconclusive";
        for (String s : categories) {
            double probability = Math.log(priorProbabilities.get(s)) + getProbabilityOfRowGivenCategory(inputVector, s);
            if (probability > max) {
                max = probability;
                prediction = s;
            }
        }
        return prediction;

    }

    void run() {
        readFile();
        initMatrix();
        fillMatrix();
        computeNumCategories();
        initClassifier();
        fillClassifier();
        computePriorProbabilities();
    }


}