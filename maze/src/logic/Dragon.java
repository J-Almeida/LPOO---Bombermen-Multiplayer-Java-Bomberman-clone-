package logic;

import model.Position;
import utils.Key;
import utils.RandomEngine;

/**
 * Dragon unit
 */
public class Dragon extends Unit
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Valid values for dragon behaviour.
     */
    public enum Behaviour
    {
        /** No movement at all. */
        Idle,

        /** Random movement. */
        RandomMovement,

        /** Random movement with ability to sleep. */
        Sleepy
    }

    /** Is dragon sleeping? */
    private boolean _asleep = false;

    /** Sleep countdown. */
    private int _sleepTimer = 0;

    /** The behaviour. */
    private final Behaviour _db;

    /** True if dragon is on top of sword. */
    private boolean _onSword = false;

    /**
     * Instantiates a new dragon.
     *
     * @param db the dragon behaviour
     */
    public Dragon(Behaviour db)
    {
        super(UnitType.Dragon);
        _db = db;
    }

    /* (non-Javadoc)
     * @see logic.WorldObject#GetSymbol()
     */
    @Override
    public char GetSymbol()
    {
        return _asleep ?
                    _onSword ? 'f' : 'd' :
                    _onSword ? 'F' : 'D' ;
    }

    /**
     * Puts the Dragon to sleep.
     *
     * @param time the time in seconds
     */
    public void SetToSleep(int time)
    {
        _asleep = true;
        _sleepTimer = time;
    }

    /**
     * Wakes up the Dragon.
     */
    public void WakeUp()
    {
        _asleep = false;
        _sleepTimer = 0;
    }

    /**
     * Checks if Dragon is sleeping.
     *
     * @return true, if Dragon is sleeping
     */
    public boolean IsSleeping()
    {
        return _asleep;
    }

    /**
     * Can move?
     *
     * @return true, if Dragon can move
     */
    public boolean CanMove()
    {
        if (_db == Behaviour.Idle)
            return false;

        if (_asleep)
            return false;

        return true;
    }

    /**
     * Can sleep?
     *
     * @return true, if Dragon is able to sleep
     */
    public boolean CanSleep()
    {
        if (_db != Behaviour.Sleepy)
            return false;

        return true;
    }

    /* (non-Javadoc)
     * @see logic.Unit#Update(logic.Maze)
     */
    @Override
    public void Update(Maze maze)
    {
        if (CanSleep())
        {
            if (_alive && _asleep)
            {
                _sleepTimer--;

                if (_sleepTimer == 0)
                    _asleep = false;
            }
            else
            {
                // 15% chance of sleeping between 1 and 3 seconds
                if (RandomEngine.GetBool(15))
                    SetToSleep(RandomEngine.GetInt(1, 3));
            }
        }

        if (CanMove())
        {
            Key dirKey = Key.toEnum(RandomEngine.GetInt(0, 4));

            if (dirKey != null)
            {
                Position newPos = _position.clone();

                Direction dir = Direction.FromKey(dirKey);
                Direction.ApplyMovement(newPos, dir);

                if (maze.IsPathPosition(newPos))
                {
                    _position = newPos;
                    maze.ForwardEventToUnits(new MovementEvent(this, dir));

                    Sword s = maze.FindSword();
                    _onSword  = s != null && _position.equals(s.GetPosition());

                    _direction = dir;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see logic.Unit#HandleEvent(logic.Maze, logic.Event)
     */
    @Override
    public void HandleEvent(Maze maze, Event event)
    {
        if (event.IsRequestMovementEvent())
        {
            RequestMovementEvent ev = event.ToRequestMovementEvent();

            Position newPos = _position.clone();
            Direction.ApplyMovement(newPos, ev.Direction);

            if (maze.IsPathPosition(newPos))
            {
                _position = newPos;
                _direction = ev.Direction;
                maze.ForwardEventToUnits(new MovementEvent(this, ev.Direction));
            }
        }
        else if (event.IsMovementEvent())
        {
            MovementEvent ev = event.ToMovementEvent();
            if (!this.IsSleeping())
            {
                if (ev.Actor.IsHero() || ev.Actor.IsEagle())
                {
                    if (Position.IsAdjacent(_position, ev.Actor.GetPosition()) || _position.equals(ev.Actor.GetPosition()))
                    {
                        if (ev.Actor.IsEagle() && ev.Actor.ToEagle().CanBeKilledByDragon())
                        {
                            ev.Actor.Kill();
                        }
                        else if (ev.Actor.IsHero())
                        {
                            if (ev.Actor.ToHero().IsArmed())
                                this.Kill();
                            else
                                ev.Actor.Kill();
                        }
                    }
                }
            }
            else
            {
                if (ev.Actor.IsHero() && ev.Actor.ToHero().IsArmed())
                    if (Position.IsAdjacent(_position, ev.Actor.GetPosition()) || _position.equals(ev.Actor.GetPosition()))
                        this.Kill();
            }
        }
    }
}
