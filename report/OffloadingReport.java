package report;

import applications.MobileWebApplication;
import core.*;

import java.util.*;

public class OffloadingReport extends Report implements ApplicationListener {
    private Map<DTNHost, SingleNodeReport> mNodes;
    private boolean bDropFailedRequests;
    private boolean bDropSelfCachedRequests;
    private boolean bReportRawData;

    private static final String S_DROP_FAILED_REQUESTS = "dropFailedRequests";
    private static final String S_DROP_SELF_CACHED_REQUESTS = "dropSelfCachedRequests";
    private static final String S_REPORT_RAW_DATA = "reportRawData";

    public OffloadingReport() {
        super();
        mNodes = new HashMap<>();

        Settings s = getSettings();
        bDropFailedRequests = s.getBoolean(S_DROP_FAILED_REQUESTS,true);
        bDropSelfCachedRequests = s.getBoolean(S_DROP_SELF_CACHED_REQUESTS,false);
        bReportRawData = s.getBoolean(S_REPORT_RAW_DATA,true);
    }

    @Override
    public void done() {
        List<Double> responseTimes = new ArrayList<>();
        List<Double> latencies = new ArrayList<>();
        List<Double> offloadedRequests = new ArrayList<>();
        List<Double> offloadedRequestsPercentage = new ArrayList<>();
        List<Double> offloadedBytes = new ArrayList<>();
        List<Double> offloadedBytesPercentage = new ArrayList<>();
        List<MessageReport> reports;
        int totalRequests = 0;
        double totalBytes = 0;
        int p2pOffloadedRequests = 0, wifiOffloadedRequests = 0;
        double p2pOffloadedBytes = 0, wifiOffloadedBytes = 0;
        int totalFailed = 0;
        int amount, offloaded1, offloaded2, failed;
        double bytes, byteOffloaded1, byteOffloaded2;
        double percentage;
        double computed;

        StringBuilder sb = new StringBuilder();

        write("Stats for scenario " + getScenarioName());
        //REPORTING RAW DATA
        if (bReportRawData) {
            reportRawData(sb);
        }
        //REPORTING OFFLOADING
        //Also storing some additional data in the process
        write("\nOFFLOADING:");
        for (DTNHost host : mNodes.keySet()) {
            SingleNodeReport report = mNodes.get(host);
            responseTimes.addAll(report.getResponseTimes());
            latencies.addAll(report.getLatencies());

            sb.append("Node ");
            sb.append(host.toString());
            sb.append(" - Requests: ");
            // Total amount of requests
            amount = report.getAmountOfRequests();
            sb.append(amount);
            totalRequests += amount;
            // P2P offloaded requests
            reports = report.getP2POffloadedReports();
            offloaded1 = reports.size();
            p2pOffloadedRequests += offloaded1;
            sb.append(", P2P: ");
            sb.append(offloaded1);
            byteOffloaded1 = countReceivedBytes(reports); //p2p offloaded bytes
            // WiFi offloaded requests
            reports = report.getWifiOffloadedReports();
            offloaded2 = reports.size();
            wifiOffloadedRequests += offloaded2;
            sb.append(", WiFi: ");
            sb.append(offloaded2);
            byteOffloaded2 = countReceivedBytes(reports); //wifi offloaded bytes
            // Total offloaded requests
            sb.append(", Total offloaded: ");
            sb.append(offloaded1 + offloaded2);
            offloadedRequests.add((double)(offloaded1 + offloaded2));
            // Percentage offloaded requests
            percentage = computePercentage(amount, offloaded1 + offloaded2);
            sb.append(", Percentage: ");
            sb.append(format(percentage));
            offloadedRequestsPercentage.add(percentage);
            // Failed requests
            sb.append("%, Failed requests: ");
            failed = report.getFailedRequests();
            totalFailed += failed;
            sb.append(failed);

            // Total amount of bytes
            sb.append("; Bytes: ");
            bytes = report.getTotalReceivedBytes();
            totalBytes += bytes;
            sb.append((long)bytes);
            // P2P offloaded bytes
            p2pOffloadedBytes += byteOffloaded1;
            sb.append(", P2P: ");
            sb.append((long)byteOffloaded1);
            // WiFi offloaded bytes
            wifiOffloadedBytes += byteOffloaded2;
            sb.append(", WiFi: ");
            sb.append((long)byteOffloaded2);
            // Total offloaded bytes
            sb.append(", Total offloaded: ");
            sb.append((long)(byteOffloaded1 + byteOffloaded2));
            offloadedBytes.add(byteOffloaded1 + byteOffloaded2);
            // Percentage offloaded bytes
            percentage = computePercentage(bytes, byteOffloaded1 + byteOffloaded2);
            sb.append(", Percentage: ");
            sb.append(format(percentage));
            sb.append("%");
            offloadedBytesPercentage.add(percentage);
            // Writing line
            write(sb.toString());
            sb.setLength(0);
        }
        // Total report
        sb.append("\nTotal Requests: ");
        sb.append(totalRequests);
        sb.append(", P2P: ");
        sb.append(p2pOffloadedRequests);
        sb.append(", WiFi: ");
        sb.append(wifiOffloadedRequests);
        sb.append(", Total offloaded: ");
        sb.append(p2pOffloadedRequests + wifiOffloadedRequests);
        percentage = computePercentage(totalRequests, p2pOffloadedRequests + wifiOffloadedRequests);
        sb.append(", Percentage: ");
        sb.append(format(percentage));
        sb.append("%, Failed: ");
        sb.append(totalFailed);
        sb.append("\nTotal Bytes: ");
        sb.append((long)totalBytes);
        sb.append(", P2P: ");
        sb.append((long)p2pOffloadedBytes);
        sb.append(", WiFi: ");
        sb.append((long)wifiOffloadedBytes);
        sb.append(", Total offloaded: ");
        sb.append((long)p2pOffloadedBytes + wifiOffloadedBytes);
        percentage = computePercentage(totalBytes, p2pOffloadedBytes + wifiOffloadedBytes);
        sb.append(", Percentage: ");
        sb.append(format(percentage));
        sb.append("%");
        write(sb.toString());
        sb.setLength(0);

        //REPORTING OFFLOADED REQUESTS
        Collections.sort(offloadedRequests);
        write("\nOFFLOADED REQUESTS PER NODE:");
        sb.append("ABSOLUTE - ");
        sb.append("Mean: ");
        computed = computeMeanValue(offloadedRequests);
        sb.append(format(computed));
        sb.append(", Median: ");
        computed = computeMedianValue(offloadedRequests);
        sb.append(format(computed));
        sb.append(", 95%tile: ");
        computed = compute95Percentile(offloadedRequests);
        sb.append(format(computed));
        sb.append(", Min: ");
        computed = computeMinValue(offloadedRequests);
        sb.append(format(computed));
        sb.append(", Max: ");
        computed = computeMaxValue(offloadedRequests);
        sb.append(format(computed));
        sb.append("\nPERCENTAGE - ");
        sb.append("Mean: ");
        computed = computeMeanValue(offloadedRequestsPercentage);
        sb.append(format(computed));
        sb.append("%, Median: ");
        computed = computeMedianValue(offloadedRequestsPercentage);
        sb.append(format(computed));
        sb.append("%, 95%tile: ");
        computed = compute95Percentile(offloadedRequestsPercentage);
        sb.append(format(computed));
        sb.append("%, Min: ");
        computed = computeMinValue(offloadedRequestsPercentage);
        sb.append(format(computed));
        sb.append("%, Max: ");
        computed = computeMaxValue(offloadedRequestsPercentage);
        sb.append(format(computed));
        sb.append("%");
        write(sb.toString());
        sb.setLength(0);

        //REPORTING OFFLOADED BYTES
        Collections.sort(offloadedBytes);
        write("\nOFFLOADED BYTES PER NODE:");
        sb.append("ABSOLUTE - ");
        sb.append("Mean: ");
        computed = computeMeanValue(offloadedBytes);
        sb.append(format(computed));
        sb.append(", Median: ");
        computed = computeMedianValue(offloadedBytes);
        sb.append(format(computed));
        sb.append(", 95%tile: ");
        computed = compute95Percentile(offloadedBytes);
        sb.append(format(computed));
        sb.append(", Min: ");
        computed = computeMinValue(offloadedBytes);
        sb.append(format(computed));
        sb.append(", Max: ");
        computed = computeMaxValue(offloadedBytes);
        sb.append(format(computed));
        sb.append("\nPERCENTAGE - ");
        sb.append("Mean: ");
        computed = computeMeanValue(offloadedBytesPercentage);
        sb.append(format(computed));
        sb.append("%, Median: ");
        computed = computeMedianValue(offloadedBytesPercentage);
        sb.append(format(computed));
        sb.append("%, 95%tile: ");
        computed = compute95Percentile(offloadedBytesPercentage);
        sb.append(format(computed));
        sb.append("%, Min: ");
        computed = computeMinValue(offloadedBytesPercentage);
        sb.append(format(computed));
        sb.append("%, Max: ");
        computed = computeMaxValue(offloadedBytesPercentage);
        sb.append(format(computed));
        sb.append("%");
        write(sb.toString());
        sb.setLength(0);

        //REPORTING RESPONSE TIMES
        Collections.sort(responseTimes);
        write("\n\nRESPONSE TIMES:");
        sb.append("Mean: ");
        computed = computeMeanValue(responseTimes);
        sb.append(format(computed));
        sb.append(", Median: ");
        computed = computeMedianValue(responseTimes);
        sb.append(format(computed));
        sb.append(", 95%tile: ");
        computed = compute95Percentile(responseTimes);
        sb.append(format(computed));
        sb.append(", Min: ");
        computed = computeMinValue(responseTimes);
        sb.append(format(computed));
        sb.append(", Max: ");
        computed = computeMaxValue(responseTimes);
        sb.append(format(computed));
        write(sb.toString());
        sb.setLength(0);

        //REPORTING LATENCIES
        Collections.sort(latencies);
        write("\nWEBPAGE DOWNLOAD TIMES:");
        sb.append("Mean: ");
        computed = computeMeanValue(latencies);
        sb.append(format(computed));
        sb.append(", Median: ");
        computed = computeMedianValue(latencies);
        sb.append(format(computed));
        sb.append(", 95%tile: ");
        computed = compute95Percentile(latencies);
        sb.append(format(computed));
        sb.append(", Min: ");
        computed = computeMinValue(latencies);
        sb.append(format(computed));
        sb.append(", Max: ");
        computed = computeMaxValue(latencies);
        sb.append(format(computed));
        write(sb.toString());

        super.done();
    }

