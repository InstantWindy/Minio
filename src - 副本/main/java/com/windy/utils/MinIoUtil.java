package com.windy.utils;
import com.windy.beans.FileInfos;
import com.windy.config.MinIoProperties;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MinIoUtil {
    @Autowired  // 自动导入依赖的bean。
            MinIoProperties minIoProperties ;
    private static MinioClient minioClient;


    /**
     * 初始化minio配置
     *
     * @param :
     * @return: void
     * @date : 2020/8/16 20:56
     */
    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder().endpoint(minIoProperties.getUrl()).
                    credentials(minIoProperties.getAccessKey(),
                            minIoProperties.getSecretKey())
                    .build();

            createBucket(minIoProperties.getBucketName());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("初始化minio配置异常: 【{}】", e.fillInStackTrace());
        }
    }

    /**
     * 判断 bucket是否存在
     *
     * @param bucketName:
     *            桶名
     * @return: boolean
     * @date : 2020/8/16 20:53
     */
    @SneakyThrows(Exception.class)
    public static boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(bucketName);
    }

    /**
     * 创建 bucket
     *
     * @param bucketName:
     *            桶名
     * @return: void
     * @date : 2020/8/16 20:53
     */
    @SneakyThrows(Exception.class)
    public static void createBucket(String bucketName) throws Exception {
        boolean isExist = minioClient.bucketExists(bucketName);
        if (!isExist) {
            minioClient.makeBucket(bucketName);
        }
    }

    /**
     * 获取全部bucket
     *
     * @param :
     * @return: java.util.List<io.minio.messages.Bucket>
     * @date : 2020/8/16 23:28
     */
    @SneakyThrows(Exception.class)
    public static List<Bucket> getAllBuckets()  throws Exception{
        return minioClient.listBuckets();
    }

    /**
     * 文件上传
     *
     * @param bucketName:
     *            桶名
     * @param fileName:
     *            文件名
     * @param filePath:
     *            文件路径
     * @return: void
     * @date : 2020/8/16 20:53
     */
    @SneakyThrows(Exception.class)
    public static void upload(String bucketName, String fileName, String filePath) throws Exception {
        minioClient.putObject(bucketName, fileName, filePath, null);
    }

    /**
     * 文件上传
     *
     * @param bucketName:
     *            桶名
     * @param fileName:
     *            文件名
     * @param stream:
     *            文件流
     * @return: java.lang.String : 文件url地址
     * @date : 2020/8/16 23:40
     */
    @SneakyThrows(Exception.class)
    public static String upload(String bucketName, String fileName, InputStream stream) throws Exception {
        minioClient.putObject(bucketName, fileName, stream, new PutObjectOptions(stream.available(), -1));
        return getFileUrl(bucketName, fileName);
    }

    /**
     * 文件上传
     *
     * @param bucketName:
     *            桶名
     * @param file:
     *            文件
     * @return: java.lang.String : 文件url地址
     * @date : 2020/8/16 23:40
     */
    @SneakyThrows(Exception.class)
    public static String upload(String bucketName, MultipartFile file) throws Exception {
        final InputStream is = file.getInputStream();
        final String fileName = file.getOriginalFilename();
        minioClient.putObject(bucketName, fileName, is, new PutObjectOptions(is.available(), -1));
        is.close();
        return getFileUrl(bucketName, fileName);
    }

    /**
     * 删除文件
     *
     * @param bucketName:
     *            桶名
     * @param fileName:
     *            文件名
     * @return: void
     * @date : 2020/8/16 20:53
     */
    @SneakyThrows(Exception.class)
    public static void deleteFile(String bucketName, String fileName) throws Exception {
        minioClient.removeObject(bucketName, fileName);
    }

    /**
     * 下载文件
     *
     * @param bucketName:
     *            桶名
     * @param fileName:
     *            文件名
     * @param response:
     * @return: void
     * @date : 2020/8/17 0:34
     */
    @SneakyThrows(Exception.class)
    public static void download(String bucketName, String fileName, HttpServletResponse response) throws Exception {
        // 获取对象的元数据
        final ObjectStat stat = minioClient.statObject(bucketName, fileName);
        response.setContentType(stat.contentType());
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        InputStream is = minioClient.getObject(bucketName, fileName);
        IOUtils.copy(is, response.getOutputStream());
        is.close();
    }

    /**
     * 下载文件并保存到本地
     *
     * @param bucketName String 存储桶名称
     * @param objectName String 存储桶里的对象名称
     * @param filePath   String 指定下载的本地文件夹路径
     * @return
     */
    public static String downloadFile(String bucketName, String objectName, String filePath) {
        String downloadFileTips = "";
        try {
            // 判断下载的objectName在存储桶中是否存在
            if (minioClient.statObject(bucketName, objectName) != null) {
                File file = new File(filePath);
                if (!file.exists())
                    file.mkdir();
                minioClient.getObject(bucketName, objectName, filePath + "/" + objectName);
                downloadFileTips = objectName + " 下载成功!";
            } else {
                downloadFileTips = objectName + " 文件不存在";
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
        }
        return downloadFileTips;
    }

    /**
     * 获取minio文件的下载地址
     *
     * @param bucketName:
     *            桶名
     * @param fileName:
     *            文件名
     * @return: java.lang.String
     * @date : 2020/8/16 22:07
     */
    @SneakyThrows(Exception.class)
    public static String getFileUrl(String bucketName, String fileName) throws Exception {
        return minioClient.presignedGetObject(bucketName, fileName);
    }

    /**
     * 获取存储桶中的所有文件
     *
     * @param bucketName String 存储桶名称
     * @return
     */
    public static void getAllFiles(String bucketName) {
        try {
            // 先判断存储桶和文件对象是否存在
            boolean isExist = minioClient.bucketExists(bucketName);
            if (isExist) {
                Iterable<Result<Item>> myObjects = minioClient.listObjects(bucketName);
                for (Result<Item> result : myObjects) {
                    Item item = result.get();
//                    System.out.println(getFileUrl(bucketName,item.objectName()));
                }
            } else {
                System.out.println("=========");
            }
        } catch (Exception e) {
            System.out.println("Error occured: " + e);
        }

    }


    /**
     * 列出存储桶中的所有文件
     * bucketName	String	存储桶名称。
     *
     * @return
     */
    public static ArrayList<FileInfos> getFileLists(String bucketName) throws Exception{
        ArrayList<FileInfos> fileLists = new ArrayList<>();

        try {
            boolean isExist = minioClient.bucketExists(bucketName);
            if (isExist) {
                Iterable<Result<Item>> allFiles = minioClient.listObjects(bucketName);
                for (Result<Item> fileItem : allFiles) {
                    Item item = fileItem.get();
                    //对中文进行编码解码
                    String filename = new String(item.objectName().getBytes("UTF-8"),
                            "gbk");
                    System.out.println("filename="+filename);
                    FileInfos fileInfoIem = new FileInfos(item.lastModified(),
//                            URLEncoder.encode(item.objectName(), "UTF-8"),
                            filename,
                            getSize(item.size()));
                    fileLists.add(fileInfoIem);
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
        }
        return fileLists;
    }


    public static String getSize(long size) {
        if (size >= 1024 * 1024 * 1024) {
            return new Double(size / 1073741824L) + "G";
        } else if (size >= 1024 * 1024) {
            return new Double(size / 1048576L) + "M";
        } else if (size >= 1024) {
            return new Double(size / 1024) + "K";
        } else
            return size + "B";
    }
}
