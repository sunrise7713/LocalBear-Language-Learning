package com.rojdaurper.ingilizcemobil

import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt // KTX uyarısı için eklendi
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rojdaurper.ingilizcemobil.databinding.ItemCardBinding
import java.util.*

class WordAdapter(private val wordList: List<WordModel>) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    private var tts: TextToSpeech? = null

    class WordViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        tts = TextToSpeech(parent.context) { status ->
            if (status != TextToSpeech.ERROR) { tts?.language = Locale.US }
        }
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = wordList[position]
        val context = holder.itemView.context

        // 1. Metin ve Resim
        holder.binding.tvEnglish.text = word.eng
        holder.binding.tvTurkishTranslation.text = word.tr

        if (word.imageUrl.isNotEmpty()) {
            Glide.with(context)
                .load(word.imageUrl)
                .placeholder(R.drawable.logo_ayicik)
                .into(holder.binding.ivHeader)
        }

        // 2. Çeviri Butonu
        holder.binding.tvTurkishTranslation.isGone = true
        holder.binding.btnTranslate.setOnClickListener {
            if (holder.binding.tvTurkishTranslation.isGone) {
                holder.binding.tvTurkishTranslation.visibility = View.VISIBLE
                holder.binding.btnTranslate.text = "Hide"
            } else {
                holder.binding.tvTurkishTranslation.isGone = true
                holder.binding.btnTranslate.text = "Translate"
            }
        }

        // 3. Sesli Okuma
        holder.binding.btnSpeakFAB.setOnClickListener {
            tts?.speak(word.eng, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        // 4. Favori (Kategoriyi de Kaydediyoruz!)
        updateHeartIcon(holder, word.isFavorite)

        holder.binding.btnBookmark.setOnClickListener {
            word.isFavorite = !word.isFavorite
            val sharedPrefs = context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)

            if (word.isFavorite) {
                // KRİTİK DEĞİŞİKLİK: Türkçe anlamı ve kategoriyi '|' ile birleştirip kaydediyoruz
                val combinedData = "${word.tr}|${word.category}"
                sharedPrefs.edit().putString(word.eng, combinedData).apply()
                Toast.makeText(context, "Saved to library!", Toast.LENGTH_SHORT).show()
            } else {
                sharedPrefs.edit().remove(word.eng).apply()
                Toast.makeText(context, "Removed!", Toast.LENGTH_SHORT).show()
            }
            updateHeartIcon(holder, word.isFavorite)
        }
    }

    private fun updateHeartIcon(holder: WordViewHolder, isFavorite: Boolean) {
        if (isFavorite) {
            holder.binding.btnBookmark.setImageResource(R.drawable.ic_heart_filled)
            // IDE uyarısını toColorInt() ile giderdik
            holder.binding.btnBookmark.setColorFilter("#EC4899".toColorInt())
        } else {
            holder.binding.btnBookmark.setImageResource(R.drawable.ic_heart_empty)
            holder.binding.btnBookmark.clearColorFilter()
        }
    }

    override fun getItemCount(): Int = wordList.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        tts?.stop()
        tts?.shutdown()
    }
}