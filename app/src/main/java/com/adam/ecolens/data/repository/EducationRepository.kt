package com.adam.ecolens.data.repository

import com.adam.ecolens.data.model.EncyclopediaItem
import com.adam.ecolens.data.model.NewsItem
import com.adam.ecolens.data.model.WasteCategory

class EducationRepository {

    fun getEncyclopediaItems(): List<EncyclopediaItem> {
        return listOf(
            EncyclopediaItem(
                id = "enc_1",
                category = WasteCategory.ORGANIK,
                title = "Mengenal Sampah Organik",
                shortDesc = "Sampah sisa tumbuhan & makanan yang bisa membusuk alami.",
                fullContent = "Sampah organik berasal dari sisa-sisa makhluk hidup seperti makanan, sayuran, buah-buahan, dan daun kering. Jenis sampah ini mudah membusuk secara alami oleh bantuan mikroorganisme tanah.\n\n" +
                        "🌱 **Cara Pengolahan:**\n" +
                        "1. Pisahkan ke tempat sampah warna HIJAU.\n" +
                        "2. Olah menjadi pupuk kompos untuk tanaman.\n" +
                        "3. Gunakan komposter sederhana di rumah atau sekolah.",
                iconEmoji = "🌱"
            ),
            EncyclopediaItem(
                id = "enc_2",
                category = WasteCategory.ANORGANIK,
                title = "Mengenal Sampah Anorganik",
                shortDesc = "Sampah buatan manusia (plastik, kaleng, kaca) yang sulit diurai.",
                fullContent = "Sampah anorganik dihasilkan dari proses industri buatan manusia seperti botol plastik, kaleng minuman, wadah stirofoam, dan sedotan. Sampah ini tidak dapat membusuk atau membutuhkan ratusan tahun untuk terurai.\n\n" +
                        "♻️ **Cara Pengolahan:**\n" +
                        "1. Kumpulkan di wadah sampah warna KUNING atau BIRU.\n" +
                        "2. Bersihkan sisa minuman/makanan di wadah sebelum disimpan.\n" +
                        "3. Kreasikan menjadi kerajinan (Reuse) atau kirim ke Bank Sampah (Recycle).",
                iconEmoji = "🍾"
            ),
            EncyclopediaItem(
                id = "enc_3",
                category = WasteCategory.B3,
                title = "Mengenal Sampah B3 (Bahaya & Beracun)",
                shortDesc = "Baterai, lampu, obat kadaluarsa yang berdampak buruk pada kesehatan.",
                fullContent = "Sampah B3 (Bahan Berbahaya dan Beracun) mengandung zat kimia berbahaya seperti raksa, timbal, dan asam yang dapat merusak sel tanah dan sumber air minum jika dibuang sembarangan.\n\n" +
                        "⚠️ **Cara Pengolahan:**\n" +
                        "1. Masukkan ke wadah khusus tempat sampah warna MERAH.\n" +
                        "2. JANGAN dibakar atau dibuang ke sungai!\n" +
                        "3. Serahkan ke TPS B3 / Drop Point E-Waste terdekat.",
                iconEmoji = "🔋"
            )
        )
    }

    fun getNewsItems(): List<NewsItem> {
        return listOf(
            NewsItem(
                id = "news_1",
                title = "Aksi Gerakan Pilah Sampah di Sekolah Dasar",
                excerpt = "Siswa kelas 5 antusias belajar memisahkan sampah organik dan anorganik di kantin sekolah.",
                date = "22 Juli 2026",
                readTime = "3 min baca",
                author = "Tim EcoLens"
            ),
            NewsItem(
                id = "news_2",
                title = "Bahaya Logam Baterai Bekas bagi Lingkungan",
                excerpt = "Mengapa kita tidak boleh membuang baterai bekas bersama sampah dapur biasa? Simak penjelasannya!",
                date = "18 Juli 2026",
                readTime = "4 min baca",
                author = "Edukasi Lingkungan"
            ),
            NewsItem(
                id = "news_3",
                title = "Kreatif! Daur Ulang Botol Plastik Menjadi Pot Bunga",
                excerpt = "Tips seru memanfaatkan botol bekas menjadi taman vertikal cantik di ruang kelas.",
                date = "10 Juli 2026",
                readTime = "5 min baca",
                author = "Klub Hijau"
            )
        )
    }
}
