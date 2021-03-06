package com.tenXen.client.common;

import com.tenXen.core.domain.User;
import com.tenXen.core.model.MessageModel;
import com.tenXen.core.model.UserFriendModel;
import com.tenXen.core.model.UserGroupModel;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wt on 2016/10/25.
 */
public class ConnectContainer {

    public static EventLoopGroup USER_GROUP;

    public static Channel CHANNEL;

    public static User SELF;

    public static Map<String, UserFriendModel> FRIENDS;

    public static List<UserGroupModel> GROUPS;

    public static Map<String, List<MessageModel>> UNREAD_MSG = new HashMap<>();
}
