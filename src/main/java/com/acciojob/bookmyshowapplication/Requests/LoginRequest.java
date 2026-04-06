package com.acciojob.bookmyshowapplication.Requests;


public class LoginRequest {
    private String mobNo;
    private String password;

    public String getMobNo() {
        return mobNo;
    }

    public void setMobNo(String mobNo) {
        this.mobNo = mobNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