    private void reportRawData(StringBuilder sb) {
        write("RAW DATA:");
        write("Host,M_ID,RespTime,Download_time,Bytes,Type,P2PAttempts\t\t(Types: 1=3G, 2=WiFi, 3=WiFi_offloaded, 4=P2P)");
        for (DTNHost host : mNodes.keySet()) {
            SingleNodeReport report = mNodes.get(host);
            sb.append(host.toString());
            sb.append(",");
            report.reportRaw(sb);
            sb.setLength(0);
        }
    }

    @Override
    public void gotEvent(String event, Object params, Application app, DTNHost host) {
        if (isWarmup()) {
            return;
        }
        SingleNodeReport report = mNodes.get(host);
        //This listener handles multiple events
        if (MobileWebApplication.E_RESP_RECEIVED.equals(event)) {
            if (report != null) {
                report.addResponse((Message)params);
            }
            //If report == null, something went wrong. This should never happen
        }
        else {
            if (report == null) {
                report = new SingleNodeReport();
                mNodes.put(host, report);
            }
            Object [] parameters = (Object[]) params;
            if (MobileWebApplication.E_REQ_SENT_CELLULAR.equals(event)) {
                report.addRequest((Message)parameters[0],
                        MobileWebApplication.REQ_TYPE_CELLULAR,
                        (double)parameters[1]);
            }
            else if (MobileWebApplication.E_REQ_SENT_WIFI.equals(event)) {
                report.addRequest((Message)parameters[0],
                        MobileWebApplication.REQ_TYPE_WIFI,
                        (double)parameters[1]);
            }
            else if (MobileWebApplication.E_REQ_SENT_OFFLOADED.equals(event)) {
                report.addRequest((Message)parameters[0],
                        MobileWebApplication.REQ_TYPE_OFFLOAD,
                        (double)parameters[1]);
            }
            else if (MobileWebApplication.E_REQ_SENT_P2P.equals(event)) {
                report.addRequest((Message)parameters[0],
                        MobileWebApplication.REQ_TYPE_P2P,
                        (double)parameters[1]);
            }
        }
    }

