
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;
import org.json.JSONArray;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author USER
 */
public class CekCuacaFrame extends javax.swing.JFrame {
    
    private void cekCuaca() {
        String lokasi = (String) boxLokasi.getSelectedItem();
        if (lokasi == null || lokasi.trim().isEmpty()) {
        lblHasil.setText("Pilih kota terlebih dahulu.");
        return;
        }

        String apiKey = "d008df889cbedb42812d4d1ed6642741";
        String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" + lokasi + "&appid=" + apiKey;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) { // Periksa status HTTP
                lblHasil.setText("Kota tidak ditemukan atau API gagal.");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parsing JSON untuk menampilkan data cuaca
            String cuaca = HasilCuaca(response.toString());
            lblHasil.setText("Cuaca di " + lokasi + ": " + cuaca);
        } catch (Exception ex) {
            lblHasil.setText("Terjadi kesalahan saat mengambil data cuaca.");
            ex.printStackTrace();
        }
    }
    
    private String HasilCuaca(String jsonResponse) {
        try {
        org.json.JSONObject obj = new org.json.JSONObject(jsonResponse); 
        String kondisi = obj.getJSONArray("weather").getJSONObject(0).getString("description");
        double suhu = obj.getJSONObject("main").getDouble("temp") - 273.15; // Mengkonversi Kelvin ke Celsius
        int kelembapan = obj.getJSONObject("main").getInt("humidity");
        double angin = obj.getJSONObject("wind").getDouble("speed");

        return String.format("Kondisi: %s, Suhu: %.2f°C, Kelembapan: %d%%, Angin: %.2f m/s",
                             kondisi, suhu, kelembapan, angin);
        } catch (Exception e) {
        e.printStackTrace();
        return "Tidak diketahui";
        }
    }

    
    private boolean KotaSudahAda(String kota) {
        for (int i = 0; i < boxLokasi.getItemCount(); i++) {
            if (boxLokasi.getItemAt(i).equalsIgnoreCase(kota)) {
                return true; // Kota ditemukan
            }
        }
        return false; // Kota belum ada
    }
    
    private void tambahKota() {
         String kotaBaru = txtKota.getText().trim(); // Ambil teks dari txtKota
    if (!kotaBaru.isEmpty() && !KotaSudahAda(kotaBaru)) { // Validasi
        boxLokasi.addItem(kotaBaru); // Tambahkan kota ke ComboBox
        txtKota.setText(""); // Kosongkan input teks
        JOptionPane.showMessageDialog(this, "Kota berhasil ditambahkan!");
    } else {
        JOptionPane.showMessageDialog(this, "Kota sudah ada atau nama kosong.");
    }
    }
    
    private void resetForm() {
    txtKota.setText(""); 
    lblHasil.setText("Hasil"); 
    if (boxLokasi.getItemCount() > 0) {
        boxLokasi.setSelectedIndex(0); 
    }
}
    private void simpanFavorit() {
    try (FileWriter writer = new FileWriter("favorit_kota.txt", true)) {
        for (int i = 0; i < boxLokasi.getItemCount(); i++) {
            writer.write(boxLokasi.getItemAt(i) + "\n");
        }
        JOptionPane.showMessageDialog(this, "Kota favorit berhasil disimpan ke favorit.");
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan kota favorit.");
        e.printStackTrace();
    }
}
    private void muatFavorit() {
    try (BufferedReader reader = new BufferedReader(new FileReader("favorit_kota.txt"))) {
        String kota;
        while ((kota = reader.readLine()) != null) {
            if (!KotaSudahAda(kota)) {
                boxLokasi.addItem(kota);
            }
        }
    } catch (Exception e) {
        // File mungkin belum ada, tidak masalah
    }
}
    
   private void simpanFile(String lokasi, String dataCuaca) {
    try (FileWriter writer = new FileWriter("data_cuaca.csv", true)) {
        // Validasi jika dataCuaca belum lengkap
        if (dataCuaca == null || dataCuaca.equals("Hasil")) {
            JOptionPane.showMessageDialog(this, "Data cuaca belum tersedia untuk disimpan!");
            return;
        }

        // Proses split untuk memastikan format data sesuai
        String[] cuacaSplit = dataCuaca.split(", ");
        if (cuacaSplit.length >= 4) {
            String kondisi = cuacaSplit[0].split(": ")[1];
            String suhu = cuacaSplit[1].split(": ")[1];
            String kelembapan = cuacaSplit[2].split(": ")[1];
            String angin = cuacaSplit[3].split(": ")[1];

            // Simpan data ke file CSV
            writer.append(lokasi).append(",")
                  .append(kondisi).append(",")
                  .append(suhu).append(",")
                  .append(kelembapan).append(",")
                  .append(angin).append("\n");

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            Histori(); // Update histori setelah menyimpan
        } else {
            JOptionPane.showMessageDialog(this, "Format data cuaca tidak valid!");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan data.");
        e.printStackTrace();
    }
}


    private void Histori() {
    DefaultTableModel model = (DefaultTableModel) tableHistori.getModel();
    model.setRowCount(0); // Hapus data lama

    // Kolom pada tabel
    String[] kolom = {"Kota", "Kondisi", "Suhu (°C)", "Kelembapan (%)", "Kecepatan Angin (m/s)"};
    model.setColumnIdentifiers(kolom);

    try (BufferedReader reader = new BufferedReader(new FileReader("data_cuaca.csv"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(","); // Memisahkan kolom
            if (data.length == kolom.length) { 
                model.addRow(data); 
            } else {
                System.err.println("Format data salah: " + line); // Debugging jika ada data salah
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat histori.");
        e.printStackTrace();
    }
}



    



    /**
     * Creates new form CekCuacaFrame
     */
    public CekCuacaFrame() {
        initComponents();
        
        btnTambahKota.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            tambahKota(); // Memanggil metode tambahKota
        }
    });
        btnCekCuaca.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnCekCuacaActionPerformed(evt);
    }
    });
        btnReset.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetForm();
    }
    });
        btnSimpanFile.addActionListener(evt -> {
        String lokasi = (String) boxLokasi.getSelectedItem();
        String dataCuaca = lblHasil.getText();

        if (lokasi == null || lokasi.trim().isEmpty() || dataCuaca.equals("Hasil")) {
            JOptionPane.showMessageDialog(this, "Data belum lengkap untuk disimpan!");
            return;
        }

        simpanFile(lokasi, dataCuaca);
    });
        muatFavorit();
        Histori();
    }
    
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtKota = new javax.swing.JTextField();
        boxLokasi = new javax.swing.JComboBox<>();
        btnCekCuaca = new javax.swing.JButton();
        btnTambahKota = new javax.swing.JButton();
        lblHasil = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        btnFavorit = new javax.swing.JButton();
        btnKeluar = new javax.swing.JButton();
        btnSimpanFile = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableHistori = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(153, 204, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(51, 51, 255));
        jLabel1.setText("Aplikasi Cek Cuaca");
        jPanel1.add(jLabel1);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setBackground(new java.awt.Color(255, 255, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cek Cuaca Form", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel2.setText("Masukkan Nama Kota");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel3.setText("Pilih Kota");

        txtKota.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        boxLokasi.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnCekCuaca.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnCekCuaca.setText("Cek Cuaca");
        btnCekCuaca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekCuacaActionPerformed(evt);
            }
        });

        btnTambahKota.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnTambahKota.setText("Tambah Kota");
        btnTambahKota.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahKotaActionPerformed(evt);
            }
        });

        lblHasil.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblHasil.setText("Hasil");

        btnReset.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnFavorit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnFavorit.setText("Simpan Favorit");
        btnFavorit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFavoritActionPerformed(evt);
            }
        });

        btnKeluar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnKeluar.setText("Keluar");
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });

        btnSimpanFile.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnSimpanFile.setText("Simpan File");
        btnSimpanFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanFileActionPerformed(evt);
            }
        });

        tableHistori.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableHistori.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableHistoriMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tableHistori);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(240, 240, 240)
                        .addComponent(jLabel3)
                        .addGap(6, 6, 6)
                        .addComponent(boxLokasi, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(jLabel2)
                        .addGap(6, 6, 6)
                        .addComponent(txtKota, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(168, 168, 168)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 740, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnCekCuaca)
                                .addGap(18, 18, 18)
                                .addComponent(btnTambahKota)
                                .addGap(18, 18, 18)
                                .addComponent(btnFavorit)
                                .addGap(18, 18, 18)
                                .addComponent(btnKeluar)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblHasil)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(btnReset)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnSimpanFile)))))))
                .addContainerGap(380, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel2))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtKota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblHasil)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel3))
                    .addComponent(boxLokasi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(46, 46, 46)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCekCuaca)
                    .addComponent(btnTambahKota)
                    .addComponent(btnFavorit)
                    .addComponent(btnKeluar)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnReset)
                        .addComponent(btnSimpanFile)))
                .addGap(34, 34, 34)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCekCuacaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekCuacaActionPerformed
        cekCuaca();        // TODO add your handling code here:
    }//GEN-LAST:event_btnCekCuacaActionPerformed

    private void btnTambahKotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahKotaActionPerformed
        tambahKota();        // TODO add your handling code here:
    }//GEN-LAST:event_btnTambahKotaActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        resetForm();        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnFavoritActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFavoritActionPerformed
        simpanFavorit();        // TODO add your handling code here:
    }//GEN-LAST:event_btnFavoritActionPerformed

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        System.exit(0);        // TODO add your handling code here:
    }//GEN-LAST:event_btnKeluarActionPerformed

    private void btnSimpanFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanFileActionPerformed
        String lokasi = (String) boxLokasi.getSelectedItem();
        String dataCuaca = lblHasil.getText();

        if (lokasi == null || lokasi.trim().isEmpty() || dataCuaca.equals("Hasil")) {
            JOptionPane.showMessageDialog(this, "Data belum lengkap untuk disimpan!");
            return;
        }

        simpanFile(lokasi, dataCuaca);        // TODO add your handling code here:
    }//GEN-LAST:event_btnSimpanFileActionPerformed

    private void tableHistoriMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableHistoriMouseClicked
                // TODO add your handling code here:
    }//GEN-LAST:event_tableHistoriMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            CekCuacaFrame app = new CekCuacaFrame();
            app.setVisible(true);
        });
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CekCuacaFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CekCuacaFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> boxLokasi;
    private javax.swing.JButton btnCekCuaca;
    private javax.swing.JButton btnFavorit;
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSimpanFile;
    private javax.swing.JButton btnTambahKota;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblHasil;
    private javax.swing.JTable tableHistori;
    private javax.swing.JTextField txtKota;
    // End of variables declaration//GEN-END:variables
}
