package com.paz.ssd;

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.paz.ssd.ml.MobilenetV110224Quant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    companion object {
        private const val RESULT_LOAD_IMG = 2901
        private const val REQUEST_IMAGE_CAPTURE = 1803


    }
    // views
    private lateinit var mainLSTResults: RecyclerView
    private lateinit var mainIMGInput: ImageView
    private lateinit var mainBTNCamera: MaterialButton
    private lateinit var mainBTNDetect: MaterialButton
    private lateinit var mainBTNSelect: MaterialButton

    private var bitmap: Bitmap? = null
    private var arrLabels: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViews()
        setOnClicks()
        initLabels()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        //TODO implement
    }

    private fun initLabels() {
        arrLabels = ArrayList()
        try {
            val reader = BufferedReader(InputStreamReader(assets.open("labels.txt")))
            var line = reader.readLine()
            while (line != null) {
                arrLabels?.add(line)
                line = reader.readLine()
            }

        } catch (e: IOException) {

        }
    }


    private fun getResultsList(arr: FloatArray): ArrayList<MyData> {
        val resList = ArrayList<MyData>()
        for (i in arr.indices) {
            resList.add(MyData(arrLabels?.get(i), i, arr[i], arr[i] / 255 * 100))
        }
        resList.sort()
        resList.reverse()
        return resList
    }

    private fun bindResultsList(results: ArrayList<MyData>) {
        mainLSTResults.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ResultsAdapter(results)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                val imageUri = data?.data
                imageUri?.let {
                    try {
                        val st = contentResolver.openInputStream(it)
                        bitmap = BitmapFactory.decodeStream(st)
                        mainIMGInput.setImageBitmap(bitmap)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "file not found", Toast.LENGTH_SHORT).show()
                    }

                }

            } else {
                Toast.makeText(this, "file not selected", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                bitmap = data?.extras?.get("data") as Bitmap
                mainIMGInput.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setOnClicks() {
        mainBTNCamera.setOnClickListener {
            openCamera()
        }
        mainBTNDetect.setOnClickListener {
            funcDetect()
        }
        mainBTNSelect.setOnClickListener {
            funcSelect()
        }
    }


    fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    fun funcSelect() {
        val photoSelect = Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
        }
        startActivityForResult(photoSelect, RESULT_LOAD_IMG)
    }

    fun funcDetect() {
        bitmap?.let {
            val resized = Bitmap.createScaledBitmap(it, 224, 224, true)
            val selectImage = TensorImage.fromBitmap(resized)
            val byteBuffer = selectImage.buffer
            val model = MobilenetV110224Quant.newInstance(this)

// Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
            inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val resList = getResultsList(outputFeature0.floatArray)

            bindResultsList(resList.subList(0, 100).filter { data -> data.per != 0f } as ArrayList<MyData>)

// Releases model resources if no longer used.
            model.close()
        }


    }

    private fun findViews() {
        mainLSTResults = findViewById(R.id.main_LST_results)
        mainIMGInput = findViewById(R.id.main_IMG_input)
        mainBTNCamera = findViewById(R.id.main_BTN_camera)
        mainBTNDetect = findViewById(R.id.main_BTN_detect)
        mainBTNSelect = findViewById(R.id.main_BTN_select)

    }

}