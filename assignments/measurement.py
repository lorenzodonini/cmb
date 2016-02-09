__author__ = 'lorenzodonini'

def isEmpty(var):
        return var == None or var == '' or var == '-'

class sample:
    def __init__(self):
        self.time = None
        self.value = None

class measurement:
    def __init__(self):
        self.startedAt = None
        self.vendor = None
        self.model = None
        self.osversion = None
        self.installationId = None
        self.imsi = None
        self.uid = None
        self.subscriber_mcc = None
        self.subscriber_mnc = None
        self.subscriber_operator = None
        self.network_mcc = None
        self.network_mnc = None
        self.network_operator = None
        self.latency = None
        self.downlink = None
        self.uplink = None
        self.latitude = None
        self.longitude = None
        self.privateIP = None
        self.publicIP = None
        self.measurementServer = None
        self.radiotech = None
        self.signal_strength = None
        self.area_code = None
        self.cellID = None
        self.gsm_rssi = None
        self.gsm_ber = None
        self.gsm_receive_power = None
        self.wcdma_asu = None
        self.wcdma_receiver_power = None
        self.lte_asu = None
        self.lte_rsrp = None
        self.lte_cqi = None
        self.lte_rsrq = None
        self.lte_rssnr = None
        self.pings = []
        self.dlsamples = []
        self.ulsamples = []
        self.pings_num = 0
        self.dlsamples_num = 0
        self.ulsamples_num = 0

    def parseDataLine(self,line):
        values = line.rstrip('\n').split(",")
        if isEmpty(self.startedAt): self.startedAt = values[0]
        if isEmpty(self.vendor): self.vendor = values[1]
        if isEmpty(self.model): self.model = values[2]
        if isEmpty(self.osversion): self.osversion = values[3]
        if isEmpty(self.installationId): self.installationId = values[4]
        if isEmpty(self.imsi): self.imsi = values[5]
        if isEmpty(self.uid): self.uid = values[6]
        if isEmpty(self.uid): self.subscriber_mcc = values[7]
        if isEmpty(self.subscriber_mnc): self.subscriber_mnc = values[8]
        if isEmpty(self.subscriber_operator): self.subscriber_operator = values[9]
        if isEmpty(self.network_mcc): self.network_mcc = values[10]
        if isEmpty(self.network_mnc): self.network_mnc = values[11]
        if isEmpty(self.network_operator): self.network_operator = values[12]
        if isEmpty(self.latency): self.latency = values[13]
        if isEmpty(self.downlink): self.downlink = values[14]
        if isEmpty(self.uplink): self.uplink = values[15]
        if isEmpty(self.latitude): self.latitude = values[16]
        if isEmpty(self.longitude): self.longitude = values[17]
        if isEmpty(self.privateIP): self.privateIP = values[18]
        if isEmpty(self.publicIP): self.publicIP = values[19]
        if isEmpty(self.measurementServer): self.measurementServer = values[20]
        if isEmpty(self.radiotech): self.radiotech = values[21]
        if isEmpty(self.signal_strength): self.signal_strength = values[22]
        if isEmpty(self.area_code): self.area_code = values[23]
        if isEmpty(self.cellID): self.cellID = values[24]
        if isEmpty(self.gsm_rssi): self.gsm_rssi = values[25]
        if isEmpty(self.gsm_ber): self.gsm_ber = values[26]
        if isEmpty(self.gsm_receive_power): self.gsm_receive_power = values[27]
        if isEmpty(self.wcdma_asu): self.wcdma_asu = values[28]
        if isEmpty(self.wcdma_receiver_power): self.wcdma_receiver_power = values[29]
        if isEmpty(self.lte_asu): self.lte_asu = values[30]
        if isEmpty(self.lte_rsrp): self.lte_rsrp = values[31]
        if isEmpty(self.lte_cqi): self.lte_cqi = values[32]
        if isEmpty(self.lte_rsrq): self.lte_rsrq = values[33]
        if isEmpty(self.lte_rssnr): self.lte_rssnr = values[34]
        # If there aren't any samples --> exit
        if len(values) == 35:
            return
        # Parsing pings
        i = 0
        offset = 35
        while (i < self.pings_num*2):
            time = values[i + offset]
            i += 1
            try:
                val = int(values[i + offset])
            except:
                val = -1
            i+= 1
            ping = sample()
            ping.time = time
            ping.value = val
            self.pings.append(ping)
        self.pings.sort(key=lambda sample:sample.time)

        offset += i
        i = 0
        while (i < self.dlsamples_num*2):
            time = values[i + offset]
            i += 1
            try:
                val = int(values[i + offset])
            except:
                val = -1
            i += 1
            dl = sample()
            dl.time = time
            dl.value = val
            self.dlsamples.append(dl)
        self.dlsamples.sort(key=lambda sample:sample.time)

        offset += i
        i = 0
        while (i < self.ulsamples_num*2):
            time = values[i + offset]
            i += 1
            try:
                val = int(values[i+ offset])
            except:
                val = -1
            i += 1
            ul = sample()
            ul.time = time
            ul.value = val
            self.ulsamples.append(ul)
        self.ulsamples.sort(key=lambda sample:sample.time)


    #Utility functions for getting the actualt mean measurement values
    def getMeanPing(self):
        total = 0
        amount = 0
        for ping in self.pings:
            if (ping.value != '-' and ping.value >= 0):
                total += ping.value
                amount += 1

        return -1 if amount == 0 else total / amount

    def getMeanDownloadRate(self):
        total = 0
        amount = 0
        for dl in self.dlsamples:
            if (dl.value != '-' and dl.value >= 0):
                total += dl.value
                amount += 1

        return -1 if amount == 0 else total / amount

    def getMeanUploadRate(self):
        total = 0
        amount = 0
        for ul in self.ulsamples:
            if (ul.value != '-' and ul.value >= 0):
                total += ul.value
                amount += 1

        return -1 if amount == 0 else total / amount

    def getFailedPings(self):
        total = 0
        for ping in self.pings:
            if (ping.value != '-' and ping.value < 0):
                total += 1
        return total

    def getFailedDownloads(self):
        total = 0
        for dl in self.pings:
            if (dl.value != '-' and dl.value < 0):
                total += 1
        return total

    def getFailedUploads(self):
        total = 0
        for ul in self.ulsamples:
            if (ul.value != '-' and ul.value < 0):
                total += 1
        return total
