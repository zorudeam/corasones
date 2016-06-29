package corasones;

public class NetworkPlayerProperties {

    public double X, Y;
    public String message;

    public NetworkPlayerProperties(double X, double Y, String message){
        this.X = X;
        this.Y = Y;
        this.message = message;
    }

    public NetworkPlayerProperties(double X, double Y){
        this.X = X;
        this.Y = Y;
        this.message = null;
    }

    @Override
    public boolean equals(Object object){
        if(object == null && this!=null) return false; //LOL THIS IS THE BEST LINE I'VE EVER WRITTEN (sarcasm)
        if(object == this) return true;
        if(object instanceof NetworkPlayerProperties){
            NetworkPlayerProperties npp = (NetworkPlayerProperties) object;
            if(npp.X == this.X && npp.Y == this.Y && npp.message.equals(this.message))
                return true;
        }
        return false;
    }
}
