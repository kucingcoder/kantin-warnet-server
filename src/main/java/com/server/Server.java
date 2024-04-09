/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import model.Menu;
import model.Autentikasi;
import model.Pengguna;
import model.Pesanan;
import model.Data;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.Instant;

/**
 *
 * @author Hanif
 */
public class Server {
    public static Connection koneksidb;
    public static HashMap<String, Pengguna> daftar_sesi = new HashMap<>();
    
    public static void main(String[] args){
        try {
            koneksidb = DriverManager.getConnection("jdbc:sqlite:database.db");
            
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream("kredensial.sesi"));
                daftar_sesi = (HashMap<String, Pengguna>) in.readObject();
                in.close();
                System.out.println("Daftar Sesi dimuat kembali");
            } catch (Exception e) {
                System.out.println("Daftar Sesi tidak dimuat kembali");
            }
        
            ServerSocket peladen = new ServerSocket(2024);
            
            System.out.println("Server Kantin Warnet berjalan | Siap");

            while (true) {
                Socket socket = peladen.accept();
                
                try {
                    Penanganan proses = new Penanganan(socket);
                    proses.start();
                } catch (Exception e) {
                    ObjectOutputStream keluaran = new ObjectOutputStream(socket.getOutputStream());
                    Data data = new Data("ditolak", "tidak diketahui", null, "");
                    keluaran.writeObject(data);
                    keluaran.flush();
                    keluaran.close();
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    
    private static String MD5(String password) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();

        StringBuilder md5_password = new StringBuilder();
        for (byte b : digest) { md5_password.append(String.format("%02x", b & 0xff)); }
        
        return  md5_password.toString();
    }
    
    private static class Penanganan extends Thread {
        private final Socket permintaan;
        private final ObjectInputStream masukan;
        private final ObjectOutputStream keluaran;

        public Penanganan(Socket permintaan) throws Exception {
            this.permintaan = permintaan;
            this.masukan = new ObjectInputStream(permintaan.getInputStream());
            this.keluaran = new ObjectOutputStream(permintaan.getOutputStream());
        }
        
        @Override
        public void run() {
            Data data = null;
            
            try {
                Data informasi = (Data) masukan.readObject();
                String sesi = informasi.getSesi();
                
                switch (informasi.getKategori()) {
                    case "masuk" -> {
                        Autentikasi masuk = (Autentikasi) informasi.getData();
                        
                        String username = masuk.getUsername();
                        String password = MD5(masuk.getPassword());
                        
                        Statement stm = koneksidb.createStatement();
                        ResultSet rs = stm.executeQuery("SELECT id, nama, jenis FROM pengguna WHERE username = '" + username + "' AND password = '" + password + "'");
                        
                        if (!rs.next()) {
                            data = new Data("gagal", "masuk", null, sesi);
                        } else {
                            int id = rs.getInt("id");
                            String nama = rs.getString("nama");
                            String jenis = rs.getString("jenis");
                            
                            Pengguna pengguna = new Pengguna(id, nama, jenis);
                            
                            sesi = MD5(Instant.now().toString());
                            
                            daftar_sesi.put(sesi, pengguna);
                            
                            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("kredensial.sesi", false));
                            out.writeObject(daftar_sesi);
                            out.close();
                            
                            data = new Data("berhasil", "masuk", pengguna, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " masuk lewat halaman masuk");
                        }
                    }
                    
                    case "cek sesi" -> {
                        if (!daftar_sesi.containsKey(sesi)){
                            data = new Data("ditolak", "cek sesi", null, "");
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            
                            data = new Data("berhasil", "cek sesi", daftar_sesi.get(sesi), sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " masuk lewat sesi");
                        }
                    }
                    
                    case "hapus sesi" -> {
                        if (!daftar_sesi.containsKey(sesi)){
                            data = new Data("ditolak", "cek sesi", null, "");
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            
                            daftar_sesi.remove(sesi);
                            
                            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("kredensial.sesi", false));
                            out.writeObject(daftar_sesi);
                            out.close();
                            
                            data = new Data("berhasil", "hapus sesi", null, "");
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " keluar");
                        }
                    }
                    
                    case "daftar pengguna" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "tambah pengguna", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            
                            Statement stm = koneksidb.createStatement();
                            ResultSet rs = stm.executeQuery("SELECT id, nama, jenis FROM pengguna");

                            ArrayList<Pengguna> daftar_pengguna = new ArrayList<>();
                            
                            while (rs.next()) {
                                daftar_pengguna.add(new Pengguna(rs.getInt("id"), rs.getString("nama"), rs.getString("jenis")));
                            }
                        
                            data = new Data("berhasil", "daftar pengguna", daftar_pengguna, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " mengambil daftar pengguna");
                        }
                    }
                    
