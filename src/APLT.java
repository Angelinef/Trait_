import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.Double;

//Global Power Law Transform
//Lin et al. 2014 Computer Methods and Programs in Biomedecine
//g'(x,y) = g(x,y)^gamma
//gamma = k * log(1/d(x,y)
//avec d = max - min sur une fenetre de rayon 'radius'

public class APLT implements PlugIn {


	private double radius =7.0;
	private double k = 2.0;
	
	public void run(String arg) {

		// Boite de dialogue pour les parametres
		GenericDialog gd = new GenericDialog("ADPLT settings");
		gd.addNumericField("radius:",radius,2);
		gd.addNumericField("k:",k, 2);
		gd.showDialog();
		if (gd.wasCanceled()) {
			IJ.error("PlugIn canceled!");
			return;
		}
		radius = gd.getNextNumber();
		k = gd.getNextNumber();

		//image originale
		ImagePlus imp = IJ.getImage();
		int width=imp.getWidth();
		int height=imp.getHeight();
		//IJ.run(imp, "8-bit", "");
		
		//recuperer les pixels de l'image 8bits
		byte[] pixels =(byte[]) imp.getProcessor().getPixels();
		
		//les convertir en float
		float[] pixelsNormalised = new float [pixels.length];
		for(int i=0; i<pixels.length; i++){
			pixelsNormalised[i]=(float)(pixels[i] & 0xff)/(float)255.0;
		}
		//les passer à un autre ImagePlus
		FloatProcessor fp=new FloatProcessor(width,height,pixelsNormalised);
		ImagePlus imp1 = new Duplicator().run(imp);
		imp1.setProcessor(fp);

		ImagePlus impMax = new Duplicator().run(imp1);
		ImagePlus impMin = new Duplicator().run(imp1);
		ImagePlus impDiff = new Duplicator().run(imp1);
		IJ.run(impMax,"Maximum...", "radius="+Double.toString(radius));
		IJ.run(impMin,"Minimum...", "radius="+Double.toString(radius));
		ImageCalculator ic = new ImageCalculator();
		impDiff = ic.run("Subtract create 32-bit", impMax,impMin);
		IJ.run(impDiff, "Median...", "radius="+Double.toString(radius*2));
		//impDiff.updateAndDraw();
		//impDiff.show();
		
		float[] d = (float[]) impDiff.getProcessor().getPixels();
		float[] result = new float [d.length];
		
		for(int i=0; i<pixels.length; i++){
			result[i]= (float) Math.pow((double)pixelsNormalised[i],(double) -k*Math.log((double)d[i]) );
			
		}
		imp1.setProcessor(new FloatProcessor(width, height,result));
		imp1.show();
		imp1.setTitle("APLT_k="+Double.toString(k)+"_r="+Double.toString(radius));
		
		impMax.close();
		impMin.close();
		impDiff.close();
	}

}
