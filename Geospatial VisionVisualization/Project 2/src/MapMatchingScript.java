import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapMatchingScript {

    static ArrayList<Probe> readProbeData = new ArrayList<Probe>();
    static ArrayList<Link> readLinkData = new ArrayList<Link>();

    public static void main(String[] args) throws IOException {

        System.out.println("Reading Data...");
        String probeFilePath = "./data/Partition6467ProbePoints.csv";
        readProbeData = readProbeData(probeFilePath);
        String linkFilePath = "./data/Partition6467LinkData.csv";
        readLinkData = readLinkData(linkFilePath);

        int minIndex = 0, previousProbeID = 0;
        String closestLink = "", previousClosestLink = "";

        double previousdistanceToMapMatched = 0.0d;
        double perpendicularDistance = 0.0d;
        double minDistanceForLink = 0.0d;
        double minDistanceForProbe = 0.0d;
        double distanceToMapMatched = 0.0d;


        //Process read data for analysis
        System.out.println("Processing Data please wait...");
        for (int probeIndex = 0; probeIndex < readProbeData.size(); probeIndex++) {

            Probe probe = readProbeData.get(probeIndex);
            int progress = (int) (((float)probeIndex/(float) readProbeData.size()) * 100f);
            System.out.println("Progress "+probeIndex+" %");

            Double[] mapMatchedPoint = null;

            for (int i = 0; i < readLinkData.size(); i++) {

                Link link = readLinkData.get(i);
                ArrayList<Probe> prbList = null;
                List<Double> distMapMatchedPoints = new ArrayList<Double>();
                for (int j = 0; j < link.shapeInfo.size() - 1; j++) {

                    Double dStart[] = link.shapeInfo.get(j);
                    Double dEnd[] = link.shapeInfo.get(j + 1);

                    Double[] lineVector = new Double[] { dEnd[0] - dStart[0], dEnd[1] - dStart[1] };
                    Double[] pointVector = new Double[] { probe.latitude - dStart[0], probe.longitude - dStart[1] };
                    Double magnitude = Math.sqrt(Math.pow(lineVector[0], 2) + Math.pow(lineVector[1], 2));
                    Double[] lineUnitVector = new Double[] { lineVector[0] / magnitude, lineVector[1] / magnitude };
                    Double[] pointUnitVector = new Double[] { pointVector[0] / magnitude, pointVector[1] / magnitude };
                    Double dotProduct = lineUnitVector[0] * pointUnitVector[0] + lineUnitVector[1] * pointUnitVector[1];

                    if (dotProduct < 0.0) {
                        dotProduct = 0.0;
                    } else if (dotProduct > 1.0) {
                        dotProduct = 1.0;
                    }

                    mapMatchedPoint = new Double[] { lineVector[0] * dotProduct, lineVector[1] * dotProduct };
                    perpendicularDistance = Math.sqrt(Math.pow(pointVector[0] - mapMatchedPoint[0], 2) + Math.pow(pointVector[1] - mapMatchedPoint[1], 2)) * 1000;
                    mapMatchedPoint[0] = mapMatchedPoint[0] + dStart[0];
                    mapMatchedPoint[1] = mapMatchedPoint[1] + dStart[1];
                    distMapMatchedPoints.add(perpendicularDistance);
                }
                minDistanceForLink = Collections.min(distMapMatchedPoints);
                if (i == 0 || minDistanceForLink < minDistanceForProbe) {
                    minDistanceForProbe = minDistanceForLink;
                    closestLink = link.linkPVID;
                    minIndex = distMapMatchedPoints.indexOf(minDistanceForLink);
                    distanceToMapMatched = calculateMapDistance(link, mapMatchedPoint, minIndex);
                }
            }
            if (minDistanceForProbe < 20) {
                if (probeIndex == 0) {
                    probe.probeDirection = 'U';
                } else {
                    if (probe.sampleID == previousProbeID && previousClosestLink == closestLink) {
                        if (previousdistanceToMapMatched > distanceToMapMatched) {
                            probe.probeDirection = 'T';
                        } else if (previousdistanceToMapMatched < distanceToMapMatched) {
                            probe.probeDirection = 'F';
                        } else {
                            probe.probeDirection = 'U';
                        }
                    } else {
                        probe.probeDirection = 'U';
                    }
                }
                probe.distanceFromRef = distanceToMapMatched;
                probe.linkPVID = closestLink;
                previousClosestLink = closestLink;
                previousdistanceToMapMatched = distanceToMapMatched;
                previousProbeID = probe.sampleID;
                probe.mdistFromLink = minDistanceForProbe;

            } else {
                continue;
            }
        }
        calculateSlope(readLinkData, readProbeData);

    }

    public static ArrayList<Probe> readProbeData(String path) {
        String line = "";
        BufferedReader br;
        ArrayList<Probe> lstProbe = new ArrayList<Probe>();
        try {
            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                String[] strProbe = line.split(",");
                int sid = Integer.parseInt(strProbe[0]);
                String datetime  = strProbe[1];
                int scode = Integer.parseInt(strProbe[2]);
                double lat = Double.parseDouble(strProbe[3]);
                double lng = Double.parseDouble(strProbe[4]);
                double alt = Double.parseDouble(strProbe[5]);
                double speed = Double.parseDouble(strProbe[6]);
                double heading = Double.parseDouble(strProbe[7]);
                Probe probe = new Probe(sid,datetime,scode,lat,lng,alt,speed,heading);
                lstProbe.add(probe);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstProbe;
    }


    public static ArrayList<Link> readLinkData(String filePath){
        BufferedReader br;
        ArrayList<Link> lstLink = new ArrayList<Link>();
        String line = "";
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {

                String[] strLink = line.split(",");
                String linkPVID  = strLink[0];;
                String refNodeID  = strLink[1];;
                String nrefNodeID = strLink[2];
                Double length  = Double.parseDouble(strLink[3]);;
                int functionalClass = Integer.parseInt(strLink[4]);;
                char directionOfTravel  = strLink[5].charAt(0);
                int speedCategory  = Integer.parseInt(strLink[6]);;
                int fromRefSpeedLimit  = Integer.parseInt(strLink[7]);;
                int toRefSpeedLimit = Integer.parseInt(strLink[8]);
                int fromRefNumLanes = Integer.parseInt(strLink[9]);
                int toRefNumLanes = strLink[10].charAt(0);
                char multiDigitized = strLink[11].charAt(0);
                char urban = strLink[12].charAt(0);
                Double timeZone = Double.parseDouble(strLink[13]);
                ArrayList<Double[]> shapeInfo = new ArrayList<>();
                ArrayList<Double[]> curvature_Info = new ArrayList<>();
                ArrayList<Double[]> slope_Info = new ArrayList<>();


                if (strLink.length >= 15) {

                    if (!strLink[14].isEmpty()) {
                        shapeInfo = new ArrayList<>();
                        String[] data = strLink[14].split("\\|");
                        for (int i = 0; i < data.length; i++) {
                            String[] arrShapeInfo = data[i].split("/");
                            Double[] d = new Double[arrShapeInfo.length];
                            for (int j = 0; j < arrShapeInfo.length; j++) {
                                d[j] = Double.parseDouble(arrShapeInfo[j]);
                            }
                            shapeInfo.add(d);
                        }
                    }
                }

                if (strLink.length >= 16) {

                    if (!strLink[15].isEmpty()) {
                        curvature_Info = new ArrayList<>();
                        String[] data = strLink[15].split("\\|");
                        for (int i = 0; i < data.length; i++) {
                            String[] arrCurvatureInfo = data[i].split("/");
                            Double[] d = new Double[arrCurvatureInfo.length];
                            for (int j = 0; j < arrCurvatureInfo.length; j++) {
                                d[j] = Double.parseDouble(arrCurvatureInfo[j]);
                            }
                            curvature_Info.add(d);
                        }
                    }
                }

                if (strLink.length >= 17) {

                    if (!strLink[16].isEmpty()) {
                        slope_Info = new ArrayList<>();
                        String[] data = strLink[16].split("\\|");
                        for (int i = 0; i < data.length; i++) {
                            String[] arrSlopeInfo = data[i].split("/");
                            Double[] d = new Double[arrSlopeInfo.length];
                            for (int j = 0; j < arrSlopeInfo.length; j++) {
                                d[j] = Double.parseDouble(arrSlopeInfo[j]);
                            }
                            slope_Info.add(d);
                        }
                    }
                }

                Link link = new Link(linkPVID,refNodeID,nrefNodeID,length,functionalClass,directionOfTravel,speedCategory,fromRefSpeedLimit,toRefSpeedLimit,fromRefNumLanes,toRefNumLanes,multiDigitized,urban,timeZone,shapeInfo,curvature_Info,slope_Info);
                lstLink.add(link);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstLink;
    }

    //haversine formula
    public static double greatCircleDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLong = Math.toRadians(lng2 - lng1);
        double tmp = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLong / 2), 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double distance = 2 * Math.asin(Math.sqrt(tmp)) * 6372.8;
        return distance;
    }

    //function which calculates mapping distance
    public static double calculateMapDistance(Link link, Double[] point, int index) {
        double distance = 0.0d;
        for (int i = 0; i < index - 1; i++)
        {
            distance += greatCircleDistance(link.shapeInfo.get(i)[0], link.shapeInfo.get(i)[1], link.shapeInfo.get(i + 1)[0], link.shapeInfo.get(i + 1)[1]);
        }
        distance += greatCircleDistance(link.shapeInfo.get(index)[0], link.shapeInfo.get(index)[1], point[0], point[1]);
        distance *= 1000;
        return distance;
    }

    public static void calculateSlope(List<Link> listLink, List<Probe> matchedPoints)
    {
        Probe prev = null;
        System.out.println("Calculating slope please wait...");
        int datasize = matchedPoints.size();
        ArrayList<Probe> dumplist = new ArrayList<>();
        int dumpsize = 100000;
        initializeMatchedPointFile();
        for (int i = 0; i < datasize; i++) {

            Probe next = matchedPoints.get(i);
            if (prev == null) {
                next.slope = "U";
            } else if (next.linkPVID != prev.linkPVID) {
                next.slope = "U";
            } else {
                double diffAltitude = next.altitude - prev.altitude;
                double base = greatCircleDistance(next.latitude, next.longitude, prev.latitude, prev.longitude);
                double slope = Math.toRadians(Math.atan(diffAltitude * 1000 / base));
                next.slope = String.valueOf(slope);
            }
            prev = next;
            dumplist.add(next);
            setProbeLinkData(listLink, next);


            if(i >= dumpsize && i % dumpsize == 0){
                dumpMatchedPoints(dumplist);
                dumplist.clear();
                int progress = (int) (( (float)i/ (float)datasize) * 100f);
                System.out.println("progress "+progress+"%.");


            }

        }
        System.out.println("Slope Calculation task finished.");
        System.out.println("Matched point csv file is generated");
        calculateNewSlopeMean(listLink);
    }


    public static void initializeMatchedPointFile(){
        try{
            FileWriter fw = new FileWriter("MatchedPointsData.csv", false);
            String header = "SAMPLE ID,DATE TIME,SOURCE CODE,LATITUTE,LONGITUTE,ALTITUTE,SPEED,HEADING,LINK PVID,DIRECTION,DISTANCE FROM REF,DISTANCE FROM LINK,SLOPE";
            fw.append(header);
            fw.append("\n");
            fw.flush();
            fw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void dumpMatchedPoints(ArrayList<Probe> list){
        try{
            FileWriter fw = new FileWriter("MatchedPointsData.csv", true);
            for(Probe probe:list){
                String data = probe.sampleID + "," + probe.dateTime + ","
                        + probe.souceCode + "," + probe.latitude + "," + probe.longitude + ","
                        + probe.altitude + "," + probe.speed + "," + probe.heading + ","
                        + probe.linkPVID + "," + probe.probeDirection + "," + probe.distanceFromRef + ","
                        + probe.mdistFromLink + "," + probe.slope;

                fw.append(data);
                fw.append("\n");
            }
            fw.flush();
            fw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //Calculate the slope mean
    public static void calculateNewSlopeMean(List<Link> listLink) {
        int dataCount = 0;

        initializeSlopeEvaluationFile();
        for (int i = 0; i < listLink.size(); i++) {
            ArrayList<Probe> prbList = listLink.get(i).link_Probes;

            double linkTotalSlope = 0.0;
            int count = 0, linkCount = 0;
            double meanSlope = 0.0;
            double totalSlope = 0.0;
            double linkmeanSlope = 0.0;

            if (prbList != null) {
                for (Probe p : prbList) {
                    String slope = p.slope;
                    if (slope != "0" && slope != "U") {
                        if (p.probeDirection == 'T') {
                            totalSlope -= Double.parseDouble(slope);
                            count += 1;
                        } else {
                            totalSlope += Double.parseDouble(slope);
                            count += 1;
                        }
                    }
                }
                if (count != 0) {
                    meanSlope = totalSlope / count;
                }
                ArrayList<Double[]> linkSlope = listLink.get(i).slope_Info;
                for (int k = 0; k < linkSlope.size(); k++) {
                    linkTotalSlope += linkSlope.get(k)[1];
                    linkCount += 1;
                }
                if (linkCount != 0) {
                    linkmeanSlope = linkTotalSlope / linkCount;
                }
                writeSlopeData(listLink.get(i).linkPVID, meanSlope, linkmeanSlope);
                dataCount += 1;
            }
        }
        System.out.println("Data is written into slope evaluation file.");
    }

    public static void setProbeLinkData(List<Link> listLink, Probe probe) {

        for (int i = 0; i < listLink.size(); i++) {
            if (listLink.get(i).linkPVID == probe.linkPVID && listLink.get(i).slope_Info != null) {
                ArrayList<Probe> prbList = null;
                if (listLink.get(i).link_Probes != null) {
                    prbList = listLink.get(i).link_Probes;
                } else {
                    prbList = new ArrayList<>();
                }
                prbList.add(probe);
                listLink.get(i).link_Probes = prbList;
            }
        }
    }

    public static void initializeSlopeEvaluationFile(){
        try {
            String filename = "SlopeEvaluation.csv";
                FileWriter fw = new FileWriter(filename, false);
                String header = "LINK PVID,CALCULATED MEAN SLOPE,PROVIDED MEAN SLOPE";
                fw.append(header);
                fw.append("\n");
                fw.flush();
                fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSlopeData(String linkID, double calculatedMean, double givenMean) {

        String filename = "SlopeEvaluation.csv";
        try {
                FileWriter fw = new FileWriter(filename, true);
                String data = linkID + "," + String.valueOf(calculatedMean) + "," + String.valueOf(givenMean);
                fw.append(data);
                fw.append("\n");
                fw.flush();
                fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
