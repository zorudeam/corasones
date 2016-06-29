package corasones;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

public class Start extends Application {
    private static ImageView playerImage, player2Image;
    static double maxY=500;
    static double maxX=458.333;
    //To the moment only for two players
    private InetAddress [] playersAddresses = new InetAddress [2];
    private GamePlayer player;
    private NetworkPlayerProperties player2Properties;
    private boolean startOrder=false;
    private static int mode;
    private Scene scene;
    private TextArea textArea;

    @Override
    public void start(Stage primaryStage) throws Exception {
        playersAddresses[0] = InetAddress.getByName("localhost");
        player = new GamePlayer("p1.png");
        player2Image = new ImageView(new Image("p2.png", 100, 100, true, true));
        playerImage = player.playerImage;
        playerImage.setX(1);
        playerImage.setY(1);

        Group root = new Group();
        root.getChildren().addAll(playerImage, player2Image);

        TextField textField = new TextField();
        textField.setOnAction(event -> {
            if (textField.isEditable()) textField.setEditable(false);
            if (textField.getText().equals("/connect")) {
                //triggers start of threads
                startOrder = true;
                mode = 1;
            }
            else if (textField.getText().equals("/start")) {
                //triggers start of threads without networking
                startOrder = true;
                mode = 0;
            }
            else if (textField.getText().equals("/restart")) return;
            else if (textField.getText().equals("/exit")) System.exit(0);
            else
                player.message = textField.getText();
            textArea.appendText(player.message+"\n");
            textField.clear();
        });
        textField.setOnMouseClicked(event -> {
            if (!textField.isEditable()) textField.setEditable(true);
        });
        textField.setLayoutX(maxX - 200);
        textField.setLayoutY(maxY - 40);
        root.getChildren().add(textField);

        textArea = new TextArea("...");
        textArea.setEditable(false);
        textArea.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        textArea.setStyle("-fx-text-fill: white");
        textArea.setOnMouseClicked(event -> {
            textField.setEditable(false);
        });
        textArea.setLayoutX(maxX - 200);
        textArea.setPadding(new Insets(100, 100, 100, 100));
        textArea.setLayoutY(10);
        root.getChildren().add(textArea);


        primaryStage.setTitle("<3 te amo");
        scene = new Scene(root, 500, 458.333, Color.BLACK);
        scene.getStylesheets().add("transparent-text-area.css");
        scene.setOnMouseClicked(event -> {
            textField.setEditable(false);
            root.requestFocus();
        });

        maxY = scene.getHeight();
        maxX = scene.getWidth();
        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            maxY = (double) newValue;
        });
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            maxX = (double) newValue;
        });
        primaryStage.setScene(scene);
        primaryStage.show();

        //window resize handling
        new Thread(() -> {
            while (true) {
                textField.setLayoutX(maxX - 200);
                textField.setLayoutY(maxY - 40);
                textArea.setLayoutX(maxX - 200);
            }
        }).start();

        //wait for start
        new Thread(()->{
            while(true){
                if(startOrder){
                    InitializeThreads();
                    break;
                }
            }
        }).start();
    }

    private void InitializeThreads(){

        //handle movement
        scene.setOnKeyPressed((KeyEvent event) -> {
            player.move(event.getCode());
        });

        if (mode == 1) {
            //multiplayer connection loop
            new Thread(() -> {
                player2Image.setVisible(true);
                while (true) {
                    NetworkPlayerProperties lastP2Prop = null;
                    sendNetworkPlayerProperties(1);
                    player2Properties = receiveNetworkPlayerProperties(1);
                    if (player2Properties != null && !player2Properties.equals(lastP2Prop)) {
                        player2Image.setX(player2Properties.X);
                        player2Image.setY(player2Properties.Y);
                        textArea.appendText(player2Properties.message + "\n");
                    }
                    lastP2Prop = player2Properties;
                }
            }).start();
        } else if (mode == 0) player2Image.setVisible(false);
    }

    private NetworkPlayerProperties receiveNetworkPlayerProperties(int playerId){
        try {
            DatagramSocket socket = new DatagramSocket(6654);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    playersAddresses[playerId],
                    6654);
            socket.receive(packet);
            ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            if(playersAddresses[playerId] == null) playersAddresses[playerId] = packet.getAddress();
            return (NetworkPlayerProperties) ois.readObject();
        } catch (SocketException e) {} catch (IOException e) {} catch(ClassNotFoundException cnfe) {}
        return null;
    }

    private void sendNetworkPlayerProperties(int playerId) {
        try {
            DatagramSocket socket = new DatagramSocket(6654);
            byte[] data = player.serializePlayerProperties();
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    playersAddresses[playerId],
                    6654
            );
            socket.send(packet);
        } catch (SocketException e) {} catch (IOException e) {}
    }

    public static void main(String[] args) {
        try {
            if (args[0] != null && args[1] != null) {
                new Thread(() -> {
                    System.out.println("Taken " + args[0] + " as host address and " + args[1] + " as network address.");
                    try {
                        InetAddress address = InetAddress.getByAddress(args[0], args[1].getBytes());
                    } catch (UnknownHostException uhe) {
                        System.out.println("I'm sorry, unknown host. Closing program.");
                        System.exit(-1);
                    }
                });
            }
        }catch(ArrayIndexOutOfBoundsException aioobe){System.out.println("Initializing without networking addresses as parameters.");}

        launch(args);
    }
}