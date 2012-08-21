#!/usr/bin/env python

# (C) 2011-2012 by Ignacio Laguna (ilagunap@gmail.com)
#
# ---------------------------------------------------------------------------
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# ---------------------------------------------------------------------------

import optparse
import os
import subprocess
import sys
import time
import socket

###############################################################################
# Helper functions                                                            #
###############################################################################

def parseOptions():
    usage = "usage: %prog [options] [program]"
    parser = optparse.OptionParser(usage)
    
    parser.add_option("-s", "--sampling-rate", dest="rate", 
                      help="Sampling rate (seconds). Default is 5 sec.")
    parser.add_option("-d", "--dir", dest="dir",
                      help="Output directory", metavar="DIR")
    parser.add_option("-n", "--samples-number", dest="num", 
                      help="Number of samples. Default is 10.")
    parser.add_option("-p", "--pid", dest="PID",
                      help="Process ID.")
    
    (options, args) = parser.parse_args()
    if len(args) > 1:
        parser.print_help()
        exit()
        
    return (options, args)

def getSamplingRate(options):
    if options.rate is None:
        rate = 5
    else:
        rate = options.rate
    return rate

def getProcessID(options):
    if options.PID is None:
        pid = 0
    else:
        pid = options.PID
    
    return pid
        
def processExist(pid):
    return os.path.exists("/proc/" + str(pid))

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

#def getSysStatMetrics(pid):
#    cmd = ['pidstat','-p',str(pid),'-h','-d','-u','-I','-r','-s','-w','1','1']
#    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
#    lines = out.stdout.readlines()
#    values = lines[len(lines)-1].split() # get last value
#    return values

# Metrics names:
# 10: minflt (minor faults) 
# 12: majflt (major faults)
# 14: utime (user-mode CPU time)
# 15: stime (kernel-mode CPU time)
# 20: num_threads
# 23: vsize (virtual memory size)
# 24: rss (RAM memory)
# 28: startstack (address of bottom of the stack)
# 30: kstkeip (current EIP; instruction pointer)
# 39: processor (CPU number last executed on)
metricsInStatFile = (10,12,14,15,20,23,24,28,30,39);

def getStatFileMetrics(pid):
    global metricsInStatFile
    filename = os.path.join('/proc', str(pid), 'stat')
    file = open(filename, 'r')
    data = file.readlines()[-1:][0].split(" ")
    ret = []
    for m in metricsInStatFile:
        ret.append(data[m-1])
    file.close()
    
    # calculate stack size
    stack_size = int(data[27]) - int(data[29])
    del(ret[7])
    del(ret[7])
    ret.append(str(stack_size))
    return ret
    
def getIOStats(pid):
    filename = os.path.join('/proc', str(pid), 'io')
    file = open(filename, 'r')
    data = file.readlines()
    
    ret = []
    ret.append(data[0].split()[1])
    ret.append(data[1].split()[1])
    ret.append(data[4].split()[1])
    ret.append(data[5].split()[1])
    ret.append(data[6].split()[1])
    
    file.close()
    return ret

# Get number of open file descriptors
def getNumberOfFDs(pid):
    p = '/proc/' + str(pid) + '/fd'
    cmd = ['ls','-l',p]
    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    return str(len(out.stdout.readlines()))

# Get number of threads
#def getNumberOfThreads(pid):
#    n = 0
#    p = '/proc/' + str(pid) + '/status'
#    try:
#        fd = open(p, 'r')
#        for l in fd.readlines():
#            if 'Threads:' in l:
#                n = l.split()[1]
#                break;
#    except IOError:
#        n = 0
#    return n

def getMetrics(pid):
    #ret = getSysStatMetrics(pid)
    ret = getStatFileMetrics(pid)
    ret.extend(getIOStats(pid))
    ret.append(getNumberOfFDs(pid))
    #ret.append(getNumberOfThreads(pid))
    return ret

def getFeatures():
#    cmd = ['pidstat','-p',str(os.getpid()),'-h','-d',
#           '-u','-I','-r','-s','-w','1','1']
#    out = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
#    lines = out.stdout.readlines()
#    values = lines[len(lines)-2].split()
#    del values[0]
    features = ('minflt','majflt','utime','stime','num_threads','vsize','rss',
                'processor','stack_size','rchar','wchar','read_bytes',
                'write_bytes','cancelled_write_bytes','num_file_desc')
    return features

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

pid = getProcessID(options)
if (pid != 0):
    mode = "ATTACH_PROCESS"
else:
    mode = "CREATE_PROCESS"

# Call target program
if mode is "CREATE_PROCESS":
    argsList = args[0].split()
    log('Calling main program...')
    program = subprocess.Popen(argsList)
    pid = str(program.pid)
    log('Main program has PID ' + pid)
else:
    log('Program has PID ' + pid)

# Data hash table. Keys: pids; Values: lists of metric values.
data = {}

# Get samples
for n in range(numberOfSamples):
    if mode is "CREATE_PROCESS":
        program.poll()
        if program.returncode != None:
            break
    else:
        if not processExist(pid):
            break
    
    valuesList = getMetrics(pid)
    saveMetricValues(pid, data, valuesList)
    
    listOfChilds = getChilds(pid)
    if len(listOfChilds) != 0:
        for c in listOfChilds:
            valuesList = getMetrics(c)
            saveMetricValues(c, data, valuesList)
    
    time.sleep(float(rate))

# Wait until the program finishes
if mode is "CREATE_PROCESS":
    program.wait()
    log('Main program is done.')

# Save data into files
log('Saving data in files...')
if not os.path.exists(dir):
    os.makedirs(dir)
for pid in data.keys():
    fileName = str(time.time()).split('.')[0] + '_' + str(pid) + '_' + socket.gethostname() +'.dat'
    file = open(os.path.join(dir, fileName),'w')
    for f in features:
        file.write(f + ',')
    file.write('\n')
    
    for l in data[pid]:
        for v in l:
            file.write(str(v) + ',')
        file.write('\n')
    file.close()

