/*
Copyright (c) 2014 Stephen Stair (sgstair@akkit.org)
Additional code Miguel Parra (miguelvp@msn.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

using winusbdotnet;

namespace winusbdotnet.UsbDevices
{
    public class CalibratedThermalFrame
    {
        public readonly int Width, Height;
        internal CalibratedThermalFrame(UInt16[] data)
        {
            Width = 208;
            Height = 156;
            PixelData = data;

            ushort min = 0xFFFF;
            ushort max = 0x0000;
            foreach (ushort v in PixelData)
            {
                //if (v < 2000) continue;
                //if (v > 49152) continue;
                // 14K to 32K is valid for min/max
                if (v < 14336) continue;
                if (v > 32767) continue;
                if (v < min) min = v;
                if (v > max) max = v;
            }
            MinValue = min;
            MaxValue = max;
        }
        // Needed to override Min/Max (for external adjustment).
        public void SetMinMax(UInt16 min, UInt16 max)
        {
            MinValue = min;
            MaxValue = max;
        }
        public readonly UInt16[] PixelData;
        public UInt16 MinValue;
        public UInt16 MaxValue;
    }

    public class ThermalFrame
    {
        public readonly int Width, Height;
        public readonly byte[] RawData;
        public readonly UInt16[] RawDataU16;
        public readonly UInt16 MinValue;
        public readonly UInt16 MaxValue;
        public readonly bool IsCalibrationFrame;
        public readonly bool IsUsableFrame;
        public readonly byte StatusByte;
        public readonly UInt16 StatusWord;

        internal ThermalFrame(Byte[] data)
        {
            Width = 208;
            Height = 156;
            // Original data stream.
            RawData = data;

            StatusByte = data[20]; // Uh, this byte appears to be an actual meaningful status byte.
            // just hanging out in the first dead pixel.
            // Some values seem to be 3=Normal frame, 1=Calibration frame, 6= pre-calibration?

            IsCalibrationFrame = StatusByte == 1;
            IsUsableFrame = StatusByte == 3;

            ushort min = 0xFFFF;
            ushort max = 0x0000;

            // Convert to 16 bit as well for easier manipulation of data.
            RawDataU16 = new UInt16[data.Length / 2];

            for (int i = 0; i < (data.Length / 2); i++)
            {
                UInt16 v = (UInt16)BitConverter.ToInt16(data, i * 2);
                if ((v > 1999) && (v < (16384 + 1638)))
                {
                    if (v < min) min = v;
                    if (v > max) max = v;
                }
                RawDataU16[i] = v;
            }
            MinValue = min;
            MaxValue = max;
        }

        // In case it's needed
        public void Inverse()
        {
            UInt16 temp;
            for (int i = 0; i < RawDataU16.Length; i++)
            {
                temp = RawDataU16[i];
                RawDataU16[i] = 65535;
                RawDataU16[i] -= temp;
            }
        }

        public CalibratedThermalFrame ProcessFrameU16(ThermalFrame calibrationFrame, ThermalFrame frameID4)
        {
            UInt16[] output = new UInt16[Width * Height];

            for (int i = 0; i < output.Length; i++)
            {
                long v = RawDataU16[i];
                long c = calibrationFrame.RawDataU16[i];
                long r = frameID4.RawDataU16[i];
                long div;

                // Only accept values if both the image data and the calibration data are within the accepted range
                // [2000-16383]
                if ((v < 2000) || (v > 16383) || (c < 2000) || (c > 16383))
                {
                    // Dead pixel, clamp it to zero.
                    v = 0;
                }
                else
                {
                    // Adjust divider for frameID4 based in the sensor data, instead of a constant
                    // Not sure what value to scale the divider based on the sensor input (v)
                    // in order to get rid off the horizontal bands at all ranges.
                    // Also it needs more adjustements because is brighter in the upper left
                    // div = 11;
                    div = v / 2048 + 8;
                    r /= div;
                    v += 0x4000;
                    v -= c;
                    v += r;
                }
                output[i] = (UInt16)v;

            }
            return new CalibratedThermalFrame(output);
        }
    }

    public class SeekThermal
    {
        public static IEnumerable<WinUSBEnumeratedDevice> Enumerate()
        {
            foreach (WinUSBEnumeratedDevice dev in WinUSBDevice.EnumerateAllDevices())
            {
                // Seek Thermal "iAP Interface" device - Use Zadig to install winusb driver on it.
                if (dev.VendorID == 0x289D && dev.ProductID == 0x0010 && dev.UsbInterface == 0)
                {
                    yield return dev;
                }
            }
        }

        WinUSBDevice device;

        public SeekThermal(WinUSBEnumeratedDevice dev)
        {
            device = new WinUSBDevice(dev);

            // device setup sequence
            try
            {
                device.ControlTransferOut(0x41, 0x54, 0, 0, new byte[] { 0x01 });
            }
            catch
            {
                // Try deinit device and repeat.
                Deinit();
                device.ControlTransferOut(0x41, 0x54, 0, 0, new byte[] { 0x01 });
            }

            device.ControlTransferOut(0x41, 0x3c, 0, 0, new byte[] { 0x00, 0x00 });

            byte[] data1 = device.ControlTransferIn(0xC1, 0x4e, 0, 0, 4);

            byte[] data2 = device.ControlTransferIn(0xC1, 0x36, 0, 0, 12);

            // Analysis of 0x56 payload: 
            // First byte seems to be half the size of the output data.
            // It seems like this command may be retriving some sensor data?
            device.ControlTransferOut(0x41, 0x56, 0, 0, new byte[] { 0x20, 0x00, 0x30, 0x00, 0x00, 0x00 });

            byte[] data3 = device.ControlTransferIn(0xC1, 0x58, 0, 0, 0x40);

            device.ControlTransferOut(0x41, 0x56, 0, 0, new byte[] { 0x20, 0x00, 0x50, 0x00, 0x00, 0x00 });

            byte[] data4 = device.ControlTransferIn(0xC1, 0x58, 0, 0, 0x40);

            device.ControlTransferOut(0x41, 0x56, 0, 0, new byte[] { 0x0C, 0x00, 0x70, 0x00, 0x00, 0x00 });

            byte[] data5 = device.ControlTransferIn(0xC1, 0x58, 0, 0, 0x18);

            device.ControlTransferOut(0x41, 0x56, 0, 0, new byte[] { 0x06, 0x00, 0x08, 0x00, 0x00, 0x00 });

            byte[] data6 = device.ControlTransferIn(0xC1, 0x58, 0, 0, 0x0c);


            device.ControlTransferOut(0x41, 0x3E, 0, 0, new byte[] { 0x08, 0x00 });

            byte[] data7 = device.ControlTransferIn(0xC1, 0x3D, 0, 0, 2);

            device.ControlTransferOut(0x41, 0x3E, 0, 0, new byte[] { 0x08, 0x00 });

            device.ControlTransferOut(0x41, 0x3C, 0, 0, new byte[] { 0x01, 0x00 });

            byte[] data8 = device.ControlTransferIn(0xC1, 0x3D, 0, 0, 2);

        }

        // 
        public void Deinit()
        {
            device.ControlTransferOut(0x41, 0x3C, 0, 0, new byte[] { 0x00, 0x00 });
            device.ControlTransferOut(0x41, 0x3C, 0, 0, new byte[] { 0x00, 0x00 });
            device.ControlTransferOut(0x41, 0x3C, 0, 0, new byte[] { 0x00, 0x00 });
        }

        public ThermalFrame GetFrameBlocking()
        {
            // Request frame (vendor interface request 0x53; data "C0 7e 00 00" which is half the size of the return data)
            device.ControlTransferOut(0x41, 0x53, 0, 0, new byte[] { 0xc0, 0x7e, 0, 0 });

            // Read data from IN 1 pipe
            return new ThermalFrame(device.ReadExactPipe(0x81, 0x7ec0 * 2));
        }
    }
}
