package fouriertransform.frame;

import java.awt.BasicStroke;
import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.utils.Converters;
import org.opencv.videoio.VideoCapture;

import fouriertransform.fourier.Fourier;
import fouriertransform.utils.Cycle;

@SuppressWarnings("serial")
public class Frame extends JPanel implements ActionListener {

    JFrame frame;

    public Frame() throws FileNotFoundException {
        frame = new JFrame("Fourier Transform drawer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(1050, 650);
        frame.setVisible(true);
        frame.add(this);

        init();
    }

    Timer timer;

    public static double xVal = 0;
    public static ArrayList < Double > drawingX;
    public static ArrayList < Double > drawingY;
    public static ArrayList < Double > valuesX;
    public static ArrayList < Double > valuesY;
    public static ArrayList < Cycle > cyclesX;
    public static ArrayList < Cycle > cyclesY;
    public static double cycleYpos = -1;
    public static double cycleXpos = -1;

    Date tarih = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd'_at_'hh.mm.ss'_'a'_'zzz");
    String saat = ft.format(tarih);

    int bulunanyuz = 0;

    private void init() throws FileNotFoundException {

        this.setLayout(null);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        drawingX = new ArrayList < > ();
        drawingY = new ArrayList < > ();
        valuesX = new ArrayList < > ();
        valuesY = new ArrayList < > ();
        cyclesX = new ArrayList < > ();
        cyclesY = new ArrayList < > ();

        this.setBackground(Color.BLACK);

        Button b = new Button("Hesapla");
        b.setBounds(140, 100, 60, 30);
        frame.add(b);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    drawFaceContour();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });
        Button foto = new Button("fotograf cek");
        foto.setBounds(50, 100, 80, 30);
        frame.add(foto);
        foto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                drawingX.clear();
                drawingY.clear();
                VideoCapture vc = new VideoCapture();
                //hangi kamera istendigi buradan ayarlanir.
                vc.open(1);
                String file = "img/cekilen_resim/login_" + saat + ".png";

