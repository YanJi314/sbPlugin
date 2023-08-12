package top.simsoft;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.internal.deps.okhttp3.*;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoRespond {
    public static ArrayList<String> nudgeMessagePool;
    public static HashMap<Long, String> messageHistory = new HashMap<Long, String>();
    private static void initializeMessagePool(){
        nudgeMessagePool = new ArrayList<>();
        nudgeMessagePool.addAll(Arrays.asList(nudgeMessages));
    }
    public static final OkHttpClient http = new OkHttpClient.Builder().connectTimeout(6, TimeUnit.SECONDS).readTimeout(10,TimeUnit.SECONDS).build();
    public static void onEnable(){
        initializeMessagePool();
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(YanJisMiraiPlugin.INSTANCE);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            String message = g.getMessage().contentToString();
            Boolean doSendMessage = true;
            Integer starxnGroup = 638992550;
            Integer testGroup = 157926237;

            // =====================禁止复读鸡=====================
            if(messageHistory.getOrDefault(g.getGroup().getId(),"").equals(message) && !message.contains("动画表情") && !message.contains("语音") && !message.contains("图片") && !message.contains("草") && Math.random()>0.6){
                if(!message.equals("打断复读！")){g.getGroup().sendMessage("打断复读！");}
                doSendMessage=false;
            }
            messageHistory.put(g.getGroup().getId(),message);

            if(doSendMessage) {

                // =====================星辰云特色功能=====================
                if (g.getGroup().getId() == starxnGroup || g.getGroup().getId() == testGroup) {
                    // 星辰云各站链接
                    if (message.equals("link")) {
                        sendMessage(g, "--== < Links > ==--\n" +
                                "星辰云主站：starxn.com\n" +
                                "星辰云二级：dns.starxn.com\n" +
                                "星辰云论坛：forum.starxn.com\n" +
                                "服务状态页：status.starxn.com\n" +
                                "莹主机教程：yuque.com/afqaq/zvbhh4\n" +
                                "服务器教程：yuque.com/afqaq/ei24o3");
                    // 获取星辰云当前状态
                    } else if (message.equals("stat")) {
                        g.getGroup().sendMessage("--== < Status > ==--\n" + getStatusWarn());
                    }
                }

                // =====================DST生草句子功能=====================
                String dstDefault = "阿付 盐鸡 射线 XIAYM 合金锭 鸽子 幽灵 归星 李田所 延时";
                if (g.getGroup().getId() == starxnGroup){dstDefault = "阿付 wuli 归星 小脆糖 盐鸡 射线 XIAYM 冰块 Yo酱 亦云 茗茶 LiCaoZ";}
                if (message.startsWith("dst")) {
                    String dstPeople = message.replace("dst ", "").replace("dst", "");
                    if (dstPeople.isEmpty()) { dstPeople = dstDefault; }
                    getDst(dstPeople, g);
                }

                // =====================JDC黑历史云储库=====================
                if (message.startsWith("jdc ")) {
                    String searchWord=message.replace("jdc ","");
                    if(searchWord.isEmpty()){
                        sendMessage(g,"搜索内容为空。");
                    }else {
                        try {
                            File file = fetchJdc(searchWord);

                            if (file != null) {
                                ExternalResource resource = ExternalResource.create(file);
                                Image image = g.getGroup().uploadImage(resource);
                                g.getGroup().sendMessage(image);
                                messageHistory.put(g.getGroup().getId(),"");
                                resource.close();
                            }
                        } catch (Exception ex) {
                            if (ex instanceof InterruptedIOException) return;
                            sendMessage(g,"未匹配到查找的 JDC 图片。");
                            ex.printStackTrace();
                        }
                    }
                }


                // =====================今日人品查询器=====================
                if (message.startsWith("今日人品")) {
                    String searchWord= message.replace("今日人品","").replace("@","").replace(" ","");
                    if(searchWord.isEmpty()){
                        searchWord= String.valueOf(g.getSender().getId());
                    }
                    Request request = new Request.Builder().url("https://api.simsoft.top/luck/todayLuck?q="+searchWord).get().build();
                    http.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {sendMessage(g,"查询人品时出现错误！");}
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String responseContent = response.body().string();
                            sendMessage(g,responseContent);
                        }
                    });
                }
                if (message.startsWith("人品统计")) {
                    String searchWord= message.replace("人品统计","").replace("@","").replace(" ","");
                    if(searchWord.isEmpty()){
                        searchWord= String.valueOf(g.getSender().getId());
                    }
                    Request request = new Request.Builder().url("https://api.simsoft.top/luck/searchLuck?q="+searchWord).get().build();
                    http.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {sendMessage(g,"查询人品时出现错误！");}
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String responseContent = response.body().string();
                            sendMessage(g,responseContent);
                        }
                    });
                }


            }
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


    private static void getDst(String people,GroupMessageEvent g){
        Request request = new Request.Builder().url("https://i.simsv.com/dosth/api?people="+ URLEncoder.encode(people, StandardCharsets.UTF_8)).get().build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {sendMessage(g,"Dosth API 连接失败，请稍后再试。");}
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String dstResponseContent = response.body().string();
                if(!dstResponseContent.contains("[DoSth API]")) {sendMessage(g, dstResponseContent);}
                else{sendMessage(g,"DoSth API 返回异常，可能包含违禁词。");}
            }
        });
    }


    private static String getStatusWarn() {
        Request request = new Request.Builder().url("https://status.starxn.com/status/starxn").get().build();
        String testResult;
        try {
            Response res = http.newCall(request).execute();
            String htmlData=res.body().string();
            testResult = "目前暂无运营状态信息";
            String[] htmlSplit = htmlData.split("'warning','title':'");
            if(htmlSplit.length>1) {
                htmlData = htmlSplit[1];
                String warningTitle = htmlData.split("'")[0];
                if (!warningTitle.isEmpty()) {
                    testResult = "运营状态信息：" + unicodeDecode(warningTitle) + "，请前往 status.starxn.com 查看。";
                }
            }else{testResult = "目前暂无运营状态信息";}
        } catch (Exception e) {
            testResult = "目前暂无运营状态信息";
        }

        return testResult;
    }

    public static synchronized File fetchJdc(String search) throws Exception {
        try {
            File file = new File(YanJisMiraiPlugin.INSTANCE.getDataFolder(), "cache/");
            if (!file.exists() && !file.mkdirs()) {return null;}

            Request imageReq = new Request.Builder().url("https://jdc.nlrdev.top/botsearch.php?find=" + URLEncoder.encode(search,StandardCharsets.UTF_8)).get().build();
            Response imageRes = http.newCall(imageReq).execute();
            if (imageRes.body() == null) throw new Exception("L");

            Path cachePicturePath = new File(file, search).toPath();
            Files.deleteIfExists(cachePicturePath);
            Files.copy(imageRes.body().byteStream(), cachePicturePath);

            return cachePicturePath.toFile();
        } catch (Exception e) {
            throw e;
        }
    }


    // Unicode转中文器
    public static String unicodeDecode(String string){
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);
        char ch;
        while (matcher.find()){
            ch=(char) Integer.parseInt(matcher.group(2),16);
            string = string.replace(matcher.group(1),ch+"");
        }
        return string;
    }
}

