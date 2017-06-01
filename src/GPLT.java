import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.Double;

//Global Power Law Transform
//Lin et al. 2014 Computer Methods and Programs in Biomedecine
//g'(x,y) = g(x,y)^gamma
//gamma fixe, exemples figure3 : gamma=2 ou gamma=0.5

public class GPLT implements PlugIn {

	private double gamma= 2.5;
 
	public void run(String arg) {

		// Boite de dialogue pour les parametres
		GenericDialog gd = new GenericDialog("ADPLT settings");
		gd.addNumericField("Gamma:",gamma,2);
		gd.showDialog();
		if (gd.wasCanceled()) {
			IJ.error("PlugIn canceled!");
			return;
		}   
    
		gamma = gd.getNextNumber();
		 
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
		ImagePlus imp1 = new ImagePlus();
		imp1.setProcessor(fp);

		//elever la valeur du pixel a la puissance gamma
		for(int i=0; i<pixels.length; i++){
			pixelsNormalised[i]= (float) Math.pow((double)pixelsNormalised[i],gamma);
		}
		
		imp1.setProcessor(new FloatProcessor(width, height,pixelsNormalised));
		imp1.show();
		imp1.setTitle("GPLT_gamma="+Double.toString(gamma));
	}

}
