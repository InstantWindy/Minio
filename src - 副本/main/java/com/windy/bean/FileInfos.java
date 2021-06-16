package com.windy.beans;

import java.time.ZonedDateTime;
import java.util.Date;

public class FileInfos {
    private ZonedDateTime createTime;
    private String name;
    private String size;

    public FileInfos() {
    }

    public FileInfos
            (ZonedDateTime createTime, String name, String size) {
        this.createTime = createTime;
        this.name = name;
        this.size = size;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "{" +
                "createTime:" + createTime +
                ", name:'" + name + '\'' +
                ", size:'" + size + '\'' +
                '}';
    }
}