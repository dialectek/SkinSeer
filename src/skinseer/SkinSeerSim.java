/*
 * Copyright (c) 2018-2019 Tom Portegys (portegys@gmail.com). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, is
 * not permitted without express permission from the author. In addition:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY TOM PORTEGYS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// SkinSeer skin scanner simulation.

package skinseer;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import skinseer.NormalDistribution;

public class SkinSeerSim
{
   // Version.
   public static final String VERSION = "1.1";

   // Default random seed.
   public static final int DEFAULT_RANDOM_SEED = 4517;

   // Usage.
   public static final String Usage = "Usage: java SkinSeerSim -steps <steps>"
                                      + "\n\t[-parameterFile <file name>] [-printParameters]"
                                      + "\n\t[-photonDetectorCountsFile <file name>] (otherwise sent to standard output)";

   // Parameters.
   public static int     SCANNER_WIDTH       = 450;
   public static int     SCANNER_HEIGHT      = 250;
   public static float   SCANNER_SPEED       = 0.0f;
   public static float   EPIDERMIS_THICKNESS = 100.0f;
   public static float   EPIDERMIS_PHOTON_ABSORPTION_PROBABILITY  = 0.0f;
   public static float   EPIDERMIS_PHOTON_SCATTER_PROBABILITY     = 0.0f;
   public static double  EPIDERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN = 100.0;
   public static double  EPIDERMIS_PHOTON_SCATTER_ANGLE_SIGMA     = 50.0;
   public static float   DERMIS_THICKNESS = 75.0f;
   public static float   DERMIS_PHOTON_ABSORPTION_PROBABILITY  = 0.0f;
   public static float   DERMIS_PHOTON_SCATTER_PROBABILITY     = 1.0f;
   public static double  DERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN = -100.0;
   public static double  DERMIS_PHOTON_SCATTER_ANGLE_SIGMA     = 25.0;
   public static boolean NEVUS_VALID           = true;
   public static float   NEVUS_WIDTH           = 60.0f;
   public static float   NEVUS_HEIGHT          = 120.0f;
   public static float   NEVUS_X               = 200.0f;
   public static float   NEVUS_EPIDERMIS_DEPTH = 20.0f;
   public static float   NEVUS_PHOTON_ABSORPTION_PROBABILITY  = 1.0f;
   public static float   NEVUS_PHOTON_SCATTER_PROBABILITY     = 0.0f;
   public static double  NEVUS_PHOTON_SCATTER_ANGLE_ZERO_MEAN = 100.0;
   public static double  NEVUS_PHOTON_SCATTER_ANGLE_SIGMA     = 50.0;
   public static float   PHOTON_SOURCE_X           = 10.0f;
   public static float   PHOTON_SOURCE_RADIUS      = 30.0f;
   public static int     PHOTON_EMISSION_RATE      = 1;
   public static float   PHOTON_RADIUS             = 2.0f;
   public static float   PHOTON_MIN_EMISSION_ANGLE = 270.0f;
   public static float   PHOTON_MAX_EMISSION_ANGLE = 360.0f;
   public static float   PHOTON_SPEED          = 1.0f;
   public static float   PHOTON_DETECTOR_X     = 150.0f;
   public static float   PHOTON_DETECTOR_WIDTH = 15.0f;
   public static int     NUM_PHOTON_COUNTERS   = 10;

   // Load parameters.
   public static void loadParameters(String filename)
   {
      try (BufferedReader br = new BufferedReader(new FileReader(filename)))
         {
            for (String line; (line = br.readLine()) != null; )
            {
               if (line.startsWith("#")) { continue; }
               String[] parts = line.split("=");
               if ((parts == null) || (parts.length != 2))
               {
                  System.err.println("Invalid parameter in file " + filename);
                  System.exit(1);
               }
               String name  = parts[0];
               String value = parts[1];
               if (name.equals("SCANNER_WIDTH"))
               {
                  SCANNER_WIDTH = Integer.parseInt(value);
               }
               else if (name.equals("SCANNER_HEIGHT"))
               {
                  SCANNER_HEIGHT = Integer.parseInt(value);
               }
               else if (name.equals("SCANNER_SPEED"))
               {
                  SCANNER_SPEED = Float.parseFloat(value);
               }
               else if (name.equals("EPIDERMIS_THICKNESS"))
               {
                  EPIDERMIS_THICKNESS = Float.parseFloat(value);
               }
               else if (name.equals("EPIDERMIS_PHOTON_ABSORPTION_PROBABILITY"))
               {
                  EPIDERMIS_PHOTON_ABSORPTION_PROBABILITY = Float.parseFloat(value);
               }
               else if (name.equals("EPIDERMIS_PHOTON_SCATTER_PROBABILITY"))
               {
                  EPIDERMIS_PHOTON_SCATTER_PROBABILITY = Float.parseFloat(value);
               }
               else if (name.equals("EPIDERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN"))
               {
                  EPIDERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN = Double.parseDouble(value);
               }
               else if (name.equals("EPIDERMIS_PHOTON_SCATTER_ANGLE_SIGMA"))
               {
                  EPIDERMIS_PHOTON_SCATTER_ANGLE_SIGMA = Double.parseDouble(value);
               }
               else if (name.equals("DERMIS_THICKNESS"))
               {
                  DERMIS_THICKNESS = Float.parseFloat(value);
               }
               else if (name.equals("DERMIS_PHOTON_ABSORPTION_PROBABILITY"))
               {
                  DERMIS_PHOTON_ABSORPTION_PROBABILITY = Float.parseFloat(value);
               }
               else if (name.equals("DERMIS_PHOTON_SCATTER_PROBABILITY"))
               {
                  DERMIS_PHOTON_SCATTER_PROBABILITY = Float.parseFloat(value);
               }
               else if (name.equals("DERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN"))
               {
                  DERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN = Double.parseDouble(value);
               }
               else if (name.equals("DERMIS_PHOTON_SCATTER_ANGLE_SIGMA"))
               {
                  DERMIS_PHOTON_SCATTER_ANGLE_SIGMA = Double.parseDouble(value);
               }
               else if (name.equals("NEVUS_VALID"))
               {
                  NEVUS_VALID = Boolean.parseBoolean(value);
               }
               else if (name.equals("NEVUS_WIDTH"))
               {
                  NEVUS_WIDTH = Float.parseFloat(value);
               }
               else if (name.equals("NEVUS_HEIGHT"))
               {
                  NEVUS_HEIGHT = Float.parseFloat(value);
               }
               else if (name.equals("NEVUS_X"))
               {
                  NEVUS_X = Float.parseFloat(value);
               }
               else if (name.equals("NEVUS_EPIDERMIS_DEPTH"))
               {
                  NEVUS_EPIDERMIS_DEPTH = Float.parseFloat(value);
               }
               else if (name.equals("NEVUS_PHOTON_ABSORPTION_PROBABILITY"))
               {
                  NEVUS_PHOTON_ABSORPTION_PROBABILITY = Float.parseFloat(value);
               }
               else if (name.equals("NEVUS_PHOTON_SCATTER_PROBABILITY"))
               {
                  NEVUS_PHOTON_SCATTER_PROBABILITY = Float.parseFloat(value);
               }
               else if (name.equals("NEVUS_PHOTON_SCATTER_ANGLE_ZERO_MEAN"))
               {
                  NEVUS_PHOTON_SCATTER_ANGLE_ZERO_MEAN = Double.parseDouble(value);
               }
               else if (name.equals("NEVUS_PHOTON_SCATTER_ANGLE_SIGMA"))
               {
                  NEVUS_PHOTON_SCATTER_ANGLE_SIGMA = Double.parseDouble(value);
               }
               else if (name.equals("PHOTON_SOURCE_X"))
               {
                  PHOTON_SOURCE_X = Float.parseFloat(value);
               }
               else if (name.equals("PHOTON_SOURCE_RADIUS"))
               {
                  PHOTON_SOURCE_RADIUS = Float.parseFloat(value);
               }
               else if (name.equals("PHOTON_EMISSION_RATE"))
               {
                  PHOTON_EMISSION_RATE = Integer.parseInt(value);
               }
               else if (name.equals("PHOTON_RADIUS"))
               {
                  PHOTON_RADIUS = Float.parseFloat(value);
               }
               else if (name.equals("PHOTON_MIN_EMISSION_ANGLE"))
               {
                  PHOTON_MIN_EMISSION_ANGLE = Float.parseFloat(value);
               }
               else if (name.equals("PHOTON_MAX_EMISSION_ANGLE"))
               {
                  PHOTON_MAX_EMISSION_ANGLE = Float.parseFloat(value);
               }
               else if (name.equals("PHOTON_SPEED"))
               {
                  PHOTON_SPEED = Float.parseFloat(value);
               }
               else if (name.equals("PHOTON_DETECTOR_X"))
               {
                  PHOTON_DETECTOR_X = Float.parseFloat(value);
               }
               else if (name.equals("PHOTON_DETECTOR_WIDTH"))
               {
                  PHOTON_DETECTOR_WIDTH = Float.parseFloat(value);
               }
               else if (name.equals("NUM_PHOTON_COUNTERS"))
               {
                  NUM_PHOTON_COUNTERS = Integer.parseInt(value);
               }
            }
         }
         catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
         }
         catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }
         catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(1);
         }

   }


   // Print parameters.
   public static void printParameters()
   {
      System.out.println("Parameters:");
      System.out.println("SCANNER_WIDTH=" + SCANNER_WIDTH);
      System.out.println("SCANNER_HEIGHT=" + SCANNER_HEIGHT);
      System.out.println("EPIDERMIS_THICKNESS=" + EPIDERMIS_THICKNESS);
      System.out.println("EPIDERMIS_PHOTON_ABSORPTION_PROBABILITY=" + EPIDERMIS_PHOTON_ABSORPTION_PROBABILITY);
      System.out.println("EPIDERMIS_PHOTON_SCATTER_PROBABILITY=" + EPIDERMIS_PHOTON_SCATTER_PROBABILITY);
      System.out.println("EPIDERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN=" + EPIDERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN);
      System.out.println("EPIDERMIS_PHOTON_SCATTER_ANGLE_SIGMA=" + EPIDERMIS_PHOTON_SCATTER_ANGLE_SIGMA);
      System.out.println("DERMIS_THICKNESS=" + DERMIS_THICKNESS);
      System.out.println("DERMIS_PHOTON_ABSORPTION_PROBABILITY=" + DERMIS_PHOTON_ABSORPTION_PROBABILITY);
      System.out.println("DERMIS_PHOTON_SCATTER_PROBABILITY=" + DERMIS_PHOTON_SCATTER_PROBABILITY);
      System.out.println("DERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN=" + DERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN);
      System.out.println("DERMIS_PHOTON_SCATTER_ANGLE_SIGMA=" + DERMIS_PHOTON_SCATTER_ANGLE_SIGMA);
      if (NEVUS_VALID)
      {
         System.out.println("NEVUS_VALID=true");
      }
      else
      {
         System.out.println("NEVUS_VALID=false");
      }
      System.out.println("NEVUS_WIDTH=" + NEVUS_WIDTH);
      System.out.println("NEVUS_HEIGHT=" + NEVUS_HEIGHT);
      System.out.println("NEVUS_X=" + NEVUS_X);
      System.out.println("NEVUS_EPIDERMIS_DEPTH=" + NEVUS_EPIDERMIS_DEPTH);
      System.out.println("NEVUS_PHOTON_ABSORPTION_PROBABILITY=" + NEVUS_PHOTON_ABSORPTION_PROBABILITY);
      System.out.println("NEVUS_PHOTON_SCATTER_PROBABILITY=" + NEVUS_PHOTON_SCATTER_PROBABILITY);
      System.out.println("NEVUS_PHOTON_SCATTER_ANGLE_ZERO_MEAN=" + NEVUS_PHOTON_SCATTER_ANGLE_ZERO_MEAN);
      System.out.println("NEVUS_PHOTON_SCATTER_ANGLE_SIGMA=" + NEVUS_PHOTON_SCATTER_ANGLE_SIGMA);
      System.out.println("PHOTON_SOURCE_X=" + PHOTON_SOURCE_X);
      System.out.println("PHOTON_SOURCE_RADIUS=" + PHOTON_SOURCE_RADIUS);
      System.out.println("PHOTON_EMISSION_RATE=" + PHOTON_EMISSION_RATE);
      System.out.println("PHOTON_RADIUS=" + PHOTON_RADIUS);
      System.out.println("PHOTON_MIN_EMISSION_ANGLE=" + PHOTON_MIN_EMISSION_ANGLE);
      System.out.println("PHOTON_MAX_EMISSION_ANGLE=" + PHOTON_MAX_EMISSION_ANGLE);
      System.out.println("PHOTON_SPEED=" + PHOTON_SPEED);
      System.out.println("PHOTON_DETECTOR_X=" + PHOTON_DETECTOR_X);
      System.out.println("PHOTON_DETECTOR_WIDTH=" + PHOTON_DETECTOR_WIDTH);
      System.out.println("NUM_PHOTON_COUNTERS=" + NUM_PHOTON_COUNTERS);
   }


   // Photon source.
   public class PhotonSource
   {
      public Point2D.Float center;
      public float         epidermisY;
      public float         dermisY;

      // Photon.
      public ArrayList<Point2D.Float> photonTrace;
      public double        photonAngle;
      public Point2D.Float photonDirection;

      // Constructor.
      public PhotonSource()
      {
         float cx = PHOTON_SOURCE_X + PHOTON_SOURCE_RADIUS;
         float cy = PHOTON_SOURCE_RADIUS + EPIDERMIS_THICKNESS + DERMIS_THICKNESS;

         center     = new Point2D.Float(cx, cy);
         epidermisY = EPIDERMIS_THICKNESS + DERMIS_THICKNESS;
         dermisY    = DERMIS_THICKNESS;
      }


      // Clear.
      public void clearPhoton()
      {
         photonTrace     = null;
         photonDirection = null;
      }


      // Update.
      // Return false when photon track completed.
      public boolean updatePhoton()
      {
         if (photonTrace == null)
         {
            photonTrace     = new ArrayList<Point2D.Float>();
            photonDirection = new Point2D.Float();
            photonAngle     = (Math.random() * (PHOTON_MAX_EMISSION_ANGLE - PHOTON_MIN_EMISSION_ANGLE)) +
                              PHOTON_MIN_EMISSION_ANGLE;
            photonDirection.x = (float)Math.cos(toRadians(photonAngle));
            photonDirection.y = (float)Math.sin(toRadians(photonAngle));
            Point2D.Float p = new Point2D.Float();
            p.x = center.x + (photonDirection.x * PHOTON_RADIUS);
            p.y = center.y + (photonDirection.y * PHOTON_RADIUS);
            photonTrace.add(p);
            return(true);
         }
         else
         {
            Point2D.Float p1 = photonTrace.get(photonTrace.size() - 1);
            if (nevus.contains(p1))
            {
               if (Math.random() < NEVUS_PHOTON_ABSORPTION_PROBABILITY)
               {
                  photonTrace = null;
                  return(false);
               }
               else if (Math.random() < NEVUS_PHOTON_SCATTER_PROBABILITY)
               {
                  photonAngle += scatterAngle(NEVUS_PHOTON_SCATTER_ANGLE_ZERO_MEAN,
                                              NEVUS_PHOTON_SCATTER_ANGLE_SIGMA);
                  photonDirection.x = (float)Math.cos(toRadians(photonAngle));
                  photonDirection.y = (float)Math.sin(toRadians(photonAngle));
               }
            }
            else
            {
               if ((p1.x >= 0) && (p1.x < (float)SCANNER_WIDTH))
               {
                  if ((p1.y <= epidermisY) && (p1.y > dermisY))
                  {
                     if (Math.random() < EPIDERMIS_PHOTON_ABSORPTION_PROBABILITY)
                     {
                        photonTrace = null;
                        return(false);
                     }
                     else if (Math.random() < EPIDERMIS_PHOTON_SCATTER_PROBABILITY)
                     {
                        photonAngle += scatterAngle(EPIDERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN,
                                                    EPIDERMIS_PHOTON_SCATTER_ANGLE_SIGMA);
                        photonDirection.x = (float)Math.cos(toRadians(photonAngle));
                        photonDirection.y = (float)Math.sin(toRadians(photonAngle));
                     }
                  }
                  else if ((p1.y <= dermisY) && (p1.y >= 0.0f))
                  {
                     if (Math.random() < DERMIS_PHOTON_ABSORPTION_PROBABILITY)
                     {
                        photonTrace = null;
                        return(false);
                     }
                     else if (Math.random() < DERMIS_PHOTON_SCATTER_PROBABILITY)
                     {
                        photonAngle += scatterAngle(DERMIS_PHOTON_SCATTER_ANGLE_ZERO_MEAN,
                                                    DERMIS_PHOTON_SCATTER_ANGLE_SIGMA);
                        photonDirection.x = (float)Math.cos(toRadians(photonAngle));
                        photonDirection.y = (float)Math.sin(toRadians(photonAngle));
                     }
                  }
               }
            }
            Point2D.Float p2 = new Point2D.Float();
            p2.x = p1.x + (photonDirection.x * PHOTON_SPEED);
            p2.y = p1.y + (photonDirection.y * PHOTON_SPEED);
            if ((p2.x < 0.0f) || (p2.x >= (float)SCANNER_WIDTH) ||
                (p2.y < 0.0f) || (p2.y >= (float)SCANNER_HEIGHT))
            {
               clearPhoton();
               return(false);
            }
            if ((p2.y > epidermisY) && (photonDirection.y > 0.0f))
            {
               // Detect photon.
               if (photonDetector.detect(p2) != -1)
               {
                  clearPhoton();
                  return(false);
               }
            }
            photonTrace.add(p2);
            return(true);
         }
      }


      // Get scatter angle.
      public double scatterAngle(double mean, double sigma)
      {
         NormalDistribution scatterer = new NormalDistribution(mean, sigma);

         return(scatterer.nextValue() - mean);
      }


      // Convert degrees to radians.
      double toRadians(double angle)
      {
         return(angle * (Math.PI / 180.0));
      }


      // Euclidean distance.
      float dist(Point2D.Float p1, Point2D.Float p2)
      {
         return((float)Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2)));
      }
   }

   // Photon detector.
   public class PhotonDetector
   {
      public int[] photonCounters;
      public float epidermisY;
      public float width;

      // Constructor.
      public PhotonDetector()
      {
         photonCounters = new int[NUM_PHOTON_COUNTERS];
         epidermisY     = EPIDERMIS_THICKNESS + DERMIS_THICKNESS;
         width          = SkinSeerSim.PHOTON_DETECTOR_WIDTH * (float)SkinSeerSim.NUM_PHOTON_COUNTERS;
      }


      // Reset.
      public void reset()
      {
         for (int i = 0; i < NUM_PHOTON_COUNTERS; i++)
         {
            photonCounters[i] = 0;
         }
      }


      // Detect photon.
      public int detect(Point2D.Float photon)
      {
         for (int i = 0; i < NUM_PHOTON_COUNTERS; i++)
         {
            float x2 = (int)(PHOTON_DETECTOR_X + (PHOTON_DETECTOR_WIDTH * (float)i));
            if ((photon.x >= x2) && (photon.x < (x2 + PHOTON_DETECTOR_WIDTH)))
            {
               photonCounters[i]++;
               return(i);
            }
         }
         return(-1);
      }


      // Print photon counts.
      public void printCounts(String photonDetectorCountsFilename)
      {
         PrintWriter writer = new PrintWriter(System.out);

         if (photonDetectorCountsFilename != null)
         {
            FileOutputStream file = null;
            try
            {
               file = new FileOutputStream(new File(photonDetectorCountsFilename));
            }
            catch (Exception e)
            {
               System.err.println("Cannot open photon detector counts file " + photonDetectorCountsFilename + ":" + e.getMessage());
            }
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(file)));
         }
         String extension = "";
         int    i         = photonDetectorCountsFilename.lastIndexOf('.');
         if (i > 0)
         {
            extension = photonDetectorCountsFilename.substring(i + 1);
         }
         if (extension.equals("csv"))
         {
            for (i = 0; i < NUM_PHOTON_COUNTERS; i++)
            {
               writer.print(photonCounters[i] + "");
               if (i < NUM_PHOTON_COUNTERS - 1)
               {
                  writer.print(",");
               }
               else
               {
                  writer.println();
               }
            }
         }
         else
         {
            writer.println("Photon counts:");
            for (i = 0; i < NUM_PHOTON_COUNTERS; i++)
            {
               writer.println(i + ": " + photonCounters[i]);
            }
         }
         writer.flush();
         if (photonDetectorCountsFilename != null)
         {
            writer.close();
         }
      }
   }

   // Nevus.
   public class Nevus
   {
      Ellipse2D.Float shape;

      // Constructor.
      public Nevus()
      {
         float epidermisY = EPIDERMIS_THICKNESS + DERMIS_THICKNESS;

         shape = new Ellipse2D.Float(NEVUS_X, epidermisY - NEVUS_EPIDERMIS_DEPTH, NEVUS_WIDTH, NEVUS_HEIGHT);
      }


      // Contains point?
      public boolean contains(Point2D.Float p)
      {
         Ellipse2D.Float s = (Ellipse2D.Float)nevus.shape.clone();
         s.y -= s.height;
         return(s.intersects(p.x - PHOTON_RADIUS, p.y - PHOTON_RADIUS,
                             PHOTON_RADIUS * 2.0f, PHOTON_RADIUS * 2.0f));
      }


      // Reset.
      public void reset()
      {
         shape.x = NEVUS_X;
      }
   }

   // Components.
   public PhotonSource   photonSource;
   public PhotonDetector photonDetector;
   public Nevus          nevus;

   // Client status notification.
   public SkinSeerSimNotifier notifier;

   // Constructors.
   public SkinSeerSim()
   {
      // Create components.
      photonSource   = new PhotonSource();
      photonDetector = new PhotonDetector();
      nevus          = new Nevus();
      notifier       = null;
   }


   public SkinSeerSim(SkinSeerSimNotifier notifier)
   {
      // Create components.
      photonSource   = new PhotonSource();
      photonDetector = new PhotonDetector();
      nevus          = new Nevus();
      this.notifier  = notifier;
   }


   // Reset.
   public void reset()
   {
      photonSource.clearPhoton();
      photonDetector.reset();
      nevus.reset();
   }


   // Step.
   // Return false if nevus out of photon range.
   public boolean step()
   {
      // Nevus out of range?
      if ((nevus.shape.x) >= (float)SCANNER_WIDTH)
      {
         notifyClient();
         return(false);
      }

      // Emit photons.
      for (int i = 0; i < PHOTON_EMISSION_RATE; i++)
      {
         // Update photon.
         while (photonSource.updatePhoton())
         {
            notifyClient();
         }
         notifyClient();
      }

      // Move scanner.
      nevus.shape.x += SCANNER_SPEED;
      notifyClient();
      return(true);
   }


   // Notify client of status.
   public void notifyClient()
   {
      if (notifier != null)
      {
         notifier.scannerStatus(this);
      }
   }


   // Main.
   public static void main(String[] args)
   {
      int     steps                        = -1;
      String  parameterFilename            = null;
      String  photonDetectorCountsFilename = null;
      boolean printParms                   = false;

      // Get arguments.
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-steps"))
         {
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            if (steps >= 0)
            {
               System.err.println("Duplicate steps");
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
         if (args[i].equals("-parameterFile"))
         {
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            if (parameterFilename != null)
            {
               System.err.println("Duplicate parameterFilename");
               System.err.println(Usage);
               System.exit(1);
            }
            parameterFilename = args[i];
            continue;
         }
         if (args[i].equals("-photonDetectorCountsFile"))
         {
            i++;
            if (i == args.length)
            {
               System.err.println(Usage);
               System.exit(1);
            }
            if (photonDetectorCountsFilename != null)
            {
               System.err.println("Duplicate photonDetectorCountsFile");
               System.err.println(Usage);
               System.exit(1);
            }
            photonDetectorCountsFilename = args[i];
            continue;
         }
         if (args[i].equals("-printParameters"))
         {
            printParms = true;
            continue;
         }
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         if (args[i].equals("-version"))
         {
            System.out.println("SkinSeerSim version = " + VERSION);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.err.println(Usage);
         System.exit(1);
      }

      // Load parameters?
      if (parameterFilename != null)
      {
         loadParameters(parameterFilename);
      }

      // Print parameters?
      if (printParms)
      {
         printParameters();
      }

      if (steps == -1)
      {
         if (printParms)
         {
            System.exit(0);
         }
         System.err.println(Usage);
         System.exit(1);
      }

      // Create scanner.
      SkinSeerSim scanner = new SkinSeerSim();

      // Run.
      for (int i = 0; i < steps && scanner.step(); i++) {}

      // Print photon detector counts.
      scanner.photonDetector.printCounts(photonDetectorCountsFilename);

      System.exit(0);
   }
}
