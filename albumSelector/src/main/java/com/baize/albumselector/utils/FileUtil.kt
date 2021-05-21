package com.baize.albumselector.utils

import java.io.File
import java.io.FileInputStream
import java.math.RoundingMode
import java.text.DecimalFormat

class FileUtil {
    companion object{

        /**
         * 文件大小转化为相应的B/MB/G单位
         *
         * @param fileSize
         * @return
         */
        fun convertStorage(fileSize: Long): String {
            val kb: Long = 1024
            val mb = kb * 1024
            val gb = mb * 1024
            if (fileSize >= gb) {
                return String.format("%.1f GB", fileSize.toFloat() / gb)
            } else if (fileSize >= mb) {
                val f = fileSize.toFloat() / mb
                return String.format(if (f > 100) "%.0f MB" else "%.1f MB", f)
            } else if (fileSize >= kb) {
                val f = fileSize.toFloat() / kb
                return String.format(if (f > 100) "%.0f KB" else "%.1f KB", f)
            } else {
                return String.format("%d B", fileSize)
            }
        }

        fun getFileSize(filePath: String): Long {
            var size: Long = 0
            val file = File(filePath)
            if (file.exists()) {
                return try {
                    val fis = FileInputStream(file)
                    size = fis.available().toLong()
                    fis.close()
                    size
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
            return 0
        }

        /**
         * @value 数字字符串
         * @doublePattern 保留的位数 0.00
         * @integerPattern 保留的位数 0
         * @roundingMode 位数取舍的模式
         */
        fun formatNumberJudgeInteger(
            value: Double,
            doublePattern: String,
            integerPattern: String = "0",
            roundingMode: RoundingMode = RoundingMode.HALF_UP
        ): String {
            return try {
                if (isIntegerForDouble(value)) {
                    val decimalFormat = DecimalFormat(integerPattern)
                    decimalFormat.roundingMode = roundingMode
                    decimalFormat.format(value)
                } else {
                    val decimalFormat = DecimalFormat(doublePattern)
                    decimalFormat.roundingMode = roundingMode
                    decimalFormat.format(value)
                }
            } catch (e: Exception) {
                "$value"
            }
        }

        fun isIntegerForDouble(obj: Double): Boolean {
            val eps = 1e-10 // 精度范围
            return obj - Math.floor(obj) < eps
        }
    }
}