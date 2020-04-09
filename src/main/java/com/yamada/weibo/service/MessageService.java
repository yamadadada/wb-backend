package com.yamada.weibo.service;

import com.yamada.weibo.enums.MessageType;
import com.yamada.weibo.pojo.Comment;
import com.yamada.weibo.pojo.Message;
import com.yamada.weibo.pojo.Weibo;
import com.yamada.weibo.vo.BadgeVO;

import java.util.List;

public interface MessageService {

    void sendAt(Weibo weibo, Integer loginUid);

    void sendAt(Comment comment, Integer loginUid);

    void sendAtByWid(Integer wid, Integer loginUid);

    void sendComment(Comment comment, Integer loginUid);

    void sendLikeByWid(Integer wid, Integer loginUid);

    void sendLikeByCid(Integer cid, Integer loginUid);

    void sendSystem(Integer uid, String content, Integer loginUid);

    List<Message> getList(MessageType type);

    void delete(Integer mid);

    BadgeVO badge(Integer atId, Integer commentId, Integer likeId, Integer systemId);
}
