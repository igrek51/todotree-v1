package igrek.todotree.service.backup

import igrek.todotree.service.backup.Backup
import java.util.*

class Backup(val filename: String, private val date: Date) : Comparable<Backup> {
    fun getDate(): Date? {
        return date
    }

    override fun compareTo(b: Backup): Int {
        // sort by date descending (from the newest)
        if (getDate() == null) return +1
        return if (b.getDate() == null) -1 else -getDate()!!.compareTo(b.getDate())
    }
}