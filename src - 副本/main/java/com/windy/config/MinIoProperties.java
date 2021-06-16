package com.windy.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
  使用@Data：自动生成getter和setter函数，必须安装lombok插件和添加依赖
  @ConfigurationProperties(prefix = "minio")自动将配置文件前缀为minio的值赋值给类属性，可以同时
  赋值多个
   @Value ("${minio.url}")：每次只能将配置文件的值一次赋值给属性
 */
@ConfigurationProperties(prefix = "minio")
@Configuration
//@Data
public class MinIoProperties {
    /**
     * minio地址
     */
    @Value ("${minio.url}")
    private String url;

    /**
     * minio用户名
     */
    @Value ("${minio.accessKey}")
    private String accessKey;

    /**
     * minio密码
     */
    @Value ("${minio.secretKey}")
    private String secretKey;
    /**
     * 文件桶的名称
     */
    @Value ("${minio.bucketName}")
    private String bucketName;

    public String getUrl() {
        return url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }
}
