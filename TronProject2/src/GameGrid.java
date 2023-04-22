import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class GameGrid
{
    public static final int GRID_WIDTH = 640;
    public static final int GRID_HEIGHT = 640;

    public static final int PATH_WIDTH = 5;

    public static final Point PLAYER1_START_POS = new Point(160, 320);
    public static final Point PLAYER2_START_POS = new Point(480, 320);

    Player player;
    Player enemy;

    Pane pane;

    Polyline playerPath;
    Polyline enemyPath;

    public GameGrid(Pane pane, Player player, Player enemy)
    {
        this.pane = pane;
        this.player = player;
        this.enemy = enemy;

        //keep track of enemy and player paths
        playerPath = player.path;
        enemyPath = enemy.path;

        playerPath.setStroke(Color.GREEN);
        enemyPath.setStroke(Color.RED);

        playerPath.setStrokeWidth(PATH_WIDTH);
        enemyPath.setStrokeWidth(PATH_WIDTH);

        //add paths to the display
        Platform.runLater(() -> {
            pane.getChildren().add(0, playerPath);
            pane.getChildren().add(0, enemyPath);
        });
    }

    /**
     * Updates the Polyline paths of the player and enemy
     */
    void draw()
    {
        updatePath(player);
        updatePath(enemy);
    }

    /**
     * Updates a given player's path object
     */
    void updatePath(Player player)
    {
        synchronized(player.path)
        {
            ObservableList<Double> points = player.path.getPoints();

            //update last point in path to reflect current x and y
            points.set(points.size() - 1, (double) ((int) player.y));
            points.set(points.size() - 2, (double) ((int) player.x));
        }
    }

    /**
     * Checks for collision and then moves player to next position
     */
    void updatePlayerPosition()
    {
        //reset direction to default if 0,0
        if(player.direction.x == 0 && player.direction.y == 0)
            player.direction = new Point(0, 1);

        //die on collision
        if(checkCollision())
            player.die();

        //update position
        player.x += player.direction.x * Player.MOVEMENT_SPEED;
        player.y += player.direction.y * Player.MOVEMENT_SPEED;
    }

    /**
    Checks the collision for player only based on path intersection and grid bounds
     */
    public boolean checkCollision()
    {
        //collision if out of bounds
        if(player.x >= GRID_WIDTH || player.x < 0 || player.y >= GRID_HEIGHT || player.y < 0)
            return true;

        //check for intersections between own path and enemy's path
        return checkPathIntersection(playerPath) || checkPathIntersection(enemyPath);
    }

    boolean checkPathIntersection(Polyline path)
    {
        //check intersection of each line in path

        //get points on Polyline
        ObservableList<Double> playerPoints = playerPath.getPoints();

        //the end line is the line we are checking intersection with
        //end is a small line starting at the player's next position and ending a bit after
        int lastIndex = playerPoints.size() - 1;
        double endX1 = playerPoints.get(lastIndex - 1) + player.direction.x * 2;
        double endY1 = playerPoints.get(lastIndex) + player.direction.y * 2;
        double endX2 = endX1 + player.direction.x * Player.MOVEMENT_SPEED;
        double endY2 = endY1 + player.direction.y * Player.MOVEMENT_SPEED;

        Line2D end = new Line2D.Double(endX1, endY1, endX2, endY2);

        for(int i = 0; i < path.getPoints().size(); i += 2)
        {
            if(i + 3 >= path.getPoints().size())
                continue;

            //get points for current line
            double x1 = path.getPoints().get(i);
            double y1 = path.getPoints().get(i + 1);

            double x2 = path.getPoints().get(i + 2);
            double y2 = path.getPoints().get(i + 3);

            if(endX1 == x2 && endY1 == y2)
                continue;

            //create line and check intersection
            Line2D line2D = new Line2D.Double(new Point2D.Double(x1, y1), new Point2D.Double(x2, y2));
            if(end.intersectsLine(line2D))
            {
                //System.out.println("INTERSECTION " + end.getP1() + ", " + end.getP2() + "; " + line2D.getP1() + ", " + line2D.getP2());
                return true;
            }
        }

        return false;
    }
}
