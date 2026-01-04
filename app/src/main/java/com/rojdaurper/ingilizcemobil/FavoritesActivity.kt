package com.rojdaurper.ingilizcemobil

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.rojdaurper.ingilizcemobil.databinding.ActivityFavoritesBinding

class FavoritesActivity : BaseActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private var adapter: FavoritesAdapter? = null

    // Filtreleme için listeler
    private val favoriteList = mutableListOf<WordModel>()
    private val filteredList = mutableListOf<WordModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. MERKEZİ NAVİGASYON: Tüm mantık BaseActivity'de toplandı
        setupCommonNavigation(binding.bottomNavigation)

        // 2. Favorileri Yükle ve UI Ayarları
        loadFavorites()
        setupSearch()
        setupCategoryButtons()
    }

    private fun setupCategoryButtons() {
        val categoryButtons = mapOf(
            binding.btnFilterAll to "",
            binding.btnFilterDaily to "Daily",
            binding.btnFilterShopping to "Shopping",
            binding.btnFilterAirport to "Airport",
            binding.btnFilterTravel to "Travel",
            binding.btnFilterHotel to "Hotel"
        )

        categoryButtons.forEach { (button, category) ->
            button.setOnClickListener {
                filter(category)
            }
        }
    }

    private fun loadFavorites() {
        val sharedPrefs = getSharedPreferences("Favorites", MODE_PRIVATE)
        val allEntries = sharedPrefs.all

        favoriteList.clear()

        for ((eng, savedData) in allEntries) {
            val dataString = savedData.toString()
            val parts = dataString.split("|")

            val turkishMeaning = if (parts.size > 1) parts[0].trim() else dataString.trim()
            val categoryName = if (parts.size > 1) parts[1].trim() else "Daily"

            favoriteList.add(
                WordModel(
                    eng = eng,
                    tr = turkishMeaning,
                    level = "Beginner",
                    category = categoryName,
                    imageUrl = "",
                    isFavorite = true
                )
            )
        }

        filteredList.clear()
        filteredList.addAll(favoriteList)

        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        adapter = FavoritesAdapter(filteredList)
        binding.rvFavorites.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearchSaved.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(query: String) {
        filteredList.clear()
        val lowerCaseQuery = query.lowercase().trim()

        for (word in favoriteList) {
            if (word.category.lowercase().contains(lowerCaseQuery) ||
                word.eng.lowercase().contains(lowerCaseQuery)) {
                filteredList.add(word)
            }
        }
        adapter?.notifyDataSetChanged()

        // Buton Renklendirme
        val buttonsMap = mapOf(
            "daily" to binding.btnFilterDaily,
            "shopping" to binding.btnFilterShopping,
            "airport" to binding.btnFilterAirport,
            "travel" to binding.btnFilterTravel,
            "hotel" to binding.btnFilterHotel
        )

        buttonsMap.values.forEach { it.setBackgroundResource(R.drawable.filter_item_dark_bg) }
        binding.btnFilterAll.setBackgroundResource(R.drawable.filter_item_dark_bg)

        if (lowerCaseQuery.isEmpty()) {
            binding.btnFilterAll.setBackgroundResource(R.drawable.gradient_button_bg)
        } else {
            buttonsMap.forEach { (name, button) ->
                if (name.contains(lowerCaseQuery)) {
                    button.setBackgroundResource(R.drawable.gradient_button_bg)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvFavorites.adapter = null
    }
}