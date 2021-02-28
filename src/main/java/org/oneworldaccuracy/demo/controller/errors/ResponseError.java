package org.oneworldaccuracy.demo.controller.errors;

public class ResponseError {
    private String detail;
    private int status;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ResponseError detail(String detail) {
        this.detail = detail;
        return this;
    }

    public ResponseError status(int status) {
        this.status = status;
        return this;
    }
}
