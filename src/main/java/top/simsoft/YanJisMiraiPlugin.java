package top.simsoft;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;

public final class YanJisMiraiPlugin extends JavaPlugin {
    public static final YanJisMiraiPlugin INSTANCE = new YanJisMiraiPlugin();
    private static final AutoRespond autoRespond = new AutoRespond();
    private YanJisMiraiPlugin() {
        super(new JvmPluginDescriptionBuilder("top.simsoft.mirai", "0.1.0")
                .name("sbPlugin")
                .info("你是一个一个插件")
                .author("YanJi")
                .build());
    }

    @Override
    public void onEnable() {
        autoRespond.onEnable();
        getLogger().info("sbPlugin 已成功加载，撅飞你！");
    }
}