package com.rojdaurper.ingilizcemobil

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.rojdaurper.ingilizcemobil.databinding.ActivityLearnBinding
import com.rojdaurper.ingilizcemobil.databinding.DialogWhereAmIBinding
import com.rojdaurper.ingilizcemobil.databinding.DialogWordHunterBinding

class LearnActivity : BaseActivity() {

    private lateinit var binding: ActivityLearnBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCommonNavigation(binding.bottomNavigation)
        updateHeaderInfo()
        setupClickListeners()
    }

    // --- HEADER VE PUAN MANTIĞI ---

    private fun updateHeaderInfo() {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val score = sharedPref.getInt("user_score", 0)
        val streak = sharedPref.getInt("user_streak", 0)

        // 1. Temel Metinler ve Puanlar
        binding.headerInclude.tvUserScore.text = score.toString()
        binding.headerInclude.tvStreakTitle.text = "$streak Day Streak" // Tasarıma uygun İngilizce metin

        // 2. İlerleme Barları (Dairesel ve Yatay)
        binding.headerInclude.headerScoreProgress.progress = score % 100
        binding.headerInclude.headerStreakProgress.progress = (streak * 10).coerceAtMost(100)

        // 3. AKTİF: 8/10 Today Özelliği
        // Mantık: Her 10 puan bir soru sayılır. Maksimum 10 gösterir.
        val dailyProgress = (score / 10).coerceAtMost(10)
        binding.headerInclude.tvTodayProgress.text = "$dailyProgress/10 Today"
    }

    private fun addScore(points: Int) {
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentScore = sharedPref.getInt("user_score", 0)
        sharedPref.edit().putInt("user_score", currentScore + points).apply()

        // Puan eklendiği anda header'daki "8/10 Today" metni de dahil her şeyi tazele
        updateHeaderInfo()
    }

    private fun setupClickListeners() {
        binding.missionInclude.btnStartMission.setOnClickListener { startWhereAmIGame() }
        binding.gameInclude.btnStartGame?.setOnClickListener { startWordHunterGame() }

        // Logo tıklama etkileşimi
        binding.headerInclude.ivAppLogoHeader.setOnClickListener {
            Toast.makeText(this, "LocalBear: Harika gidiyorsun!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- OYUN 1: WHERE AM I? ---

    private fun startWhereAmIGame() {
        Toast.makeText(this, "Görev: 10 soruda en yüksek puanı topla!", Toast.LENGTH_SHORT).show()
        Firebase.firestore.collection("sentences").limit(30).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val randomWord = documents.documents.random().toObject(WordModel::class.java)
                randomWord?.let { showGameBottomSheet(it) }
            }
        }.addOnFailureListener { exception ->
            Log.e("GameError", "Veri çekme hatası: ", exception)
        }
    }

    private fun showGameBottomSheet(firstWord: WordModel) {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogWhereAmIBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        var questionCounter = 0
        val totalQuestions = 10
        var correctCount = 0

        fun loadNextQuestionIntoUI(word: WordModel) {
            questionCounter++
            if (questionCounter > totalQuestions) {
                finishMission(correctCount, totalQuestions, dialog)
                return
            }

            dialogBinding.tvQuestionCounter.text = "QUESTION $questionCounter/$totalQuestions"
            dialogBinding.tvQuestionSentence.text = word.eng

            val buttons = listOf(dialogBinding.btnOption1, dialogBinding.btnOption2, dialogBinding.btnOption3, dialogBinding.btnOption4)
            val categories = mutableListOf("Daily", "Shopping", "Airport", "Hotel", "Travel")

            buttons.forEach { btn ->
                val card = btn.parent as MaterialCardView
                card.setCardBackgroundColor(Color.parseColor("#2D231A"))
                card.setStrokeColor(Color.parseColor("#26FFFFFF"))
                btn.isEnabled = true
            }

            categories.remove(word.category)
            val finalOptions = (categories.shuffled().take(3) + word.category).shuffled()

            buttons.forEachIndexed { index, button ->
                button.text = finalOptions[index]
                button.setOnClickListener {
                    val selectedCardView = button.parent as MaterialCardView
                    buttons.forEach { it.isEnabled = false }

                    if (button.text.toString().equals(word.category, ignoreCase = true)) {
                        selectedCardView.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                        selectedCardView.setStrokeColor(Color.parseColor("#A5D6A7"))
                        addScore(10) // Her doğru cevap 10 Today ilerlemesine +1 ekler
                        correctCount++
                    } else {
                        selectedCardView.setCardBackgroundColor(Color.parseColor("#F44336"))
                        buttons.forEach { btn ->
                            if (btn.text.toString().equals(word.category, ignoreCase = true)) {
                                val correctCard = btn.parent as MaterialCardView
                                correctCard.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                            }
                        }
                    }

                    button.postDelayed({
                        fetchAnotherRandomWord { nextWord -> loadNextQuestionIntoUI(nextWord) }
                    }, 1500)
                }
            }
        }
        loadNextQuestionIntoUI(firstWord)
        dialog.show()
    }

    // --- OYUN 2: WORD HUNTER ---

    private fun startWordHunterGame() {
        Firebase.firestore.collection("sentences").limit(30).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val randomWord = documents.documents.random().toObject(WordModel::class.java)
                randomWord?.let { showWordHunterBottomSheet(it) }
            }
        }
    }

    private fun showWordHunterBottomSheet(firstWord: WordModel) {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogWordHunterBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        var hunterCounter = 0
        val totalQuestions = 10
        var correctCount = 0

        fun loadHunterQuestion(word: WordModel) {
            hunterCounter++
            if (hunterCounter > totalQuestions) {
                finishMission(correctCount, totalQuestions, dialog)
                return
            }

            dialogBinding.tvHunterCounter.text = "Quest $hunterCounter/$totalQuestions"
            dialogBinding.hunterProgress.progress = hunterCounter * 10

            val correctWords = word.eng.split(" ")
            val shuffledWords = correctWords.shuffled()
            val userSelections = mutableListOf<String>()

            dialogBinding.cgWordPool.removeAllViews()
            dialogBinding.cgSelectedWords.removeAllViews()

            shuffledWords.forEach { wordText ->
                val chip = Chip(this)
                chip.text = wordText
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#342A22"))
                chip.setTextColor(Color.WHITE)
                chip.chipStrokeWidth = 0f

                chip.setOnClickListener {
                    userSelections.add(wordText)
                    val selectedChip = Chip(this)
                    selectedChip.text = wordText
                    selectedChip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#3C83F6"))
                    selectedChip.setTextColor(Color.WHITE)
                    dialogBinding.cgSelectedWords.addView(selectedChip)
                    chip.visibility = View.GONE
                }
                dialogBinding.cgWordPool.addView(chip)
            }

            dialogBinding.btnCheckAnswer.setOnClickListener {
                if (userSelections.joinToString(" ") == word.eng) {
                    correctCount++
                    addScore(15) // Daha zor oyun olduğu için +1.5 ilerleme puanı gibi çalışır
                    fetchAnotherRandomWord { nextWord -> loadHunterQuestion(nextWord) }
                } else {
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
                    loadHunterQuestion(word)
                    hunterCounter--
                }
            }
        }
        loadHunterQuestion(firstWord)
        dialog.show()
    }

    private fun finishMission(correct: Int, total: Int, dialog: BottomSheetDialog) {
        Toast.makeText(this, "Görev Tamamlandı! $correct/$total", Toast.LENGTH_LONG).show()
        if (correct == total) addScore(50) // Mükemmel bitiriş +5 Today ilerlemesi verir
        dialog.dismiss()
    }

    private fun fetchAnotherRandomWord(onSuccess: (WordModel) -> Unit) {
        Firebase.firestore.collection("sentences").limit(30).get().addOnSuccessListener { docs ->
            if (!docs.isEmpty) {
                val next = docs.documents.random().toObject(WordModel::class.java)
                next?.let { onSuccess(it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeaderInfo()
    }
}