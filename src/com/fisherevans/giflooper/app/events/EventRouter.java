package com.fisherevans.giflooper.app.events;

import java.util.*;

/**
 * Author: Fisher Evans
 * Date: 6/17/14
 */
public class EventRouter {
    public static Map<EventType, Set<EventRouterListener>> _listeners;

    public static void init() {
        _listeners = new HashMap<EventType, Set<EventRouterListener>>();
    }

    public static void addListener(EventRouterListener listener, EventType eventType) {
        if(_listeners.get(eventType) == null)
            _listeners.put(eventType, new HashSet<EventRouterListener>());
        _listeners.get(eventType).add(listener);
    }

    public static void removeListener(EventRouterListener listener, EventType eventType) {
        if(_listeners.get(eventType) == null)
            return;
        _listeners.get(eventType).remove(listener);
        if(_listeners.get(eventType).size() == 0)
            _listeners.remove(eventType);
    }

    public static void event(Object source, EventType eventType, Object object) {
        if(_listeners.get(eventType) == null)
            return;
        for(EventRouterListener listener:_listeners.get(eventType))
        	listener.event(eventType, source, object);
    }

    public interface EventRouterListener {
        public void event(EventType eventType, Object source, Object obj);
    }
}
