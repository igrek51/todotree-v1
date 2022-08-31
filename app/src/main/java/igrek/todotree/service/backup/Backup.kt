package igrek.todotree.service.backup

import java.util.*

class Backup(
    val filename: String,
    val date: Date,
) : Comparable<Backup> {

    override fun compareTo(other: Backup): Int {
        // sort by date descending (from the newest)
        return -date.compareTo(other.date)
    }
}