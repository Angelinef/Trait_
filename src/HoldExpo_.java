
import ij.IJ;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import ij.plugin.*;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.*;
  
    
public class HoldExpo_ implements PlugIn {

	public void run(String arg) {
  
		//image originale
		ImagePlus imp = IJ.getImage();
		imp.setTitle("Image intiale");
 
		// conversion de l'image en 8 bits
		ImageConverter icg = new ImageConverter(imp);
		icg.convertToGray8();
		imp.updateAndDraw();
		 
		//On dupplique l'image en 3 fois 
		ImagePlus imp1 = new Duplicator().run(imp);
		ImagePlus imp3 = new Duplicator().run(imp);
		ImagePlus imp5 = new Duplicator().run(imp);
		
		// Convolution avec 1
				// On convertit l'image en 32 bits
				ImageConverter icg1 = new ImageConverter(imp1);
				icg1.convertToGray32();
				//Convolution
				IJ.run(imp1, "Convolve...", "text1=1\n");
				//On récupère la largeur et la hauteur de l'image
				int width=imp.getWidth();
				int height=imp.getHeight();
		
				//On récupère les pixels de l'image 32bits
				float[] pixels1 =(float[]) imp1.getProcessor().getPixels();
				// On applique le logarithme 
				for(int i=0; i<pixels1.length; i++){
					pixels1[i]=(float) Math.log((double)pixels1[i]);
					//System.out.println(pixels1[8039]);
				}
				
				// On créé une image 32 bits avec les pixels
				FloatProcessor fp1 = new FloatProcessor(width, height, pixels1);
				//Remplace l'image avec les nouvelles valeurs des pixels
				imp1.setProcessor(fp1);
//				imp1.updateAndDraw();
//				imp1.show();
//				imp1.setTitle("Image finale 1");
		
		// Convolution avec matrice 3*3 de 1
				ImageConverter icg2 = new ImageConverter(imp3);
				icg2.convertToGray32();
				IJ.run(imp3, "Convolve...", "text1=[1 1 1\n1 1 1\n1 1 1\n]");
				//On récupère les pixels de l'image 32bits
				float[] pixels3 =(float[]) imp3.getProcessor().getPixels();
				// log 
				for(int i=0; i<pixels3.length; i++){
					pixels3[i]=(float) Math.log((double)pixels3[i]);
					//System.out.println(pixels3[i]);
				}
			
				FloatProcessor fp3 = new FloatProcessor(width, height, pixels3);
				imp3.setProcessor(fp3);
//				imp3.show();
//				imp3.updateAndDraw();
//				imp3.setTitle("Image finale 2");
				
		// Convolution avec matrice 5*5 de 1		
				ImageConverter icg3 = new ImageConverter(imp5);
				icg3.convertToGray32();
				IJ.run(imp5, "Convolve...", "text1=[1 1 1 1 1\n1 1 1 1 1\n1 1 1 1 1\n1 1 1 1 1\n1 1 1 1 1\n]");
				
				//On récupère les pixels de l'image 32bits
				float[] pixels5 =(float[]) imp5.getProcessor().getPixels();
				// log 
				for(int i=0; i<pixels5.length; i++){
					pixels5[i]=(float) Math.log((double)pixels5[i]);
					//System.out.println(pixels5[i]);
				}
		 	   
				FloatProcessor fp5 = new FloatProcessor(width, height, pixels5);
				imp5.setProcessor(fp5);
//				imp5.updateAndDraw();
//				imp5.show();	
//				imp5.setTitle("Image finale 3");
		
		 // Valeurs des alphas 		
				double[] x = {Math.log(1), Math.log(3), Math.log(5)};
				double[] alpha = new double[pixels1.length]; 
				double[] coeff = new double[pixels1.length];
				// On calcule les valeurs des alphas pour chaque pixel 	
				for (int i=1; i<pixels1.length; i++){
					double[] y = {pixels1[i],pixels3[i],pixels5[i]};
					SimpleRegression regression = new SimpleRegression();
					double[][] data = {{x[0],y[0]},{x[1],y[1]},{x[2],y[2]}};
					regression.addData(data);

					 double a = regression.getSlope();
					 double rs = regression.getRSquare();

					 alpha[i]= a ; 
					 coeff[i]=rs;
				}
		//Image alpha
				//Affichage du coefficient de regression sous format image
				FloatProcessor fpr = new FloatProcessor(width, height, coeff);
					ImagePlus imprs = new Duplicator().run(imp);
					imprs.setProcessor(fpr);
//					imprs.show();
//					imprs.updateAndDraw();	
//					imprs.setTitle("Coeff");
				//On affiche l'image alpha
				FloatProcessor fp = new FloatProcessor(width, height, alpha);
				ImagePlus impf = new Duplicator().run(imp);
				impf.setProcessor(fp);
				impf.show();
				impf.updateAndDraw();	
				impf.setTitle("Image alpha");

	} 
}

