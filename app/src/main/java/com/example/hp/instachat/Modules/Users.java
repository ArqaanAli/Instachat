package com.example.hp.instachat.Modules;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users {

    public String name;
    public String image;
    public String status;
    public String thumb_image;
    public String phone;





    public Users(){}


    public Users(String name, String image, String status, String thumb_image,String phone) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image ;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getPhone() {
        return phone; }

    public void setPhone(String phone) {
        this.phone = phone;
    }




}
