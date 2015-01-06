using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;
using winusbdotnet.UsbDevices;
using System.Threading;

namespace TestSeek
{
    static class Program
    {

        //Seek Thermal object
        static SeekThermal thermal;

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {

            //init seek thermals
            var device = SeekThermal.Enumerate().FirstOrDefault();
            if (device == null)
            {
                MessageBox.Show("No Seek Thermal devices found.");
                return;
            }
            thermal = new SeekThermal(device);

            //start thermal thread
            Thread thermalThread = new Thread(ThermalThreadProc);
            thermalThread.IsBackground = true;
            thermalThread.Start();

            //loop
            while (true) ;

            /*
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new Form1());
            */
        }

        static void ThermalThreadProc()
        {
            //init
            ThermalFrame lastCalibrationFrame = null;
            ThermalFrame frameID4 = null;
            ThermalFrame frameID9 = null;
            ThermalFrame frameID8 = null;
            ThermalFrame frameID7 = null;
            ThermalFrame frameID10 = null;
            ThermalFrame frameID5 = null;
            CalibratedThermalFrame lastUsableFrame = null;
            int frameCount = 1;
            int fwctr = 1;

            //loop
            while (thermal != null)
            {
                bool progress = false;

                // Get frame
                ThermalFrame lastFrame = thermal.GetFrameBlocking();

                // Keep the first 6 frames, or anytime those frame IDs are encountered.
                // They might be usefull for image processing.
                switch (lastFrame.RawDataU16[10])
                {
                    case 4:
                        frameID4 = lastFrame;
                        //frameID4.Inverse();
                        break;
                    case 9:
                        frameID9 = lastFrame;
                        break;
                    case 8:
                        frameID8 = lastFrame;
                        break;
                    case 7:
                        frameID7 = lastFrame;
                        break;
                    case 10:
                        frameID10 = lastFrame;
                        break;
                    case 5:
                        frameID5 = lastFrame;
                        break;
                    default:
                        // Ignore the rest from safe keeping, since they are dealt with later.
                        //
                        // ID 1 is a calibration frame
                        // ID 6 is a pre-calibration frame
                        // ID 3 is a visual frame.
                        break;
                }

                if (lastFrame.IsCalibrationFrame)
                {
                    lastCalibrationFrame = lastFrame;
                }
                else
                {
                    if (lastCalibrationFrame != null && lastFrame.IsUsableFrame)
                    {
                        lastUsableFrame = lastFrame.ProcessFrameU16(lastCalibrationFrame, frameID4);
                        progress = true;
                    }
                }

                // Increase frame count.
                frameCount++;

                if (progress)
                {
                    //out to file
                    using (System.IO.StreamWriter file = new System.IO.StreamWriter(@"C:\Users\prat\Desktop\winusbdotnet-master\TestSeek\bin\Debug\frames\" + fwctr + ".csv", true))
                    {
                        //first line is (min,max) of current frame
                        file.WriteLine(lastFrame.MinValue + "," + lastFrame.MaxValue + "," + "0");
                        
                        int c = 0;
                        for (int y = 0; y < 156; y++)
                        {
                            for (int x = 0; x < 208; x++)
                            {
                                int v = lastUsableFrame.PixelData[c++];
                                file.WriteLine(x + "," + y + "," + v);
                            }
                        }
                    }

                    //inc ctr
                    fwctr++;
                }
            }
        }
    }
}
