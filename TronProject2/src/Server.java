import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        System.out.println("START SERVER");

        ServerSocket server = new ServerSocket(8000);
        while(true)
        {
            //get clients and give them their numbers
            Socket player1 = server.accept();
            player1.getOutputStream().write(1);

            Socket player2 = server.accept();
            player2.getOutputStream().write(2);

            new Thread(new GameTask(player1, player2)).start();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class GameTask implements Runnable
{
    Socket player1;
    Socket player2;

    public GameTask(Socket player1, Socket player2)
    {
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public void run()
    {
        try
        {
            //setup connections between players and server
            DataInputStream inputStream1 = new DataInputStream(player1.getInputStream());
            DataOutputStream outputStream1 = new DataOutputStream(player1.getOutputStream());

            DataInputStream inputStream2 = new DataInputStream(player2.getInputStream());
            DataOutputStream outputStream2 = new DataOutputStream(player2.getOutputStream());

            //signal players to initialize
            outputStream1.write(0);
            outputStream2.write(0);

            //wait for clients to finish initializing, then begin
            inputStream1.read();
            inputStream2.read();

            //Loop
            while(true)
            {
                //signal player1 to give direction and position
                outputStream1.write(0);

                int xDir1 = inputStream1.readInt();
                int yDir1 = inputStream1.readInt();

                double xPos1 = inputStream1.readDouble();
                double yPos1 = inputStream1.readDouble();

                boolean player1Dead = inputStream1.readBoolean();

                //signal player2 to give direction and position
                outputStream2.write(0);

                int xDir2 = inputStream2.readInt();
                int yDir2 = inputStream2.readInt();

                double xPos2 = inputStream2.readDouble();
                double yPos2 = inputStream2.readDouble();

                boolean player2Dead = inputStream2.readBoolean();

                System.out.println(xDir1 + ", " + yDir1 + "; " + xDir2 + ", " + yDir2);

                //test for end of game
                if(player1Dead || player2Dead)
                {
                    System.out.println("END GAME " + xDir1 + " " + yDir1 + " " + xDir2 + " " + yDir2);

                    //give players final values and tell them to stop
                    outputStream2.writeInt(0);
                    outputStream2.writeInt(0);
                    outputStream2.writeDouble(xPos1);
                    outputStream2.writeDouble(yPos1);
                    outputStream2.writeBoolean(true);

                    outputStream1.writeInt(0);
                    outputStream1.writeInt(0);
                    outputStream1.writeDouble(xPos2);
                    outputStream1.writeDouble(yPos2);
                    outputStream1.writeBoolean(true);

                    //if opponent is dead, tell player they have won, else tell them they lost
                    outputStream1.writeBoolean(player2Dead);
                    outputStream2.writeBoolean(player1Dead);

                    break;
                }

                //give players direction and position of other player and tell them to continue
                outputStream2.writeInt(xDir1);
                outputStream2.writeInt(yDir1);
                outputStream2.writeDouble(xPos1);
                outputStream2.writeDouble(yPos1);
                outputStream2.writeBoolean(false);

                outputStream1.writeInt(xDir2);
                outputStream1.writeInt(yDir2);
                outputStream1.writeDouble(xPos2);
                outputStream1.writeDouble(yPos2);
                outputStream1.writeBoolean(false);

                Thread.sleep(100);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}