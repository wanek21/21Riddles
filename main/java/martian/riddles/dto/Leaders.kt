package martian.riddles.dto

import android.util.Log
import martian.riddles.data.local.Leader

data class Leaders(
    val riddle: Int = 0,
    val nickname: String = "...",
    val countUsersOnThisRiddle: Int = 0,
    val isCompleteGame: Boolean = false
) {
    companion object {
        fun fromDao(l: List<Leader>?): ArrayList<Leaders> {
            val resultLeaders: ArrayList<Leaders> = arrayListOf(
                Leaders(), Leaders(), Leaders(), Leaders()
            )/*MutableList<Leaders> = mutableListOf(
                Leaders(), Leaders(), Leaders(), Leaders()
            )*/
            if(l != null) {
                for (i in l.indices) {
                    resultLeaders[i] = Leaders(
                        riddle = l[i].level,
                        nickname = l[i].nickname,
                        countUsersOnThisRiddle = l[i].countUsersOnLevel,
                        isCompleteGame = l[i].completeGame
                    )
                }
            }
            Log.d("my", resultLeaders.toString())
            return resultLeaders
        }
        fun toDao(l: List<Leaders>?): ArrayList<Leader> {
            val resultLeaders: ArrayList<Leader> = arrayListOf(
                Leader(1,"...",0,0,false),
                Leader(2,"...",0,0,false),
                Leader(3,"...",0,0,false),
                Leader(4,"...",0,0,false)
            )
            if(l != null) {
                for (i in l.indices) {
                    resultLeaders[i] = Leader(
                        id = (i+1),
                        nickname = l[i].nickname,
                        level = l[i].riddle,
                        countUsersOnLevel =  l[i].countUsersOnThisRiddle,
                        completeGame = l[i].isCompleteGame
                    )
                }
            }
            Log.d("my", "toDao: $resultLeaders")
            return resultLeaders
        }
    }
}