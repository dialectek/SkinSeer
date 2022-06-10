// Generate machine learning classification data.
// Dangerous nevus penetrates dermis.

package skinseer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;

public class GenClassificationData
{
   public static final String Usage = "Usage: java GenClassificationData\n\t-datasetSize <dataset size>\n\t-datasetFilename <dataset file name>\n\t"
                                      + "-steps <steps per scan>\n\t"
                                      + "-nevusDistribution (repeatable argument)\n\t\t<mean width (0=no nevus)>\n\t\t<standard deviation of width>\n\t\t<mean height>\n\t\t<standard deviation of height>"
                                      + "\n\t\t<mean depth in epidermis>\n\t\t<standard deviation of depth in epidermis>\n\t\t<distribution frequency>\n\t"
                                      + "[-parameterFile <parameter file name>]";

   // Main.
   public static void main(String[] args)
   {
      int    datasetSize     = -1;
      String datasetFilename = null;
      int    steps           = -1;

      ArrayList<Double> nevusWidthMean             = new ArrayList<Double>();
      ArrayList<Double> nevusWidthSigma            = new ArrayList<Double>();
      ArrayList<Double> nevusHeightMean            = new ArrayList<Double>();
      ArrayList<Double> nevusHeightSigma           = new ArrayList<Double>();
      ArrayList<Double> nevusEpidermisDepthMean    = new ArrayList<Double>();
      ArrayList<Double> nevusEpidermisDepthSigma   = new ArrayList<Double>();
      ArrayList<Float>  nevusDistributionFrequency = new ArrayList<Float>();
      String            parameterFilename          = null;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-datasetSize"))
         {
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            datasetSize = Integer.parseInt(args[i]);
            if (datasetSize < 0)
            {
               System.err.println("Invalid datasetSize");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-datasetFilename"))
         {
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            datasetFilename = args[i];
            continue;
         }
         if (args[i].equals("-steps"))
         {
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            steps = Integer.parseInt(args[i]);
            if (steps < 0)
            {
               System.err.println("Invalid steps");
               System.err.println(Usage);
               System.exit(1);
            }
            continue;
         }
         if (args[i].equals("-nevusDistribution"))
         {
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            double widthMean = Double.parseDouble(args[i]);
            if (widthMean < 0.0)
            {
               System.err.println("Invalid nevus width mean");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            double widthSigma = Double.parseDouble(args[i]);
            if ((widthMean > 0.0) && (widthSigma < 0.0))
            {
               System.err.println("Invalid nevus width sigma");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            double heightMean = Double.parseDouble(args[i]);
            if (heightMean < 0.0)
            {
               System.err.println("Invalid nevus height mean");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            double heightSigma = Double.parseDouble(args[i]);
            if ((heightMean > 0.0) && (heightSigma < 0.0))
            {
               System.err.println("Invalid nevus height sigma");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            double depthMean = Double.parseDouble(args[i]);
            if (depthMean < 0.0)
            {
               System.err.println("Invalid nevus epidermis depth mean");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            double depthSigma = Double.parseDouble(args[i]);
            if (depthSigma < 0.0)
            {
               System.err.println("Invalid nevus epidermis depth sigma");
               System.err.println(Usage);
               System.exit(1);
            }
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            float distributionFrequency = Float.parseFloat(args[i]);
            if ((distributionFrequency < 0.0f) || (distributionFrequency > 1.0f))
            {
               System.err.println("Invalid nevus distribution frequency");
               System.err.println(Usage);
               System.exit(1);
            }
            nevusWidthMean.add(widthMean);
            nevusWidthSigma.add(widthSigma);
            nevusHeightMean.add(heightMean);
            nevusHeightSigma.add(heightSigma);
            nevusEpidermisDepthMean.add(depthMean);
            nevusEpidermisDepthSigma.add(depthSigma);
            nevusDistributionFrequency.add(distributionFrequency);
            continue;
         }
         if (args[i].equals("-parameterFile"))
         {
            i++;
            parameterFilename = args[i];
            continue;
         }
         System.err.println(Usage);
         System.exit(1);
      }
      if ((datasetSize == -1) || (datasetFilename == null) || (steps == -1) ||
          (nevusWidthMean.size() == 0))
      {
         System.err.println(Usage);
         System.exit(1);
      }
      float sum = 0.0f;
      for (Float distributionFrequency : nevusDistributionFrequency)
      {
         sum += distributionFrequency;
      }
      if (Math.abs(sum - 1.0f) > 0.001f)
      {
         System.err.println("Sum of distribution frequencies must equal 1");
         System.err.println(Usage);
         System.exit(1);
      }

      // Load parameters?
      if (parameterFilename != null)
      {
         SkinSeerSim.loadParameters(parameterFilename);
      }

      // Open the dataset file.
      try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                                                 new FileOutputStream(datasetFilename), StandardCharsets.UTF_8)))
         {
            // Generate data.
            SecureRandom random = new SecureRandom();
            for (int i = 0; i < datasetSize; i++)
            {
               // Choose a distribution.
               int   n     = 0;
               float p     = random.nextFloat();
               float accum = 0.0f;
               for ( ; n < nevusDistributionFrequency.size(); n++)
               {
                  accum += nevusDistributionFrequency.get(n);
                  if (p < accum) { break; }
               }
               if (n == nevusDistributionFrequency.size())
               {
                  n = 0;
               }

               // Set nevus properties.
               if (nevusWidthMean.get(n) == 0.0)
               {
                  SkinSeerSim.NEVUS_VALID = false;
               }
               else
               {
                  SkinSeerSim.NEVUS_VALID           = true;
                  SkinSeerSim.NEVUS_WIDTH           = (float)getDistributionValue(nevusWidthMean.get(n), nevusWidthSigma.get(n));
                  SkinSeerSim.NEVUS_HEIGHT          = (float)getDistributionValue(nevusHeightMean.get(n), nevusHeightSigma.get(n));
                  SkinSeerSim.NEVUS_EPIDERMIS_DEPTH = (float)getDistributionValue(nevusEpidermisDepthMean.get(n), nevusEpidermisDepthSigma.get(n));
               }

               // Run scanner.
               SkinSeerSim scanner = new SkinSeerSim();
               for (int step = 0; step < steps && scanner.step(); step++) {}

               // Nevus is dangerous if it penetrates dermis.
               boolean dangerous = false;
               if (nevusWidthMean.get(n) > 0.0)
               {
                  if ((scanner.nevus.shape.y - SkinSeerSim.NEVUS_HEIGHT) < SkinSeerSim.DERMIS_THICKNESS)
                  {
                     dangerous = true;
                  }
               }

               // Write dataset entry.
               for (int j = 0; j < SkinSeerSim.NUM_PHOTON_COUNTERS; j++)
               {
                  writer.write(scanner.photonDetector.photonCounters[j] + ",");
               }
               if (dangerous)
               {
                  writer.write("danger");
               }
               else
               {
                  writer.write("ok");
               }
               writer.write("\n");
            }
         }
         catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }




      System.exit(0);
   }


   // Get normal distribution value.
   public static double getDistributionValue(double mean, double sigma)
   {
      NormalDistribution distribution = new NormalDistribution(mean, sigma);

      return(distribution.nextValue());
   }
}
