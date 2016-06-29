package corasones;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class GamePlayer implements java.io.Serializable{

    private String message = null;
    Image playerIcon;
    ImageView playerImage;
    private volatile double up = -0.001, right = 0.001;

    public GamePlayer(String imageName){
        playerIcon = new Image(imageName, 100, 100, true, true);
        playerImage = new ImageView(playerIcon);
    }

    void setMessage(String m){
        synchronized(App.getInstance()){
        message = m;
        }
    }

    public String popMessage(){
        synchronized (App.getInstance()) {
            String temp = message;
            message = null;
            return message;
        }
    }

    String getMessage(){
        synchronized (App.getInstance()) {
            return message;
        }
    }

    private Thread movTicker = new Thread(()->{
        while(playerImage != null){
            //check if you are on borders
            if(playerImage.getX() <= 0){
                right *= -0.3;
                playerImage.setX(1);
            }
            if(playerImage.getY() <= 0){
                up *= -0.3;
                playerImage.setY(1);
            }
            if(100+playerImage.getX()>= App.maxX){
                right *= -0.3;
                playerImage.setX(App.maxX-101);
            }
            if(100+playerImage.getY()>= App.maxY){
                up *= -0.3;
                playerImage.setY(App.maxY-101);
            }
            //set velocities
            if(right!=0.0 && up!=0.0) {
                playerImage.setX(playerImage.getX() + right);
                playerImage.setY(playerImage.getY() - up);
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {}

            if(App.isBuggy()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(!App.isRunning()) break;
        }
    });

    public synchronized void move(KeyCode kc){

        new Thread(()-> {

            /*There is only up and right so we can take advantage of negative numbers.
            (I know I should have made a down variable instead of an up variable...)
            The if's are there so it doesn't look too rough at low speeds*/
            switch (kc) {
                case UP: {
                    if(up<1 && up>0){
                        up = up+0.2;
                    }
                    else
                        up++;
                }
                break;
                case LEFT: {
                    if(right<1 && right>0){
                        right = right - 0.2;
                    }
                    else
                        right--;
                }
                break;
                case RIGHT: {
                    if(right<1 && right>0){
                        right = right +0.2;
                    }
                    else
                        right++;
                }
                break;
                case DOWN: {
                    if(up<1 && up>0){
                        up = up-0.2;
                    }
                    else
                        up--;
                }
                break;
            }
        }).start();

        if(!movTicker.isAlive() && App.isRunning()){
            movTicker.start();
        }
    }

    public byte[] serializePlayerProperties(){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream ous = new ObjectOutputStream(baos);
            ous.writeObject(new NetworkPlayerProperties(playerImage.getX(), playerImage.getY(), message));
            byte[] playerInstance = baos.toByteArray();
            return playerInstance;
        } catch (IOException e) { return null; }
    }
}
