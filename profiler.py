#!/usr/bin/env python

import optparse
import os
import subprocess
import sys
import time

###############################################################################
# Helper functions                                                            #
###############################################################################

def parseOptions():
    usage = "usage: %prog [options] PID"
    parser = optparse.OptionParser(usage)
    
    parser.add_option("-s", "--sampling-rate", dest="rate", 
                      help="Sampling rate (seconds). Default is 5 sec.")
    parser.add_option("-d", "--dir", dest="dir",
                      help="Output directory", metavar="DIR")
    parser.add_option("-n", "--samples-number", dest="num", 
                      help="Number of samples. Default is 10.")
    
    (options, args) = parser.parse_args()
    if len(args) == 0:
        parser.print_help()
        exit()
        
    return (options, args)

def getSamplingRate(options):
    if options.rate is None:
        rate = 5
    else:
        rate = options.rate
    return rate

def getNumberOfSamples(options):
    if options.num is None:
        n = 10
    else:
        n = int(options.num)
    return n

def getOutputDirectory(options):
    if options.dir is None:
        path = os.path.join(os.getcwd(), 'output')
        dir = str(path)
    else:
        dir = options.dir
    return dir

# Returns a list of child process IDs
def getChilds(pid):
    cmd = ['ps','-o','pid','--ppid',pid,'--no-headers']
    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    lines = []
    for l in out.stdout.readlines():
        lines.append(int(l[:-1]))
    return lines

def getSysStatMetrics(pid):
    cmd = ['pidstat','-p',str(pid),'-h','-d','-u','-I','-r','-s','-w','1','1']
    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    lines = out.stdout.readlines()
    values = lines[len(lines)-1].split() # get last value
    return values

# Get number of open file descriptors
def getNumberOfFDs(pid):
    p = '/proc/' + str(pid) + '/fd'
    cmd = ['ls','-l',p]
    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    return str(len(out.stdout.readlines()))

# Get number of threads
def getNumberOfThreads(pid):
    n = 0
    p = '/proc/' + str(pid) + '/status'
    try:
        fd = open(p, 'r')
        for l in fd.readlines():
            if 'Threads:' in l:
                n = l.split()[1]
                break;
    except IOError:
        n = 0
    return n

def getMetrics(pid):
    ret = getSysStatMetrics(pid)
    ret.append(getNumberOfFDs(pid))
    ret.append(getNumberOfThreads(pid))
    return ret

def getFeatures():
    cmd = ['pidstat','-p',str(os.getpid()),'-h','-d',
           '-u','-I','-r','-s','-w','1','1']
    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    lines = out.stdout.readlines()
    values = lines[len(lines)-2].split()
    del values[0]
    values.append('FDs') # Add number of fds
    values.append('NumThreads') # Add number of threads
    return values

#def programIsAlive(pid):
#    cmd = ['ps','-o','pid','--no-headers',str(pid)]
#    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
#    ret = True
#    if len(out.stdout.readlines()) == 0:
#        ret = False
#    return ret

def saveMetricValues(pid, data, valuesList):
    if pid not in data.keys():
        data[pid] = []
    data[pid].append(valuesList)

def log(msg):
    print '[PROFILER]:', msg
        
###############################################################################
# Main script                                                                 #
###############################################################################

# Parse options
(options, args) = parseOptions()
rate = getSamplingRate(options)
dir = getOutputDirectory(options)
numberOfSamples = getNumberOfSamples(options)
features = getFeatures()

# Call target program
argsList = args[0].split()
log('Calling main program...')
program = subprocess.Popen(argsList)
log('Main program has PID ' + str(program.pid))

# Data hash table. Keys: pids; Values: lists of metric values.
data = {}

# Get samples
for n in range(numberOfSamples):
    program.poll()
    if program.returncode != None:
        break
    
    valuesList = getMetrics(program.pid)
    saveMetricValues(program.pid, data, valuesList)
    
    listOfChilds = getChilds(str(program.pid))
    if len(listOfChilds) != 0:
        for c in listOfChilds:
            valuesList = getMetrics(c)
            saveMetricValues(c, data, valuesList)
    
    time.sleep(float(rate)-1)

# Wait until the program finishes
program.wait()
log('Main program is done.')

# Save data into files
log('Saving data in files...')
if not os.path.exists(dir):
    os.makedirs(dir)
for pid in data.keys():
    fileName = str(time.time()).split('.')[0] + '_' + str(pid) + '.dat'
    file = open(os.path.join(dir, fileName),'w')
    for f in features:
        file.write(f + ',')
    file.write('\n')
    
    for l in data[pid]:
        for v in l:
            file.write(str(v) + ',')
        file.write('\n')
    file.close()

