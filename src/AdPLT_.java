
import ij.IJ;
import ij.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.Double;
   
public class AdPLT_ implements PlugIn {

	private static double c=1;	//1;
	private static double g=1.25;	//1.25;
	private static double k=5;	//5;
	private static double radius=10.0;
    
	//*************************************************	
	// compute c(1+kd) r^(g(1-kd))
	public void run(String arg) {

  //image originale
		ImagePlus imp = IJ.getImage();
		int width=imp.getWidth();
		int height=imp.getHeight();
  //recuperer les pixels de l'image 8bits
		ImageConverter icg1 = new ImageConverter(imp);
		icg1.convertToGray32();
    float[] pixels =(float[]) imp.getProcessor().getPixels();

   //les passer � un autre ImagePlus
		FloatProcessor fp=new FloatProcessor(width,height,pixels);
		ImagePlus imp2 = new Duplicator().run(imp);
		imp2.setProcessor(fp);
		//imp2.show();

    //Dupliquer pour appliquer un filtre mean
		ImagePlus impD = new Duplicator().run(imp2);
		IJ.run(impD, "Mean...", "radius="+Double.toString(radius));
		ImageCalculator ic = new ImageCalculator();
		//difference =(i-imean)
		impD = ic.run("Subtract create 32-bit", imp2, impD);
		//impD.show();
		
    //Recuperer les pixels de l'image difference
		float[] d =(float[]) impD.getProcessor().getPixels();
		float[] result = new float [d.length];
		
		//compute the transform
		for(int i=0; i<pixels.length; i++){
			result[i]=(float)(c*(1+k*d[i]) * Math.pow((double)(pixels[i]) , (double) g*(1-k*d[i])));
			}
		
		imp2.setProcessor(new FloatProcessor(width, height,result));
		IJ.run(imp2, "Enhance Contrast", "saturated=0.35");
		imp2.show();
		impD.close();
	}

}
