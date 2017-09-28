import java.awt.Color;

public class SeamCarver {
    private Picture pic;
    private double[][] energyMatrix;

    public SeamCarver(Picture picture) {// create a seam carver object based on the given picture
        if (picture == null) {
            throw new NullPointerException("picture is null");
        }
        pic = new Picture(picture);
        energyMatrix();
    }

    public Picture picture() {                         // current picture
        return pic;
    }

    public int width() {                           // width of current picture
        return pic.width();
    }

    public int height() {                          // height of current picture
        return pic.height();
    }

    public double energy(int x, int y) { // energy of pixel at column x and row y
        if (x < 0 || x > (width() - 1)) {
            throw new IndexOutOfBoundsException
            ("x-coordinate is outside of range");
        }

        if (y < 0 || y > (height() - 1)) {
            throw new IndexOutOfBoundsException
            ("y-coordinate is outside of range");
        }

        double deltax = -1.0;
        double deltay = -1.0;

        if (x == 0){
            deltax = borderdelta(pic.get(width() - 1, y), pic.get(x + 1, y));
        }
        else if (x == (width() - 1)) {
            deltax = borderdelta(pic.get(0, y), pic.get(x - 1, y));
        } else {
            deltax = borderdelta(pic.get(x - 1, y), pic.get(x + 1, y));
        }
        if (y == 0) {
            deltay = borderdelta(pic.get(x, height() - 1), pic.get(x, y + 1));
        }
        else if (y == (height() - 1)){
            deltay = borderdelta(pic.get(x, y - 1), pic.get(x, 0));
        }
        else {
            deltay = borderdelta(pic.get(x, y - 1), pic.get(x, y + 1));
        }
        double sum = deltax + deltay;
        return Math.sqrt(sum);

    }

    private double borderdelta (Color one, Color two) {
        double r = Math.abs(one.getRed() - two.getRed());
        double g = Math.abs(one.getGreen() - two.getGreen());
        double b = Math.abs(one.getBlue() - two.getBlue());
        return r * r + g * g + b * b;
    }

    public int[] findHorizontalSeam() { // sequence of indices for horizontal seam
        //transpose the picture and call findVerticalSeam() and removeVerticalSeam()
        //Don't forget to transpose the picture back, when needed
        transpose();
        int[] seam = findVerticalSeam();
        transpose();
        return seam;
    }

    private void transpose() {
        Picture tp = pic;
        double[][] energy = new double[height()][width()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                tp.set(j, i, pic.get(i, j));
                energy[j][i] = energyMatrix[i][j];
            }
        }
        energyMatrix = energy;
        pic = tp;
        //parent = new int[picture.width()][picture.height()];
    }

    public int[] findVerticalSeam() {  // sequence of indices for vertical seam
        //construct a W-by-H energy matrix using the energy() method (2D energy matrix)
        //Find a shortest Path (data structure)
        double [][] eM = energyMatrix();
        return seamfinder(eM, width(), height());
    }

    private int[] seamfinder(double[][] energyMatrix, int w, int h) {
        w = width();
        h = height();
        int n = w * h;
        double[] to = new double[n];
        int[] from = new int[n];
        initEdge(to, from);

        for (int i = 0; i < height() - 1; i++) {
            for (int j = 0; j < width() - 1; j++) {
                int p = width() * j + i;
                int toP = width() * (j - 1) + (i + 1);
                int rmP = width() * j + (i + 1);
                int rbP = width() * (j + 1) + (i + 1);

                //right up
                if (j > 0) {
                    relax(p, toP, to, from);
                }
                //right mid
                relax(p, rmP, to, from); 
                //right down
                if (j + 1 < height()) {
                    relax(p, rbP, to, from); 
                }
            }
        }

        double curmin = Double.POSITIVE_INFINITY;
        int lastp = -1;
        for (int i = 0; i < width(); i++) 
        {
            int mP = width() * (height() - 1) + i;
            if (to[mP] < curmin) {
                curmin = to[mP];
                lastp = mP;
            }
        }
        int[] seam = new int[h];
        seam[h - 1] = lastp;
        for (int i = h - 2; i >= 0; i--) {
            seam[i] = from[i+1];
        }
        return seam;
    }

    private double[][] energyMatrix() {
        //construct a W-by-H energy matrix using the energy() method (2D energy matrix)
        energyMatrix = new double[width()][height()];

        for (int i = 0; i < width(); i++) {
            for(int j = 0; j < height(); j++) {
                energyMatrix[i][j] = energy(i, j);
            }         
        }
        return energyMatrix;
        //return wbyhmatrix;
    }
    //to find the shortest path relax edges and keep updating them then run the 
    //topological sort algorithm

    private void initEdge(double[] to, int[] from) {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (i == 0) {
                    to[(width() * i + j)] = 0;
                } else {
                    to[(width() * i + j)] = Double.POSITIVE_INFINITY;
                }
                from[(width() * i + j)] = -1; 
            }      
        }
    }

    private void relax(int fromP, int toP, double[] to, int[] from) {
        if (to[toP] > to[fromP] + energyMatrix[toP % width()][toP / width()]) {
            to[toP] = to[fromP] + energyMatrix[toP % width()][toP / width()];
            from[toP] = fromP;
        }
    }

    private void validSeamandLength (int[] seam, boolean vertical) {
        for (int i = 1; i < seam.length; i++) {
            if (Math.abs(seam[i] - seam[i - 1]) > 1)
            {
                throw new IllegalArgumentException("Invalid Seam.");
            }
        }

        if (vertical)
        {
            if (seam == null || seam.length != height()) {
                throw new IllegalArgumentException("Invalid Length.");  
            }
        }

        if (seam == null || seam.length != width()) {
            throw new IllegalArgumentException("Invalid Length.");  
        }
    }

    public void removeHorizontalSeam(int[] seam) {  // remove horizontal seam from current picture
        //transpose the picture and call findVerticalSeam() and removeVerticalSeam()
        //Don't forget to transpose the picture back, when needed.
        validSeamandLength(seam, false);
        //         Picture newpic = new Picture(width(), height() - 1);
        //         
        //         for (int i = 0; i <= width() - 1; i++) 
        //         {
        //             int k = 0;
        //             for (int j = 0; j <= height() - 1; j++)
        //             {
        //                 if (j != seam[i]) 
        //                 {
        //                     newpic.set(i, k, pic.get(i, j));
        //                     k++;
        //                 }
        //             }
        //         }
        //return newpic;
        transpose();
        removeVerticalSeam(seam);
        transpose();
        //return seam;
        //pic = newpic;
    }

    public void removeVerticalSeam(int[] seam) { // remove vertical seam from current picture
        //should work for any valid seam
        validSeamandLength(seam, true);
        Picture newpic = new Picture(width() - 1, height());
        for (int i = 0; i <= height() - 1; i++) 
        {
            int k = 0;
            for (int j = 0; j <= width() - 1; j++)
            {
                if (j != seam[i]) 
                {
                    newpic.set(k, i, pic.get(j, i));
                    k++;
                }
            }
        }
        //return newpic;
        pic = newpic;    
    }

    public static void main(String[] args) {   // do unit testing of this class
    }
}