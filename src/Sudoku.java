import com.sun.javafx.geom.Vec2f;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.Core.line;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.highgui.Highgui.imread;
import static org.opencv.highgui.Highgui.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class Sudoku {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    
    private static Mat KERNEL = getStructuringElement(MORPH_RECT, new Size(3, 3));

    public static void main(String[] args) {
        System.out.println("Welcome to OpenCV " + Core.VERSION);

        Mat image = imread("sudoku.jpg", 0);

        Mat outerBox = initaliseImage(image);

        imwrite("output/dilate2.jpg", outerBox);

        detectOutline(outerBox);
        imwrite("output/outline.jpg", outerBox);

        erode(outerBox, outerBox, KERNEL);
        imwrite("output/erode.jpg", outerBox);

        detectLines(outerBox);
        imwrite("output/lines.jpg", outerBox);
    }

    private static Mat initaliseImage(Mat image) {
        Mat outerBox = new Mat(image.size(), CV_8UC1);

        Imgproc.GaussianBlur(image, image, new Size(11, 11), 0);

        Imgproc.adaptiveThreshold(image, outerBox, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);

        bitwise_not(outerBox, outerBox);

        dilate(outerBox, outerBox, KERNEL);
        return outerBox;
    }

    private static void detectOutline(Mat outerBox) {
        int max=-1;
        int maxPtX = 0;
        int maxPtY = 0;
        for(int y=0;y<outerBox.size().height;y++) {
            for(int x=0;x<outerBox.size().width;x++) {
               double[] value = outerBox.get(y, x);
                if(value != null && value[0] >=128) {
                    Mat newOuterBox = Mat.zeros(outerBox.rows() + 2, outerBox.cols() + 2, CV_8UC1);
                    int area = floodFill(outerBox, newOuterBox, new Point(x,y), new Scalar(64));
                    if(area>max) {
                        maxPtX = x;
                        maxPtY = y;
                        max = area;
                    }
                }
            }
        }

        imwrite("output/middle.jpg", outerBox);
        System.out.println("max " + maxPtX + " " + maxPtY);

        Mat mask = Mat.zeros(outerBox.rows() + 2, outerBox.cols() + 2, CV_8UC1);
        floodFill(outerBox, mask, new Point(maxPtX,maxPtY), new Scalar(255));

        for(int y=0;y<outerBox.size().height;y++) {
            for(int x=0;x<outerBox.size().width;x++) {
                double[] value = outerBox.get(y, x);
                if(value != null && value[0] ==64 && x != maxPtX && y != maxPtY) {
                    Mat newOuterBox = Mat.zeros(outerBox.rows() + 2, outerBox.cols() + 2, CV_8UC1);
                    floodFill(outerBox, newOuterBox, new Point(x,y), new Scalar(0));
                }
            }
        }
    }

    private static void detectLines(Mat outerBox) {
        Mat lines = new Mat();
        HoughLines(outerBox, lines, 1, Math.PI /180, 200);

        for (int i = 0; i < lines.cols(); i++) {
            drawLine(lines.get(0, i), outerBox, new Scalar(128));
        }

    }

    private static void drawLine(double[] vals, Mat mat, Scalar rgb) {
        System.out.println(vals.length + " " + vals[0] + " " + vals[1]);
        double r = vals[0];
        double theta = vals[1];
        if(r !=0) {
            double m = -1.0/Math.tan(r);
            double c = r/Math.sin(theta);

            line(mat, new Point(0, c), new Point(mat.size().width, m* mat.size().width + c), rgb);
        } else {
//            line(mat, new Point(0, 0), new Point(0, mat.size().height), rgb);
        }
    }
}
