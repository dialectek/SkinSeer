// Normal Distribution.

package skinseer;

import java.security.SecureRandom;

public class NormalDistribution
{
   public static final double DEFAULT_MEAN  = 10.0;
   public static final double DEFAULT_SIGMA = 2.0;
   private double             mean;
   private double             sigma;
   private SecureRandom       random;

   // Constructors.
   public NormalDistribution(double mean, double sigma)
   {
      this.mean  = mean;
      this.sigma = sigma;
      random     = new SecureRandom();
   }


   public NormalDistribution()
   {
      mean   = DEFAULT_MEAN;
      sigma  = DEFAULT_SIGMA;
      random = new SecureRandom();
   }


   public double getMean()
   {
      return(mean);
   }


   public void setMean(double mean)
   {
      this.mean = mean;
   }


   public double getSigma()
   {
      return(sigma);
   }


   public void setSigma(double sigma)
   {
      this.sigma = sigma;
   }


   // Get next value from distribution.
   public double nextValue()
   {
      double result = (random.nextGaussian() * sigma) + mean;

      if (result < 0.0)
      {
         result = 0.0;
      }
      return(result);
   }


   // Get probability value for x.
   public double phi(double x)
   {
      double d = x - mean;

      return(Math.exp(-(d * d) / (2.0 * sigma * sigma)) / (sigma * Math.sqrt(2.0 * Math.PI)));
   }
}
