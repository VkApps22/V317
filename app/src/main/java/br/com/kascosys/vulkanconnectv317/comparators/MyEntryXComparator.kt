package br.com.kascosys.vulkanconnectv317.comparators

import com.github.mikephil.charting.data.Entry
import java.util.Comparator
import kotlin.math.sign

class MyEntryXComparator : Comparator<Entry> {
    override fun compare(entry1: Entry, entry2: Entry): Int {
        return sign(entry1.x - entry2.x).toInt()
    }
}