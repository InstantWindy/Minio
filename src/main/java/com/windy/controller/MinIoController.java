package com.windy.controller;
import com.github.pagehelper.PageHelper;
import com.windy.bean.BucketInfo;
import com.windy.bean.PageBean;
import com.windy.config.MinIoProperties;

import com.windy.utils.MinIoUtil;
import io.minio.messages.Bucket;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
//import sun.text.resources.cldr.FormatData;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
//@RestController
@RequestMapping("/minio")
@Api(tags = {"MinIO文件操作"}) // @Api 用在类上，说明该类的作用
public class MinIoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinIoController.class);
//    private static int num=0;
    @Autowired // 自动导入依赖的bean。
            MinIoProperties minIoProperties ;

    @RequestMapping("/uploadFile")
    public String uploadFile(){
        return "uploadFile";
    }


    @ApiOperation(value = "列处存储桶所有文件")
    @GetMapping(value = "/getFiles")
    @ResponseBody
    public ArrayList getFiles(@RequestParam("bucketName") String bucketName) throws Exception{
        return MinIoUtil.getFileLists(bucketName);
    }

    @ApiOperation(value = "获取分页数据")
    @GetMapping(value = "/getPageFiles")
    @ResponseBody
    public PageBean<com.windy.beans.FileInfos> getFileByPage(@RequestParam("currentPage")Integer currentPage,
                                                             @RequestParam("pageSize") Integer pageSize,
                                                             @RequestParam("bucketName") String bucketName){
        try{
        PageBean<com.windy.beans.FileInfos> entity = new PageBean();
        if (null != pageSize) {
            entity.setPageSize(pageSize);
        }
        if (null != currentPage) {
            entity.setCurrentPage(currentPage);
        }
        PageHelper.startPage(entity.getCurrentPage(), entity.getPageSize());
            //获取需要分页的所有数据
        List<com.windy.beans.FileInfos> fileLists = MinIoUtil.getFiles(bucketName,
                pageSize,0,currentPage,0);
            //获取分页的数据总数
        Integer count = fileLists.size();
            System.out.println("当前页面的文件数："+count);
        entity.setItems(fileLists);
        entity.setTotalNum(MinIoUtil.count);
        return entity;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
    }

    @ApiOperation(value = "获取存储桶的文件列表迭代器")
    @GetMapping(value = "/getIterator")
    @ResponseBody
    public Integer PageFile(@RequestParam("bucketName")String bucketName) throws Exception {
        return MinIoUtil.getIterator(bucketName);
//        System.out.println("调用PageFile");
    }

    @ApiOperation(value = "获取存储桶所有文件")
    @GetMapping(value = "/getAllFiles")
    public void getAllFiles(@RequestParam("bucketName") String bucketName) {
        MinIoUtil.getAllFiles(bucketName);
    }

    @ApiOperation(value = "上传文件")
    @PostMapping(value = "/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        System.out.println("upload file before");
        String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
        System.out.println("upload file after");
        LOGGER.info("上传成功");
        return "文件下载地址：" + fileUrl;
    }

//    @ApiOperation(value = "上传多文件")
//    @PostMapping(value = "/uploadFiles")
//    @ResponseBody
//    public String uploadFiles(HttpServletRequest request) throws Exception {
//        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
//        // 本地上传文件路径
////        String filePath = "E:/temptest/";
//        for (int i = 0; i < files.size(); i++) {
//            MultipartFile file = files.get(i);
//            if (file.isEmpty()) {
//                System.out.println("未找到附件。。。");
//                System.out.println("上传第" + (++i) + "个文件失败");
//                i--; // 需要判断下一个附件是否可以进行上传，把上一行+1的下标减回去
//                continue;
//            }
//            try {
//                MinIoUtil.upload(minIoProperties.getBucketName(), file);
//                log.info("第" + (i + 1) + "个文件上传成功");
//            } catch (IOException e) {
//                log.error(e.toString(), e);
//                return "上传第" + (++i) + "个文件失败";
//            }
//        }
//        return "上传成功";
////        String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
////        return "文件下载地址：" + fileUrl;
//    }

//    @ApiOperation(value = "上传多文件")
//    @PostMapping(value = "/uploadFileLists")
//    @ResponseBody
//    public String uploadFiles( @RequestParam("bucketName") String bucketName,
//                               HttpServletRequest request) throws Exception {
//
//        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("files");
//        // 本地上传文件路径
//        for (int i = 0; i < files.size(); i++) {
//            MultipartFile file = files.get(i);
//            if (file.isEmpty()) {
//                System.out.println("未找到附件。。。");
//                System.out.println("上传第" + (++i) + "个文件失败");
//                i--; // 需要判断下一个附件是否可以进行上传，把上一行+1的下标减回去
//                continue;
//            }
//            try {
//                MinIoUtil.upload(bucketName, file);
//                log.info("第" + (i + 1) + "个文件上传成功");
//            } catch (IOException e) {
//                log.error(e.toString(), e);
//                return "上传第" + (++i) + "个文件失败";
//            }
//        }
//        return "上传成功";
////        String fileUrl = MinIoUtil.upload(minIoProperties.getBucketName(), file);
////        return "文件下载地址：" + fileUrl;
//    }


    @ApiOperation(value = "上传多文件")
    @PostMapping(value = "/uploadFileLists")
    @ResponseBody
    public String uploadFiles(@RequestParam("bucketName") String bucketName,
                              @RequestParam("files") MultipartFile[] files) throws Exception {

        for (int i = 0; i < files.length; i++) {
            MultipartFile file =  files[i];
            if (file.isEmpty()) {
                System.out.println("未找到附件。。。");
                System.out.println("上传第" + (++i) + "个文件失败");
                i--; // 需要判断下一个附件是否可以进行上传，把上一行+1的下标减回去
                continue;
            }
            try {
                MinIoUtil.upload(bucketName, file);
                log.info("第" + (i + 1) + "个文件上传成功");
            } catch (IOException e) {
                log.error(e.toString(), e);
                return "上传第" + (++i) + "个文件失败";
            }
        }
        return "上传成功";
    }


    @ApiOperation(value = "下载文件")
    @GetMapping(value = "/download")
    public void download(@RequestParam(value="fileName") String fileName, HttpServletResponse response) throws Exception{
        MinIoUtil.download(minIoProperties.getBucketName(), fileName, response);
    }

    @ApiOperation(value = "下载文件并保存到本地")
    @GetMapping(value = "/downloadandsave")
    @ResponseBody
    public String download(@RequestParam("objectName") String objectName,
                           @RequestParam("filePath")  String filePath) {
        String ss = MinIoUtil.downloadFile(minIoProperties.getBucketName(),objectName,filePath);
        System.out.println(ss);
        return ss;
//                MinIoUtil.downloadFile(minIoProperties.getBucketName(),objectName,filePath);
    }

    @ApiOperation(value = "删除文件")
    @GetMapping(value = "/delete")
    @ResponseBody
    public String delete(@RequestParam("fileName") String fileName) throws Exception {
        MinIoUtil.deleteFile(minIoProperties.getBucketName(), fileName);
        return "删除成功";
    }

    @ApiOperation(value = "获取文件下载地址")
    @GetMapping(value = "/url")
    @ResponseBody
    public String getUrl(String name,String bucket) throws Exception {
        return MinIoUtil.getFileUrl(bucket,name);
    }

    @ApiOperation(value = "获取所有存储桶")
    @GetMapping(value = "/getbuckets")
    @ResponseBody
    public List<BucketInfo> getBucktes()throws Exception{
        List<Bucket> buckets = MinIoUtil.getAllBuckets();
        List<BucketInfo>  bucketInfos = new ArrayList<>();
        int i=1;
        for (Bucket bucket:buckets){
            bucketInfos.add(new BucketInfo(bucket.name(),i));
            i+=1;
        }
        return bucketInfos;
    }
}
