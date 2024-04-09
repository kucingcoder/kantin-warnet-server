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
public class Pesanan implements Serializable{
    private int id, harga;
    private String nama_pengguna, nama_menu;
    final private int id_pengguna, id_menu;
    private String catatan, status;

    // ambil info pesanan
    public Pesanan(int id, int harga, String nama_pengguna, String nama_menu, int id_pengguna, int id_menu, String catatan, String status) {
        this.id = id;
        this.harga = harga;
        this.nama_pengguna = nama_pengguna;
        this.nama_menu = nama_menu;
        this.id_pengguna = id_pengguna;
        this.id_menu = id_menu;
        this.catatan = catatan;
        this.status = status;
    }

    // buat pesanan baru
    public Pesanan(int id_pengguna, int id_menu, String catatan) {
        this.id_pengguna = id_pengguna;
        this.id_menu = id_menu;
        this.catatan = catatan;
    }

    // edit pesanan
    public Pesanan(int id, int id_pengguna, int id_menu, String status) {
        this.id = id;
        this.id_pengguna = id_pengguna;
        this.id_menu = id_menu;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getHarga() {
        return harga;
    }

    public String getNama_pengguna() {
        return nama_pengguna;
    }

    public String getNama_menu() {
        return nama_menu;
    }

    public int getId_pengguna() {
        return id_pengguna;
    }

    public int getId_menu() {
        return id_menu;
    }

    public String getCatatan() {
        return catatan;
    }

    public String getStatus() {
        return status;
    }
}
