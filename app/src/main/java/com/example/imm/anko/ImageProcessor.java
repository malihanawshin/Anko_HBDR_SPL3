package com.example.imm.anko;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;

public class ImageProcessor {

    public String resultText;
    public String resultNum;

    private static final String TAG = "Processed Image";
    ArrayList<RectObject> rectImages = new ArrayList<RectObject>(50);
    public NumberToWord numConverter = new NumberToWord();
    private ImageUtils io = new ImageUtils();

    public Mat preProcessImage(Bitmap image) {
        Size sz = new Size(320, 240);
        ArrayList<Rect> rectList;
        Rect rect;
        int top,bottom,left,right;

        Mat origImageMatrix = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC3);
        Mat tempImageMat = new Mat(image.getWidth(),image.getHeight(),CvType.CV_8UC1,new Scalar(0));
        Utils.bitmapToMat(image,origImageMatrix);

        Mat imgToProcess = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);
        Mat imgToProcessCanny = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(image,imgToProcess);

        Imgproc.resize(imgToProcess, imgToProcess, sz,0,0,Imgproc.INTER_NEAREST);
        Imgproc.resize(origImageMatrix, origImageMatrix, sz,0,0,Imgproc.INTER_NEAREST);
        Imgproc.resize(tempImageMat, tempImageMat, sz,0,0,Imgproc.INTER_NEAREST);
        Imgproc.cvtColor(imgToProcess, imgToProcess, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(imgToProcess,imgToProcess,new Size(3,3),0);

        Mat imgGrayInv = new Mat(sz,CvType.CV_8UC1,new Scalar(255.0));

        Core.subtract(imgGrayInv,imgToProcess,imgGrayInv);
        Imgproc.Canny(imgToProcess,imgToProcessCanny,13,39,3,false);

        rectList = this.boundingBox(imgToProcessCanny);
        Log.d(TAG,"Rectangular objects: " + rectList.size());

        if (rectList.size() != 0) {
            rect = rectList.get(0);
            top = rect.y;
            bottom = rect.y + rect.height;
            left = rect.x;
            right = rect.x + rect.height;
            for (int i = 1; i < rectList.size(); i++) {
                rect = rectList.get(i);
                if (rect.y < top) {
                    top = rect.y;
                }
                if (rect.y + rect.height > bottom) {
                    bottom = rect.y + rect.height;
                }
                if (rect.x < left) {
                    left = rect.x;
                }
                if (rect.x + rect.width > right) {
                    right = rect.x + rect.width;
                }
            }

            Mat aux = tempImageMat.colRange(left, right).rowRange(top, bottom);
            MatOfDouble matMean = new MatOfDouble();
            MatOfDouble matStd = new MatOfDouble();
            Double mean, std;
            Mat roiImage = imgGrayInv.submat(top, bottom, left, right).clone();
            Core.meanStdDev(roiImage, matMean, matStd);
            mean = matMean.toArray()[0];
            std = matStd.toArray()[0];
            Imgproc.threshold(roiImage, roiImage,mean + std,255, Imgproc.THRESH_BINARY);
            roiImage.copyTo(aux);
        }

        sz = new Size(image.getWidth(),image.getHeight());
        Imgproc.resize(tempImageMat,tempImageMat,sz);
        return tempImageMat;
    }

    public ArrayList<Rect> boundingBox(Mat imgToProcess) {
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        ArrayList<Rect> rectList = new ArrayList<>(50);
        Mat hierarchy = new Mat();

        Imgproc.findContours(imgToProcess, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

            double contourArea = Imgproc.contourArea(contours.get(contourIdx));

            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());

            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = Imgproc.boundingRect(points);

            if (rect.height > 5) {
                rectList.add(rect);
            }
        }
        rectangles(rectList);
        return rectList;
    }



    public Mat segmentAndRecognize(Mat imgToProcess,Bitmap origImage, DigitClassifier digitClassifier) {
        StringBuilder ResultDigits  = new StringBuilder("");
        rectImages.clear();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Mat origImageMatrix = new Mat(origImage.getWidth(), origImage.getHeight(), CvType.CV_8UC3);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Mat roiImage;
        Utils.bitmapToMat(origImage, origImageMatrix);
        Imgproc.findContours(imgToProcess, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

            double contourArea = Imgproc.contourArea(contours.get(contourIdx));

            if (contourArea < 500.0) {
                continue;
            }

            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());

            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            MatOfPoint points = new MatOfPoint(approxCurve.toArray());

            Rect rect = Imgproc.boundingRect(points);
            Imgproc.rectangle(origImageMatrix, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0, 255), 3);
            if ((rect.y + rect.height > origImageMatrix.rows()) || (rect.x + rect.width > origImageMatrix.cols())) {
                continue;
            }

            MatOfDouble matMean = new MatOfDouble();
            MatOfDouble matStd = new MatOfDouble();
            Double mean;

            roiImage = imgToProcess.submat(rect.y,rect.y + rect.height ,rect.x,rect.x + rect.width );
            int xCord = rect.x;
            Core.copyMakeBorder(roiImage, roiImage, 100, 100, 100, 100, Core.BORDER_ISOLATED);

            Size sz = new Size(32, 32);
            Imgproc.resize(roiImage, roiImage, sz);

            Core.meanStdDev(roiImage, matMean, matStd);
            mean = matMean.toArray()[0];

            Imgproc.threshold(roiImage, roiImage, mean, 255, Imgproc.THRESH_BINARY_INV);
            Bitmap tempImage = Bitmap.createBitmap(roiImage.cols(), roiImage.rows(), conf);
            Utils.matToBitmap(roiImage, tempImage);
            Bitmap scaledImage = Bitmap.createScaledBitmap(tempImage, 32, 32, false);
            RectObject roiObject = new RectObject(xCord,scaledImage);
            rectImages.add(roiObject);

            io.saveImage(tempImage,"singleImage.jpg");
        }

        Collections.sort(rectImages);

        int max = (rectImages.size() > 9) ? 9 : rectImages.size();
        for (int i = 0; i < max; i++) {
            RectObject roi = rectImages.get(i);
            int [] pixels = getPixelData(roi.bitmap);

            float[] pixelsInFloat = makeFloat(pixels);
            DigitClassification dc = digitClassifier.recognize(pixelsInFloat);
            //if(dc!=null) {
                int digit = Integer.parseInt(dc.getLabel());
                Log.i(TAG, "digit: " + digit);
                ResultDigits.append("" + digit);
            //}
            //else {
              //  resultNum = "";
                //resultText = "Failed to recognize as a number. Try another image!";
            //}
        }
        resultNum = ResultDigits.toString();
        if(resultNum==""){
            resultNum = "Failed to recognize as a number";
            resultText = "Try another image!";
        }
        else {
            int number = Integer.parseInt(resultNum);
            Log.i(TAG, "number: " + number);
            resultText = numConverter.numberToWords(number);
        }
        return origImageMatrix;
    }

    public String getResultText() {
        return this.resultText;
    }
    
    public String getResultNum() { return this.resultNum; }

    private int [] getPixelData(Bitmap tempImage) {
        int [] pixels = new int[tempImage.getWidth() * tempImage.getHeight()];
        tempImage.getPixels(pixels, 0, tempImage.getWidth(), 0, 0, tempImage.getWidth(),tempImage.getHeight());
        int[] retPixels = new int[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {

            int pix = pixels[i];
            pix = pix & 0xff;
            int b = pix & 0xff;
            retPixels[i] = 0xff - b;
        }
        return retPixels;

    }


    public static float[] makeFloat(int[] pixels){

        float[] floatArray = new float[pixels.length];
        for(int i=0;i<pixels.length;i++){
            floatArray[i] = (float) pixels[i];
        }

        return floatArray;
    }

    private ArrayList<Rect> rectangles(ArrayList<Rect> rects) {
        double sum = 0.0;
        double mean = 0.0;
        for (int i = 0; i < rects.size(); i++) {
            sum += rects.get(i).height;
        }

        mean = sum / rects.size();

        for (int i = 0; i < rects.size(); i++) {
            if (rects.get(i).height < (mean - 5.0)) {
                rects.remove(i);
            }
        }
        return rects;
    }



}
