
import ij.IJ;
import ij.gui.GenericDialog;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import ij.plugin.*;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.*;
     
public class HoldExpo implements PlugIn {

	public void run(String arg) {
		
		// Boite de dialogue pour les parametres
		GenericDialog gd = new GenericDialog("HoldExpo settings");
		String[] items={"Avg","Sum","Max","Min"};
		gd.addRadioButtonGroup("Method", items, 3, 1, "Avg");
		gd.addCheckbox("Show all", false);
		gd.showDialog();
		if (gd.wasCanceled()) {
			IJ.error("PlugIn canceled!");
			return;
		}
		
		String method = gd.getNextRadioButton();
		boolean show = gd.getNextBoolean();
		
		//image originale
		ImagePlus imp = IJ.getImage();
		//On récupère la largeur et la hauteur de l'image
		int width=imp.getWidth();
		int height=imp.getHeight();
		
		// On convertit l'image en 32 bits
		ImageConverter icg = new ImageConverter(imp);
		icg.convertToGray32(); 
				 
		//On duplique l'image 3 fois 
		ImagePlus imp1 = new Duplicator().run(imp);
		ImagePlus imp3 = new Duplicator().run(imp);
		ImagePlus imp5 = new Duplicator().run(imp);
		 
		//******Convolution avec 1******	
		//Convolution : inutile
		//IJ.run(imp1, "Convolve...", "text1=1\n");
		//On récupère les pixels de l'image 32bits
		float[] pixels1 =(float[]) imp1.getProcessor().getPixels();
		// On applique le logarithme 
		for(int i=0; i<pixels1.length; i++){
			pixels1[i]=(float) Math.log((double)pixels1[i]);
			}
		// On créé une image 32 bits avec les pixels
		FloatProcessor fp1 = new FloatProcessor(width, height, pixels1);
		//Remplace l'image avec les nouvelles valeurs des pixels
		imp1.setProcessor(fp1);
		imp1.setTitle("log(1x1)");
		imp1.updateAndDraw();
		
		//*********Convolution avec matrice 3*3
		
		if(method=="Sum"){
			//sum
			IJ.run(imp3, "Convolve...", "text1=[1 1 1\n 1 1 1\n 1 1 1\n]");
		}
		else if(method=="Min"){
			IJ.run(imp3, "Minimum...", "radius=1");
		}
		else if(method=="Max"){
			IJ.run(imp3, "Maximum...", "radius=1");
		}
		else{
			//moyenne
			IJ.run(imp3, "Convolve...", "text1=[1 1 1\n 1 1 1\n 1 1 1\n] normalize");
		}
		//On récupère les pixels de l'image 32bits
		float[] pixels3 =(float[]) imp3.getProcessor().getPixels();
		// log 
		for(int i=0; i<pixels3.length; i++){
			pixels3[i]=(float) Math.log((double)pixels3[i]);
			//System.out.println(pixels3[i]);
		}
		FloatProcessor fp3 = new FloatProcessor(width, height, pixels3);
		//Remplace l'image avec les nouvelles valeurs des pixels
		imp3.setProcessor(fp3);
		imp3.setTitle("log(3x3)");
		imp3.updateAndDraw();
		
		//**********Convolution avec matrice 5*5		
		if(method=="Sum"){
			//sum
			IJ.run(imp5, "Convolve...", "text1=[1 1 1 1 1\n 1 1 1 1 1\n 1 1 1 1 1\n 1 1 1 1 1\n 1 1 1 1 1\n]");
		}
		else if(method=="Min"){
			IJ.run(imp5, "Minimum...", "radius=2");
		}
		else if(method=="Max"){
			IJ.run(imp5, "Maximum...", "radius=2");
		}
		else{
			//moyenne
			IJ.run(imp5, "Convolve...", "text1=[1 1 1 1 1\n 1 1 1 1 1\n 1 1 1 1 1\n 1 1 1 1 1\n 1 1 1 1 1\n] normalize");
		}
		//On récupère les pixels de l'image 32bits
		float[] pixels5 =(float[]) imp5.getProcessor().getPixels();
		// log 
		for(int i=0; i<pixels5.length; i++){
			pixels5[i]=(float) Math.log((double)pixels5[i]);
			//System.out.println(pixels5[i]);
			}
		//Remplace l'image avec les nouvelles valeurs des pixels 	   
		FloatProcessor fp5 = new FloatProcessor(width, height, pixels5);
		imp5.setProcessor(fp5);	
		imp5.setTitle("log(5x5)");
		imp5.updateAndDraw();
		
		//*********************************************************************
		 // Valeurs des alphas 		
		double[] x = {Math.log(1), Math.log(3), Math.log(5)};
		double[] alpha = new double[pixels1.length]; 
		double[] coeff = new double[pixels1.length];
		double[] SD = new double[pixels1.length];
		
		SimpleRegression regression = new SimpleRegression();

		// On calcule les valeurs des alphas pour chaque pixel 	
		for (int i=1; i<pixels1.length; i++){
			double[] y = {pixels1[i],pixels3[i],pixels5[i]};
			double[][] data = {{x[0],y[0]},{x[1],y[1]},{x[2],y[2]}};
			regression.clear();
			regression.addData(data);
			alpha[i]= regression.getSlope();
			coeff[i]= regression.getRSquare();
			SD[i]= regression.getSlopeStdErr();
			}
		
		//Image alpha
		//Affichage du R^2 sous format image
		FloatProcessor fpr = new FloatProcessor(width, height, coeff);
		ImagePlus imprs = new ImagePlus();
		imprs.setProcessor(fpr);
		imprs.setTitle("Image R^2");
		
		//On affiche ecart type de la pente
		FloatProcessor fp2 = new FloatProcessor(width, height, alpha);
		ImagePlus impf2 = new ImagePlus();
		impf2.setProcessor(fp2);
		impf2.setTitle("Image SD de alpha");
		
		//On affiche l'image alpha
		FloatProcessor fp = new FloatProcessor(width, height, alpha);
		ImagePlus impf = new ImagePlus();
		impf.setProcessor(fp);
		impf.setTitle("Image alpha "+method);
		impf.show();
		impf.updateAndDraw();	
		IJ.run(impf, "Enhance Contrast", "saturated=0.35");
		
		if(show){
			imp1.show();
			imp1.updateAndDraw();
			imp3.show();
			imp3.updateAndDraw();
			imp5.show();
			imp5.updateAndDraw();
			imprs.show();
			imprs.updateAndDraw();
			impf2.show();
			impf2.updateAndDraw();	
		}
		else{
			//fermer les images inutiles
			imp1.close();
			imp3.close();
			imp5.close();
			imprs.close();
			impf2.close();
		}
	} 
}