                    case "tambah pengguna" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "tambah pengguna", null, sesi);
                        } else {
                            String nama_pengguna = daftar_sesi.get(sesi).getNama();
                            
                            Pengguna pengguna_baru = (Pengguna) informasi.getData();
                            
                            Statement stm = koneksidb.createStatement();
                            
                            String username, password, nama, jenis;
                            
                            username = pengguna_baru.getUsername();
                            password = MD5(pengguna_baru.getPassword());
                            nama = pengguna_baru.getNama();
                            jenis = pengguna_baru.getJenis();
                            
                            stm.executeUpdate("INSERT INTO pengguna (username, password, nama, jenis) VALUES ('" + username + "', '" + password + "', '" + nama + "', '" + jenis + "')");
                            
                            data = new Data("berhasil", "tambah pengguna", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama_pengguna + " menambahkan pengguna baru");
                        }
                    }
                    
                    case "edit pengguna" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "edit pengguna", null, sesi);
                        } else {
                            String nama_pengguna = daftar_sesi.get(sesi).getNama();
                            
                            Pengguna pengguna_lama = (Pengguna) informasi.getData();
                            
                            String username, password, nama, jenis;
                            
                            int id = pengguna_lama.getId();
                            
                            username = pengguna_lama.getUsername();
                            password = MD5(pengguna_lama.getPassword());
                            nama = pengguna_lama.getNama();
                            jenis = pengguna_lama.getJenis();
                            
                            Statement stm = koneksidb.createStatement();
                            
                            stm.executeUpdate("UPDATE pengguna SET username = '" + username + "', password = '" + password + "', nama = '" + nama + "', jenis = '" + jenis + "' WHERE id = " + id);
                            
                            data = new Data("berhasil", "edit pengguna", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama_pengguna + " mengedit salah satu pengguna");
                        }
                    }
                    
                    case "hapus pengguna" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "hapus pengguna", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            
                            int id = (int) informasi.getData();
                            
                            Statement stm = koneksidb.createStatement();
                            
                            stm.executeUpdate("DELETE FROM pengguna WHERE id = " + id);
                            
                            data = new Data("berhasil", "hapus pengguna", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " menghapus salah satu pengguna");
                        }
                    }
                    
                    case "daftar menu" -> {
                        if (!daftar_sesi.containsKey(sesi)) {
                            data = new Data("ditolak", "tambah menu", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            
                            Statement stm = koneksidb.createStatement();
                            ResultSet rs = stm.executeQuery("SELECT * FROM menu");

                            ArrayList<Menu> daftar_menu = new ArrayList<>();

                            while (rs.next()) {
                                daftar_menu.add(new Menu(rs.getInt("id"), rs.getString("nama"), rs.getInt("harga")));
                            }
                        
                            data = new Data("berhasil", "daftar menu", daftar_menu, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " mengambil daftar menu");
                        }
                    }
                    
                    case "tambah menu" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "tambah menu", null, sesi);
                        } else {
                            String nama_pengguna = daftar_sesi.get(sesi).getNama();
                            Menu menu_baru = (Menu) informasi.getData();
                            
                            String nama = menu_baru.getNama();
                            int harga = menu_baru.getHarga();
                            
                            Statement stm = koneksidb.createStatement();
                            
                            stm.executeUpdate("INSERT INTO menu (nama, harga) VALUES ('" + nama + "', " + harga + ")");
                            
                            data = new Data("berhasil", "tambah menu", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama_pengguna + " menambahkan menu baru");
                        }
                    }
                    
                    case "edit menu" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "edit menu", null, sesi);
                        } else {
                            String nama_pengguna = daftar_sesi.get(sesi).getNama();
                            Menu menu_lama = (Menu) informasi.getData();
                            
                            int id = menu_lama.getId();
                            String nama = menu_lama.getNama();
                            int harga = menu_lama.getHarga();
                            
                            Statement stm = koneksidb.createStatement();
                            
                            stm.executeUpdate("UPDATE menu SET nama = '" + nama + "', harga = " + harga + " WHERE id = " + id);
                            
                            data = new Data("berhasil", "edit menu", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama_pengguna + " mengedit salah satu menu");
                        }
                    }
                    
                    case "hapus menu" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "hapus menu", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            int id = (int) informasi.getData();
                            
                            Statement stm = koneksidb.createStatement();
                            
                            stm.executeUpdate("DELETE FROM menu WHERE id = " + id);
                            
                            data = new Data("berhasil", "hapus menu", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " menghapus salah satu menu");
                        }
                    }
                    
                    case "buat pesanan" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"pelanggan".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "buat pesanan", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            
                            ArrayList<Pesanan> daftar_pesanan = (ArrayList<Pesanan>) informasi.getData();
                            
                            for (Pesanan pesanan : daftar_pesanan) {
                                int id_pengguna = pesanan.getId_pengguna();
                                int id_menu = pesanan.getId_menu();
                                String catatan = pesanan.getCatatan();

                                Statement stm = koneksidb.createStatement();

                                stm.executeUpdate("INSERT INTO pesanan (id_pengguna, id_menu, catatan, status) VALUES (" + id_pengguna + ", " + id_menu + ", '" + catatan + "', 'Dipesan')");
                            }
                            
                            data = new Data("berhasil", "buat pesanan", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " memesan " + daftar_pesanan.size() + " menu");
                        }
                    }
                    
                    case "riwayat pesanan" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"pelanggan".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "riwayat pesanan", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                        
                            int id = (int) informasi.getData();

                            Statement stm = koneksidb.createStatement();
                            ResultSet rs = stm.executeQuery("SELECT pesanan.id, pesanan.id_pengguna, pesanan.id_menu, pengguna.nama AS 'nama_pengguna', menu.nama AS 'nama_menu', menu.harga, pesanan.catatan, pesanan.status FROM pesanan INNER JOIN menu ON pesanan.id_menu = menu.id INNER JOIN pengguna ON pesanan.id_pengguna = pengguna.id WHERE id_pengguna =  " + id);

                            ArrayList<Pesanan> daftar_pesanan = new ArrayList<>();

                            while (rs.next()) {
                                Pesanan pesanan =  new Pesanan(rs.getInt("id"), rs.getInt("harga"), rs.getString("nama_pengguna"), rs.getString("nama_menu"), rs.getInt("id_pengguna"), rs.getInt("id_menu"), rs.getString("catatan"), rs.getString("status"));
                                daftar_pesanan.add(pesanan);
                            }

                            data = new Data("berhasil", "riwayat pesanan", daftar_pesanan, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " mengambil riwayat pesanan miliknya");
                        }
                    }
                    
                    case "riwayat semua pesanan" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "riwayat semua pesanan", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                        
                            Statement stm = koneksidb.createStatement();
                            ResultSet rs = stm.executeQuery("SELECT pesanan.id, pesanan.id_pengguna, pesanan.id_menu, pengguna.nama AS 'nama_pengguna', menu.nama AS 'nama_menu', menu.harga, pesanan.catatan, pesanan.status FROM pesanan INNER JOIN menu ON pesanan.id_menu = menu.id INNER JOIN pengguna ON pesanan.id_pengguna = pengguna.id");

                            ArrayList<Pesanan> daftar_pesanan = new ArrayList<>();

                            while (rs.next()) {
                                Pesanan pesanan =  new Pesanan(rs.getInt("id"), rs.getInt("harga"), rs.getString("nama_pengguna"), rs.getString("nama_menu"), rs.getInt("id_pengguna"), rs.getInt("id_menu"), rs.getString("catatan"), rs.getString("status"));
                                daftar_pesanan.add(pesanan);
                            }

                            data = new Data("berhasil", "riwayat semua pesanan", daftar_pesanan, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " mengambil semua riwayat pesanan");
                        }
                    }
                    
                    case "update pesanan" -> {
                        if (!daftar_sesi.containsKey(sesi) || !"operator".equals(daftar_sesi.get(sesi).getJenis())) {
                            data = new Data("ditolak", "update pesanan", null, sesi);
                        } else {
                            String nama = daftar_sesi.get(sesi).getNama();
                            
                            Pesanan pesanan = (Pesanan) informasi.getData();
                            
                            int id, id_pengguna, id_menu;
                            String status;
                            
                            id = pesanan.getId();
                            id_pengguna = pesanan.getId_pengguna();
                            id_menu = pesanan.getId_menu();
                            status = pesanan.getStatus();
                            
                            Statement stm = koneksidb.createStatement();
                            
                            stm.executeUpdate("UPDATE pesanan SET status = '" + status + "' WHERE id = " + id + " AND id_pengguna = " + id_pengguna + " AND id_menu = " + id_menu);
                            
                            data = new Data("berhasil", "kehabisan pesanan", null, sesi);
                            System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| " + nama + " memperbaharui status salah satu pesanan menjadi " + status);
                        }
                    }
                    
                    default -> {
                        data = new Data("ditolak", "tidak diketahui", null, sesi);
                        System.out.println(permintaan.getInetAddress().getHostAddress() + "\t| aktivitas aneh terdeteksi");
                    } 
                }
                
                keluaran.writeObject(data);
                keluaran.flush();
                masukan.close();
                keluaran.close();
                permintaan.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}