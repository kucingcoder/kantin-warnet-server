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
public class Pengguna implements Serializable{
    private int id;
    private String username, password;
    private final String nama, jenis;

    // ambil info pengguna
    public Pengguna(int id, String nama, String jenis) {
        this.id = id;
        this.nama = nama;
        this.jenis = jenis;
    }

    // buat pengguna baru
    public Pengguna(String username, String password, String nama, String jenis) {
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.jenis = jenis;
    }

    // edit penggguna
    public Pengguna(int id, String username, String password, String nama, String jenis) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.jenis = jenis;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNama() {
        return nama;
    }

    public String getJenis() {
        return jenis;
    }
}
