__author__ = 'lorenzodonini'

from measurement import measurement, sample
from report import report

wifi_tech = '0'
wdca_tech = '8'
gsm_tech = '15'
lte_tech = '13'
cellular_technologies = [wdca_tech,lte_tech,gsm_tech]

#Utility functions
def getWifiMeasurements(measurements):
    result = []
    for m in measurements:
        if m.radiotech == wifi_tech:
            result.append(m)
    return result

def getCellularMeasurements(measurements):
    result = []
    for m in measurements:
        if m.radiotech in cellular_technologies:
            result.append(m)
    return result

# Evolution of metrics within a measurement run
def algMetricsEvolution(measurements):
    highPing = 250
    rep = report()
    rep.createNewWorkbook()
    rep.createNewSheet('Pings')
    header = []
    for i in range(1,21):
        header.append("Ping"+str(i))
    rep.writeHeaderLine(header)
    for m in measurements:
        values = []
        for p in m.pings:
            val = p.value if p.value >= 0 else highPing
            values.append(val)
        rep.writeLine(values)
    rep.saveWorkbook('test.xlsx')

# Max/min/average network speed - parsing single samples
def networkSpeedEffective(measurements):
    minDl = 100000
    maxDl = 0
    minUl = 100000
    maxUl = 0
    minPing = 100000
    maxPing = 0
    avgPing = 0
    avgDl = 0
    avgUl = 0
    for m in measurements:
        for sample in m.dlsamples:
            if  sample.value != '-' and sample.value != '' and sample.value > 0:
                minDl = min(sample.value,minDl)
                maxDl = max(sample.value,maxDl)
        for sample in m.ulsamples:
            if sample.value != '-' and sample.value != '' and sample.value > 0:
                minUl = min(sample.value,minUl)
                maxUl = max(sample.value,maxUl)
        for sample in m.pings:
            if sample.value != '-' and sample.value != '' and sample.value >= 0:
                minPing = min(sample.value,minPing)
                maxPing = max(sample.value,maxPing)
        avgPing += m.getMeanPing()
        avgDl += m.getMeanDownloadRate()
        avgUl += m.getMeanUploadRate()

    avgPing = int(avgPing / len(measurements))
    avgDl = int(avgDl / len(measurements))
    avgUl = int(avgUl / len(measurements))
    print("Ping - min:"+str(minPing)+" max:"+str(maxPing)+" avg:"+str(avgPing))
    print("Download - min:"+str(minDl)+" max:"+str(maxDl)+" avg:"+str(avgDl))
    print("Upload - min:"+str(minUl)+" max:"+str(maxUl)+" avg:"+str(avgUl))

# Max/min/average network speed - using uplink, downlink, latency values
def networkSpeed(measurements):
    minDl = 100000
    maxDl = 0
    minUl = 100000
    maxUl = 0
    minPing = 100000
    maxPing = 0
    avgPing = 0
    avgDl = 0
    avgUl = 0
    pingCount = 0
    dlCount = 0
    ulCount = 0
    for m in measurements:
        if m.downlink != '-' and m.downlink != '':
            val = float(m.downlink)
            if val > 0:
                minDl = min(val,minDl)
                maxDl = max(val,maxDl)
                avgDl += val
                dlCount += 1
        if m.uplink != '-' and m.uplink != '':
            val = float(m.uplink)
            if val > 0:
                minUl = min(val,minUl)
                maxUl = max(val,maxUl)
                avgUl += val
                ulCount += 1
        if m.latency != '-' and m.latency != '':
            val = float(m.latency)
            if val > 0:
                minPing = min(val,minPing)
                maxPing = max(val,maxPing)
                avgPing += val
                pingCount += 1

    minPing = int(minPing)
    maxPing = int(maxPing)
    minDl = int(minDl)
    maxDl = int(maxDl)
    minUl = int(minUl)
    maxUl = int(maxUl)
    avgDl = int(avgDl / dlCount)
    avgUl = int(avgUl / ulCount)
    avgPing = int(avgPing / pingCount)
    print("Ping - min:"+str(minPing)+" max:"+str(maxPing)+" avg:"+str(avgPing))
    print("Download - min:"+str(minDl)+" max:"+str(maxDl)+" avg:"+str(avgDl))
    print("Upload - min:"+str(minUl)+" max:"+str(maxUl)+" avg:"+str(avgUl))


#Parsing of data
def parseDataFromFiles(base,extended):
    f = open(base,"r")
    lines = f.readlines()
    measurements = []
    i = 1
    max = len(lines)
    while i < max:
        m = measurement()
        m.pings_num = 20
        m.dlsamples_num = 200
        m.ulsamples_num = 200
        m.parseDataLine(lines[i])
        measurements.append(m)
        i += 1
    f.close()

    f = open(extended,"r")
    lines = f.readlines()
    i = 1
    max = len(lines)
    while i < max:
        m = measurements[i - 1]
        m.parseDataLine(lines[i])
        i += 1
    f.close()

    return measurements

def main():
    measurements = parseDataFromFiles("cmb.team4_base","cmb.team4_extended")

    #algMetricsEvolution(measurements)
    cellular = getCellularMeasurements(measurements)
    wifi = getWifiMeasurements(measurements)
    print("\nCELLULAR")
    networkSpeed(cellular)
    print("EFFECTIVE")
    networkSpeedEffective(cellular)
    print("\nWIFI")
    networkSpeed(wifi)
    print("EFFECTIVE")
    networkSpeedEffective(wifi)

#Just calling main. In there, the algorithms to call are decided upon
main()