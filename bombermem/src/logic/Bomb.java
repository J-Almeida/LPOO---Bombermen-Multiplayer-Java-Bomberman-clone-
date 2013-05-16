package logic;

import java.awt.geom.Point2D;

import logic.events.Event;

public abstract class Bomb extends WorldObject
{
    public Bomb(int guid, Point2D position, int playerOwnerId, int radius, int strength, int time)
    {
        super(WorldObjectType.Bomb, guid, position);

        Radius = radius;
        Strength = strength;
        Time = time;
        PlayerOwnerId = playerOwnerId;
        
        _timer = Time * 1000;
        _shouldExplode = false;
    }

    public int Radius; // Number of cells in each direction
    public int Strength; // Damage in number of hitpoints
    public int Time; // Time to explode
    public int PlayerOwnerId; // Player who planted the bomb

    private int _timer;
    protected boolean _shouldExplode;
    
    @Override
    public void Update(int diff)
    {
        _timer -= diff;

        if (_timer <= 0)
        {
            _shouldExplode = true;
            _timer = -1;
        }
    }

    @Override
    public boolean IsAlive()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void HandleEvent(GameState gs, WorldObject src, Event event)
    {
        // TODO Auto-generated method stub
        
    }
}