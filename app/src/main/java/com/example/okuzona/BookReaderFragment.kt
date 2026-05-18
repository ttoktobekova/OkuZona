package com.example.okuzona

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.ymg.pdf.viewer.PDFView

class BookReaderFragment : Fragment() {

    private lateinit var pdfView: PDFView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonBack: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_reader, container, false)

        pdfView = view.findViewById(R.id.pdfView)
        progressBar = view.findViewById(R.id.progressBar)
        buttonBack = view.findViewById(R.id.buttonBack)

        // Обработка нажатия на кнопку назад
        buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val pdfUrl = arguments?.getString("pdfUrl")

        if (!pdfUrl.isNullOrEmpty()) {
            downloadAndShowPdf(pdfUrl)
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Ссылка на книгу пустая", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun downloadAndShowPdf(url: String) {
        try {
            // Включаем крутилку перед скачиванием
            progressBar.visibility = View.VISIBLE

            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            val maxDownloadSize: Long = 1024 * 1024 * 50

            storageRef.getBytes(maxDownloadSize)
                .addOnSuccessListener { bytes ->
                    progressBar.visibility = View.GONE
                    if (bytes != null && bytes.isNotEmpty()) {
                        pdfView.fromBytes(bytes)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .defaultPage(0)
                            .load()
                    }
                }
                .addOnFailureListener { exception ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Ошибка: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Критическая ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
