package com.storemate.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Pengelola foto produk.
 * Database hanya menyimpan PATH teks; file foto disimpan di folder "images/"
 * (relatif terhadap folder kerja aplikasi = folder project saat dijalankan dari NetBeans).
 */
public final class ImageUtil {
    private static final File IMAGE_DIR = new File("images");
    private static final Map<String, ImageIcon> CACHE = new HashMap<>();   // agar grid kasir cepat

    private ImageUtil() { }

    /** Baca file gambar, kecilkan (sisi terpanjang <= max px), ubah ke JPEG. */
    public static byte[] toJpegBytes(File f, int max) throws IOException {
        BufferedImage src = ImageIO.read(f);
        if (src == null) throw new IOException("File bukan gambar yang valid (gunakan JPG/PNG/GIF).");
        double s = Math.min(1.0, (double) max / Math.max(src.getWidth(), src.getHeight()));
        int w = Math.max(1, (int) (src.getWidth() * s)), h = Math.max(1, (int) (src.getHeight() * s));
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, w, h);                   // latar putih utk PNG transparan
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(out, "jpg", bos);
        return bos.toByteArray();
    }

    /** Tulis foto ke folder images/ dan kembalikan path relatif untuk disimpan di database. */
    public static String store(byte[] jpeg, String productCode) throws IOException {
        if (!IMAGE_DIR.exists() && !IMAGE_DIR.mkdirs()) throw new IOException("Tidak dapat membuat folder images/.");
        String safe = productCode.replaceAll("[^A-Za-z0-9_-]", "_");
        String rel = "images/" + safe + "_" + System.currentTimeMillis() + ".jpg";   // nama unik tiap ganti foto
        try (FileOutputStream out = new FileOutputStream(new File(rel))) { out.write(jpeg); }
        return rel;
    }

    /** Hapus file foto (diam-diam bila tidak ada). */
    public static void delete(String path) {
        if (path == null || path.isEmpty()) return;
        new File(path).delete();
        CACHE.clear();
    }

    /** Ikon dari file pada path; null / file hilang -> placeholder "Tanpa Foto". */
    public static ImageIcon iconPath(String path, int size) {
        if (path == null || path.isEmpty()) return placeholder(size);
        String key = path + "@" + size;
        ImageIcon cached = CACHE.get(key);
        if (cached != null) return cached;
        File f = new File(path);
        ImageIcon icon = null;
        try {
            if (f.exists()) icon = fit(ImageIO.read(f), size);
        } catch (IOException ignored) { }
        if (icon == null) return placeholder(size);                         // file hilang/rusak
        CACHE.put(key, icon);
        return icon;
    }

    /** Ikon dari data di memori (untuk pratinjau sebelum disimpan). */
    public static ImageIcon iconBytes(byte[] data, int size) {
        try {
            if (data != null) {
                ImageIcon i = fit(ImageIO.read(new ByteArrayInputStream(data)), size);
                if (i != null) return i;
            }
        } catch (IOException ignored) { }
        return placeholder(size);
    }

    private static ImageIcon fit(BufferedImage img, int size) {
        if (img == null) return null;
        double s = Math.min((double) size / img.getWidth(), (double) size / img.getHeight());
        int w = Math.max(1, (int) (img.getWidth() * s)), h = Math.max(1, (int) (img.getHeight() * s));
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    private static ImageIcon placeholder(int size) {
        BufferedImage b = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = b.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0xECEFF1)); g.fillRect(0, 0, size, size);
        g.setColor(new Color(0x90A4AE));
        g.setFont(new Font("SansSerif", Font.PLAIN, Math.max(9, size / 7)));
        String t = "Tanpa Foto";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(t, (size - fm.stringWidth(t)) / 2, size / 2 + fm.getAscent() / 3);
        g.dispose();
        return new ImageIcon(b);
    }
}
