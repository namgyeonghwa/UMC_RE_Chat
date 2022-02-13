package com.chat_soon_e.re_chat.data.remote.folder

import android.util.Log
import retrofit2.Callback
import com.chat_soon_e.re_chat.ApplicationClass.Companion.retrofit
import com.chat_soon_e.re_chat.data.entities.ChatList
import com.chat_soon_e.re_chat.data.entities.Folder
import com.chat_soon_e.re_chat.ui.view.*
import retrofit2.Call
import retrofit2.Response

class FolderService {
    private val tag = "SERVICE/FOLDER"

    // 전체 폴더목록 가져오기 (숨김폴더 제외)
    fun getFolderList(folderListView: FolderListView, userIdx: Long) {
        val folderList = ArrayList<FolderList>()
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.getFolderList(userIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!
                Log.d(tag, "getFolderList()/onResponse()")

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val folderName = jsonElement.asJsonObject.get("folder_name").asString
                                val folderImg = if(jsonElement.asJsonObject.get("folderImg").isJsonNull) null else jsonElement.asJsonObject.get("folderImg").asString

                                Log.d(tag, "folderImg: ${folderImg.isNullOrEmpty()}")
                                Log.d(tag, "folderImg: $folderImg")

                                val folder = FolderList(folderName, folderImg)
                                folderList.add(folder)
                                Log.d(tag, "folderList: $folderList")
                            }
                        }
                        folderListView.onFolderListSuccess(folderList)
                        Log.d(tag, "$folderList")
                    }
                    else -> folderListView.onFolderListFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                folderListView.onFolderListFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 생성하기
    fun createFolder(createFolderView: CreateFolderView, userIdx: Long) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.createFolder(userIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> createFolderView.onCreateFolderSuccess()
                    else -> createFolderView.onCreateFolderFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                createFolderView.onCreateFolderFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 이름 바꾸기
    fun changeFolderName(changeFolderNameView: ChangeFolderNameView, userIdx: Long, folderIdx: Int, folderName: String) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.changeFolderName(userIdx, folderIdx, folderName).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> changeFolderNameView.onChangeFolderNameSuccess()
                    else -> changeFolderNameView.onChangeFolderNameFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                changeFolderNameView.onChangeFolderNameFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 아이콘 바꾸기
    fun changeFolderIcon(changeFolderIconView: ChangeFolderIconView, userIdx: Long, folderIdx: Int, folderImg: String?) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.changeFolerIcon(userIdx, folderIdx, folderImg!!).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> changeFolderIconView.onChangeFolderIconSuccess()
                    else -> changeFolderIconView.onChangeFolderIconFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                changeFolderIconView.onChangeFolderIconFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 삭제하기
    fun deleteFolder(deleteFolderView: DeleteFolderView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.deleteFolder(userIdx, folderIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> deleteFolderView.onDeleteFolderSuccess()
                    else -> deleteFolderView.onDeleteFolderFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                deleteFolderView.onDeleteFolderFailure(400, "네트워크 오류")
            }
        })
    }

    // 숨김 폴더목록 가져오기
    fun getHiddenFolderList(hiddenFolderListView: HiddenFolderListView, userIdx: Long) {
        val hiddenFolderList = ArrayList<HiddenFolderList>()
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.getHiddenFolderList(userIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!
                Log.d(tag, "getFolderList()/onResponse()")

                when(resp.code) {
                    1000 -> {
                        val jsonArray = resp.result
                        if(jsonArray != null) {
                            // JsonArray parsing
                            for(i in 0 until jsonArray.size()) {
                                val jsonElement = jsonArray.get(i)
                                val folderName = jsonElement.asJsonObject.get("folderName").asString
                                val folderImg = if(jsonElement.asJsonObject.get("folderImg").isJsonNull) null else jsonElement.asJsonObject.get("folderImg").asString

                                Log.d(tag, "folderImg: ${folderImg.isNullOrEmpty()}")
                                Log.d(tag, "folderImg: $folderImg")

                                val hiddenFolder = HiddenFolderList(folderName, folderImg)
                                hiddenFolderList.add(hiddenFolder)
                                Log.d(tag, "hiddenFolderList: $hiddenFolderList")
                            }
                        }
                        hiddenFolderListView.onHiddenFolderListSuccess(hiddenFolderList)
                        Log.d(tag, "$hiddenFolderList")
                    }
                    else -> hiddenFolderListView.onHiddenFolderListFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                hiddenFolderListView.onHiddenFolderListFailure(400, "네트워크 오류")
            }
        })
    }

    // 폴더 숨기기
    fun hideFolder(hideFolderView: HideFolderView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.hideFolder(userIdx, folderIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> hideFolderView.onHideFolderSuccess()
                    else -> hideFolderView.onHideFolderFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                hideFolderView.onHideFolderFailure(400, "네트워크 오류")
            }
        })
    }

    // 숨김 폴더 다시 해제하기
    fun unhideFolder(unhideFolderView: UnhideFolderView, userIdx: Long, folderIdx: Int) {
        val folderService = retrofit.create(FolderRetrofitInterface::class.java)

        folderService.hideFolder(userIdx, folderIdx).enqueue(object: Callback<FolderResponse> {
            override fun onResponse(call: Call<FolderResponse>, response: Response<FolderResponse>) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> unhideFolderView.onUnhideFolderSuccess()
                    else -> unhideFolderView.onUnhideFolderFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<FolderResponse>, t: Throwable) {
                Log.d(tag, t.message.toString())
                unhideFolderView.onUnhideFolderFailure(400, "네트워크 오류")
            }
        })
    }
}