package pers.liufushihai.panocamclient.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : 图像实体类
 */

public class ImageBean implements Serializable{

    private String uri;            //图像在本地的Uri
    private String descriptor;     //图像描述
    private Date date;             //图像拍摄日期

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ImageBean(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "ImageBean{" +
                "uri='" + uri + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", date=" + date +
                '}';
    }
}
