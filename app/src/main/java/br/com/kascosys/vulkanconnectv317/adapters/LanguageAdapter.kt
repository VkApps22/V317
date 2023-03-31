package br.com.kascosys.vulkanconnectv317.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.kascosys.vulkanconnectv317.R
import br.com.kascosys.vulkanconnectv317.constants.*
import br.com.kascosys.vulkanconnectv317.interfaces.LangClickListener

class LanguageAdapter(
    private val context: Context, private val listener: LangClickListener
) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>(){

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LanguageViewHolder {
        // create a new view
        return LanguageViewHolder.from(parent)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val item = languageList[position]
        holder.bind(item, context, listener)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = languageList.size

    class LanguageViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val langText: TextView = itemView.findViewById(R.id.lang_text)
        private val langIcon: ImageView = itemView.findViewById(R.id.lang_icon)

        companion object {
            fun from(parent: ViewGroup): LanguageViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.lang_item, parent, false)

                return LanguageViewHolder(
                    itemView
                )
            }
        }

        fun bind(item: String, context: Context, listener: LangClickListener) {
            langText.text = context.getString(when(item){
                LANGUAGE_EN -> R.string.lang_en
                LANGUAGE_ES -> R.string.lang_es
                LANGUAGE_PT -> R.string.lang_pt
                else -> R.string.lang_en
            })

            langIcon.setImageResource(when(item){
                LANGUAGE_EN -> R.drawable.ic_gb
                LANGUAGE_ES -> R.drawable.ic_es
                LANGUAGE_PT -> R.drawable.ic_br
                else -> R.drawable.ic_gb
            })

            itemView.setOnClickListener { listener.onLanguageClicked(adapterPosition) }

        }
    }
}