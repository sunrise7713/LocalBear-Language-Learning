package com.rojdaurper.ingilizcemobil

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rojdaurper.ingilizcemobil.databinding.ActivityMainBinding
import com.rojdaurper.ingilizcemobil.databinding.DialogFilterBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private var wordList = mutableListOf<WordModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Veritabanı ve Liste Kurulumu
        db = Firebase.firestore
        recyclerView = binding.rvWords
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 2. MERKEZİ NAVİGASYON
        setupCommonNavigation(binding.bottomNavigation)

        // 3. Tıklama Dinleyicileri
        setupClickListeners()

        // 4. Veri Yükleme ve Header Başlatma
        loadFilteredData("A1", "Daily")
        updateStreakAndScore()
    }

    private fun setupClickListeners() {
        // Filtre Butonu
        binding.btnLocationFilter.setOnClickListener {
            showModernFilterDialog()
        }

        // Header - Uygulama Logosu Tıklama (LocalBear Mesajı)
        binding.headerInclude.ivAppLogoHeader.setOnClickListener {
            Toast.makeText(this, "LocalBear: Dil öğrenmek hiç bu kadar eğlenceli olmamıştı!", Toast.LENGTH_SHORT).show()
        }

        // Header - Puan Kabı Tıklama
        binding.headerInclude.scoreContainer.setOnClickListener {
            val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val currentScore = sharedPref.getInt("user_score", 0)
            Toast.makeText(this, "Toplam Puanın: $currentScore", Toast.LENGTH_SHORT).show()
        }
    }

    // --- FİLTRELEME VE UI GÜNCELLEME MANTIĞI ---

    private fun showModernFilterDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogFilterBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val selectedLevel = "A1"

        dialogBinding.etSearchCategory.setOnEditorActionListener { textView, actionId, _ ->
            val query = textView.text.toString().trim()
            if (actionId == EditorInfo.IME_ACTION_SEARCH && query.isNotEmpty()) {
                val formattedQuery = query.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                applyFilter(selectedLevel, formattedQuery, dialog)
                true
            } else false
        }

        dialogBinding.btnDaily.setOnClickListener { applyFilter(selectedLevel, "Daily", dialog) }
        dialogBinding.btnShopping.setOnClickListener { applyFilter(selectedLevel, "Shopping", dialog) }
        dialogBinding.btnAirport.setOnClickListener { applyFilter(selectedLevel, "Airport", dialog) }
        dialogBinding.btnHotel.setOnClickListener { applyFilter(selectedLevel, "Hotel", dialog) }
        dialogBinding.btnTravel.setOnClickListener { applyFilter(selectedLevel, "Travel", dialog) }

        dialog.show()
    }

    private fun applyFilter(level: String, category: String, dialog: BottomSheetDialog) {
        loadFilteredData(level, category)
        updateCategoryUI(category)
        dialog.dismiss()
    }

    private fun updateCategoryUI(category: String) {
        binding.tvCurrentLocation.text = category
        val iconRes = when (category) {
            "Daily" -> android.R.drawable.ic_menu_today
            "Shopping" -> android.R.drawable.ic_menu_manage
            "Airport" -> android.R.drawable.ic_menu_send
            "Hotel" -> android.R.drawable.ic_menu_view
            "Travel" -> android.R.drawable.ic_menu_directions
            else -> android.R.drawable.ic_menu_help
        }
        binding.ivCategoryIcon.setImageResource(iconRes)
    }

    private fun loadFilteredData(level: String, category: String) {
        db.collection("sentences")
            .whereEqualTo("level", level)
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                wordList.clear()
                for (document in documents) {
                    val word = document.toObject(WordModel::class.java)
                    wordList.add(word)
                }

                if (wordList.isEmpty()) {
                    Toast.makeText(this, "$category için henüz içerik yok.", Toast.LENGTH_SHORT).show()
                } else {
                    wordList.shuffle()
                    recyclerView.adapter = WordAdapter(wordList)
                    recyclerView.scrollToPosition(0)
                }
            }
    }

    // --- PUAN VE SERİ MANTIĞI ---

    private fun updateStreakAndScore() {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        val lastLoginDate = sharedPref.getString("last_login_date", "")
        var currentStreak = sharedPref.getInt("user_streak", 0)
        val currentScore = sharedPref.getInt("user_score", 0)

        if (lastLoginDate != today) {
            val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.let { sdf.format(it.time) }
            currentStreak = if (lastLoginDate == yesterday) currentStreak + 1 else 1
            editor.putString("last_login_date", today).putInt("user_streak", currentStreak).apply()
        }

        // Yeni Header ID'lerine göre UI Güncelleme
        binding.headerInclude.tvStreakTitle.text = "$currentStreak Day Streak"
        binding.headerInclude.tvUserScore.text = currentScore.toString()
        binding.headerInclude.headerScoreProgress.progress = currentScore % 100
        binding.headerInclude.headerStreakProgress.progress = (currentStreak * 10).coerceAtMost(100)

        // YENİ: 8/10 Today Metni (Puan bazlı örnek ilerleme)
        val dailyProgress = (currentScore / 10) % 11
        binding.headerInclude.tvTodayProgress.text = "$dailyProgress/10 Today"
    }

    fun addScore(points: Int) {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val newScore = sharedPref.getInt("user_score", 0) + points
        sharedPref.edit().putInt("user_score", newScore).apply()
        updateStreakAndScore()
    }

    override fun onResume() {
        super.onResume()
        updateStreakAndScore() // Diğer sayfalardan dönüldüğünde puanları tazele
    }
}