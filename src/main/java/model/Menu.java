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
public class Menu implements Serializable{
    private int id;
    private final String nama;
    private final int harga;
    
    // ambil info dan edit menu
    public Menu(int id, String nama, int harga) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
    }

    // buat menu baru
    public Menu(String nama, int harga) {
        this.nama = nama;
        this.harga = harga;
    }

    public int getId() {
        return id;
    }
    
    public String getNama() {
        return nama;
    }

    public int getHarga() {
        return harga;
    }
}
