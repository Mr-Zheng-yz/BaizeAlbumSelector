package com.baize.baizealbumselector

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SecondActivity : AppCompatActivity() {
    companion object {
        fun open(context: Context,images:ArrayList<String>) {
            val intent = Intent(context, SecondActivity::class.java)
            intent.putStringArrayListExtra("images",images)
            context.startActivity(intent)
        }
    }

    private val images : ArrayList<String> by lazy {
        intent.getStringArrayListExtra("images")
    }

    private lateinit var recyclerView : RecyclerView
    private val imageAdapter by lazy { ImageAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@SecondActivity, 3)
            adapter = imageAdapter
        }
        imageAdapter.datas.addAll(images)
        imageAdapter.notifyDataSetChanged()
    }

}