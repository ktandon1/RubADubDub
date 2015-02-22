import os
import sys
import subprocess

p = sys.argv[1]
dirs = os.listdir(p)
BG_LOC = sys.argv[2]

segment_cmd = "java SegmentHands %s" % BG_LOC
remap_cmd = "java -classpath PXCUPipeline.jar;. Remapper"

for d in dirs:
    loc = p + "/" + d
    if os.path.isdir(loc):
        cmd1 = segment_cmd + " " + loc + " 0"
        cmd2 = remap_cmd + " " + loc
        print cmd1
        subprocess.call(cmd1, shell=True)
        print cmd2
        subprocess.call(cmd2, shell=True)


