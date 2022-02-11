package com.chat_soon_e.re_chat.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.chat_soon_e.re_chat.data.entities.Chat
import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.data.entities.FolderContent

@Dao
interface FolderContentDao {

    @Insert
    fun insert(folderContent:FolderContent)

    //채팅 한개를 폴더에 추가, 검증
    @Query("INSERT INTO FolderContentTable (folderIdx, chatIdx, status) VALUES (:folderIdx, :chatIdx,'ACTIVE')")
    fun insertChat(folderIdx:Int, chatIdx:Int)

    //갠톡 채팅들 폴더에 추가, 검증
    @Query("INSERT INTO FolderContentTable (folderIdx, chatIdx)\n" +
            "SELECT :folderIdx, chatIdx\n" +
            "FROM ChatTable\n" +
            "WHERE groupName == 'null' AND status != 'DELETE' AND\n" +
            "      otherUserIdx IN (SELECT otherUserIdx FROM ChatTable WHERE chatIdx = :chatIdx);")
    fun insertOtOChat(folderIdx:Int,chatIdx: Int)

    //단톡 채팅들 폴더에 추가, 검증
    @Query("INSERT INTO FolderContentTable (folderIdx, chatIdx)\n" +
            "SELECT :folderIdx, chatIdx\n" +
            "FROM ChatTable\n" +
            "WHERE groupName IN (SELECT groupName FROM ChatTable WHERE chatIdx =:chatIdx ) AND\n" +
            "      otherUserIdx IN (SELECT otherUserIdx FROM OtherUserTable WHERE kakaoUserIdx =:userIdx );")
    fun insertOrgChat(chatIdx: Int, folderIdx: Int, userIdx:Long)

    //채팅 한개를 폴더에서 제거, 검증
    @Query("DELETE FROM FolderContentTable WHERE folderIdx= :folderIdx AND chatIdx= :chatIdx")
    fun deleteChat(folderIdx: Int, chatIdx: Int)

    //폴더의 모든 챗 가져오기, 검증
    @Query("SELECT C.postTime, C.folderIdx, C.chatIdx, C.otherUserIdx, C.isChecked, C.message, C.groupName, C.status, C.isNew FROM ChatTable C INNER JOIN OtherUserTable OU ON C.otherUserIdx=OU.otherUserIdx INNER JOIN FolderContentTable FC ON C.chatIdx=FC.chatIdx INNER JOIN FolderTable F ON FC.folderIdx=F.idx WHERE OU.kakaoUserIdx= :user_id AND FC.folderIdx=:folder_id ORDER BY C.postTime DESC")
    fun getFolderChat(user_id:Long, folder_id:Int):LiveData<List<ChatList>>

}