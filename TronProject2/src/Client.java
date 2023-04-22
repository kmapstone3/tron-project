import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends Application
{
    final int DISPLAY_WIDTH = 640;
    final int DISPLAY_HEIGHT = 640;

    DataInputStream inputStream;
    DataOutputStream outputStream;

    BorderPane pane;
    Scene scene;
    Stage stage;

    Text text;
    Rectangle textBacking;

    GameGrid grid;

    Player player;
    Player enemy;

    int playerNumber;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        //setup GUI
        pane = new BorderPane();

        text = new Text();
        text.setScaleX(2);
        text.setScaleY(2);
        text.setStroke(Color.WHITE);
        text.setTextAlignment(TextAlignment.CENTER);

        textBacking = new Rectangle(-32, DISPLAY_HEIGHT / 2 - 32, DISPLAY_WIDTH + 64, 64);
        textBacking.setStroke(Color.WHITE);
        textBacking.visibleProperty().bind(text.textProperty().length().greaterThan(0));

        pane.getChildren().add(textBacking);
        pane.setCenter(text);

        scene = new Scene(pane, DISPLAY_WIDTH, DISPLAY_HEIGHT);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.setTitle("TRON");
        stage.show();

        scene.setOnKeyPressed(e -> new Thread(() -> player.setDirection(e)).start());
        scene.setOnMouseDragEntered((eventHandler) -> setupConnections());

        new Thread(() -> setupConnections()).start();
    }

    void setupConnections()
    {
        try
        {
            Socket socket = new Socket("localhost", 8000);

            //setup connections
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            playerNumber = inputStream.read();

            Platform.runLater(() -> stage.setTitle("TRON - PLAYER " + playerNumber));

            new Thread(() -> runClient()).start();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    void runClient()
    {
        try
        {
            Platform.runLater(() -> text.setText("WAITING FOR OTHER PLAYERS TO CONNECT..."));

            //wait for server to start session

            inputStream.read();
            Platform.runLater(() -> text.setText(""));
            System.out.println("START CLIENT");

            //Start of session
            //Use player number (given by server) to get starting position
            Point playerStartPos = GameGrid.PLAYER1_START_POS;
            if(playerNumber == 2)
                playerStartPos = GameGrid.PLAYER2_START_POS;

            Point enemyStartPos = GameGrid.PLAYER2_START_POS;
            if(playerNumber == 2)
                enemyStartPos = GameGrid.PLAYER1_START_POS;

            //initialize player and enemy
            player = new Player(playerStartPos.x, playerStartPos.y);
            enemy = new Player(enemyStartPos.x, enemyStartPos.y);

            grid = new GameGrid(pane, player, enemy);

            //tell server that this client is ready
            outputStream.write(0);

            while(true)
            {
                //wait for server to be ready
                inputStream.read();

                //output direction and position to server
                outputStream.writeInt(player.direction.x);
                outputStream.writeInt(player.direction.y);

                outputStream.writeDouble(player.x);
                outputStream.writeDouble(player.y);

                //output player status to server
                outputStream.writeBoolean(player.dead);

                //get enemy direction
                int xDirEnemy = inputStream.readInt();
                int yDirEnemy = inputStream.readInt();

                double xPosEnemy = inputStream.readDouble();
                double yPosEnemy = inputStream.readDouble();

                //look for end signal from server
                boolean endGame = inputStream.readBoolean();

                if(endGame)
                {
                    //get winner from server
                    boolean result = inputStream.readBoolean();

                    //end game
                    endGame(result);
                    return;
                }

                //update enemy position and direction
                enemy.x = xPosEnemy;
                enemy.y = yPosEnemy;
                enemy.setDirection(xDirEnemy, yDirEnemy);

                //update player position using the GameGrid (includes collision checks)
                grid.updatePlayerPosition();

                Platform.runLater(() -> grid.draw());
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * stops the client, with result telling whether the client won or not
     */
    void endGame(boolean result)
    {
        if(result)
        {
            Platform.runLater(() -> text.setText("YOU WON"));
            System.out.println("YOU WON");
        }
        else
        {
            Platform.runLater(() -> text.setText("YOU LOST"));
            System.out.println("YOU LOST");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
