// SkinSeer skin scanner simulation display.

package skinseer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SkinSeerSimDisplay extends JFrame
{
   private static final long serialVersionUID = 1L;

   public static final String Usage = "Usage: java SkinSeerSimDisplay"
                                      + "\n\t[-parameterFile <file name>] [-printParameters]"
                                      + "\n\t[-steps <steps> [-photonDetectorCountsFile <file name>] (otherwise sent to standard output)]";

   // Scanner.
   public SkinSeerSim scanner;

   // Steps.
   public int steps;

   // Photon detector counts file name.
   String photonDetectorCountsFilename;

   // Photon source display.
   public class PhotonSourceDisplay
   {
      public final Color COLOR = Color.WHITE;

      // Draw source and photon trace.
      public void draw(Graphics graphics)
      {
         graphics.setColor(COLOR);
         int   d = (int)(SkinSeerSim.PHOTON_SOURCE_RADIUS * 2.0f);
         float y = (float)SkinSeerSim.SCANNER_HEIGHT - scanner.photonSource.center.y;
         graphics.fillArc((int)(scanner.photonSource.center.x - SkinSeerSim.PHOTON_SOURCE_RADIUS),
                          (int)(y - SkinSeerSim.PHOTON_SOURCE_RADIUS), d, d, 180, 180);
         if (scanner.photonSource.photonTrace != null)
         {
            d = (int)(SkinSeerSim.PHOTON_RADIUS * 2.0f);
            for (Point2D.Float p : scanner.photonSource.photonTrace)
            {
               y = (float)SkinSeerSim.SCANNER_HEIGHT - p.y;
               graphics.fillOval((int)(p.x - SkinSeerSim.PHOTON_RADIUS),
                                 (int)(y - SkinSeerSim.PHOTON_RADIUS), d, d);
            }
         }
      }
   }

   // Photon detector display.
   public class PhotonDetectorDisplay
   {
      public final Color COLOR                     = Color.GREEN;
      public final float HEIGHT_ASPECT             = 0.15f;
      public final int   PHOTON_COUNTER_SATURATION = 10;
      public float       width, height, c, y;

      // Constructor.
      public PhotonDetectorDisplay()
      {
         width  = SkinSeerSim.PHOTON_DETECTOR_WIDTH * (float)SkinSeerSim.NUM_PHOTON_COUNTERS;
         height = width * HEIGHT_ASPECT;
         c      = height / (float)PHOTON_COUNTER_SATURATION;
         y      = SkinSeerSim.SCANNER_HEIGHT - height -
                  SkinSeerSim.EPIDERMIS_THICKNESS - SkinSeerSim.DERMIS_THICKNESS;
      }


      // Draw.
      public void draw(Graphics graphics)
      {
         graphics.setColor(COLOR);
         for (int i = 0; i < SkinSeerSim.NUM_PHOTON_COUNTERS; i++)
         {
            int photonCount = scanner.photonDetector.photonCounters[i];
            if (photonCount > 0)
            {
               if (photonCount > PHOTON_COUNTER_SATURATION)
               {
                  photonCount = PHOTON_COUNTER_SATURATION;
               }
               float h2 = (int)(c * (float)photonCount);
               float y2 = y + (height - h2);
               graphics.fillRect((int)(SkinSeerSim.PHOTON_DETECTOR_X + (SkinSeerSim.PHOTON_DETECTOR_WIDTH * (float)i)), (int)y2,
                                 (int)SkinSeerSim.PHOTON_DETECTOR_WIDTH, (int)h2);
            }
         }
         for (int i = 0; i < SkinSeerSim.NUM_PHOTON_COUNTERS; i++)
         {
            graphics.drawRect((int)(SkinSeerSim.PHOTON_DETECTOR_X + (SkinSeerSim.PHOTON_DETECTOR_WIDTH * (float)i)), (int)y,
                              (int)SkinSeerSim.PHOTON_DETECTOR_WIDTH, (int)height);
         }
      }
   }

   // Nevus display.
   public class NevusDisplay
   {
      public final Color COLOR = new Color(139, 0, 0);

      // Draw.
      public void draw(Graphics graphics)
      {
         if (SkinSeerSim.NEVUS_VALID)
         {
            graphics.setColor(COLOR);
            Ellipse2D.Float shape = scanner.nevus.shape;
            float           y     = (float)SkinSeerSim.SCANNER_HEIGHT - shape.y;
            graphics.fillOval((int)shape.x, (int)y, (int)shape.width, (int)shape.height);
         }
      }
   }

   // Scanner display.
   public class ScannerDisplay extends Canvas implements SkinSeerSimNotifier, Runnable
   {
      private static final long serialVersionUID = 1L;

      // Components.
      public PhotonSourceDisplay   photonSourceDisplay;
      public PhotonDetectorDisplay photonDetectorDisplay;
      public NevusDisplay          nevusDisplay;

      public final Color EPIDERMIS_COLOR = new Color(255, 205, 148);
      public final Color DERMIS_COLOR    = new Color(255, 173, 96);
      public float       epidermisY;
      public float       dermisY;
      public int         step;

      // Display update frequency (ms).
      public static final int DISPLAY_DELAY = 50;

      // Buffered display.
      Graphics graphics;
      Image    image;
      Graphics imageGraphics;

      // Message and font.
      String      message;
      Font        font = new Font("Helvetica", Font.BOLD, 12);
      FontMetrics fontMetrics;
      int         fontAscent;
      int         fontWidth;
      int         fontHeight;

      // Display thread.
      private Thread thread;

      // Constructor.
      public ScannerDisplay()
      {
         setSize(SkinSeerSim.SCANNER_WIDTH, SkinSeerSim.SCANNER_HEIGHT);
         photonSourceDisplay   = new PhotonSourceDisplay();
         photonDetectorDisplay = new PhotonDetectorDisplay();
         nevusDisplay          = new NevusDisplay();
         epidermisY            = SkinSeerSim.SCANNER_HEIGHT -
                                 (SkinSeerSim.EPIDERMIS_THICKNESS + SkinSeerSim.DERMIS_THICKNESS);
         dermisY = SkinSeerSim.SCANNER_HEIGHT - SkinSeerSim.DERMIS_THICKNESS;
         step    = 0;
      }


      // Start.
      public void start()
      {
         // Set graphics and font.
         graphics      = getGraphics();
         image         = createImage(SkinSeerSim.SCANNER_WIDTH, SkinSeerSim.SCANNER_HEIGHT);
         imageGraphics = image.getGraphics();
         graphics.setFont(font);
         imageGraphics.setFont(font);
         fontMetrics = graphics.getFontMetrics();
         fontAscent  = fontMetrics.getMaxAscent();
         fontWidth   = fontMetrics.getMaxAdvance();
         fontHeight  = fontMetrics.getHeight();

         // Start thread.
         thread = new Thread(this);
         thread.setPriority(Thread.MIN_PRIORITY);
         thread.start();
      }


      // Run.
      public void run()
      {
         boolean active = true;

         while (Thread.currentThread() == thread)
         {
            drawStep();

            // Steps completed?
            if ((steps != -1) && (step >= steps))
            {
               // Print photon detector counts?
               if (photonDetectorCountsFilename != null)
               {
                  scanner.photonDetector.printCounts(photonDetectorCountsFilename);
               }
               System.exit(0);
            }

            // Step scanner?
            if (active && (controls.delay < Controls.MAX_DELAY))
            {
               active = scanner.step();
               step++;
            }
         }
      }


      // Draw step.
      void drawStep()
      {
         while (true)
         {
            try
            {
               if (steps != -1)
               {
                  setMessage("step=" + step + " / " + steps);
               }
               else
               {
                  setMessage("step=" + step);
               }
               draw();
               if (controls.delay == Controls.MAX_DELAY)
               {
                  Thread.sleep(DISPLAY_DELAY);
               }
               else
               {
                  int t = 0;
                  while (t < controls.delay && controls.delay < Controls.MAX_DELAY)
                  {
                     int t2 = controls.delay - t;
                     if (t2 > DISPLAY_DELAY)
                     {
                        t2 = DISPLAY_DELAY;
                     }
                     Thread.sleep(t2);
                     t += t2;
                     draw();
                  }
                  if (controls.delay < Controls.MAX_DELAY)
                  {
                     break;
                  }
               }
            }
            catch (InterruptedException e) {}
         }
      }


      // Draw display.
      void draw()
      {
         // Clear display.
         imageGraphics.setColor(Color.BLACK);
         imageGraphics.fillRect(0, 0, SkinSeerSim.SCANNER_WIDTH, SkinSeerSim.SCANNER_HEIGHT);

         // Draw epidermis and dermis.
         imageGraphics.setColor(EPIDERMIS_COLOR);
         imageGraphics.fillRect(0, (int)epidermisY, SkinSeerSim.SCANNER_WIDTH,
                                (int)SkinSeerSim.EPIDERMIS_THICKNESS);
         imageGraphics.setColor(DERMIS_COLOR);
         imageGraphics.fillRect(0, (int)dermisY, SkinSeerSim.SCANNER_WIDTH,
                                (int)SkinSeerSim.DERMIS_THICKNESS);

         // Draw nevus.
         nevusDisplay.draw(imageGraphics);

         // Draw photon detector.
         photonDetectorDisplay.draw(imageGraphics);

         // Draw photon source.
         photonSourceDisplay.draw(imageGraphics);

         // Draw message.
         drawMessage();

         // Refresh display.
         graphics.drawImage(image, 0, 0, this);
      }


      // Set message.
      public void setMessage(String s)
      {
         message = s;
      }


      // Draw message.
      private void drawMessage()
      {
         int w;

         if (message == null) { return; }
         imageGraphics.setColor(Color.black);
         w = fontMetrics.stringWidth(message);
         imageGraphics.setColor(Color.white);
         imageGraphics.drawString(message, (SkinSeerSim.SCANNER_WIDTH - w) / 2, fontHeight + 4);
      }


      // Scanner status callback.
      public void scannerStatus(SkinSeerSim scanner)
      {
         drawStep();
      }
   }

   // Display.
   ScannerDisplay scannerDisplay;

   // Control panel.
   class Controls extends JPanel implements ActionListener, ChangeListener
   {
      private static final long serialVersionUID = 1L;

      public final static int CONTROLS_HEIGHT = 75;

      // Simulation speed given as update delay time (ms).
      public final static int MIN_DELAY = 1;
      public final static int MAX_DELAY = 50;
      public int              delay;

      // Components.
      Button  controlButton;
      JSlider speedSlider;

      // Constructor.
      Controls()
      {
         setSize(SkinSeerSim.SCANNER_WIDTH, CONTROLS_HEIGHT);
         controlButton = new Button("Reset");
         controlButton.addActionListener(this);
         add(controlButton);
         add(new Label("Fast", Label.RIGHT));
         speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_DELAY,
                                   MAX_DELAY, MAX_DELAY);
         speedSlider.addChangeListener(this);
         add(speedSlider);
         add(new Label("Stop", Label.LEFT));
         if (steps != -1)
         {
            delay = 0;
            speedSlider.setValue(0);
         }
         else
         {
            delay = MAX_DELAY;
         }
      }


      // Reset button listener.
      public void actionPerformed(ActionEvent evt)
      {
         scanner.reset();
         scannerDisplay.step = 0;
      }


      // Speed slider listener.
      public void stateChanged(ChangeEvent evt)
      {
         delay = speedSlider.getValue();
      }
   }

   // Controls.
   Controls controls;

   // Constructor.
   public SkinSeerSimDisplay(int steps, String photonDetectorCountsFilename)
   {
      this.steps = steps;
      this.photonDetectorCountsFilename = photonDetectorCountsFilename;

      // Set title.
      setTitle("SkinSeer scanner simulation");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      // Create display.
      setLayout(new BorderLayout());
      scannerDisplay = new ScannerDisplay();
      getContentPane().add(scannerDisplay, BorderLayout.NORTH);

      // Create scanner, giving it display callback.
      scanner = new SkinSeerSim(scannerDisplay);

      // Create controls.
      controls = new Controls();
      getContentPane().add(controls, BorderLayout.SOUTH);

      // Show app.
      pack();
      setVisible(true);

      // Start display thread.
      scannerDisplay.start();
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
         if (args[i].equals("-help") || args[i].equals("-h") || args[i].equals("-?"))
         {
            System.out.println(Usage);
            System.exit(0);
         }
         if (args[i].equals("-version"))
         {
            System.out.println("SkinSeerSim version = " + SkinSeerSim.VERSION);
            System.exit(0);
         }
         System.err.println("Invalid option: " + args[i]);
         System.out.println(Usage);
         System.exit(1);
      }
      if ((steps == -1) && (photonDetectorCountsFilename != null))
      {
         System.out.println(Usage);
         System.exit(1);
      }

      // Load parameters?
      if (parameterFilename != null)
      {
         SkinSeerSim.loadParameters(parameterFilename);
      }

      // Print parameters?
      if (printParms)
      {
         SkinSeerSim.printParameters();
      }

      @SuppressWarnings("unused")
      SkinSeerSimDisplay scannerDisplay = new SkinSeerSimDisplay(steps, photonDetectorCountsFilename);
   }
}
