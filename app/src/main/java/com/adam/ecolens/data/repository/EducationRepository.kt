package com.adam.ecolens.data.repository

import com.adam.ecolens.data.model.EncyclopediaItem
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
                        "🌱 Cara Pengolahan:\n" +
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
                        "♻️ Cara Pengolahan:\n" +
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
                        "⚠️ Cara Pengolahan:\n" +
                        "1. Masukkan ke wadah khusus tempat sampah warna MERAH.\n" +
                        "2. JANGAN dibakar atau dibuang ke sungai!\n" +
                        "3. Serahkan ke TPS B3 / Drop Point E-Waste terdekat.",
                iconEmoji = "🔋"
            ),
            EncyclopediaItem(
                id = "enc_4",
                category = WasteCategory.ANORGANIK, // Atau kategori khusus edukasi/3R jika ada
                title = "Konsep 3R: Reduce (Mengurangi)",
                shortDesc = "Langkah awal mencegah timbulnya sampah dari aktivitas harian.",
                fullContent = "Reduce berarti mengurangi penggunaan barang-barang yang berpotensi menjadi sampah. Ini adalah langkah terbaik dan paling efektif dalam pengelolaan sampah karena mencegah sampah tercipta sejak awal.\n\n" +
                        "📉 Contoh Aksi Reduce:\n" +
                        "1. Membawa kantong belanja kain sendiri saat berbelanja.\n" +
                        "2. Membawa botol minum (tumbler) dan wadah makan sendiri.\n" +
                        "3. Menolak penggunaan sedotan plastik atau kantong sekali pakai.\n" +
                        "4. Membeli barang sesuai kebutuhan untuk menghindari sampah sisa makanan/barang.",
                iconEmoji = "🛑"
            ),
            EncyclopediaItem(
                id = "enc_5",
                category = WasteCategory.ANORGANIK,
                title = "Konsep 3R: Reuse (Menggunakan Kembali)",
                shortDesc = "Memanfaatkan kembali barang yang masih layak tanpa proses olah balik.",
                fullContent = "Reuse adalah kegiatan menggunakan kembali barang-barang bekas tanpa perlu mengubah bentuk atau memprosesnya secara industri. Dengan memanfaatkan barang secara berulang, kita memperpanjang umur pakai barang tersebut.\n\n" +
                        "🔄 Contoh Aksi Reuse:\n" +
                        "1. Menggunakan kembali botol/baskom bekas untuk pot tanaman.\n" +
                        "2. Menggunakan botol kaca sisa selai sebagai tempat bumbu dapur.\n" +
                        "3. Menyumbangkan baju bekas layak pakai atau buku bacaan yang tidak terpakai.\n" +
                        "4. Memanfaat kardus bekas menjadi kotak penyimpanan di rumah.",
                iconEmoji = "🔄"
            ),
            EncyclopediaItem(
                id = "enc_6",
                category = WasteCategory.ANORGANIK,
                title = "Konsep 3R: Recycle (Mendaur Ulang)",
                shortDesc = "Mengolah sampah menjadi produk baru yang berguna.",
                fullContent = "Recycle adalah proses mengolah kembali sampah/bahan bekas menjadi barang atau produk baru yang memiliki nilai guna. Biasanya melibatkan proses pemilahan, pembersihan, penghancuran, dan pencetakan ulang.\n\n" +
                        "♻️ Contoh Aksi Recycle:\n" +
                        "1. Mengolah sisa makanan & daun kering menjadi pupuk kompos (Recycle Organik).\n" +
                        "2. Mengirim botol plastik bekas ke Bank Sampah untuk dilebur jadi biji plastik.\n" +
                        "3. Mendaur ulang kertas bekas menjadi kertas daur ulang atau kerajinan tangan.\n" +
                        "4. Peleburan kaleng aluminium bekas menjadi kemasan baru.",
                iconEmoji = "♻️"
            )
        )
    }
}
