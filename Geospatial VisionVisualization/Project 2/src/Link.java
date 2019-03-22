import java.util.ArrayList;

public class Link {

    public String linkPVID;
    public String refNodeID;
    public String nrefNodeID;
    public Double length;
    public int functionalClass;
    public char directionOfTravel;
    public int speedCategory;
    public int fromRefSpeedLimit;
    public int toRefSpeedLimit;
    public int fromRefNumLanes;
    public int toRefNumLanes;
    public char multiDigitized;
    public char urban;
    public Double timeZone;
    public ArrayList<Double[]> shapeInfo;
    public ArrayList<Double[]> curvature_Info;
    public ArrayList<Double[]> slope_Info;

    public ArrayList<Probe> link_Probes;


    public Link(String linkPVID, String refNodeID, String nrefNodeID, Double length, int functionalClass, char directionOfTravel, int speedCategory, int fromRefSpeedLimit, int toRefSpeedLimit, int fromRefNumLanes, int toRefNumLanes, char multiDigitized, char urban, Double timeZone, ArrayList<Double[]> shapeInfo, ArrayList<Double[]> curvature_Info, ArrayList<Double[]> slope_Info) {
        this.linkPVID = linkPVID;
        this.refNodeID = refNodeID;
        this.nrefNodeID = nrefNodeID;
        this.length = length;
        this.functionalClass = functionalClass;
        this.directionOfTravel = directionOfTravel;
        this.speedCategory = speedCategory;
        this.fromRefSpeedLimit = fromRefSpeedLimit;
        this.toRefSpeedLimit = toRefSpeedLimit;
        this.fromRefNumLanes = fromRefNumLanes;
        this.toRefNumLanes = toRefNumLanes;
        this.multiDigitized = multiDigitized;
        this.urban = urban;
        this.timeZone = timeZone;
        this.shapeInfo = shapeInfo;
        this.curvature_Info = curvature_Info;
        this.slope_Info = slope_Info;
    }
}
