import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.Double;


public class APLT_2_ implements PlugIn {


	private double radius =2;

	public void run(String arg) {

		// Boite de dialogue pour les parametres
		GenericDialog gd = new GenericDialog("ADPLT settings");

		gd.addNumericField("radius:",radius,2);

		gd.showDialog();

		if (gd.wasCanceled()) {
			IJ.error("PlugIn canceled!");
			return;
		}

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
		ImagePlus imp1 = new Duplicator().run(imp);
		imp1.setProcessor(fp);

		ImagePlus imp2 = new Duplicator().run(imp1);
		ImagePlus imp3 = new Duplicator().run(imp1);
		ImagePlus impD = new Duplicator().run(imp1);
		IJ.run(imp2,"Maximum...", "radius="+Double.toString(radius));
		IJ.run(imp3,"Minimum...", "radius="+Double.toString(radius));
		ImageCalculator ic = new ImageCalculator();
		impD = ic.run("Subtract create 32-bit", imp2,imp3);
		impD.updateAndDraw();
//		impD.show();
		
		float[] d = (float[]) impD.getProcessor().getPixels();
		float[] result = new float [d.length];
		
		for(int i=0; i<pixels.length; i++){
			result[i]= (float) Math.pow((double)pixelsNormalised[i],(double) Math.log((double) (1/d[i])));
			
		}
		imp1.setProcessor(new FloatProcessor(width, height,result));
		imp1.show();
		imp1.setTitle("APLT");
	}

}
