public class Probe {

    public int sampleID;
    public String dateTime;
    public int souceCode;
    public Double latitude;
    public Double longitude;
    public Double altitude;
    public Double speed;
    public Double heading;

    public Double mdistFromLink;
    public String linkPVID;
    public Double distanceFromRef;
    public char probeDirection;
    public String slope;

    public Probe(){

    }

    public Probe(int sampleID, String dateTime, int souceCode, Double latitude, Double longitude, Double altitude, Double speed, Double heading) {
        this.sampleID = sampleID;
        this.dateTime = dateTime;
        this.souceCode = souceCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.heading = heading;
    }
}
