package com.chatsoone.rechat.data.remote.folder

import android.util.Log
import com.chatsoone.rechat.ApplicationClass.Companion.SERVICE
import retrofit2.Callback
import com.chatsoone.rechat.ApplicationClass.Companion.retrofit
import com.chatsoone.rechat.data.remote.*
import com.chatsoone.rechat.ui.view.FolderAPIView
import com.chatsoone.rechat.ui.view.FolderListView
import com.chatsoone.rechat.ui.view.HiddenFolderListView
import retrofit2.Call
import retrofit2.Response

class FolderService {

    // 전체 폴더목록 가져오기 (숨김폴더 제외)
    fun getFolderList(folderListView: FolderListView, userIdx: Long) {
        val folderList = ArrayList<FolderList>()
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.getFolderList(userIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!
                Log.d(SERVICE, "FOLDER/getFolderList/onResponse")

                when (resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if (jsonArray != null) {
                            // JsonArray parsing
                            for (i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val folderIdx = jsonElement.asJsonObject.get("folderIdx").asInt
                                val folderName = jsonElement.asJsonObject.get("folderName").asString
                                val folderImg =
                                    if (jsonElement.asJsonObject.get("folderImg").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "folderImg"
                                    ).asString
                                Log.d(SERVICE, "FOLDER/folderImg: ${folderImg.isNullOrEmpty()}")
                                Log.d(SERVICE, "FOLDER/folderImg: $folderImg")

                                val folder = FolderList(folderIdx, folderName, folderImg)
                                folderList.add(folder)
                                Log.d(SERVICE, "FOLDER/folderList: $folderList")
                            }
                        }
                        folderListView.onFolderListSuccess(folderList)
                        Log.d(SERVICE, "FOLDER/$folderList")
                    }
                    else -> folderListView.onFolderListFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "FOLDER/ ${t.message}")
                folderListView.onFolderListFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 생성하기
    fun createFolder(folderView: FolderAPIView, userIdx: Long) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.createFolder(userIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "FOLDER/ ${t.message}")
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 이름 바꾸기
    fun changeFolderName(
        folderView: FolderAPIView,
        userIdx: Long,
        folderIdx: Int,
        folder: FolderList
    ) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.changeFolderName(userIdx, folderIdx, folder)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    Log.d(
                        SERVICE,
                        "FOLDER/changeFolderName/onResponse/userIdx: $userIdx, folderIdx: $folderIdx, folder: $folder"
                    )
                    Log.d(
                        SERVICE,
                        "FOLDER/changeFolderName/onResponse/response.body: ${response.body()}"
                    )
                    val resp = response.body()!!

                    when (resp.code) {
                        1000 -> folderView.onFolderAPISuccess()
                        else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Log.d(SERVICE, "FOLDER/ ${t.message}")
                    folderView.onFolderAPIFailure(400, "네트워크 오류")
                }
            })
    }

    // 폴더 아이콘 바꾸기
    fun changeFolderIcon(
        folderView: FolderAPIView,
        userIdx: Long,
        folderIdx: Int,
        folder: FolderList
    ) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.changeFolerIcon(userIdx, folderIdx, folder)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    val resp = response.body()!!

                    when (resp.code) {
                        1000 -> folderView.onFolderAPISuccess()
                        else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Log.d(SERVICE, "FOLDER/ ${t.message}")
                    folderView.onFolderAPIFailure(400, "네트워크 오류")
                }
            })
    }

    // 폴더 삭제하기
    fun deleteFolder(folderView: FolderAPIView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.deleteFolder(userIdx, folderIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "FOLDER/ ${t.message}")
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 숨김 폴더목록 가져오기
    fun getHiddenFolderList(hiddenFolderListView: HiddenFolderListView, userIdx: Long) {
        val hiddenFolderList = ArrayList<FolderList>()
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.getHiddenFolderList(userIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!
                Log.d(SERVICE, "FOLDER/getFolderList/onResponse")

                when (resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if (jsonArray != null) {
                            // JsonArray parsing
                            for (i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val folderIdx = jsonElement.asJsonObject.get("folderIdx").asInt
                                val folderName = jsonElement.asJsonObject.get("folderName").asString
                                val folderImg =
                                    if (jsonElement.asJsonObject.get("folderImg").isJsonNull) null else jsonElement.asJsonObject.get(
                                        "folderImg"
                                    ).asString

                                Log.d(SERVICE, "FOLDER/folderImg: ${folderImg.isNullOrEmpty()}")
                                Log.d(SERVICE, "FOLDER/folderImg: $folderImg")

                                val hiddenFolder = FolderList(folderIdx, folderName, folderImg)
                                hiddenFolderList.add(hiddenFolder)
                                Log.d(SERVICE, "FOLDER/hiddenFolderList: $hiddenFolderList")
                            }
                        }
                        hiddenFolderListView.onHiddenFolderListSuccess(hiddenFolderList)
                        Log.d(SERVICE, "FOLDER/$hiddenFolderList")
                    }
                    else -> hiddenFolderListView.onHiddenFolderListFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "FOLDER/ ${t.message}")
                hiddenFolderListView.onHiddenFolderListFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 숨기기
    fun hideFolder(folderView: FolderAPIView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.hideFolder(userIdx, folderIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "FOLDER/ ${t.message}")
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }

    // 숨김 폴더 다시 해제하기
    fun unhideFolder(folderView: FolderAPIView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.unhideFolder(userIdx, folderIdx).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                val resp = response.body()!!

                when (resp.code) {
                    1000 -> folderView.onFolderAPISuccess()
                    else -> folderView.onFolderAPIFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Log.d(SERVICE, "FOLDER/ ${t.message}")
                folderView.onFolderAPIFailure(400, "네트워크 오류")
            }
        })
    }
}
