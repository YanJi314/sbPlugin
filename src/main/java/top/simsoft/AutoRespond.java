package top.simsoft;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.internal.deps.okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AutoRespond {
    public static ArrayList<String> nudgeMessagePool;
    public static ArrayList<String> bloodPressureMessagePool;
    public static HashMap<Long, String> messageHistory = new HashMap<Long, String>();
    private static void initializeMessagePool(){
        nudgeMessagePool = new ArrayList<>();
        nudgeMessagePool.addAll(Arrays.asList(nudgeMessages));
        bloodPressureMessagePool = new ArrayList<>();
        bloodPressureMessagePool.addAll(Arrays.asList(bloodPressureMessages));
    }
    public static final OkHttpClient http = new OkHttpClient.Builder().connectTimeout(6, TimeUnit.SECONDS).readTimeout(10,TimeUnit.SECONDS).build();
    public static void onEnable(){
        initializeMessagePool();
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(YanJisMiraiPlugin.INSTANCE);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            String message = g.getMessage().contentToString();
            Boolean doSendMessage = true;
            Integer starxnGroup = 638992550;

            // =====================禁止复读鸡=====================
            if(messageHistory.getOrDefault(g.getGroup().getId(),"").equals(message) && !message.contains("动画表情") && !message.contains("图片") && Math.random()>0.6){
                if(!message.equals("打断复读！")){g.getGroup().sendMessage("打断复读！");}
                doSendMessage=false;
            }
            messageHistory.put(g.getGroup().getId(),message);

            if(doSendMessage) {

                // =====================星辰云特色功能=====================
                if (g.getGroup().getId() == starxnGroup) {
                    // 打不开 & 用不了
                    if (message.equals("dbk")) {
                        sendMessage(g, "请去 status.starxn.com 确认服务状态，如果502就等待几分钟，如果状态正常但打不开请提供域名和错误截图。");
                    // 星辰云各站链接
                    } else if (message.equals("link")) {
                        sendMessage(g, "--== < Links > ==--\n" +
                                "星辰云主站：starxn.com\n" +
                                "星辰云二级：dns.starxn.com\n" +
                                "星辰云论坛：forum.starxn.com\n" +
                                "服务状态页：status.starxn.com\n" +
                                "莹主机教程：yuque.com/afqaq/zvbhh4\n" +
                                "服务器教程：yuque.com/afqaq/ei24o3");
                    // 获取星辰云当前状态
                    } else if (message.equals("stat")) {
                        g.getGroup().sendMessage(getStatus("hk1", "香港1区-铂金区") + "\n" +
                                getStatus("hk2", "香港2区-尊享区") + "\n" +
                                getStatus("vhost", "荧-虚拟主机"));
                    // 随机血压语录
                    } else if (Math.random() >= 0.999) {
                        String bloodPressureMessage = randomMessage(bloodPressureMessagePool);
                        sendMessage(g, bloodPressureMessage);
                    }
                }

                // =====================DST生草句子功能=====================
                String dstDefault = "阿付 盐鸡 射线 XIAYM 合金锭 鸽子 幽灵 归星 李田所 延时";
                if (g.getGroup().getId() == starxnGroup){dstDefault = "阿付 wuli 归星 小脆糖 盐鸡 射线 XIAYM 冰块 Yo酱 亦云 茗茶 LiCaoZ";}
                if (message.startsWith("dst")) {
                    String dstPeople = message.replace("dst ", "").replace("dst", "");
                    if (message.trim().equals("dst")) { dstPeople = dstDefault; }
                    getDst(dstPeople, g);
                }

            }
        });
        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
        });
        eventChannel.subscribeAlways(NudgeEvent.class, n ->{
            if(Math.random()*100<50)return;
            Contact contact = n.getSubject();
            if(n.getTarget() == n.getBot()) {
                String message = randomMessage(nudgeMessagePool);
                contact.sendMessage(message);
            }
        });
    }

    private static void sendMessage(GroupMessageEvent g, String s) {
        messageHistory.put(g.getGroup().getId(),s);
        g.getGroup().sendMessage(s);
    }

    @NotNull
    public static String randomMessage(ArrayList<String> messagePool){
        return messagePool.get((int) Math.floor(Math.random()*messagePool.size()));
    }

    public static String[] nudgeMessages = {
        "你是一个一个一个",
        "撅飞你",
        "哼哼哼啊啊啊啊啊啊啊",
        "再戳我就撅炸你"
    };
    public static String[] bloodPressureMessages = {
        "网站突然打不开了咋办",
        "网站好慢啊",
        "为什么昨天还能用今天就不行了",
        "怎么连不上服务器了",
        "二级域名怎么用",
        "哪里有永久的免费域名",
        "主机怎么用啊",
        "是不是跑路了",
        "为什么群主就能用",
        "群主欺负我",
        "官网怎么打不开了"
    };

    private static void getDst(String people,GroupMessageEvent g){
        Request request = new Request.Builder().url("https://i.simsoft.top/dosth/api?people="+ URLEncoder.encode(people, StandardCharsets.UTF_8)).get().build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                sendMessage(g,"Dosth API 连接失败，请稍后再试。");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                sendMessage(g,response.body().string());
            }
        });
    }

    private static String getStatus(String testDomain, String nodeName) {
        Request request = new Request.Builder().url("http://"+testDomain+".starxn-status.qin.cab/").get().build();
        String testResult;
        try {
            Response res = http.newCall(request).execute();
            if(res.body().string().equals("200")){testResult =  nodeName+" 当前状态：正常运行";}
            else{testResult = nodeName+" 当前状态：可能无法连接";}
        } catch (IOException e) {
            testResult = nodeName+" 当前状态：可能无法连接";
        }

        return testResult;
    }
}