package com.chat_soon_e.re_chat.data.remote.folder

import retrofit2.Call
import retrofit2.http.*

interface FolderRetrofitInterface {
    // 전체 폴더목록 가져오기 (숨김폴더 제외)
    @GET("/app/folders/{kakaoUserIdx}/folderlist")
    fun getFolderList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<FolderResponse>

    // 폴더 생성하기
    @POST("/app/folders/{kakaoUserIdx}/folder")
    fun createFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<FolderResponse>

    // 폴더 이름 바꾸기
    @PATCH("/app/folders/{kakaoUserIdx}/name")
    fun changeFolderName(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int,
        @Body folderName: String
    ): Call<FolderResponse>

    // 폴더 아이콘 바꾸기
    @PATCH("/app/folders/{kakaoUserIdx}/icon")
    fun changeFolerIcon(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int,
        @Body folderImg: String
    ): Call<FolderResponse>

    // 폴더 삭제하기
    @DELETE("/app/folders/{kakaoUserIdx}/folder")
    fun deleteFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<FolderResponse>

    // 숨김 폴더목록 가져오기
    @GET("/app/folders/{kakaoUserIdx}/hidden-folderlist")
    fun getHiddenFolderList(
        @Path("kakaoUserIdx") kakaoUserIdx: Long
    ): Call<FolderResponse>

    // 폴더 숨기기
    @PATCH("/app/folders/{kakaoUserIdx}/hide")
    fun hideFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<FolderResponse>

    // 숨김 폴더 다시 해제하기
    @PATCH("app/folders/{kakaoUserIdx}/unhide")
    fun unhideFolder(
        @Path("kakaoUserIdx") kakaoUserIdx: Long,
        @Query("folderIdx") folderIdx: Int
    ): Call<FolderResponse>
}