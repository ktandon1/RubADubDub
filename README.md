RubADubDub
==========

Science Fair 2015

Effective hand washing is one of the most important -- and easiest -- ways to
prevent the spread of diseases. According to the Center for Disease Control
(CDC), nearly 2.2 million children die each year due to diarrheal diseases and
respiratory diseases, such as pneumonia.  Proper hand washing with soap could
protect 1/3 of such children from diarrhea and 1/6 of children from pneumonia
[1]. Despite the health benefits, a Michigan State study found that only 5% of
people follow proper hand washing techniques [2].

Our project is a computer vision system that monitors a user's hand
washing and offers suggestions such that proper hand washing protocol is
followed.  

The CDC suggests 5 steps for proper hand washing:  

1.  Wet your hands with running water (comfortable temperature is suggested
[3], not too hot)
2.  Lather your hands with soap
3.  Scrub your hands for 20 seconds
4.  Rinse your hands with running water
5.  Dry your hands


Methodology and Algorithm
=========================
We mount a stationary color and depth camera and thermal camera near the sink
such that the faucet and a user's hand are visible [todo: add photo]. Our
algorithm proceeds as such: 

1. Locate and segment the user's hands in the image. 
2. Determine whether the hands are wet
3. Determine whether the hands have soap

Hand Segmentation
-----------------

Our goal in this step is to extract the location of the hands within the image.
We make two simplifying assumptions to solve this problem: 
1. The image consists of the hands (foreground) and environment (background).
2. The environment and camera are stationary. 

With these two assumptions, localizing the hands is conceptually easy. We first
take a picture of the background with no hands in it. We then take our current
image and compare which pixels have changed from the background; the ones that
did must belong to our hand. 

In practice, this is slightly more challenging due to imperfections in the
depth sensor. It is possible that the depth image values to slightly corrupted
due to noise or entirely missing on a given frame. To combat this, we record a
20 second clip of the background with no hands in it. We average the depth
image over the entire duration of the clip and use this as our background
image. This process allows our background subtraction algorithm to be more
robust and contain more information than using any individual frame.

Qualitatively, the resulting hand segmentations from our simple algorithm look fairly
good [todo: add photos].

Determining hand soapiness
--------------------------
TODO: Write this section 


[1] http://www.cdc.gov/handwashing/why-handwashing.html

[2] http://msutoday.msu.edu/news/2013/eww-only-5-percent-wash-hands-correctly/

[3] http://www.cdc.gov/handwashing/show-me-the-science-handwashing.html
