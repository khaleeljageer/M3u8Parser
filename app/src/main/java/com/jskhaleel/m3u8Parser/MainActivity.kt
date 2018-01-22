package com.jskhaleel.m3u8Parser

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.regex.Pattern

/**
 * Created by khaleeljageer on 19/1/18.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ParserThread().execute()
    }

    private inner class ParserThread : AsyncTask<Void, Void, Map<String, Int>>() {
        internal lateinit var url: URL

        override fun doInBackground(vararg voids: Void): Map<String, Int>? {
            try {
                this.url = URL("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8")
                val urlConnection = this.url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 60000
                urlConnection.readTimeout = 30000
                urlConnection.requestMethod = "GET"

                urlConnection.connect()

                return parseHLSMetadata(urlConnection.inputStream, this.url)

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(aVoid: Map<String, Int>?) {
            super.onPostExecute(aVoid)
            if (aVoid == null) return
            Log.d("Khaleel", "aVoid : " + aVoid)
        }
    }

    private fun parseHLSMetadata(i: InputStream, url: URL): HashMap<String, Int>? {

        try {
            val r = BufferedReader(InputStreamReader(i, "UTF-8"))
            val segmentsMap: HashMap<String, Int>? = null
            val pattern = Pattern.compile("^#EXT-X-STREAM-INF:.*BANDWIDTH=(\\d+).*RESOLUTION=([\\dx]+).*")
            do {
                val line: String? = r.readLine() ?: break
                Log.d("M3U8Parser", "BufferedReader : " + line)
                val match = pattern.matcher(line)
                if (match.matches()) {
                    Log.d("M3U8Parser", "Output 1: " + match.group())
                    Log.d("M3U8Parser", "Output 2: " + match.group(1))
                    Log.d("M3U8Parser", "Output 3: " + match.group(2).split("x".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
                    val renditionUrl = r.readLine()
                    Log.d("M3U8Parser", "Output 4: " + renditionUrl)
                    Log.d("M3U8Parser", "Output 5: " + url.host + "--" + url.protocol)
                    var rendition: URL
                    rendition = try {
                        URL(renditionUrl)
                    } catch (e: MalformedURLException) {
                        URL(url.protocol + "://" + url.host + "/" + renditionUrl)
                    }
                    Log.d("M3U8Parser", "Output 6:" + rendition.toString())
                }
            } while (line != null)
            r.close()
            return segmentsMap
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}