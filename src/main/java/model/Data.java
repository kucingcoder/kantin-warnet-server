/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;

/**
 *
 * @author Hanif
 */
public class Data implements Serializable{
    private final String status;
    private final String kategori;
    private final Object data;
    private final String sesi;

    public Data(String status, String kategori, Object data, String sesi) {
        this.status = status;
        this.kategori = kategori;
        this.data = data;
        this.sesi = sesi;
    }

    public String getStatus() {
        return status;
    }

    public String getKategori() {
        return kategori;
    }

    public Object getData() {
        return data;
    }

    public String getSesi() {
        return sesi;
    }
}
