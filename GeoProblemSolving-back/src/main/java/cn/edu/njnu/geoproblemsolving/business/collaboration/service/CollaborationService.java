package cn.edu.njnu.geoproblemsolving.business.collaboration.service;

import cn.edu.njnu.geoproblemsolving.business.collaboration.cache.CommunicationCache;
import cn.edu.njnu.geoproblemsolving.business.collaboration.cache.ComputeTasks;
import cn.edu.njnu.geoproblemsolving.business.collaboration.cache.OperationQueue;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.ChatMsg;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.CollaborationUser;
import cn.edu.njnu.geoproblemsolving.business.collaboration.entity.ComputeMsg;
import cn.edu.njnu.geoproblemsolving.business.collaboration.enums.CollaborationMode;
import cn.edu.njnu.geoproblemsolving.business.collaboration.utils.CollaborationBehavior;
import cn.edu.njnu.geoproblemsolving.business.collaboration.utils.CollaborationConfig;
import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.chatmessage.ChatMessageRecordsService;
import cn.edu.njnu.geoproblemsolving.business.tool.chatroom.hydrologicalconcept.AnsjSegService;
import cn.edu.njnu.geoproblemsolving.business.user.dao.IUserDao;
import cn.edu.njnu.geoproblemsolving.business.user.dto.InquiryUserDto;
import cn.edu.njnu.geoproblemsolving.common.utils.JsonResult;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class CollaborationService {

    @Autowired
    IUserDao iUserDao;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    ChatMessageRecordsService chatMessageRecordsService;

    @Autowired
    AnsjSegService ansjSegService;

    @Autowired
    CollaborationBehavior collaborationBehavior;

    @Autowired
    CommunicationCache communicationCache;

    @Autowired
    OperationQueue operationQueue;

    @Autowired
    ComputeTasks computeTasks;

    private CollaborationConfig collaborationConfig;
    private static final Map<String, CollaborationConfig> groups = new ConcurrentHashMap<>(); // collaboration groups

    public void msgStart(String groupKey, Session session, EndpointConfig config) {
        try {
            //判断会话是否存在
            if (groups.containsKey(groupKey)) {
                collaborationConfig = groups.get(groupKey);
            } else {
                collaborationConfig = new CollaborationConfig(groupKey);
                groups.put(groupKey, collaborationConfig);
            }

            // current sender
            String userId = ((HttpSession) config.getUserProperties().get(HttpSession.class.getName())).getAttribute("userId").toString();
            CollaborationUser collaborationUser = collaborationBehavior.getMemberInfo(userId, session);

            // current participants
            ArrayList<CollaborationUser> participants;
            if (collaborationConfig.getParticipants() == null) {
                participants = new ArrayList<>();
            } else {
                participants = collaborationConfig.getParticipants();
            }
            participants.add(collaborationUser);
            collaborationConfig.setParticipants(participants);
            groups.put(groupKey, collaborationConfig);

            // 发布缓存信息
            if(participants.size() > 1 && communicationCache.getCache(groupKey).size() > 0) {
                collaborationBehavior.sendMessageCache(communicationCache.getCache(groupKey), session);
                // 通知新成员上线，发布新的成员列表
                collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "on");
            }
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    public void operationStart(String groupKey, Session session, EndpointConfig config) {
        try {
            //判断会话是否存在
            if (groups.containsKey(groupKey)) {
                collaborationConfig = groups.get(groupKey);
            } else {
                collaborationConfig = new CollaborationConfig(groupKey);
                groups.put(groupKey, collaborationConfig);
            }

            // current operator
            String userId = ((HttpSession) config.getUserProperties().get(HttpSession.class.getName())).getAttribute("userId").toString();
            CollaborationUser collaborationUser = collaborationBehavior.getMemberInfo(userId, session);

            // current participants
            ArrayList<CollaborationUser> participants;
            if (collaborationConfig.getParticipants() == null) {
                participants = new ArrayList<>();
            } else {
                participants = collaborationConfig.getParticipants();
            }
            participants.add(collaborationUser);
            collaborationConfig.setParticipants(participants);
            groups.put(groupKey, collaborationConfig);

            // 发布当前协同操作模式
            if(participants.size() > 1) {
                // 通知新成员上线，发布新的成员列表
                collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "on");
                collaborationBehavior.sendCollaborationStatus(collaborationConfig, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void msgTransfer(String groupKey, String message) {
        try {
            collaborationConfig = groups.get(groupKey);

            JSONObject messageObject = JSONObject.parseObject(message);
            String messageType = messageObject.getString("type");
            String sender = messageObject.getString("sender");
            List<String> receivers = messageObject.getJSONArray("receivers").toJavaList(String.class);
            if(receivers == null) receivers = new ArrayList<>();

            switch (messageType) {
                case "test": {
                    collaborationConfig.setMode(CollaborationMode.Free);
                    groups.put(groupKey, collaborationConfig);

                    collaborationBehavior.transferMessage(collaborationConfig.getParticipants(), sender, receivers, "test");
                    break;
                }
                case "message": {
                    String text = messageObject.getString("content");
                    collaborationBehavior.transferMessage(collaborationConfig.getParticipants(), sender, receivers, text);

                    Boolean toolConcepts = messageObject.getBoolean("toolConcepts");
                    String relateConceptSet = "";
                    if (toolConcepts == null || !toolConcepts) {
                        String result = ansjSegService.processInfo(text);
                        relateConceptSet = ansjSegService.elasticSearch(result);
                    }
                    if (relateConceptSet.equals("")) {
                        collaborationBehavior.transferMessage(collaborationConfig.getParticipants(), sender, receivers, relateConceptSet);
                    }

                    // 添加消息至缓存
                    ChatMsg chatMsg = new ChatMsg();
                    ArrayList<ChatMsg> chatMsgRecords = communicationCache.getCache(groupKey);
                    if (chatMsgRecords == null) chatMsgRecords = new ArrayList<>();

                    chatMsg.setAid(groupKey);
                    chatMsg.setSender(sender);
                    chatMsg.setReceiver(receivers);
                    chatMsg.setContent(text);
                    chatMsg.setTime(new Date().toString());
                    chatMsgRecords.add(chatMsg);
                    communicationCache.putCache(groupKey, chatMsgRecords);

                    break;
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void operationTransfer(String toolId, String aid, String message){
        try {
            String groupKey = toolId + aid;
            collaborationConfig = groups.get(groupKey);

            JSONObject messageObject = JSONObject.parseObject(message);
            String messageType = messageObject.getString("type");
            String sender = messageObject.getString("sender");
            List<String> receivers = messageObject.getJSONArray("receivers").toJavaList(String.class);
            if(receivers == null) receivers = new ArrayList<>();

            switch (messageType) {
                case "test": {
                    collaborationConfig.setMode(CollaborationMode.Free);
                    groups.put(groupKey, collaborationConfig);

                    collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), sender, receivers, "test", "test");
                    break;
                }
                case "mode": {
                    String mode = messageObject.getString("mode");
                    collaborationConfig.setMode(CollaborationMode.valueOf(mode));
                    groups.put(groupKey, collaborationConfig);

                    collaborationBehavior.sendModeType(collaborationConfig.getParticipants(), mode);
                    break;
                }
                case "control-apply": {
                    String ctrUser = messageObject.getString("operator");
                    List<String> applyQueue = collaborationConfig.getApplyQueue();

                    if(applyQueue == null) applyQueue = new ArrayList<>();
                    applyQueue.add(ctrUser);
                    collaborationConfig.setApplyQueue(applyQueue);
                    groups.put(groupKey, collaborationConfig);

                    collaborationBehavior.sendControlInfo(collaborationConfig.getParticipants(), applyQueue,  ctrUser, "apply");
                    break;
                }
                case "control-stop": {
                    String ctrUser = "";
                    if(collaborationConfig.getMode().equals(CollaborationMode.SemiFree_Apply)) {
                        List<String> applyQueue = collaborationConfig.getApplyQueue();
                        if (applyQueue != null && applyQueue.size() > 0) {
                            ctrUser = applyQueue.remove(0);
                            collaborationConfig.setOperator(ctrUser);
                            collaborationConfig.setApplyQueue(applyQueue);
                        } else {
                            collaborationConfig.setOperator("");
                        }
                        groups.put(groupKey, collaborationConfig);

                        collaborationBehavior.sendControlInfo(collaborationConfig.getParticipants(), applyQueue, ctrUser, "stop");
                    } else if(collaborationConfig.getMode().equals(CollaborationMode.SemiFree_Occupy)){
                        collaborationConfig.setOperator("");
                        groups.put(groupKey, collaborationConfig);
                        collaborationBehavior.sendControlInfo(collaborationConfig.getParticipants(), new ArrayList<>(), "", "stop");
                    }
                    break;
                }
                case "operation": {
                    String behavior = messageObject.getString("behavior");
                    String object = messageObject.getString("object");
                    // 操作权限判断，冲突判断
                    CollaborationMode mode = collaborationConfig.getMode();
                    if(mode.equals(CollaborationMode.SemiFree_Apply)){
                        // 判断操作权限
                        if(collaborationConfig.getOperator().equals(sender)){
                            collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), sender, receivers, behavior, object);
                        } else {
                            collaborationBehavior.operationRefuse(collaborationConfig.getParticipants(), sender);
                        }
                    } else if(mode.equals(CollaborationMode.SemiFree_Occupy)){
                        // 抢占操作权
                        if(collaborationConfig.getOperator().equals("")){
                            collaborationConfig.setOperator(sender);
                            groups.put(groupKey, collaborationConfig);
                        }
                        // 判断操作权限
                        if(collaborationConfig.getOperator().equals(sender)){
                            collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), sender, receivers, behavior, object);
                        } else {
                            collaborationBehavior.operationRefuse(collaborationConfig.getParticipants(), sender);
                        }
                    } else if(mode.equals(CollaborationMode.Free)){
                        collaborationBehavior.transferOperation(collaborationConfig.getParticipants(), sender, receivers, behavior, object);
                    }


                    break;
                }
                case "computation":{
                    // 调用模型容器或者数据容器执行计算任务（当计算完成时，需要向前端返回计算结果）
                    //...
                    //...

                    // 添加计算任务至缓存
                    ComputeMsg computeMsg = new ComputeMsg();
                    ArrayList<ComputeMsg> computeRecords = computeTasks.getCache(groupKey);
                    if (computeRecords == null) computeRecords = new ArrayList<>();

                    computeMsg.setAid(aid);
                    computeMsg.setToolId(toolId);
                    computeMsg.setReceivers(collaborationConfig.getParticipants());
                    computeMsg.setTime(new Date().toString());
                    computeRecords.add(computeMsg);
                    computeTasks.putCache(groupKey, computeRecords);
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void communicationClose(String groupKey, Session session) {
        try {
            collaborationConfig = groups.get(groupKey);
            // remove people
            CollaborationUser collaborationUser = null;
            for (CollaborationUser user : collaborationConfig.getParticipants()) {
                if (user.getSession().equals(session)) {
                    collaborationUser = user;
                    ArrayList<CollaborationUser> participants = collaborationConfig.getParticipants();
                    participants.remove(collaborationUser);
                    collaborationConfig.setParticipants(participants);
                    groups.put(groupKey, collaborationConfig);
                    break;
                }
            }

            // 通知成员退出，发布新的成员列表
            collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "off");

            // 通讯结束，持久化存储，清除缓存
            if (groups.get(groupKey).getParticipants().size() < 1) {
                collaborationBehavior.msgCacheStore(communicationCache.getCache(groupKey));
                groups.remove(groupKey);
                communicationCache.removeCache(groupKey);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public void operationClose(String groupKey, Session session) {
        try {
            collaborationConfig = groups.get(groupKey);
            // remove people
            CollaborationUser collaborationUser = null;
            for (CollaborationUser user : collaborationConfig.getParticipants()) {
                if (user.getSession().equals(session)) {
                    collaborationUser = user;
                    ArrayList<CollaborationUser> participants = collaborationConfig.getParticipants();
                    participants.remove(collaborationUser);
                    collaborationConfig.setParticipants(participants);
                    groups.put(groupKey, collaborationConfig);
                    break;
                }
            }

            // 通知成员退出，发布新的成员列表
            collaborationBehavior.sendParticipantsInfo(collaborationConfig.getParticipants(), collaborationUser, "off");

            // 通讯结束，清除缓存
            if (groups.get(groupKey).getParticipants().size() < 1) {
                groups.remove(groupKey);
                operationQueue.removeCache(groupKey);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

}


