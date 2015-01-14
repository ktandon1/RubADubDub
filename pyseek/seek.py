# You will need to have python 2.7 (3+ may work)
# and PyUSB 1.0
# and PIL 1.1.6 or better
# and numpy
# and scipy
# and ImageMagick

# Many thanks to the folks at eevblog, especially (in no particular order) 
#   miguelvp, marshallh, mikeselectricstuff, sgstair and many others
#     for the inspiration to figure this out
# This is not a finished product and you can use it if you like. Don't be
# surprised if there are bugs as I am NOT a programmer..... ;>))


## https://github.com/sgstair/winusbdotnet/blob/master/UsbDevices/SeekThermal.cs

import usb.core
import usb.util
from PIL import Image, ImageTk
import numpy as np
from scipy.misc import toimage
import sys, os, time
import cv2


def processFrame(frame, calibFrame, frameID4):
    mask = np.logical_and(True, frame > 2000);
    mask = np.logical_and(mask, frame < 16383);
    mask = np.logical_and(mask, calibFrame > 2000);
    mask = np.logical_and(mask, calibFrame < 16383);

    p = np.array(frame, dtype=np.float64);
    div = p / 2048.0 + 8;
    r = np.divide(frameID4, div)
    p += 16384.0
    p -= calibFrame
    p += r

    # convert to Celsius
    p -= 15000
    p /= 50.338

    # convert to Fahrenheit
    p *= 9/5.0;
    p += 32;

    p[ mask != True ] = 0

    return p


# find our Seek Thermal device  289d:0010
dev = usb.core.find(idVendor=0x289d, idProduct=0x0010)
if not dev: 
    raise ValueError('Device not found')

print dev

def send_msg(bmRequestType, bRequest, wValue=0, wIndex=0, data_or_wLength=None, timeout=None):
    assert (dev.ctrl_transfer(bmRequestType, bRequest, wValue, wIndex, data_or_wLength, timeout) == len(data_or_wLength))

# alias method to make code easier to read
receive_msg = dev.ctrl_transfer

def deinit():
    '''Deinit the device'''
    msg = '\x00\x00'
    for i in range(3):
        send_msg(0x41, 0x3C, 0, 0, msg)


# set the active configuration. With no arguments, the first configuration will be the active one
dev.set_configuration()

# get an endpoint instance
cfg = dev.get_active_configuration()
intf = cfg[(0,0)]

custom_match = lambda e: usb.util.endpoint_direction(e.bEndpointAddress) == usb.util.ENDPOINT_OUT
ep = usb.util.find_descriptor(intf, custom_match=custom_match)   # match the first OUT endpoint
assert ep is not None


# Setup device
try:
    msg = '\x01'
    send_msg(0x41, 0x54, 0, 0, msg)
except Exception as e:
    deinit()
    msg = '\x01'
    send_msg(0x41, 0x54, 0, 0, msg)

#  Some day we will figure out what all this init stuff is and
#  what the returned values mean.

send_msg(0x41, 0x3C, 0, 0, '\x00\x00')
ret1 = receive_msg(0xC1, 0x4E, 0, 0, 4)
#print ret1
ret2 = receive_msg(0xC1, 0x36, 0, 0, 12)
#print ret2

send_msg(0x41, 0x56, 0, 0, '\x20\x00\x30\x00\x00\x00')
ret3 = receive_msg(0xC1, 0x58, 0, 0, 0x40)
#print ret3

send_msg(0x41, 0x56, 0, 0, '\x20\x00\x50\x00\x00\x00')
ret4 = receive_msg(0xC1, 0x58, 0, 0, 0x40)
#print ret4

send_msg(0x41, 0x56, 0, 0, '\x0C\x00\x70\x00\x00\x00')
ret5 = receive_msg(0xC1, 0x58, 0, 0, 0x18)
#print ret5

send_msg(0x41, 0x56, 0, 0, '\x06\x00\x08\x00\x00\x00')
ret6 = receive_msg(0xC1, 0x58, 0, 0, 0x0C)
#print ret6

send_msg(0x41, 0x3E, 0, 0, '\x08\x00')
ret7 = receive_msg(0xC1, 0x3D, 0, 0, 2)
#print ret7

send_msg(0x41, 0x3E, 0, 0, '\x08\x00')
send_msg(0x41, 0x3C, 0, 0, '\x01\x00')
ret8 = receive_msg(0xC1, 0x3D, 0, 0, 2)
#print ret8

frameID4 = None
calibImage = None

while True:
    # Send read frame request
    send_msg(0x41, 0x53, 0, 0, '\xC0\x7E\x00\x00')
    try:
        ret9  = dev.read(0x81, 0x3F60)
        ret9 += dev.read(0x81, 0x3F60)
        ret9 += dev.read(0x81, 0x3F60)
        ret9 += dev.read(0x81, 0x3F60)
    except usb.USBError as e:
        sys.exit()

    raw_img = Image.fromstring("I", (208,156), ret9, "raw", "I;16")
    img = np.asarray(raw_img).astype('uint16')
    status = ret9[20]
    #  Let's see what type of frame it is
    #  1 is a Normal frame, 3 is a Calibration frame
    #  6 may be a pre-calibration frame
    #  5, 10 other... who knows.
    print status

    if status == 1:
        calibImage = np.array(img, copy=True)
    elif status == 3:
        proc_img = processFrame(img, calibImage, frameID4)
        print proc_img
        nzIdx = np.nonzero(proc_img)
        nzMin = np.min(proc_img[nzIdx])
        nzMax = np.max(proc_img[nzIdx])
        disp_img = (proc_img - nzMin) / (nzMax - nzMin)
        disp_img[proc_img == 0] = 0
        
        cv2.imshow('win', disp_img) 
        cv2.waitKey(5)
    elif status == 4:
        frameID4 = np.array(img, copy=True)
