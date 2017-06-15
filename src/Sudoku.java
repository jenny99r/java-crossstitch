import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.highgui.Highgui.imread;
import static org.opencv.highgui.Highgui.imwrite;
import static org.opencv.imgproc.Imgproc.*;

public class Sudoku {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        System.out.println("Welcome to OpenCV " + Core.VERSION);

        Mat sudoku = imread("sudoku.jpg", 0);

        Mat outerBox = new Mat(sudoku.size(), CV_8UC1);

        Imgproc.GaussianBlur(sudoku, sudoku, new Size(11, 11), 0);

        Imgproc.adaptiveThreshold(sudoku, outerBox, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);

        imwrite("output/afterthresh.jpg", outerBox);

        bitwise_not(outerBox, outerBox);

        imwrite("output/bitwise.jpg", outerBox);


//        int data[] = {0, 1, 0, 1, 1, 1, 0, 1, 0};
//        int row = 0;
//        int col = 0;
//        Mat kernel = new Mat(3, 3, CvType.CV_32S);
//        kernel.put(row, col, data);


        Mat kernel = getStructuringElement(MORPH_RECT, new Size(3, 3));

        dilate(outerBox, outerBox, kernel);

        imwrite("output/dilate2.jpg", outerBox);

        detectOutline(outerBox);
        imwrite("output/outline.jpg", outerBox);
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
                //System.out.print(value[0] + " ");
                if(value != null && value[0] ==64 && x != maxPtX && y != maxPtY) {
                    System.out.println("flood fill");
                    Mat newOuterBox = Mat.zeros(outerBox.rows() + 2, outerBox.cols() + 2, CV_8UC1);
                    floodFill(outerBox, newOuterBox, new Point(x,y), new Scalar(0));
                }
            }
            System.out.println();
        }

    }
}
