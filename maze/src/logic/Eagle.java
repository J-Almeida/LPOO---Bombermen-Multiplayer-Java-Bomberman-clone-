package logic;

import java.util.Stack;

import model.Position;

public class Eagle extends Unit
{
    public enum EagleState
    {
        OnFlight,
        OnFloor,
        FollowingHero,
        ReachedSword,
        OnFlightBack
    }

    private EagleState _state;
    private static final double _speed = 1;
    private boolean _armed = false;

    public Eagle()
    {
        super(UnitType.Eagle);
        _state = EagleState.FollowingHero;
    }

    public void SetState(EagleState state)
    {
        _state = state;
    }

    public EagleState GetState()
    {
        return _state;
    }

    public void EquipSword()
    {
        _armed = true;
    }

    public void UnequipSword()
    {
        _armed = false;
    }

    public boolean IsArmed()
    {
        return _armed;
    }

    private Position _initialPosition = null;
    private Position _swordPosition = null;
    private Stack<Position> _wayPath = new Stack<Position>();

    public void SetSwordPosition(Position pos)
    {
        _swordPosition = pos;
    }

    @Override
    public void Update(Maze maze)
    {
        if (_state == EagleState.OnFlight)
        {
            if (_initialPosition == null) // takeoff
            {
                _initialPosition = GetPosition();
                _wayPath.push(_initialPosition);
            }
            else
            {
                double deltaX = _swordPosition.X - GetPosition().X;
                double deltaY = _swordPosition.Y - GetPosition().Y;

                double dist = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
                if (dist > _speed)
                {
                    double ratio = _speed / dist;
                    double xMove = ratio * deltaX;
                    double yMove = ratio * deltaY;
                    double newXPos = xMove + GetPosition().X;
                    double newYPos = yMove + GetPosition().Y;
                    SetPosition(new Position(Math.round((float)newXPos), Math.round((float)newYPos)));
                }
                else
                {
                    SetPosition(_swordPosition);
                }

                if (GetPosition().equals(_swordPosition))
                    _state = EagleState.ReachedSword;
                else
                    _wayPath.push(GetPosition());
            }
        }
        else if (_state == EagleState.OnFlightBack)
        {
            if (!_wayPath.isEmpty())
            {
                SetPosition(_wayPath.pop());
            }
            else
            {
                SetState(EagleState.OnFloor);
            }
        }

        while (!_eventQueue.isEmpty())
        {
            Event event = _eventQueue.peek();
            if (event.IsCollisionEvent())
            {
                CollisionEvent ev = event.ToCollisionEvent();
                if (ev.Other.IsSword())
                {
                    this.EquipSword();
                    SetState(EagleState.OnFlightBack);
                }
                else if (ev.Other.IsHero())
                {
                    this.UnequipSword();
                    SetState(EagleState.FollowingHero);
                }
            }
            else if (event.IsMovementEvent())
            {
                MovementEvent ev = event.ToMovementEvent();
                if (ev.Actor.IsHero() && GetState() == EagleState.FollowingHero)
                {
                    _position = ev.Actor.GetPosition().clone();
                    maze.ForwardEventToUnits(new MovementEvent(this, ev.Direction));
                }
            }
            else if (event.IsSendEagleEvent())
            {
                SendEagleEvent ev = event.ToSendEagleEvent();
                if (!ev.Hero.IsArmed() && _state == EagleState.FollowingHero)
                {
                    _swordPosition = ev.Sword.GetPosition().clone();
                    SetState(EagleState.OnFlight);
                }
            }

            _eventQueue.poll();
        }
    }

    @Override
    public char GetSymbol()
    {
        return IsArmed() ? '\u00A5' /* Yen */ : 'W';
    }
}
