package com.adam.ecolens.data.model

import com.adam.ecolens.R

enum class WasteCategory(
    val id: String,
    val displayName: String,
    val colorResId: Int,
    val lightBgResId: Int,
    val description: String,
    val disposalTip: String,
    val examples: List<String>
) {
    ORGANIK(
        id = "organik",
        displayName = "Sampah Organik",
        colorResId = R.color.category_organik,
        lightBgResId = R.color.category_organik_light,
        description = "Sampah sisa makhluk hidup yang dapat membusuk dan terurai secara alami oleh tanah.",
        disposalTip = "Pisahkan ke tempat sampah HIJAU! Bisa diolah menjadi pupuk kompos untuk tanaman.",
        examples = listOf("Sisa makanan & sayur", "Daun & ranting kering", "Kulit buah-buahan", "Cangkang telur")
    ),
    ANORGANIK(
        id = "anorganik",
        displayName = "Sampah Anorganik",
        colorResId = R.color.category_anorganik,
        lightBgResId = R.color.category_anorganik_light,
        description = "Sampah non-hayati dari bahan buatan manusia yang membutuhkan waktu sangat lama untuk terurai.",
        disposalTip = "Pisahkan ke tempat sampah KUNING/BIRU! Bersihkan lalu kumpulkan untuk didaur ulang (recycle).",
        examples = listOf("Botol & kemasan plastik", "Kaleng minuman metal", "Kardus & kertas bekas", "Kaca & sedotan")
    ),
    B3(
        id = "b3",
        displayName = "Sampah B3 (Berbahaya & Beracun)",
        colorResId = R.color.category_b3,
        lightBgResId = R.color.category_b3_light,
        description = "Sampah yang mengandung bahan kimia beracun, berbahaya bagi manusia dan lingkungan jika dibuang sembarangan.",
        disposalTip = "Pisahkan ke tempat sampah MERAH! Jangan dicampur dengan sampah lain dan serahkan ke drop-point e-waste.",
        examples = listOf("Baterai bekas", "Lampu neon pecah", "Kabel & e-waste", "Kemasan obat/pembersih")
    );

    companion object {
        fun fromLabel(label: String): WasteCategory {
            return when (label.lowercase().trim()) {
                "organik" -> ORGANIK
                "anorganik" -> ANORGANIK
                "b3" -> B3
                else -> ANORGANIK
            }
        }
    }
}
