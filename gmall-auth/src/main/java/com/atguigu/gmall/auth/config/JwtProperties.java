package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2023/5/7 22:26
 * @Email: moumouguan@gmail.com
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "jwt") // 读取以谁为为前缀的配置
public class JwtProperties {

    private String pubKeyPath; // 公钥路径
    private String priKeyPath; // 私钥路径
    private String secret; // 盐
    private Integer expire; // 过期时间
    private String cookieName; // cookie 名称
    private String unick; // 用户名

    private PublicKey publicKey; // 公钥
    private PrivateKey privateKey; // 私钥

    /**
     * 该方法在构造方法执行之后执行
     */
    @PostConstruct
    public void init(){
        try {
            // 读取公钥私钥
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);

            // 如果公钥或者私钥不存在，重新生成公钥和私钥
            if (!pubFile.exists() || !priFile.exists()) {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }

            // 读取公钥路径获取公钥对象
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            // 读取私钥路径获取私钥对象
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("生成公钥和私钥出错");
            e.printStackTrace();
        }
    }

}
