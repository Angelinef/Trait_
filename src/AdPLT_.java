import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.Double;


public class AdPLT_ implements PlugIn {

	private double g = 1.25;
	private double c = 1;
	private double k = 5;
	private double radius =10;

	public void run(String arg) {

		// Boite de dialogue pour les parametres
		GenericDialog gd = new GenericDialog("ADPLT settings");
		gd.addNumericField("c:",c,2);
		gd.addNumericField("gamma:",g ,2);
		gd.addNumericField("k:",k,2);
		gd.addNumericField("radius:",radius,2);

		gd.showDialog();

		if (gd.wasCanceled()) {
			IJ.error("PlugIn canceled!");
			return;
		}

		c = gd.getNextNumber();
		g = gd.getNextNumber();
		k = gd.getNextNumber();
		radius = gd.getNextNumber();

		//image originale
		ImagePlus imp = IJ.getImage();
		int width=imp.getWidth();
		int height=imp.getHeight();
		IJ.run(imp, "8-bit", "");
		
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
			result[i]=(float)(c*(1+k*d[i]) * Math.pow((double)(pixelsNormalised[i]) , (double) g*(1-k*d[i])));
		}
		
		

		imp2.setProcessor(new FloatProcessor(width, height,result));
		//IJ.run(imp2, "Enhance Contrast", "saturated=0.35");
		imp2.show();
		imp2.setTitle("APLT"+" "+"c="+c+" "+"g="+g+" "+"k="+k+" "+"r="+radius);  
	}

}