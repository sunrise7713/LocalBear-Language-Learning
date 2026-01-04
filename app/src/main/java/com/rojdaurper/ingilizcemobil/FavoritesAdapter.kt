package com.rojdaurper.ingilizcemobil

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.rojdaurper.ingilizcemobil.databinding.ItemSavedCardBinding
import java.util.*

class FavoritesAdapter(private val wordList: List<WordModel>) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private var tts: TextToSpeech? = null

    class ViewHolder(val binding: ItemSavedCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // TTS Başlatma
        tts = TextToSpeech(parent.context) { status ->
            if (status != TextToSpeech.ERROR) { tts?.language = Locale.US }
        }

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val word = wordList[position]
        val context = holder.itemView.context

        // 1. Metinleri Bağlama
        holder.binding.tvEnglishWord.text = word.eng
        holder.binding.tvTurkishTranslation.text = word.tr // XML'deki ID ile uyumlu olmalı
        holder.binding.tvCategoryLabel.text = word.category.uppercase()
        holder.binding.tvDateAdded.text = "Added today"

        // 2. Dinamik İkon Atama
        val iconRes = when {
            word.category.equals("Daily", ignoreCase = true) -> R.drawable.ic_daily
            word.category.equals("Shopping", ignoreCase = true) -> R.drawable.ic_shopping
            word.category.equals("Airport", ignoreCase = true) -> R.drawable.ic_airport
            word.category.equals("Hotel", ignoreCase = true) -> R.drawable.ic_hotel
            word.category.equals("Travel", ignoreCase = true) -> R.drawable.ic_travel
            word.category.equals("Restaurant", ignoreCase = true) -> R.drawable.ic_restaurant
            else -> R.drawable.ic_daily
        }
        holder.binding.ivCategoryIcon.setImageResource(iconRes)

        // 3. Sesli Okuma (btnPlay)
        holder.binding.btnPlay.setOnClickListener {
            tts?.speak(word.eng, TextToSpeech.QUEUE_FLUSH, null, null)
            holder.binding.btnPlay.setColorFilter("#ec4899".toColorInt())
            holder.binding.btnPlay.postDelayed({
                holder.binding.btnPlay.setColorFilter("#FFFFFF".toColorInt())
            }, 500)
        }

        // 4. Çeviri Butonu Mantığı (Main karttan gelen mantık)
        // Başlangıçta kapalı tutalım
        holder.binding.tvTurkishTranslation.visibility = View.GONE
        holder.binding.btnTranslate.text = "Translate"

        holder.binding.btnTranslate.setOnClickListener {
            if (holder.binding.tvTurkishTranslation.visibility == View.GONE) {
                holder.binding.tvTurkishTranslation.visibility = View.VISIBLE
                holder.binding.btnTranslate.text = "Hide"
            } else {
                holder.binding.tvTurkishTranslation.visibility = View.GONE
                holder.binding.btnTranslate.text = "Translate"
            }
        }

        // 5. Uzun Basınca Silme
        holder.binding.root.setOnLongClickListener {
            val sharedPrefs = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
            sharedPrefs.edit().remove(word.eng).apply()
            Toast.makeText(context, "Removed from library", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun getItemCount() = wordList.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        tts?.stop()
        tts?.shutdown()
    }
}