import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.Double;


//Singh, T.R. et al., 2010. Image Enhancement by Adaptive Power-Law Transformations. , 3(1), pp.1–9. 
//Available at: papers://b6c7d293-c492-48a4-91d5-8fae456be1fa/Paper/p11891%5Cnfile:///C:/Users/Serguei/OneDrive/Documents/Papers/Image Enhancement by Adaptive Power-Law-2010-12-21.pdf.

//g'(x,y) = c(1+kd) g(x,y) r^(gamma*(1-kd))
//avec d=g(x,y) - mean(g(x,y))sur une fenetre de rayon 'radius'

public class AdPLT implements PlugIn {

	
	private double b = 1; //brightness
	private double g = 1.5; //gamma
	private double s = 5; //sharpness/smooting si k<0 les contrastes locaux sont attenués si k=0 : idem filtre gamma
	private double radius =10; //rayon de la fenetre sur laquelle est calcule mean

	public void run(String arg) {

		// Boite de dialogue pour les parametres
		GenericDialog gd = new GenericDialog("ADPLT settings");
		gd.addNumericField("Brightness (b):",b,2);
		gd.addNumericField("Gamma (g):",g ,2);
		gd.addNumericField("Sharpness (s):",s,2);
		gd.addNumericField("Radius (r):",radius,2);
		gd.showDialog();
		if (gd.wasCanceled()) {
			IJ.error("PlugIn canceled!");
			return;
		}

		b = gd.getNextNumber();
		g = gd.getNextNumber();
		s = gd.getNextNumber();
		radius = gd.getNextNumber();

		//image originale
		ImagePlus imp = IJ.getImage();
		int width=imp.getWidth();
		int height=imp.getHeight();
		
		//recuperer les pixels de l'image 8bits
		byte[] pixels =(byte[]) imp.getProcessor().getPixels();
		
		//les convertir en float
		float[] pixelsNormalised = new float [pixels.length];
		for(int i=0; i<pixels.length; i++){
			pixelsNormalised[i]=(float)(pixels[i] & 0xff)/(float)255.0;
		}
		
		//les passer à un autre ImagePlus
		FloatProcessor fp=new FloatProcessor(width,height,pixelsNormalised);
		ImagePlus imp2 = new Duplicator().run(imp);
		imp2.setProcessor(fp);

		//Dupliquer pour appliquer un filtre mean
		ImagePlus impD = new Duplicator().run(imp2);
		IJ.run(impD, "Mean...", "radius="+Double.toString(radius));
		ImageCalculator ic = new ImageCalculator();
		//difference =(i-imean)
		impD = ic.run("Subtract create 32-bit", imp2, impD);

		//Recuperer les pixels de l'image difference
		float[] d =(float[]) impD.getProcessor().getPixels();
		float[] result = new float [d.length];

		//compute the transform
		for(int i=0; i<pixels.length; i++){
			result[i]=(float)(b*(1+s*d[i]) * Math.pow((double)(pixelsNormalised[i]) , (double) g*(1-s*d[i])));
		}
		
		imp2.setProcessor(new FloatProcessor(width, height,result));
		//IJ.run(imp2, "Enhance Contrast", "saturated=0.35");
		imp2.setTitle("AdPLT_b="+Double.toString(b)+"_g="+Double.toString(g)+"_s="+Double.toString(s)+"_r="+Double.toString(radius));
		imp2.show();
		
		//fermer les image inutiles
		impD.close();
	}

}