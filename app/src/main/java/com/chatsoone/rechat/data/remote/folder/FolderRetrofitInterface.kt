package com.chatsoone.rechat.data.remote.folder

import com.chatsoone.rechat.data.remote.FolderList
import com.chatsoone.rechat.data.remote.ServerResponse
import retrofit2.Call
import retrofit2.http.*

interface FolderRetrofitInterface {
    // 전체 폴더목록 가져오기 (숨김폴더 제외)
    @GET("/app/folders/{kakaoUserIdx}/folderlist")
    fun getFolderList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ServerResponse>

    // 폴더 생성하기
    @POST("/app/folders/{kakaoUserIdx}/folder")
    fun createFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ServerResponse>

    // 폴더 이름 바꾸기
    @PATCH("/app/folders/{kakaoUserIdx}/name")
    fun changeFolderName(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int,
        @Body folder: FolderList
    ): Call<ServerResponse>

    // 폴더 아이콘 바꾸기
    @PATCH("/app/folders/{kakaoUserIdx}/icon")
    fun changeFolerIcon(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int,
        @Body folder: FolderList
    ): Call<ServerResponse>

    // 폴더 삭제하기
    @DELETE("/app/folders/{kakaoUserIdx}/folder")
    fun deleteFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<ServerResponse>

    // 숨김 폴더목록 가져오기
    @GET("/app/folders/{kakaoUserIdx}/hidden-folderlist")
    fun getHiddenFolderList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<ServerResponse>

    // 폴더 숨기기
    @PATCH("/app/folders/{kakaoUserIdx}/hide")
    fun hideFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<ServerResponse>

    // 숨김 폴더 다시 해제하기
    @PATCH("app/folders/{kakaoUserIdx}/unhide")
    fun unhideFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<ServerResponse>
}
