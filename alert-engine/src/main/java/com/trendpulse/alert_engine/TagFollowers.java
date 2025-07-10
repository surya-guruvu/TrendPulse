package com.trendpulse.alert_engine;

import java.util.Set;
import java.util.HashSet;

public class TagFollowers {
    private final Set<String> userIds = new HashSet<>();

    public TagFollowers add(String user)   { userIds.add(user);    return this; }
    public TagFollowers remove(String user){ userIds.remove(user); return this; }
    public Set<String> getUserIds()        { return userIds; }
}