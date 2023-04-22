import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

import java.awt.*;

public class Player
{
    public static final double MOVEMENT_SPEED = 10.0d;

    public double x, y;
    public boolean dead;

    public Polyline path;

    public Point direction = new Point(0, 1);

    private boolean settingDirection;

    public Player(double x, double y)
    {
        this.x = x;
        this.y = y;

        //start path with two points
        path = new Polyline(x, y, x + direction.x * MOVEMENT_SPEED, y + direction.y * MOVEMENT_SPEED);
    }

    public void die()
    {
        System.out.println("DIE");
        dead = true;
    }

    //set direction method for key-based adjustment (used by player)
    //called on key press
    public void setDirection(KeyEvent e)
    {
        //get the new direction
        Point newDir = direction;
        switch(e.getCode())
        {
            case A -> newDir = new Point(-1, 0); //left
            case D -> newDir = new Point(1, 0); //right
            case W -> newDir = new Point(0, -1); //up
            case S -> newDir = new Point(0, 1); //down
        }

        //cancel if new direction is same as current or equal to 0,0
        if(settingDirection || direction.equals(newDir) || (newDir.x == 0 && newDir.y == 0))
            return;

        //prevent simultaneous method calls with settingDirection
        settingDirection = true;

        System.out.println("SET DIRECTION " + e.getCode());

        //straighten the last line by positioning player directly horizontal/vertical to last vertex
        if(Math.abs(direction.y) == 1)
            x = path.getPoints().get(path.getPoints().size() - 4);
        if(Math.abs(direction.x) == 1)
            y = path.getPoints().get(path.getPoints().size() - 3);

        //update last point to reflect position change
        path.getPoints().set(path.getPoints().size() - 1, y);
        path.getPoints().set(path.getPoints().size() - 2, x);

        //place new vertex at current position
        path.getPoints().add(x);
        path.getPoints().add(y);
        System.out.println("NEW POINT: " + x + ", " + y + " CURRENT: " + direction + " NEXT: " + newDir);

        //update direction
        direction = newDir;

        //prevent direction spamming
        try
        {
            Thread.sleep(100);
        }
        catch(InterruptedException ex)
        {
            ex.printStackTrace();
        }

        settingDirection = false;
    }

    //set direction method for manual adjustment (used by enemy)
    public void setDirection(int xDir, int yDir)
    {
        synchronized(path)
        {
            if(direction.x == xDir && direction.y == yDir)
                return;

            if(xDir == 0 && yDir == 0)
                return;

            //straighten the last line by positioning player directly horizontal/vertical to last vertex
            if(Math.abs(direction.y) == 1)
                x = path.getPoints().get(path.getPoints().size() - 4);
            if(Math.abs(direction.x) == 1)
                y = path.getPoints().get(path.getPoints().size() - 3);

            //update last point to reflect position change
            path.getPoints().set(path.getPoints().size() - 1, y);
            path.getPoints().set(path.getPoints().size() - 2, x);

            //place new vertex at current position
            path.getPoints().add(x);
            path.getPoints().add(y);

            //update direction
            direction = new Point(xDir, yDir);
        }
    }
}