    private long countReceivedBytes(List<MessageReport> reports) {
        long bytes = 0;
        for (MessageReport report : reports) {
            bytes += report.getReceivedBytes();
        }
        return bytes;
    }

    private double computeMeanValue(List<Double> values) {
        double sum = 0;
        if (values.size() == 0) {
            return Double.NaN;
        }
        for (Double d: values) {
            sum += d;
        }
        return sum / values.size();
    }

    private double computeMedianValue(List<Double> values) {
        if (values.size() == 0) {
            return Double.NaN;
        }
        int middle = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(middle);
        }
        else {
            return (values.get(middle - 1) + values.get(middle)) / 2;
        }
    }

    private double computeMaxValue(List<Double> values) {
        if (values.size() == 0) {
            return Double.NaN;
        }
        double max = values.get(0);
        for (Double d: values) {
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    private double computeMinValue(List<Double> values) {
        if (values.size() == 0) {
            return Double.NaN;
        }
        double min = values.get(0);
        for (Double d : values) {
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

    private double computePercentage(double total, double x) {
        return (x * 100d) / total;
    }

    private double roundTo2Decimals(double value) {
        return (double)Math.round(value * 100) / 100;
    }

    private double compute95Percentile(List<Double> values) {
        Map<Double, Integer> dataSet = new TreeMap<>();
        Integer val;
        int sum = 0;
        double previous = 0;
        double percentile = 0;

        for (Double d : values) {
            val = dataSet.get(d);
            if (val == null) {
                dataSet.put(d, 1);
            }
            else {
                dataSet.put(d, val+1);
            }
        }

        for (Double d : dataSet.keySet()) {
            val = dataSet.get(d);
            if (sum + val >= 0.95 * values.size()) {
                //Found the approximate percentile. Computing the correct one now
                for (int i=1; i<=val; i++) {
                    if (sum + i >= 0.95 * values.size()) {
                        percentile = previous + ((d - previous) / val) * i;
                        break;
                    }
                }
                break;
            }
            sum += val;
            previous = d;
        }

        return percentile;
    }

    private class SingleNodeReport {
        private Map<String, MessageReport> mRequests;

        public SingleNodeReport() {
            mRequests = new HashMap<>();
        }

        public void addRequest(Message req, byte type, double routingStartTime) {
            MessageReport report = mRequests.get(req.getId());
            if (report == null) {
                report = new MessageReport();
                mRequests.put(req.getId(), report);
            }
            report.setRequest(req);
            report.setType(type);
            report.setRoutingStartTime(routingStartTime);
        }

        public void addResponse(Message resp) {
            Message req = resp.getRequest();
            if (req == null) {
                return;
            }
            MessageReport report = mRequests.get(req.getId());
            if (report == null) {
                return;
            }
            if (report.getResponse() == null) {
                report.setResponse(resp);
            }
        }

        public List<Double> getLatencies() {
            List<Double> latencies = new ArrayList<>();
            for (MessageReport report : mRequests.values()) {
                if (report.getResponse() == null && bDropFailedRequests) {
                    continue;
                }
                latencies.add(roundTo2Decimals(report.getLatency()));
            }
            Collections.sort(latencies);
            return latencies;
        }

        public List<Double> getResponseTimes() {
            List<Double> responseTimes = new ArrayList<>();
            for (MessageReport report : mRequests.values()) {
                if (report.getResponse() == null && bDropFailedRequests) {
                    continue;
                }
                responseTimes.add(roundTo2Decimals(report.getResponseTime()));
            }
            Collections.sort(responseTimes);
            return responseTimes;
        }

        public List<MessageReport> getWifiOffloadedReports() {
            List<MessageReport> offloaded = new ArrayList<>();
            for (MessageReport report : mRequests.values()) {
                if (report.getType() == MobileWebApplication.REQ_TYPE_OFFLOAD) {
                    if (report.getResponse() == null && bDropFailedRequests) {
                        continue;
                    }
                    offloaded.add(report);
                }
            }
            return offloaded;
        }

        public List<MessageReport> getP2POffloadedReports() {
            List<MessageReport> offloaded = new ArrayList<>();
            for (MessageReport report : mRequests.values()) {
                if (report.getType() == MobileWebApplication.REQ_TYPE_P2P) {
                    if (report.getResponse() == null && bDropFailedRequests) {
                        continue;
                    }
                    offloaded.add(report);
                }
            }
            return offloaded;
        }

        public long getTotalReceivedBytes() {
            long bytes = 0;
            for (MessageReport report : mRequests.values()) {
                if (report.getResponse() == null && bDropFailedRequests) {
                    continue;
                }
                bytes += report.getReceivedBytes();
            }
            return bytes;
        }

        public int getAmountOfRequests() {
            if (!bDropFailedRequests) {
                return mRequests.size();
            }
            else {
                return mRequests.size() - getFailedRequests();
            }
        }

        public int getFailedRequests() {
            int failed = 0;
            for (MessageReport report : mRequests.values()) {
                if (report.getResponse() == null) {
                    failed++;
                }
            }
            return failed;
        }

        public void reportRaw(StringBuilder sb) {
            int length = sb.length();
            for (String id : mRequests.keySet()) {
                MessageReport rep = mRequests.get(id);
                if (rep.getResponse() == null && bDropFailedRequests) {
                    continue;
                }
                sb.append(id);
                sb.append(",");
                mRequests.get(id).reportRaw(sb);
                sb.setLength(length);
            }
        }
    }

    private class MessageReport {
        private Message requestMessage;
        private Message responseMessage;
        private double routingStartTime;
        private byte type;
        private int p2pAttempts;

        public MessageReport() {
            routingStartTime = -1;
            type = MobileWebApplication.REQ_TYPE_NONE;
            p2pAttempts = 0;
        }

        private double getResponseTime() {
            if (responseMessage == null) {
                return -1;
            }
            return responseMessage.getReceiveTime() - requestMessage.getCreationTime();
        }

        public int getReceivedBytes() {
            if (responseMessage == null) {
                return 0;
            }
            return responseMessage.getSize();
        }

        public int getP2PAttempts() {
            return p2pAttempts;
        }

        public double getLatency() {
            if (responseMessage == null) {
                return -1;
            }
            return responseMessage.getReceiveTime() - routingStartTime;
        }

        public Message getResponse() {
            return responseMessage;
        }

        public Message getRequest() {
            return requestMessage;
        }

        public byte getType() {
            return type;
        }

        public void setType(byte t) {
            type = t;
            if (type == MobileWebApplication.REQ_TYPE_P2P) {
                p2pAttempts++; //Increasing p2p attempts
            }
        }

        public void setRequest(Message req) {
            requestMessage = req;
        }

        public void setResponse(Message resp) {
            responseMessage = resp;
        }

        public void setRoutingStartTime(double time) {
            routingStartTime = time;
        }

        public void reportRaw(StringBuilder sb) {
            sb.append(format(getResponseTime()));
            sb.append(",");
            sb.append(format(getLatency()));
            sb.append(",");
            sb.append(getReceivedBytes());
            sb.append(",");
            sb.append(getType());
            sb.append(",");
            sb.append(getP2PAttempts());
            write(sb.toString());
        }
    }
}
