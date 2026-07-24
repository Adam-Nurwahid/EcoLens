package com.adam.ecolens.data.repository

import com.adam.ecolens.data.local.dao.QuizScoreDao
import com.adam.ecolens.data.local.dao.UserDao
import com.adam.ecolens.data.local.entity.QuizScoreEntity
import com.adam.ecolens.data.model.LeaderboardItem
import com.adam.ecolens.data.model.Question
import com.adam.ecolens.data.model.QuizLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class QuizRepository(
    private val quizScoreDao: QuizScoreDao,
    private val userDao: UserDao
) {

    private val sampleLevels = listOf(
        QuizLevel(
            levelId = 1,
            title = "Level 1: Organik & Anorganik",
            subtitle = "Pengenalan dasar memilah sampah sehari-hari",
            questions = listOf(
                Question(
                    id = 101,
                    questionText = "Manakah di bawah ini yang termasuk sampah ORGANIK?",
                    options = listOf("Botol Plastik", "Kulit Pisang", "Kaleng Minuman", "Kabel Bekas"),
                    correctAnswerIndex = 1,
                    explanation = "Kulit pisang adalah sisa makanan alami yang dapat membusuk dan terurai secara alami."
                ),
                Question(
                    id = 102,
                    questionText = "Tempat sampah berwarna HIJAU biasanya digunakan untuk menampung sampah...",
                    options = listOf("B3 Berbahaya", "Anorganik", "Organik", "Kertas Daur Ulang"),
                    correctAnswerIndex = 2,
                    explanation = "Warna hijau adalah standar tempat sampah untuk jenis sampah organik/sisa tanaman."
                ),
                Question(
                    id = 103,
                    questionText = "Mengapa botol plastik membutuhkan waktu ratusan tahun untuk terurai?",
                    options = listOf(
                        "Karena botol plastik terbuat dari bahan organik",
                        "Karena botol plastik terbuat dari bahan sintetis anorganik",
                        "Karena plastik sangat mudah terbakar",
                        "Karena plastik mengandung cangkang telur"
                    ),
                    correctAnswerIndex = 1,
                    explanation = "Plastik dibuat dari ikatan kimia minyak bumi (anorganik) yang sangat sulit diurai oleh bakteri."
                ),
                Question(
                    id = 104,
                    questionText = "Apa yang terjadi jika sampah organik diolah dengan benar?",
                    options = listOf(
                        "Menjadi racun berbahaya",
                        "Menjadi pupuk kompos untuk tanaman",
                        "Menjadi plastik baru",
                        "Menjadi baterai listrik"
                    ),
                    correctAnswerIndex = 1,
                    explanation = "Sampah organik hasil komposting menjadi pupuk alami yang menutrisi tanaman."
                ),
                Question(
                    id = 105,
                    questionText = "Sisa daun kering di halaman sekolah termasuk jenis sampah...",
                    options = listOf("B3 Beracun", "Organik", "Anorganik", "Radioaktif"),
                    correctAnswerIndex = 1,
                    explanation = "Daun berasal dari tumbuh-tumbuhan sehingga termasuk jenis organik."
                )
            )
        ),
        QuizLevel(
            levelId = 2,
            title = "Level 2: Area Berbahaya (Sampah B3)",
            subtitle = "Waspadai baterai, lampu, dan limbah berbahaya!",
            questions = listOf(
                Question(
                    id = 201,
                    questionText = "Sampah B3 merupakan singkatan dari...",
                    options = listOf(
                        "Bahan Bersih Berkelanjutan",
                        "Bahan Berbahaya dan Beracun",
                        "Barang Bekas Berharga",
                        "Bahan Baku Biologi"
                    ),
                    correctAnswerIndex = 1,
                    explanation = "B3 singkatan resmi dari Bahan Berbahaya dan Beracun."
                ),
                Question(
                    id = 202,
                    questionText = "Mengapa baterai bekas tidak boleh dibuang di tempat sampah biasa?",
                    options = listOf(
                        "Karena baterai bisa dimakan hewan",
                        "Karena mengandung cairan kimia beracun yang dapat mencemari air tanah",
                        "Karena baterai berwarna hitam",
                        "Karena baterai terbuat dari daun"
                    ),
                    correctAnswerIndex = 1,
                    explanation = "Baterai bekas mengandung logam berat (seperti raksa & timbal) yang beracun."
                ),
                Question(
                    id = 203,
                    questionText = "Tempat sampah berwarna MERAH dikhususkan untuk jenis sampah...",
                    options = listOf("Sampah Organik", "Sampah Anorganik", "Sampah B3 Berbahaya", "Kertas Bekas"),
                    correctAnswerIndex = 2,
                    explanation = "Warna merah menjadi lambang bahaya untuk penampungan sampah B3."
                ),
                Question(
                    id = 204,
                    questionText = "Berikut ini manakah benda yang tergolong sampah B3 (e-waste)?",
                    options = listOf("Kulit Apel", "Kardus Sepatu", "Lampu Neon Pecah", "Ranting Pohon"),
                    correctAnswerIndex = 2,
                    explanation = "Lampu neon mengandung gas raksa/merkuri yang berbahaya jika terhirup."
                ),
                Question(
                    id = 205,
                    questionText = "Tindakan paling tepat saat menemukan baterai bekas di rumah adalah...",
                    options = listOf(
                        "Membakarnya di kebun",
                        "Membuangnya ke sungai",
                        "Mengumpulkannya terpisah dan menyerahkan ke tempat pengolahan e-waste",
                        "Menguburnya dekat tanaman"
                    ),
                    correctAnswerIndex = 2,
                    explanation = "Pengumpulan terpisah mencegah kebocoran racun ke lingkungan sekitar."
                )
            )
        ),
        QuizLevel(
            levelId = 3,
            title = "Level 3: Pahlawan Daur Ulang",
            subtitle = "Pahami konsep 3R (Reduce, Reuse, Recycle)!",
            questions = listOf(
                Question(
                    id = 301,
                    questionText = "Prinsip 3R dalam pengelolaan sampah singkatan dari...",
                    options = listOf(
                        "Read, Run, Repeat",
                        "Reduce, Reuse, Recycle",
                        "Remove, Repair, Return",
                        "Refuse, Refill, Refresh"
                    ),
                    correctAnswerIndex = 1,
                    explanation = "3R singkatan dari Reduce (Mengurangi), Reuse (Guna Ulang), Recycle (Daur Ulang)."
                ),
                Question(
                    id = 302,
                    questionText = "Membawa tas kain sendiri saat berbelanja untuk mengurangi kantong plastik adalah contoh...",
                    options = listOf("Recycle", "Reduce", "Reuse", "Replant"),
                    correctAnswerIndex = 1,
                    explanation = "Reduce berarti tindakan mengurangi timbulan sampah sejak awal."
                ),
                Question(
                    id = 303,
                    questionText = "Mengubah kaleng bekas menjadi pot tanaman hias di kelas adalah contoh tindakan...",
                    options = listOf("Reuse", "Reduce", "Reboot", "React"),
                    correctAnswerIndex = 0,
                    explanation = "Reuse berarti menggunakan kembali barang bekas tanpa mengubah bentuk dasarnya."
                ),
                Question(
                    id = 304,
                    questionText = "Mengolah botol plastik bekas menjadi serat kain atau barang baru di pabrik dinamakan...",
                    options = listOf("Reduce", "Recycle", "Reuse", "Rebound"),
                    correctAnswerIndex = 1,
                    explanation = "Recycle berarti mendaur ulang sampah menjadi bahan atau produk baru."
                ),
                Question(
                    id = 305,
                    questionText = "Sebagai siswa sekolah dasar, peran terbaik kita menjaga lingkungan adalah...",
                    options = listOf(
                        "Membuang sampah di mana saja asal bersih",
                        "Memilah sampah sesuai wadahnya & mempraktikkan 3R",
                        "Membakar sampah di halaman sekolah",
                        "Membiarkan sampah menumpuk"
                    ),
                    correctAnswerIndex = 1,
                    explanation = "Memilah sampah sejak dari sumber adalah langkah pertama menjadi Pahlawan Lingkungan!"
                )
            )
        )
    )

    fun getLevelsFlow(username: String): Flow<List<QuizLevel>> {
        return combine(
            userDao.observeUser(username),
            quizScoreDao.getAllScoresByUsername(username)
        ) { user, scores ->
            val unlockedLevel = user?.unlockedLevel ?: 1
            val scoreMap = scores.associateBy { it.levelId }

            sampleLevels.map { level ->
                val userScore = scoreMap[level.levelId]
                level.copy(
                    isUnlocked = level.levelId <= unlockedLevel,
                    starsAchieved = userScore?.stars ?: 0
                )
            }
        }
    }

    fun getLevelById(levelId: Int): QuizLevel? {
        return sampleLevels.find { it.levelId == levelId }
    }

    suspend fun saveQuizResult(username: String, levelId: Int, score: Int): QuizScoreEntity = withContext(Dispatchers.IO) {
        val stars = when {
            score >= 90 -> 3
            score >= 70 -> 2
            score >= 50 -> 1
            else -> 0
        }

        val existingScore = quizScoreDao.getScoreByLevel(username, levelId)
        val scoreEntity = QuizScoreEntity(
            existingScore?.id ?: 0,
            username,
            levelId,
            maxOf(existingScore?.score ?: 0, score),
            maxOf(existingScore?.stars ?: 0, stars),
            System.currentTimeMillis()
        )
        quizScoreDao.insertOrUpdateScore(scoreEntity)

        // Award XP points based on score
        userDao.addPoints(username, score)

        // If score >= 70, unlock next level
        if (score >= 70 && levelId < 3) {
            userDao.updateUnlockedLevel(username, levelId + 1)
        }

        return@withContext scoreEntity
    }

    fun getLeaderboard(): Flow<List<LeaderboardItem>> {
        return userDao.getTopUsers().map { users ->
            users.mapIndexed { index, user ->
                LeaderboardItem(
                    rank = index + 1,
                    username = user.username,
                    fullName = user.fullName,
                    totalPoints = user.totalPoints,
                    currentLevel = user.unlockedLevel
                )
            }
        }
    }
}