                if (vc.isOpened()) {

                    //fotograf cekme
                    System.out.println("fotograf cekiliyor");
                    Mat mat = new Mat();
                    vc.read(mat);
                    Imgcodecs.imwrite(file, mat);
                    vc.release();

                    //yuz tarama
                    int yuzsayisi = 0;
                    try {
                        yuzsayisi = scan();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    //eğer yüzde iki tane göz bulunanazsa iptal
                    if (yuzsayisi >= 1) {
                        goztara(yuzsayisi);



                        //texte yazdırma
                        try {
                            print(saat, "Yoklama");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        //yuvarlak cizme

                        //denemeyuvarlak();	



                        //Fast Fourier Transform
                        //EkFonksiyonlar.fourier(data);
                    }


                    System.out.println("fotograf cekilmesi tamamlandi");
                } else {
                    System.out.println("!!!Kameraya baglanilamadi!!!");
                }

            }
        });

        timer = new Timer(1000 / 120, this);
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2D = (Graphics2D) g;
        g2D.fillRect(200, 200, 400, 400);

        g2D.setStroke(new BasicStroke(3));
        if (cyclesX.size() > 0 && cyclesY.size() > 0) {
            cyclesX.get(0).drawCycle(g2D, xVal);
            cyclesY.get(0).drawCycle(g2D, xVal);
        }

        g2D.setColor(new Color(255, 0, 0));

        for (int i = 1; i < drawingX.size() && i < drawingY.size(); i++) {
            double posX = drawingX.get(i) * 200;
            double posY = drawingY.get(i) * 200;
            double lposX = drawingX.get(i - 1) * 200;
            double lposY = drawingY.get(i - 1) * 200;
            g2D.drawLine((int) lposX + 820, (int) lposY + 400, (int) posX + 820, (int) posY + 400);
        }
        if (1 < drawingX.size() && 1 < drawingY.size()) {
            double posX = drawingX.get(0) * 200;
            double posY = drawingY.get(0) * 200;
            double lposX = drawingX.get(drawingX.size() - 1) * 200;
            double lposY = drawingY.get(drawingY.size() - 1) * 200;
            g2D.drawLine((int) lposX + 820, (int) lposY + 400, (int) posX + 820, (int) posY + 400);
        }

        for (int i = 1; i < valuesX.size() && i < valuesY.size(); i++) {
            double x = Math.round(valuesX.get(i));
            double lx = Math.round(valuesX.get(i - 1));
            double y = Math.round(valuesY.get(i));
            double ly = Math.round(valuesY.get(i - 1));
            g2D.drawLine((int) lx, (int) ly, (int) x, (int) y);
        }
        if (cycleXpos != -1 && cycleYpos != -1 && valuesX.size() > 1 && valuesY.size() > 1) {
            g2D.setColor(new Color(255, 0, 0, 126));
            g2D.drawLine((int) cycleXpos, 100, (int) cycleXpos, (int)((double) valuesY.get(valuesY.size() - 1)));
            g2D.drawLine(100, (int) cycleYpos, (int)((double) valuesX.get(valuesY.size() - 1)), (int) cycleYpos);
            g2D.setColor(Color.BLACK);
            g2D.fillOval((int)((double) valuesX.get(valuesY.size() - 1)) - 3, (int) cycleYpos - 3, 6, 6);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (cyclesX.size() > 0) {
            xVal += (Math.PI / (cyclesX.size() / 2));
            if (xVal >= Math.PI * 2) {
                xVal = 0;
            }
        } else {
            xVal = 0;
        }
        repaint();
    }

    public void updateCycleValue() {
        if (drawingX.size() % 2 != 0) {
            drawingX.remove(0);
        }
        if (drawingY.size() % 2 != 0) {
            drawingY.remove(0);
        }
        ArrayList < double[] > fourierX = Fourier.fourier(drawingX);
        fourierX.sort(new Comparator < double[] > () {
            @Override
            public int compare(double[] d2, double[] d1) {
                if (d1[1] > d2[1]) {
                    return 1;
                }
                if (d1[1] < d2[1]) {
                    return -1;
                }
                return 0;
            }
        });
        ArrayList < double[] > fourierY = Fourier.fourier(drawingY);
        fourierY.sort(new Comparator < double[] > () {
            @Override
            public int compare(double[] d2, double[] d1) {
                if (d1[1] > d2[1]) {
                    return 1;
                }
                if (d1[1] < d2[1]) {
                    return -1;
                }
                return 0;
            }
        });
        xVal = 0;
        cyclesX.clear();
        cyclesY.clear();
        valuesX.clear();
        valuesY.clear();
        for (int i = 0; i < fourierX.size(); i++) {
            double[] data = fourierX.get(i);
            if (cyclesX.isEmpty()) {
                cyclesX.add(new Cycle(400, 100, data[0], data[1], data[2], 0));
            } else {
                cyclesX.add(new Cycle(data[0], data[1], data[2], 0));
                cyclesX.get(i - 1).addCycle(cyclesX.get(i));
            }
        }
        for (int i = 0; i < fourierY.size(); i++) {
            double[] data = fourierY.get(i);
            if (cyclesY.isEmpty()) {
                cyclesY.add(new Cycle(100, 400, data[0], data[1], data[2], Math.PI / 2));
            } else {
                cyclesY.add(new Cycle(data[0], data[1], data[2], Math.PI / 2));
                cyclesY.get(i - 1).addCycle(cyclesY.get(i));
            }
        }
    }

    public static boolean update = false;
    public int scan() throws IOException {
        String xmlFile = "Araclar/cascade/lbpcascade_frontalface.xml";
        CascadeClassifier cc = new CascadeClassifier(xmlFile);
        String imgFile = "img/cekilen_resim/login_" + saat + ".png";
        Mat src = Imgcodecs.imread(imgFile);
        MatOfRect faceDetection = new MatOfRect();
        cc.detectMultiScale(src, faceDetection);
        System.out.println(String.format("kişi: %d", faceDetection.toArray().length));


        //print("" + saat + "\n");
        int sayac = 1;
        for (Rect rect: faceDetection.toArray()) {

            //kişinin yuzu kare icine alinir ve isaretlenir.
            Imgproc.putText(src, "Kisi", new Point(rect.x, rect.y - 5), 1, 2, new Scalar(0, 0, 0));
            Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 0), 3);

            //resimde bulunan yuzler kesilir ve kesilmis klasorune atilir.
            Mat kes = new Mat(src, rect);
            Imgcodecs.imwrite("img/kesilmis/" + saat + "_" + sayac + ".png", kes);
        }

        Imgcodecs.imwrite("img/islenen_resim/" + saat + ".png", src);

        return faceDetection.toArray().length;
    }
    public void goztara(int sayac) {
        for (int i = 1; i < sayac; i++) {
            String xmlFile = "Araclar/cascade/haarcascade_eye.xml";
            CascadeClassifier cc = new CascadeClassifier(xmlFile);
            String imgFile = "img/kesilmis/" + saat + "_" + sayac + ".png";
            Mat src = Imgcodecs.imread(imgFile);
            MatOfRect eyeDetection = new MatOfRect();
            cc.detectMultiScale(src, eyeDetection);
            if (eyeDetection.toArray().length != 2) {
                File f = new File(imgFile);
                f.delete();
            }
        }


    }
    public void drawFaceContour() throws FileNotFoundException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String file = "img/kesilmis/" + saat + "_" + 1 + ".png";
        Mat src = Imgcodecs.imread(file);
        //Converting the source image to binary
        Mat gray = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
        Imgproc.threshold(gray, binary, 100, 200, Imgproc.THRESH_BINARY_INV);
        //Finding Contours
        List < MatOfPoint > contours = new ArrayList < > ();
        Mat hierarchey = new Mat();
        Imgproc.findContours(binary, contours, hierarchey, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double[][] convertedArray = matToArray(hierarchey);

        List < Point > lista = new ArrayList < Point > ();

        int lista_enbuyuk = 0;
        int contour_enbuyuk = 0;
        System.out.println("satir:" + convertedArray.length);
        System.out.println("sutun:" + convertedArray[0].length);
        System.out.println("binary:" + binary.size());
        System.out.println("contours:" + contours.size());
        System.out.println("contours(sutun):  " + contours.get(0).size());
        for (int i = 0; i < contours.size(); i++) {
            Converters.Mat_to_vector_Point(contours.get(i), lista);
        }
        Point[][] contours_array = new Point[contours.size()][3000];
        //Point[]d
        for (int i = 0; i < contours.size(); i++) {
            Converters.Mat_to_vector_Point(contours.get(i), lista);
            if (lista_enbuyuk < lista.size()) {
                lista_enbuyuk = lista.size();
                contour_enbuyuk = i;
            }
            for (int j = 0; j < lista.size(); j++) {
                contours_array[i][j] = lista.get(j);
            }

        }

        for (int j = 0; j < lista_enbuyuk; j++) {

            System.out.print("[" + contour_enbuyuk + "][" + j + "]: " + contours_array[contour_enbuyuk][j] + "| ");
        }
        System.out.println();
        System.out.println(lista_enbuyuk);
        System.out.println(contour_enbuyuk);

        Mat draw = Mat.zeros(src.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            //Calculating the area
            double cont_area = Imgproc.contourArea(contours.get(i));
            Scalar color = new Scalar(0, 0, 255);

            if (cont_area > 5000.0) {
                Imgproc.drawContours(draw, contours, i, color, 2, Imgproc.LINE_8, hierarchey, 2, new Point());
            } else {}
        }

        for (int i = 0; i < lista_enbuyuk - 1; ++i) {
            drawingX.add(((contours_array[contour_enbuyuk][i].x) - 100) / 150);
            drawingY.add(((contours_array[contour_enbuyuk][i].y) - 100) / 150);
        }
        updateCycleValue();
        update = true;

    }
    public void print(String str, String dosya) throws IOException {
        //giren kisilerin isimleri txt'ye yazilir.
        File file = new File("img/bilgilendirme/" + dosya + ".txt");


        FileWriter fileWriter = new FileWriter(file, true);
        BufferedWriter bWriter = new BufferedWriter(fileWriter);
        bWriter.write(str);
        bWriter.close();
    }

    private double[][] matToArray(Mat frame) {
        double array[][] = new double[frame.height()][frame.width()];
        for (int i = 0; i < frame.height(); i++) {
            for (int j = 0; j < frame.width(); j++) {
                array[i][j] = frame.get(i, j)[0];
            }
        }
        return array;
    }
}